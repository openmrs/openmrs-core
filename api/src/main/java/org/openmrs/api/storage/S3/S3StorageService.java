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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;


import org.openmrs.api.StorageService;
import org.openmrs.api.storage.BaseStorageService;
import org.openmrs.api.storage.StorageServiceCondition;
import org.openmrs.api.stream.StreamDataService;
import org.openmrs.api.stream.StreamDataWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import java.util.stream.Stream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;








@Service
@Qualifier("s3")
@Conditional(StorageServiceCondition.class)
public class S3StorageService extends BaseStorageService implements StorageService {

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

      // Log the injected values to verify they're being loaded
    System.out.println("S3StorageService - accessKeyId: " + accessKeyId);
    System.out.println("S3StorageService - secretAccessKey: " + secretAccessKey);
    System.out.println("S3StorageService - region: " + region);


    if (!accessKeyId.isEmpty() && !secretAccessKey.isEmpty() && !region.isEmpty()) {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(region)
            .build();
    } else {
        this.s3Client = AmazonS3ClientBuilder.standard().build();
    }
}

  public S3StorageService(StreamDataService streamService, AmazonS3 s3Client, String bucketName) {
        super(streamService); 
        this.s3Client = s3Client;     
        this.bucketName = bucketName; 
    }
	

	

    @Override
    public InputStream getData(String key) throws IOException {
        S3Object s3object = s3Client.getObject(bucketName, key);
        return s3object.getObjectContent();
    }

       @Override
public org.openmrs.api.storage.ObjectMetadata getMetadata(String key) throws IOException {
    com.amazonaws.services.s3.model.ObjectMetadata awsMetadata = s3Client.getObjectMetadata(bucketName, key);

    org.openmrs.api.storage.ObjectMetadata metadata = new org.openmrs.api.storage.ObjectMetadata();
    metadata.setMimeType(awsMetadata.getContentType());
    metadata.setLength(awsMetadata.getContentLength());
    
    return metadata;
}

    
    @Override
    public Stream<String> getKeys(String moduleIdOrGroup, String keyPrefix) throws IOException {
        ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withPrefix(moduleIdOrGroup + "/" + keyPrefix);
        ListObjectsV2Result result = s3Client.listObjectsV2(req);
        return result.getObjectSummaries().stream().map(S3ObjectSummary::getKey);
    }
    
    
    public InputStream getTempData(String key) throws IOException {
        return getData("temp/" + key);
    }
   
    public String saveTempData(InputStream inputStream, ObjectMetadata metadata) throws IOException {
        String key = "temp/" + UUID.randomUUID().toString();
		 return saveData(inputStream, metadata, "temp", key);
	}
     
    
    public String saveTempData(StreamDataWriter writer, ObjectMetadata metadata) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writer.write(outputStream);
        return saveTempData(new ByteArrayInputStream(outputStream.toByteArray()), metadata);
    }

     
     public String saveData(InputStream inputStream, ObjectMetadata metadata, String moduleIdOrGroup, String keySuffix) throws IOException {
    com.amazonaws.services.s3.model.ObjectMetadata awsMetadata = new com.amazonaws.services.s3.model.ObjectMetadata();
    if (metadata != null) {
        awsMetadata.setContentType(metadata.getContentType());        
        awsMetadata.setContentLength(metadata.getContentLength());        
    }
    s3Client.putObject(bucketName, keySuffix, inputStream, awsMetadata);
    return keySuffix;
}


    
     public String saveData(StreamDataWriter writer, ObjectMetadata metadata, String moduleIdOrGroup, String keySuffix) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    writer.write(outputStream);
    
    byte[] data = outputStream.toByteArray();
    metadata.setContentLength(data.length); 

    try (InputStream inputStream = new ByteArrayInputStream(data)) {
        return saveData(inputStream, metadata, moduleIdOrGroup, keySuffix);
    }
}


   
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

    public void store(String key, ByteArrayInputStream byteArrayInputStream, int length) {
		 ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength((long) length);
    s3Client.putObject(bucketName, key, byteArrayInputStream, metadata);
      
    }

    public List<String> listFiles() {
         ListObjectsV2Result result = s3Client.listObjectsV2(bucketName);
    List<S3ObjectSummary> objects = result.getObjectSummaries();
    List<String> keys = new ArrayList<>();
    for (S3ObjectSummary os : objects) {
        keys.add(os.getKey());
    }
    return keys;
       
    }
   @Override
public String saveData(InputStream inputStream, org.openmrs.api.storage.ObjectMetadata metadata,
        String moduleIdOrGroup, String keySuffix) throws IOException {
    
    com.amazonaws.services.s3.model.ObjectMetadata awsMetadata = new com.amazonaws.services.s3.model.ObjectMetadata();

    if (metadata != null) {
        if (metadata.getMimeType() != null) {
            awsMetadata.setContentType(metadata.getMimeType());
        }

        Long contentLength = metadata.getLength(); // Use the correct method name here
        if (contentLength != null) {
            awsMetadata.setContentLength(contentLength);
        }
    }

    String fullKey = moduleIdOrGroup + "/" + keySuffix;
    s3Client.putObject(bucketName, fullKey, inputStream, awsMetadata);

    return fullKey;
}
}
