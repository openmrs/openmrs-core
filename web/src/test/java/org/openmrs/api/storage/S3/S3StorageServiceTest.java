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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.stream.StreamDataService;
import org.openmrs.api.stream.StreamDataWriter;
import org.openmrs.api.storage.ObjectMetadata;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3StorageServiceTest {
	 private AmazonS3 mockS3Client;
    private StreamDataService mockStreamDataService;
    private S3StorageService s3Service;

	 @Mock
    StreamDataService streamDataService;

    @Mock
    AmazonS3 s3Client;

    @InjectMocks
    S3StorageService s3StorageService;


    private final String bucketName = "PLACEHOLDER_BUCKET_NAME";
    private final String moduleId = "testModule";
    private final String key = "my/key";
    private final byte[] dummyData = "Hello".getBytes();

    @BeforeEach
    public void setUp() {
		System.setProperty("aws.region", "us-east-1");

        mockStreamDataService = mock(StreamDataService.class);
        mockS3Client = mock(AmazonS3.class);

        s3Service = new S3StorageService(mockStreamDataService) {
            {
                this.s3Client = mockS3Client;
            }
        };
	}
		
    @Test
    public void testGetData() throws Exception {
        S3Object mockObject = new S3Object();
        mockObject.setObjectContent(new S3ObjectInputStream(new ByteArrayInputStream(dummyData), null));
        when(mockS3Client.getObject(bucketName, key)).thenReturn(mockObject);

        InputStream result = s3Service.getData(key);
        assertNotNull(result);
    }

    @Test
    public void testGetTempData() throws Exception {
        S3Object mockObject = new S3Object();
        mockObject.setObjectContent(new S3ObjectInputStream(new ByteArrayInputStream(dummyData), null));
        when(mockS3Client.getObject(bucketName, "temp/" + key)).thenReturn(mockObject);

        InputStream result = s3Service.getTempData(key);
        assertNotNull(result);
    }

    @Test
    public void testGetMetadata() throws Exception {
        com.amazonaws.services.s3.model.ObjectMetadata awsMeta = new com.amazonaws.services.s3.model.ObjectMetadata();
        awsMeta.setContentType("text/plain");
        awsMeta.setContentLength(5L);
        when(mockS3Client.getObjectMetadata(bucketName, key)).thenReturn(awsMeta);

        ObjectMetadata metadata = s3Service.getMetadata(key);
        assertEquals("text/plain", metadata.getContentType());
        assertEquals(5L, metadata.getSize());
    }

    @Test
    public void testGetKeys() throws Exception {
        S3ObjectSummary summary1 = new S3ObjectSummary();
        summary1.setKey("test/one");
        S3ObjectSummary summary2 = new S3ObjectSummary();
        summary2.setKey("test/two");

        ListObjectsV2Result result = new ListObjectsV2Result();
      result.getObjectSummaries().addAll(Arrays.asList(summary1, summary2));

        when(mockS3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(result);

        Stream<String> keys = s3Service.getKeys("test", "");
        List<String> keyList = keys.collect(Collectors.toList());

        assertEquals(2, keyList.size());
        assertTrue(keyList.contains("test/one"));
        assertTrue(keyList.contains("test/two"));
    }

    @Test
    public void testSaveData_InputStream() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(dummyData);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        metadata.setSize(dummyData.length);

        String result = s3Service.saveData(inputStream, metadata, moduleId);
        assertTrue(result.startsWith(moduleId + "/"));
    }

    @Test
    public void testSaveData_InputStream_WithSuffix() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(dummyData);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        metadata.setSize(dummyData.length);

        String result = s3Service.saveData(inputStream, metadata, moduleId, key);
        verify(mockS3Client).putObject(eq(bucketName), eq(key), any(InputStream.class), any());
        assertEquals(key, result);
    }

    @Test
    public void testSaveData_Writer_WithSuffix() throws Exception {
        StreamDataWriter writer = outputStream -> outputStream.write(dummyData);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        metadata.setSize(dummyData.length);

        String result = s3Service.saveData(writer, metadata, moduleId, key);
        verify(mockS3Client).putObject(eq(bucketName), eq(key), any(InputStream.class), any());
        assertEquals(key, result);
    }

    @Test
    public void testSaveData_Writer_WithAutoKey() throws Exception {
        StreamDataWriter writer = outputStream -> outputStream.write(dummyData);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        metadata.setSize(dummyData.length);

        String result = s3Service.saveData(writer, metadata, moduleId);
        assertTrue(result.startsWith(moduleId + "/"));
    }

    @Test
    public void testSaveTempData_InputStream() throws Exception {
        InputStream inputStream = new ByteArrayInputStream(dummyData);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        metadata.setSize(dummyData.length);

        String result = s3Service.saveTempData(inputStream, metadata);
        assertTrue(result.startsWith("temp/"));
    }

    @Test
    public void testSaveTempData_Writer() throws Exception {
        StreamDataWriter writer = outputStream -> outputStream.write(dummyData);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        metadata.setSize(dummyData.length);

        String result = s3Service.saveTempData(writer, metadata);
        assertTrue(result.startsWith("temp/"));
    }

    @Test
    public void testPurgeDataExists() throws Exception {
        when(mockS3Client.doesObjectExist(bucketName, key)).thenReturn(true);

        boolean result = s3Service.purgeData(key);
        assertTrue(result);
        verify(mockS3Client).deleteObject(bucketName, key);
    }

    @Test
    public void testPurgeDataDoesNotExist() throws Exception {
        when(mockS3Client.doesObjectExist(bucketName, key)).thenReturn(false);

        boolean result = s3Service.purgeData(key);
        assertFalse(result);
        verify(mockS3Client, never()).deleteObject(bucketName, key);
    }

    @Test
    public void testExists() {
        when(mockS3Client.doesObjectExist(bucketName, key)).thenReturn(true);
        assertTrue(s3Service.exists(key));

        when(mockS3Client.doesObjectExist(bucketName, key)).thenReturn(false);
        assertFalse(s3Service.exists(key));
    }
}





