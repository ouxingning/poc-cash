package com.xnou.demo.cash.web;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xnou.demo.cash.service.FtpFile;
import com.xnou.demo.cash.service.FtpFileService;
import com.xnou.demo.cash.service.ObjectStorageService;

/**
 * OCI Object Storage actions.
 * 
 * @author OU Xingning
 * @date 2024/01/30
 */
@RestController
@RequestMapping("/rest/objectstorage")
public class ObjectStorageController {

	private static final Logger logger = LoggerFactory.getLogger(ObjectStorageController.class);

	// copy files from cash-bucket to bucket-processed
	@PostMapping("/copy-objects")
	public String copyFiles(@RequestBody List<FtpFile> files) throws SQLException {

		FtpFileService ffs = new FtpFileService();

		String destinationBucketName = "bucket-processed";
		ObjectStorageService oss = new ObjectStorageService();

		for (FtpFile file : files) {
			String objectName = file.getMtime() + "-" + file.getFilename();
			try {
				oss.copyObject(objectName, destinationBucketName, objectName);
				ffs.updateProcessed(file.getMtime(), file.getFilename(), 5); // processed
				logger.info("copy file {} to bucket-processed.", objectName);
			} catch (Exception e) {
				logger.error("An error occurs while copying {}", objectName, e);
				ffs.updateProcessed(file.getMtime(), file.getFilename(), 9); // failed
			}

		}

		return "done";
	}

}
