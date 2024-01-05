package com.xnou.demo.cash.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xnou.demo.cash.SshjUtils;
import com.xnou.demo.cash.service.FtpFile;
import com.xnou.demo.cash.service.FtpFileService;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.RemoteResourceInfo;

/**
 * List files in FTP folders.
 * 
 * @author OU Xingning
 * @date 2024/01/03
 */
@RestController
@RequestMapping("/rest/sftp")
public class SftpRestController {

	private static final Logger logger = LoggerFactory.getLogger(SftpRestController.class);

	@GetMapping("/list")
	public String listFiles() throws IOException, SQLException {
		logger.info("Start listing files in FTP.");
		int totalRows = 0;
		String[] folders = new String[] { "/APAC", "/ETW", "/HK", "/ID", "/IN", "/MO", "/MY", "/PH", "/SG", "/TH", "/TW", "/VN" };
		for (String folder : folders) {
			List<RemoteResourceInfo> files = SshjUtils.listFiles(folder);
			List<FtpFile> fileList = new ArrayList<>(files.size());
			for (RemoteResourceInfo rri : files) {
				FtpFile file = new FtpFile();
				FileAttributes attr = rri.getAttributes();

				file.setDirectory(folder);
				file.setFilename(rri.getName());
				file.setFilesize(attr.getSize());
				file.setAtime(attr.getAtime());
				file.setMtime(attr.getMtime());
				file.setProcessed(0);
				file.setPtime(0);
				file.setPtimefinished(0);

				fileList.add(file);
			}

			FtpFileService fileService = new FtpFileService();
			int[] rows = fileService.batchInsert(fileList);
			totalRows += rows.length;

		}
		logger.info("Finish listing files and saved {} rows into database.", totalRows);
		return String.valueOf(totalRows);
	}

}
