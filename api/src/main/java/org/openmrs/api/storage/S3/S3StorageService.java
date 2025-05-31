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

import java.util.UUID;


import org.openmrs.api.StorageService;
import org.openmrs.api.storage.BaseStorageService;
import org.openmrs.api.storage.ObjectMetadata;
import org.openmrs.api.storage.StorageServiceCondition;
import org.openmrs.api.stream.StreamDataService;
import org.openmrs.api.stream.StreamDataWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.stream.Stream;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;





@Service
@Qualifier("s3")
@Conditional(StorageServiceCondition.class)
public class S3StorageService extends BaseStorageService implements StorageService {

    protected AmazonS3 s3Client;
    private final String bucketName = "PLACEHOLDER_BUCKET_NAME"; // Replace with your actual S3 bucket name

    @Autowired
public S3StorageService(StreamDataService streamDataService) {
    super(streamDataService);
    this.s3Client = AmazonS3ClientBuilder.standard().build();
}

    @Override
    public InputStream getData(String key) throws IOException {
        S3Object s3object = s3Client.getObject(bucketName, key);
        return s3object.getObjectContent();
    }

    @Override
    public InputStream getTempData(String key) throws IOException {
        return getData("temp/" + key);
    }

    @Override
    public ObjectMetadata getMetadata(String key) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        com.amazonaws.services.s3.model.ObjectMetadata awsMetadata = s3Client.getObjectMetadata(bucketName, key);
        metadata.setContentType(awsMetadata.getContentType());
        metadata.setSize(awsMetadata.getContentLength());
        return metadata;
    }

    @Override
    public Stream<String> getKeys(String moduleIdOrGroup, String keyPrefix) throws IOException {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(moduleIdOrGroup + "/" + keyPrefix);
        ListObjectsV2Result result = s3Client.listObjectsV2(req);
        return result.getObjectSummaries().stream().map(S3ObjectSummary::getKey);
    }

    @Override
    public String saveData(InputStream inputStream, ObjectMetadata metadata, String moduleIdOrGroup) throws IOException {
        String key = moduleIdOrGroup + "/" + UUID.randomUUID().toString();
        return saveData(inputStream, metadata, moduleIdOrGroup, key);
    }

    @Override
    public String saveTempData(InputStream inputStream, ObjectMetadata metadata) throws IOException {
        String key = "temp/" + UUID.randomUUID().toString();
        return saveData(inputStream, metadata, "temp", key);
    }

    @Override
    public String saveTempData(StreamDataWriter writer, ObjectMetadata metadata) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writer.write(outputStream);
        return saveTempData(new ByteArrayInputStream(outputStream.toByteArray()), metadata);
    }

    @Override
    public String saveData(InputStream inputStream, ObjectMetadata metadata, String moduleIdOrGroup, String keySuffix) throws IOException {
        com.amazonaws.services.s3.model.ObjectMetadata awsMetadata = new com.amazonaws.services.s3.model.ObjectMetadata();
        if (metadata != null) {
            awsMetadata.setContentType(metadata.getContentType());
            awsMetadata.setContentLength(metadata.getSize());
        }
        s3Client.putObject(bucketName, keySuffix, inputStream, awsMetadata);
        return keySuffix;
    }

    @Override
    public String saveData(StreamDataWriter writer, ObjectMetadata metadata, String moduleIdOrGroup, String keySuffix) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writer.write(outputStream);
        return saveData(new ByteArrayInputStream(outputStream.toByteArray()), metadata, moduleIdOrGroup, keySuffix);
    }

    @Override
    public String saveData(StreamDataWriter writer, ObjectMetadata metadata, String moduleIdOrGroup) throws IOException {
        String key = moduleIdOrGroup + "/" + UUID.randomUUID().toString();
        return saveData(writer, metadata, moduleIdOrGroup, key);
    }

    @Override
    public boolean purgeData(String key) throws IOException {
        if (!s3Client.doesObjectExist(bucketName, key)) {
            return false;
        }
        s3Client.deleteObject(bucketName, key);
        return true;
    }

    @Override
    public boolean exists(String key) {
        return s3Client.doesObjectExist(bucketName, key);
    }
}
	

