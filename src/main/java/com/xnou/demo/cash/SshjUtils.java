package com.xnou.demo.cash;

import java.io.IOException;
import java.util.List;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceFilter;
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

	private static SSHClient connectHost() throws IOException {
		SSHClient client = new SSHClient();
		client.addHostKeyVerifier(new PromiscuousVerifier());
		client.connect(SftpConstant.HOST, SftpConstant.PORT);

		KeyProvider keyProvider = client.loadKeys(SftpConstant.PRIVATE_KEY, SftpConstant.PASS);
		client.authPublickey(SftpConstant.USER, keyProvider);

		client.useCompression();
		return client;
	}

	public static List<RemoteResourceInfo> listFiles(String path) throws IOException {
		SSHClient client = connectHost();

		RemoteResourceFilter filter = new RemoteResourceFilter() {

			@Override
			public boolean accept(RemoteResourceInfo resource) {
				return resource.isRegularFile() && (!resource.isDirectory());
			}
		};

		try (SFTPClient sftp = client.newSFTPClient()) {
			List<RemoteResourceInfo> files = sftp.ls(path, filter);
			System.out.println("Total files: " + files.size());
			
			return files;
			
//			for (RemoteResourceInfo rri : files) {
//				System.out.println(
//						rri.getName() + "," + rri.getAttributes().getSize() + "," + rri.getAttributes().getMtime());
//			}


		} finally {
			client.disconnect();
		}
	}

	public static void downloadFile(String remoteFile, String localFile) throws IOException {
		SSHClient client = connectHost();

		try (SFTPClient sftp = client.newSFTPClient()) {

			sftp.get(remoteFile, localFile);

		} finally {
			client.disconnect();
		}
	}

	public static void main(String[] args) throws IOException {

		String path = "/";
		if (args.length > 0) {
			path = args[0];
		}

		SshjUtils.listFiles(path);
		System.out.println("done");

	}

}
