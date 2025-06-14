/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.api.storage.S3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;

import org.openmrs.api.StorageService;
import org.openmrs.api.storage.BaseStorageService;
import org.openmrs.api.storage.StorageServiceCondition;
import org.openmrs.api.stream.StreamDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import java.util.stream.Stream;
import java.io.IOException;
import java.io.InputStream;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
/**
 * Amazon S3-based implementation of {@link StorageService}.
 *
 * <p><b>Required properties:</b> storage.s3.accessKeyId, storage.s3.secretAccessKey, storage.s3.bucketName
 *  <br><b>Optional:</b> storage.s3.region, storage.s3.endpoint
 */
@Service
@Qualifier("s3")
@Conditional(StorageServiceCondition.class)
public class S3StorageService extends BaseStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    protected AmazonS3 s3Client;

    @Value("${storage.s3.bucketName:openmrs}")
    private String bucketName;

    @Autowired
    public S3StorageService(
     StreamDataService streamDataService,
        @Value("${storage.s3.accessKeyId:}") String accessKeyId,
        @Value("${storage.s3.secretAccessKey:}") String secretAccessKey,
        @Value("${storage.s3.region:}") String region
    ) {
        super(streamDataService);

        boolean anyProvided = !accessKeyId.isEmpty() || !secretAccessKey.isEmpty() || !region.isEmpty();
        boolean allProvided = !accessKeyId.isEmpty() && !secretAccessKey.isEmpty() && !region.isEmpty();

        if (anyProvided && !allProvided) {
            throw new IllegalArgumentException("If any of storage.s3.accessKeyId, secretAccessKey, or region is set, then all three must be provided.");
        }

        if (allProvided) {
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
            this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
        } else {
            this.s3Client = AmazonS3ClientBuilder.standard().build();
        }
    }

    // Required only for tests or fallback injection
    public S3StorageService(StreamDataService streamService, AmazonS3 s3Client, String bucketName) {
        super(streamService);
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public InputStream getData(String key) throws IOException {
        S3Object s3object = s3Client.getObject(bucketName, key);
        return s3object.getObjectContent();
    }

    public org.openmrs.api.storage.ObjectMetadata getMetadata(String key) throws IOException {
        com.amazonaws.services.s3.model.ObjectMetadata awsMetadata = s3Client.getObjectMetadata(bucketName, key);
        org.openmrs.api.storage.ObjectMetadata metadata = new org.openmrs.api.storage.ObjectMetadata();
        metadata.setMimeType(awsMetadata.getContentType());
        metadata.setLength(awsMetadata.getContentLength());
        return metadata;
    }

    public Stream<String> getKeys(String moduleIdOrGroup, String keyPrefix) throws IOException {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(moduleIdOrGroup + "/" + keyPrefix);
        ListObjectsV2Result result = s3Client.listObjectsV2(req);
        return result.getObjectSummaries().stream().map(S3ObjectSummary::getKey);
    }

    public boolean purgeData(String key) throws IOException {
        if (!s3Client.doesObjectExist(bucketName, key)) {
            return false;
        }
        s3Client.deleteObject(bucketName, key);
        return true;
    }

    public boolean exists(String key) {
        return s3Client.doesObjectExist(bucketName, key);
    }

    // This method is needed by BaseStorageService to store actual S3 data.
    public String saveData(InputStream inputStream, org.openmrs.api.storage.ObjectMetadata metadata,
                           String moduleIdOrGroup, String keySuffix) throws IOException {
        com.amazonaws.services.s3.model.ObjectMetadata awsMetadata = new com.amazonaws.services.s3.model.ObjectMetadata();

        if (metadata != null) {
            if (metadata.getMimeType() != null) {
                awsMetadata.setContentType(metadata.getMimeType());
            }
            Long contentLength = metadata.getLength();
            if (contentLength != null) {
                awsMetadata.setContentLength(contentLength);
            }
        }

        String fullKey = moduleIdOrGroup + "/" + keySuffix;
        s3Client.putObject(bucketName, fullKey, inputStream, awsMetadata);
        return fullKey;
    }
}
