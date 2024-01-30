package com.xnou.demo.cash.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.pool.OracleDataSource;

/**
 *
 * @author OU Xingning
 * @date 2024/01/02
 */
public class FtpFileService {

	private static final String DEFAULT_USER = "admin";
	private static final String DEFAULT_PASSWORD = "wybwot-sybte3-Tymfet";
	private static final String DEFAULT_URL = "jdbc:oracle:thin:@cashatp_high?TNS_ADMIN=/home/opc/Wallet_CASHATP/";
	
	private static final String TABLE_NAME = "WKSP_CASH.ftp_file";

	private static OracleDataSource dataSource;

	/**
	 * Creates an OracleConnection instance and return it.
	 * 
	 * @return oracleConnection
	 * @throws SQLException
	 */
	private synchronized static OracleConnection getConnection() throws SQLException {
		if (null != dataSource) {
			return (OracleConnection) dataSource.getConnection();
		}

		dataSource = new OracleDataSource();
		dataSource.setUser(DEFAULT_USER);
		dataSource.setPassword(DEFAULT_PASSWORD);
		dataSource.setURL(DEFAULT_URL);

		return (OracleConnection) dataSource.getConnection();
	}

//	MERGE INTO 目标表 t
//	USING (SELECT 值 FROM 原表) s
//	ON (t.主键 = s.主键)
//	WHEN NOT MATCHED THEN
//	  INSERT (主键或唯一值的列，列名1, 列名2, )
//	  VALUES (值, 值, 值);
	public int[] batchInsert(List<FtpFile> fileList) throws SQLException {
		
		
		StringBuilder sb = new StringBuilder();
		sb.append(" merge into ").append(TABLE_NAME).append(" t ");
		sb.append(" using (select filename, mtime from " + TABLE_NAME + ") s ");
		sb.append(" on (t.mtime = s.mtime and t.filename = s.filename) ");
		sb.append(" when not matched then ");
		sb.append(" insert (directory, filename, filesize, atime, mtime, processed, ptime, ptimefinished) ");
		sb.append(" values(?, ?, ?, ?, ?, ?, ?, ?) ");
		
		

		Connection conn = null;
		try {
			conn = getConnection();

//			String sql = "insert into " + TABLE_NAME
//					+ " (directory, filename, filesize, atime, mtime, processed, ptime, ptimefinished) values (?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sb.toString());

			conn.setAutoCommit(false);

			for (FtpFile file : fileList) {
				ps.setString(1, file.getDirectory());
				ps.setString(2, file.getFilename());
				ps.setLong(3, file.getFilesize());
				ps.setLong(4, file.getAtime());
				ps.setLong(5, file.getMtime());
				ps.setInt(6, file.getProcessed());
				ps.setLong(7, file.getPtime());
				ps.setLong(8, file.getPtimefinished());
				ps.addBatch();
			}

			int rows[] = ps.executeBatch();
			System.out.println("inserted rows: " + Arrays.toString(rows));

			conn.commit();

			ps.close();

			return rows;

		} finally {
			if (null != conn) {
				conn.close();
			}
		}

	}

	public int[] insert(FtpFile file) throws SQLException {
		List<FtpFile> fileList = Arrays.asList(file);
		return batchInsert(fileList);
	}

	public int updateProcessed(FtpFile file) throws SQLException {
		Connection conn = null;
		try {
			conn = getConnection();

			String sql = "update " + TABLE_NAME + " set processed=?, ptime=?, ptimefinished=? where mtime=? and filename=?";
			PreparedStatement ps = conn.prepareStatement(sql);

			conn.setAutoCommit(false);

			ps.setInt(1, file.getProcessed());
			ps.setLong(2, file.getPtime());
			ps.setLong(3, file.getPtimefinished());
			ps.setLong(4, file.getMtime());
			ps.setString(5, file.getFilename());

			int rows = ps.executeUpdate();

			System.out.println("updated rows: " + rows);

			conn.commit();

			ps.close();

			return rows;

		} finally {
			if (null != conn) {
				conn.close();
			}
		}
	}
	
	public int updateProcessed(long mtime, String filename, int processed) throws SQLException {
		Connection conn = null;
		try {
			conn = getConnection();

			String sql = "update " + TABLE_NAME + " set processed=? where mtime=? and filename=?";
			PreparedStatement ps = conn.prepareStatement(sql);

			conn.setAutoCommit(false);

			ps.setInt(1, processed);
			ps.setLong(2, mtime);
			ps.setString(3, filename);

			int rows = ps.executeUpdate();

			System.out.println("updated rows: " + rows);

			conn.commit();

			ps.close();

			return rows;

		} finally {
			if (null != conn) {
				conn.close();
			}
		}
	}

	public List<FtpFile> select(int processed) throws SQLException {
		List<FtpFile> result = new ArrayList<>();
		Connection conn = null;
		try {
			conn = getConnection();
			conn.setAutoCommit(true);

			String sql = "select * from " + TABLE_NAME + " where processed=? order by mtime desc";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, processed);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				FtpFile file = new FtpFile();
				file.setDirectory(rs.getString("directory"));
				file.setFilename(rs.getString("filename"));
				file.setFilesize(rs.getLong("filesize"));
				file.setAtime(rs.getLong("atime"));
				file.setMtime(rs.getLong("mtime"));
				file.setProcessed(rs.getInt("processed"));
				file.setPtime(rs.getLong("ptime"));
				file.setPtimefinished(rs.getLong("ptimefinished"));

				result.add(file);
			}

			rs.close();

			ps.close();

		} finally {
			if (null != conn) {
				conn.close();
			}
		}

		return result;
	}

}
