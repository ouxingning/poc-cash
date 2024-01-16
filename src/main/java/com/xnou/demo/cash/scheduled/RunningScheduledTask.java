package com.xnou.demo.cash.scheduled;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.xnou.demo.cash.SshjUtils;
import com.xnou.demo.cash.service.FtpFile;
import com.xnou.demo.cash.service.FtpFileService;
import com.xnou.demo.cash.service.ObjectStorageService;

/**
 *
 * @author OU Xingning
 * @date 2024/01/03
 */
@Component
public class RunningScheduledTask {

	@Scheduled(fixedRate = 1000L * 60 * 2)
	public void run() {

		FtpFileService fileService = new FtpFileService();
		int processed = 1; // need to processed
		try {
			List<FtpFile> fileList = fileService.select(processed);
			if (fileList.size() > 0) {
				for (FtpFile file : fileList) {
					try {
						this.process(fileService, file);
					} catch (Exception e) {
						System.err.println("an error occurs while processing file " + file.getFilename());
						e.printStackTrace();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void process(FtpFileService fileService, FtpFile file) throws SQLException, IOException {
		FtpFile currentFile = new FtpFile();
		currentFile.setProcessed(2); // processing
		currentFile.setPtime(System.currentTimeMillis());
		currentFile.setMtime(file.getMtime());
		currentFile.setFilename(file.getFilename());
		

		// mark as start processing
		int rows = fileService.updateProcessed(currentFile);
		System.out.println(String.format("File %s start processing, rows={}", file.getFilename(), rows));

		// download file from FTP
		File remoteFile = new File(file.getDirectory(), file.getFilename());
		//File localFile = new File(System.getProperty("java.io.tmpdir"), file.getFilename());
		File localFile = new File("/home/opc/workspace/files/", file.getFilename());
		long start = System.currentTimeMillis();
		SshjUtils.downloadFile(remoteFile.getAbsolutePath(), localFile.getAbsolutePath());
		long end = System.currentTimeMillis();
		System.out.println("Download file elapsed: " + (end-start) / 1000 + " seconds.");

		// upload file to object storage
		ObjectStorageService oss = new ObjectStorageService();
		String objectName = file.getMtime() + "-" +  file.getFilename();
		oss.uploadObject(objectName, localFile);

		// update processed status in database
		currentFile = new FtpFile();
		currentFile.setProcessed(3); // finished
		currentFile.setPtimefinished(System.currentTimeMillis());
		currentFile.setMtime(file.getMtime());
		currentFile.setFilename(file.getFilename());

		rows = fileService.updateProcessed(currentFile);
		System.out.println(String.format("File %s finish processing, rows={}", file.getFilename(), rows));
		
		// delete the local file
		localFile.delete();
	}

}
