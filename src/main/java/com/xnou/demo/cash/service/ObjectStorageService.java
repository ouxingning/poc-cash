package com.xnou.demo.cash.service;

import java.io.File;
import java.io.IOException;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CopyObjectDetails;
import com.oracle.bmc.objectstorage.model.WorkRequest;
import com.oracle.bmc.objectstorage.requests.CopyObjectRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetWorkRequestRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CopyObjectResponse;
import com.oracle.bmc.objectstorage.responses.DeleteObjectResponse;
import com.oracle.bmc.objectstorage.responses.GetWorkRequestResponse;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadResponse;

/**
 *
 * @author OU Xingning
 * @date 2024/01/03
 */
public class ObjectStorageService {

	private static final String CONFIG_PROFILE = "DEFAULT";

	private static final Region REGION = Region.AP_SINGAPORE_1;
	private static final String BUCKET_NAME = "cash-bucket";
	private static final String NAMESPACE_NAME = "cash026";

	public String uploadObject(String objectName, File file) throws IOException {
		// Create an authentication provider.
		final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parseDefault(CONFIG_PROFILE);
		final AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);

		ObjectStorage client = ObjectStorageClient.builder().region(REGION).build(provider);

		// configure upload settings as desired
		UploadConfiguration uploadConfiguration = UploadConfiguration.builder().allowMultipartUploads(true)
				.allowParallelUploads(true).build();

		UploadManager uploadManager = new UploadManager(client, uploadConfiguration);

		PutObjectRequest request = PutObjectRequest.builder().bucketName(BUCKET_NAME).namespaceName(NAMESPACE_NAME)
				.objectName(objectName)
//						.contentType(contentType)
//						.contentLanguage(contentLanguage)
//						.contentEncoding(contentEncoding)
				.build();

		UploadRequest uploadDetails = UploadRequest.builder(file).allowOverwrite(true).build(request);

		// upload request and print result
		UploadResponse response = uploadManager.upload(uploadDetails);
		System.out.println(response);

		return response.getOpcRequestId();
	}

	// Copy object from certain bucket to another bucket.
	public void copyObject(String sourceObjectName, String destinationBucketName, String destinationObjectName)
			throws Exception {

		// Create an authentication provider.
		final ConfigFileReader.ConfigFile configFile = ConfigFileReader.parseDefault(CONFIG_PROFILE);
		final AuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);

		ObjectStorage client = ObjectStorageClient.builder().region(REGION).build(provider);

		CopyObjectDetails copyObjectDetails = CopyObjectDetails.builder().sourceObjectName(sourceObjectName)
				.destinationRegion(REGION.getRegionId()).destinationNamespace(NAMESPACE_NAME)
				.destinationBucket(destinationBucketName).destinationObjectName(destinationObjectName).build();
		CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder().namespaceName(NAMESPACE_NAME)
				.bucketName(BUCKET_NAME).copyObjectDetails(copyObjectDetails).build();

		CopyObjectResponse copyObjectResponse = client.copyObject(copyObjectRequest);
		System.out.println("Wait for copy to finish.");

		GetWorkRequestRequest getWorkRequestRequest = GetWorkRequestRequest.builder()
				.workRequestId(copyObjectResponse.getOpcWorkRequestId()).build();

		GetWorkRequestResponse getWorkRequestResponse = client.getWaiters().forWorkRequest(getWorkRequestRequest)
				.execute();

		WorkRequest.Status status = getWorkRequestResponse.getWorkRequest().getStatus();
		System.out.println("Work request is now in " + status + " state.");

		if (status == WorkRequest.Status.Completed) {
			System.out.println("Verify that the object has been copied.");

			// 删除源文件
//			if (deleteSourceObject) {
//				this.deleteObject(provider, namespace, bucketName, sourceObjectName);
//			}
		}

		client.close();
	}
	

	public void uploadObject(final AuthenticationDetailsProvider provider) {

		String namespaceName = null;
		String bucketName = null;
		String objectName = null;
		String contentType = null;
		String contentEncoding = null;
		String contentLanguage = null;
		File body = null;

		ObjectStorage client = ObjectStorageClient.builder().region(Region.AP_SINGAPORE_1).build(provider);

		// configure upload settings as desired
		UploadConfiguration uploadConfiguration = UploadConfiguration.builder().allowMultipartUploads(true)
				.allowParallelUploads(true).build();

		UploadManager uploadManager = new UploadManager(client, uploadConfiguration);

		PutObjectRequest request = PutObjectRequest.builder().bucketName(bucketName).namespaceName(namespaceName)
				.objectName(objectName).contentType(contentType).contentLanguage(contentLanguage)
				.contentEncoding(contentEncoding).build();

		UploadRequest uploadDetails = UploadRequest.builder(body).allowOverwrite(true).build(request);

		// upload request and print result
		UploadResponse response = uploadManager.upload(uploadDetails);
		System.out.println(response);

	}

	public void copyObject(final AuthenticationDetailsProvider provider, String namespace, String bucketName,
			String sourceObjectName, String destinationObjectName, boolean deleteSourceObject) throws Exception {

		ObjectStorage objectStorageClient = ObjectStorageClient.builder().build(provider);
		objectStorageClient.setRegion(Region.AP_TOKYO_1);

		CopyObjectDetails copyObjectDetails = CopyObjectDetails.builder().sourceObjectName(sourceObjectName)
				.destinationRegion(Region.AP_TOKYO_1.getRegionId()).destinationNamespace(namespace)
				.destinationBucket(bucketName).destinationObjectName(destinationObjectName).build();
		CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder().namespaceName(namespace)
				.bucketName(bucketName).copyObjectDetails(copyObjectDetails).build();

		CopyObjectResponse copyObjectResponse = objectStorageClient.copyObject(copyObjectRequest);
		System.out.println("Wait for copy to finish.");

		GetWorkRequestRequest getWorkRequestRequest = GetWorkRequestRequest.builder()
				.workRequestId(copyObjectResponse.getOpcWorkRequestId()).build();

		GetWorkRequestResponse getWorkRequestResponse = objectStorageClient.getWaiters()
				.forWorkRequest(getWorkRequestRequest).execute();

		WorkRequest.Status status = getWorkRequestResponse.getWorkRequest().getStatus();
		System.out.println("Work request is now in " + status + " state.");

		if (status == WorkRequest.Status.Completed) {
			System.out.println("Verify that the object has been copied.");

			// 删除源文件
			if (deleteSourceObject) {
				this.deleteObject(provider, namespace, bucketName, sourceObjectName);
			}
		}

		objectStorageClient.close();
	}

	public void deleteObject(final AuthenticationDetailsProvider provider, String namespace, String bucketName,
			String objectName) {

		ObjectStorage objectStorageClient = ObjectStorageClient.builder().build(provider);
		objectStorageClient.setRegion(Region.AP_TOKYO_1);

		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().namespaceName(namespace)
				.bucketName(bucketName).objectName(objectName).build();

		DeleteObjectResponse deleteObjectResponse = objectStorageClient.deleteObject(deleteObjectRequest);
		System.out.println("Delete object " + objectName + ", response: " + deleteObjectResponse);

	}

}
