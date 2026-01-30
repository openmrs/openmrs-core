/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.api.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.StorageService;
import org.openmrs.api.stream.StreamDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.async.BlockingInputStreamAsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.S3Response;

/**
 * Amazon S3-based implementation of {@link StorageService}.
 * <p>
 * It uses S3AsyncClient under the hood for all operations to handle large files in the most performant way with
 * multipart enabled by default.
 * 
 * <p>It can be configured with the following properties: <i>storage.s3.bucketName, storage.s3.accessKeyId, 
 * storage.s3.secretAccessKey, storage.s3.region, storage.s3.multipartEnabled</i>
 * <p>
 * If not configured, the default bucket name is <i>'openmrs'</i>.
 * 
 * <p>
 * Credentials and region can also be provided by standard S3AsyncClient means. See 
 * <a href="https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/regions/providers/DefaultAwsRegionProviderChain.html">DefaultAwsRegionProviderChain</a>
 * and 
 * <a href="https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/auth/credentials/DefaultCredentialsProvider.html">DefaultCredentialsProvider</a>
 * 
 * @since 2.8.0
 */
@Service
@Qualifier("s3")
@Conditional(StorageServiceCondition.class)
public class S3StorageService extends BaseStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    protected S3AsyncClient s3AsyncClient;
	
    private final String bucketName;

    @Autowired
    public S3StorageService(
     StreamDataService streamDataService,
        @Value("${storage.s3.accessKeyId:}") String accessKeyId,
        @Value("${storage.s3.secretAccessKey:}") String secretAccessKey,
        @Value("${storage.s3.region:}") String region,
	 	@Value("${storage.s3.endpoint:}") String endpoint,
	 	@Value("${storage.s3.bucketName:openmrs}") String bucketName,
	 	@Value("${storage.s3.forcePathStyle:false}") boolean forcePathStyle,
	    @Value("${storage.s3.multipartEnabled:true}") boolean multipartEnabled
    ) {
        super(streamDataService);
		this.bucketName = bucketName;

		S3AsyncClientBuilder s3AsyncClientBuilder = S3AsyncClient.builder().multipartEnabled(multipartEnabled)
			.forcePathStyle(forcePathStyle);

		if (StringUtils.isNotBlank(accessKeyId) || StringUtils.isNotBlank(secretAccessKey)) {
			log.info("Using storage.s3.accessKeyId and storage.s3.secretAccessKey for S3 client");
			if (StringUtils.isBlank(accessKeyId) || StringUtils.isBlank(secretAccessKey)) {
				throw new IllegalArgumentException("Both storage.s3.accessKeyId and storage.s3.secretAccessKey " +
						"must be provided");
			}
			StaticCredentialsProvider awsCredentials = StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKeyId, secretAccessKey));
			s3AsyncClientBuilder = s3AsyncClientBuilder.credentialsProvider(awsCredentials);
        }
		
		if (StringUtils.isNotBlank(endpoint)) {
			s3AsyncClientBuilder = s3AsyncClientBuilder.endpointOverride(URI.create(endpoint));
		}

		if (StringUtils.isNotBlank(region)) {
			log.info("Using storage.s3.region '{}' for S3 client", region);
			s3AsyncClientBuilder = s3AsyncClientBuilder.region(Region.of(region));
		}
		
		this.s3AsyncClient = s3AsyncClientBuilder.build();
    }
	
    public S3StorageService(StreamDataService streamService, S3AsyncClient s3AsyncClient, 
							String bucketName) {
        super(streamService);
        this.s3AsyncClient = s3AsyncClient;
        this.bucketName = bucketName;
    }

    public InputStream getData(String key) throws IOException {
		CompletableFuture<ResponseInputStream<GetObjectResponse>> object = s3AsyncClient.getObject(
			GetObjectRequest.builder().bucket(bucketName).key(encodeKey(key)).build(), AsyncResponseTransformer.toBlockingInputStream());
		return waitForResponse(object);
    }

	private <T> T waitForResponse(CompletableFuture<T> object) throws IOException {
		T result;
		try {
			result = object.get();
		} catch (InterruptedException e) {
			throw new InterruptedIOException(e.getMessage());
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
		return result;
	}

	public ObjectMetadata getMetadata(String key) throws IOException {
		HeadObjectRequest request = HeadObjectRequest.builder().bucket(bucketName).key(encodeKey(key)).build();
		CompletableFuture<HeadObjectResponse> headRequest = s3AsyncClient.headObject(request);
		HeadObjectResponse awsMetadata = waitForResponse(headRequest);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setMimeType(awsMetadata.contentType());
		metadata.setLength(awsMetadata.contentLength());
		return metadata;
    }

    public Stream<String> getKeys(String moduleIdOrGroup, String keyPrefix) throws IOException {
		String key = newKey(moduleIdOrGroup, keyPrefix, null);
		
        ListObjectsV2Request req = ListObjectsV2Request.builder().bucket(bucketName)
			.prefix(encodeKey(key)).build();
        CompletableFuture<ListObjectsV2Response> listObjectsRequest = s3AsyncClient.listObjectsV2(req);
		ListObjectsV2Response listObjects = waitForResponse(listObjectsRequest);
		
		return listObjects.contents().stream().map(S3Object::key).map(foundKey -> {
			foundKey = decodeKey(foundKey);
			String dirContent = foundKey.substring(key.length());
			int subdir = dirContent.indexOf("/");
			if (subdir != -1) {
				// Return only subdirectories without their content
				String dirOnly = dirContent.substring(0, subdir + 1);
				return key + dirOnly;
			} else {
				return foundKey;
			}
		}).distinct(); // Remove duplicate subdirectories
	}

    public boolean purgeData(String key) throws IOException {
		if (exists(key)) {
			DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(bucketName).key(encodeKey(key)).build();
			CompletableFuture<DeleteObjectResponse> deleteRequest = s3AsyncClient.deleteObject(request);
			return waitForBooleanResponse(deleteRequest);
		} else {
			return false;
		}
    }
	
	private boolean waitForBooleanResponse(CompletableFuture<? extends S3Response> request) throws IOException {
		try {
			request.get();
			return true;
		} catch (InterruptedException e) {
			throw new InterruptedIOException(e.getMessage());
		} catch (ExecutionException e) {
			if (e.getCause() instanceof S3Exception) {
				S3Exception s3e = (S3Exception) e.getCause();
				if (s3e.statusCode() == 404) {
					return false;
				}
			}
			throw new IOException(e);
		}
	}

    public boolean exists(String key) throws UncheckedIOException {
        HeadObjectRequest request = HeadObjectRequest.builder().bucket(bucketName).key(encodeKey(key)).build();
		CompletableFuture<HeadObjectResponse> headRequest = s3AsyncClient.headObject(request);
		try {
			return waitForBooleanResponse(headRequest);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
    public String saveData(InputStream inputStream, ObjectMetadata metadata,
                           String moduleIdOrGroup, String keySuffix) throws IOException {
		metadata = (metadata == null) ? new ObjectMetadata() : metadata;

		String key = newKey(moduleIdOrGroup, keySuffix, metadata.getFilename());
		
		if (exists(key)) {
			throw new FileAlreadyExistsException("Key " + key + " already exists");
		}
		
		PutObjectRequest request = PutObjectRequest.builder().bucket(bucketName).key(encodeKey(key))
			.contentType(metadata.getMimeType()).contentLength(metadata.getLength()).build();

		try {
			BlockingInputStreamAsyncRequestBody requestBody = AsyncRequestBody.forBlockingInputStream(metadata.getLength());
			CompletableFuture<PutObjectResponse> putRequest = s3AsyncClient.putObject(request, requestBody);
			requestBody.writeInputStream(inputStream);

			waitForResponse(putRequest);
		} catch (Exception e) {
			throw new IOException(e);
		}
			
        return key;
    }
}
