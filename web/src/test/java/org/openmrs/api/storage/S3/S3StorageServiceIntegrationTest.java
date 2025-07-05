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



import com.adobe.testing.s3mock.junit5.S3MockExtension;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.stream.StreamDataService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.util.List;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;

// NOTE: All tests in this class currently fail due to SSLHandshakeException:
// PKIX path building failed: unable to find valid certification path to requested target.
// This is likely caused by missing or untrusted CA certificates when connecting to the mock S3 endpoint.
// The test was added for review and further debugging as requested.

@ExtendWith(S3MockExtension.class)
public class S3StorageServiceIntegrationTest {
	
    
    @RegisterExtension
    static S3MockExtension S3_MOCK = S3MockExtension.builder().silent().build();
     
	private static final String BUCKET_NAME = "test-bucket";
	private S3StorageService storageService; 

    private AmazonS3 s3Client;

    private StreamDataService streamDataService;
   

    @BeforeEach
    void setup() {
		MockitoAnnotations.openMocks(this);

        s3Client = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(
                        S3_MOCK.getServiceEndpoint(),
                        "us-east-1" // or any region string
                    )
                )
                .withPathStyleAccessEnabled(true) // important for local mock
                .build();

	  this.storageService = new S3StorageService(streamDataService, s3Client, BUCKET_NAME);
	 

	}

@Test
public void testPutAndListObjects() {
    String key = "hello.txt";
    byte[] content = "Hello, S3Mock!".getBytes();

    // Put object using v1-style client
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(content.length);

    s3Client.putObject(BUCKET_NAME, key, new ByteArrayInputStream(content), metadata);

    // Verify the object exists
    ObjectListing listing = s3Client.listObjects(BUCKET_NAME);
    List<S3ObjectSummary> objects = listing.getObjectSummaries();

    assertEquals(1, objects.size());
    assertEquals(key, objects.get(0).getKey());
}

@Test
public void testStoreAndList() {
    String key = "test.txt";
    byte[] content = "Mocked data".getBytes();

    storageService.store(key, new ByteArrayInputStream(content), content.length);

    List<String> files = storageService.listFiles();
    assertTrue(files.contains(key));
}

}
