/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event.broker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import com.google.common.io.ByteStreams;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

public class BrokerIncomingEventTest {

	@Test
	public void getPayload_shouldReturnOriginalObjectWhenNotInputStream() {
		String expectedPayload = "my-payload";
		BrokerIncomingEvent<String> event = new BrokerIncomingEvent<>(expectedPayload, "my-source");

		assertEquals(expectedPayload, event.getPayload());

		// Subsequent calls should return the exact same object
		assertEquals(expectedPayload, event.getPayload());
	}

	@Test
	public void getPayload_shouldCacheAndReturnMultipleInputStreamsWhenPayloadIsInputStream() throws Exception {
		byte[] data = "test data".getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream originalStream = new ByteArrayInputStream(data);
		BrokerIncomingEvent<InputStream> event = new BrokerIncomingEvent<>(originalStream, "my-source");

		// Each call should return a new, independent InputStream
		InputStream stream1 = event.getPayload();
		InputStream stream2 = event.getPayload();

		assertNotSame(originalStream, stream1);
		assertNotSame(stream1, stream2);

		// Both streams should contain the same complete data
		assertArrayEquals(data, ByteStreams.toByteArray(stream1));
		assertArrayEquals(data, ByteStreams.toByteArray(stream2));
	}

	@Test
	public void getPayload_shouldHandleLargeInputStreamsAndSpillToFile() throws Exception {
		// Create payload larger than MAX_MEMORY_PAYLOAD_SIZE (256 KB)
		int size = 300 * 1024; // 300 KB
		byte[] data = new byte[size];
		new Random().nextBytes(data);

		ByteArrayInputStream originalStream = new ByteArrayInputStream(data);
		BrokerIncomingEvent<InputStream> event = new BrokerIncomingEvent<>(originalStream, "my-source");

		InputStream stream1 = event.getPayload();
		InputStream stream2 = event.getPayload();

		assertArrayEquals(data, ByteStreams.toByteArray(stream1));
		assertArrayEquals(data, ByteStreams.toByteArray(stream2));
	}

	@Test
	public void getPayload_shouldBeThreadSafeWhenCachingInputStream() throws Exception {
		byte[] data = "test thread-safe data".getBytes(StandardCharsets.UTF_8);
		ByteArrayInputStream originalStream = new ByteArrayInputStream(data);
		BrokerIncomingEvent<InputStream> event = new BrokerIncomingEvent<>(originalStream, "my-source");

		int numThreads = 10;
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		List<Future<byte[]>> futures = new ArrayList<>();

		for (int i = 0; i < numThreads; i++) {
			futures.add(executor.submit(() -> ByteStreams.toByteArray(event.getPayload())));
		}

		for (Future<byte[]> future : futures) {
			assertArrayEquals(data, future.get());
		}
	}
}
