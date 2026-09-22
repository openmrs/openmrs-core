/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link UuidUtil}.
 */
public class UuidUtilTest {

	/**
	 * The length of the uuid column, declared on {@code org.openmrs.BaseOpenmrsObject}, that generated
	 * values have to fit into.
	 */
	private static final int UUID_COLUMN_LENGTH = 38;

	/**
	 * @see UuidUtil#newUuidString()
	 */
	@Test
	public void newUuidString_shouldReturnAValidUuidThatFitsTheUuidColumn() {
		String uuid = UuidUtil.newUuidString();

		// UUID.fromString() throws if this string is not a valid UUID
		UUID.fromString(uuid);
		assertTrue(uuid.length() <= UUID_COLUMN_LENGTH);
	}

	/**
	 * @see UuidUtil#newUuidString()
	 */
	@Test
	public void newUuidString_shouldNotRepeatItselfOverALargeNumberOfUuids() {
		int count = 200000;
		Set<String> uuids = new HashSet<>(count);

		for (int i = 0; i < count; i++) {
			String uuid = UuidUtil.newUuidString();
			assertTrue(uuids.add(uuid), "generated the duplicate uuid " + uuid);
		}

		assertEquals(count, uuids.size());
	}

	/**
	 * Every thread draws from its own {@link java.util.concurrent.ThreadLocalRandom} stream, so this
	 * guards against those streams being reached in a way that lets two threads produce the same value.
	 *
	 * @see UuidUtil#newUuidString()
	 */
	@Test
	public void newUuidString_shouldNotRepeatItselfWhenCalledFromMultipleThreads() throws Exception {
		int threads = 8;
		int perThread = 25000;
		ExecutorService executor = Executors.newFixedThreadPool(threads);

		try {
			List<Callable<Set<String>>> tasks = new ArrayList<>(threads);
			for (int i = 0; i < threads; i++) {
				tasks.add(() -> {
					Set<String> generated = new HashSet<>(perThread);
					for (int j = 0; j < perThread; j++) {
						generated.add(UuidUtil.newUuidString());
					}
					return generated;
				});
			}

			Set<String> uuids = new HashSet<>(threads * perThread);
			for (Future<Set<String>> result : executor.invokeAll(tasks)) {
				uuids.addAll(result.get());
			}

			assertEquals(threads * perThread, uuids.size(), "the threads generated duplicate uuids");
		} finally {
			executor.shutdown();
			assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));
		}
	}

	/**
	 * @see UuidUtil#newUuid()
	 */
	@Test
	public void newUuid_shouldReturnADifferentUuidOnEveryCall() {
		assertNotEquals(UuidUtil.newUuid(), UuidUtil.newUuid());
	}

}
