package com.xnou.demo.cash.service;

import lombok.Data;

/**
 *
 * @author OU Xingning
 * @date 2024/01/02
 */
@Data
public class FtpFile {

	private String directory;
	private String filename;
	private long filesize;
	private long atime;
	private long mtime;
	private int processed; // 0-wait, 1-processing, 2-processed
	private long ptime;// processing time
	private long ptimefinished; // finished time

}
