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

import java.net.URI;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.openmrs.api.StorageService;
import org.openmrs.api.stream.StreamDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Testcontainers(disabledWithoutDocker = true) //testcontainers not supported on Windows Github Actions
public class S3StorageServiceIT extends BaseStorageServiceTest {
	
	private static final String BUCKET_NAME = "test-bucket";

	@Container
	private static S3MockContainer S3_MOCK = new S3MockContainer("4.5.0").withInitialBuckets(BUCKET_NAME);
	
	private static S3AsyncClient s3AsyncClient;
	
	@Autowired
	private StreamDataService streamDataService;
	
	@BeforeAll
	public static void setupS3Client() {
		s3AsyncClient = S3AsyncClient.builder().multipartEnabled(true)
			.region(Region.of("us-east-1"))
			.credentialsProvider(
				StaticCredentialsProvider.create(AwsBasicCredentials.create("foo", "bar")))
			.endpointOverride(URI.create(S3_MOCK.getHttpEndpoint()))
			.forcePathStyle(true)
			.build();
	}
	
	@Override
	public StorageService newStorageService() {
		return new S3StorageService(streamDataService, s3AsyncClient, BUCKET_NAME);
	}
	
	
}
