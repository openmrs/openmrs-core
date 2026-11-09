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
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class S3StorageServiceTest {

	@Test
	public void getObjectLength_shouldReadTheTotalLengthFromContentRange() {
		// A multipart download hands back part 1's response: Content-Length is the part, not the object
		GetObjectResponse response = GetObjectResponse.builder().contentLength(8388608L)
		        .contentRange("bytes 0-8388607/20971520").build();

		assertEquals(Long.valueOf(20971520L), S3StorageService.getObjectLength(response));
	}

	@Test
	public void getObjectLength_shouldFallBackToContentLengthWithoutContentRange() {
		GetObjectResponse response = GetObjectResponse.builder().contentLength(8388608L).build();

		assertEquals(Long.valueOf(8388608L), S3StorageService.getObjectLength(response));
	}

	@Test
	public void getDataWithMetadata_shouldReportTheObjectLengthForAMultipartDownload() throws IOException {
		GetObjectResponse response = GetObjectResponse.builder().contentLength(8388608L)
		        .contentRange("bytes 0-8388607/20971520").build();
		S3AsyncClient client = mock(S3AsyncClient.class);
		doReturn(CompletableFuture.completedFuture(new ResponseInputStream<>(response, InputStream.nullInputStream())))
		        .when(client).getObject(any(GetObjectRequest.class), any(AsyncResponseTransformer.class));

		try (DataWithMetadata dwm = new S3StorageService(null, client, "bucket").getDataWithMetadata("key")) {
			assertEquals(Long.valueOf(20971520L), dwm.metadata().getLength());
		}
	}
}
