package com.xnou.demo.cash;

import java.io.IOException;
import java.util.List;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

/**
 *
 * @author OU Xingning
 * @date 2023/12/22
 */
public class SshjUtils {

	private SSHClient connectHost() throws IOException {
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(SftpConstant.HOST, SftpConstant.PORT);

		KeyProvider keyProvider = client.loadKeys(SftpConstant.PRIVATE_KEY, SftpConstant.PASS);
		client.authPublickey(SftpConstant.USER, keyProvider);

		client.useCompression();
		return client;
	}
	
	public void listFiles(String path) throws IOException {
		SSHClient client = this.connectHost();
		
		try(SFTPClient sftp = client.newSFTPClient()) {
			List<RemoteResourceInfo> files= sftp.ls(path);
			System.out.println("Total files: " + files.size());
			
			int len = files.size();
			if(len > 10) {
				len = 10;
			}
			
			for(RemoteResourceInfo rri : files) {
				System.out.println(rri.getName() + "," + rri.getAttributes().getSize() + "," + rri.getAttributes().getMtime());
			}
			
			
			
			
		}finally {
			client.disconnect();
		}
	}
	
	
	
	
	
	
	
//	
//	
//	
//	private String remoteFile = "welcome.txt";
//	private String localDir = "src/main/resources/";
//	
//	private SSHClient setupSshj() throws IOException {
//	    SSHClient client = new SSHClient();
//	    client.addHostKeyVerifier(new PromiscuousVerifier());
//	    client.connect(remoteHost);
//	    client.useCompression();
//	    client.authPassword(username, password);
//	    return client;
//	}
//	
//	
//	public void whenUploadFileUsingSshj_thenSuccess() throws IOException {
//	    SSHClient sshClient = setupSshj();
//	    SFTPClient sftpClient = sshClient.newSFTPClient();
//	 
//	    sftpClient.put(localFile, remoteDir + "sshjFile.txt");
//	 
//	    sftpClient.close();
//	    sshClient.disconnect();
//	}
//	
//	public void whenDownloadFileUsingSshj_thenSuccess() throws IOException {
//	    SSHClient sshClient = setupSshj();
//	    SFTPClient sftpClient = sshClient.newSFTPClient();
//	 
//	    sftpClient.get(remoteFile, localDir + "sshjFile.txt");
//	 
//	    sftpClient.close();
//	    sshClient.disconnect();
//	}
	


	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		String path = "/";
		if(args.length > 0) {
			path = args[0];
		}
		
		
		SshjUtils u  = new SshjUtils();
		u.listFiles(path);
		System.out.println("done");

	}

}
