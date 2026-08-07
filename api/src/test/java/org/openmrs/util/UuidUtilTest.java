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
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link UuidUtil}.
 */
public class UuidUtilTest {

	/**
	 * The canonical form of a version 4 uuid: 8-4-4-4-12 lower case hex digits, with the version in the
	 * 13th digit and the variant in the 17th.
	 */
	private static final Pattern CANONICAL_VERSION_4_UUID = Pattern
	        .compile("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");

	/**
	 * The length of the uuid column, declared on {@code org.openmrs.BaseOpenmrsObject}, that generated
	 * values have to fit into.
	 */
	private static final int UUID_COLUMN_LENGTH = 38;

	/**
	 * @see UuidUtil#newUuidString()
	 */
	@Test
	public void newUuidString_shouldReturnACanonicalUuidThatFitsTheUuidColumn() {
		for (int i = 0; i < 1000; i++) {
			String uuid = UuidUtil.newUuidString();

			assertTrue(CANONICAL_VERSION_4_UUID.matcher(uuid).matches(), uuid + " is not a canonical uuid");
			assertEquals(36, uuid.length());
			assertTrue(uuid.length() <= UUID_COLUMN_LENGTH);
			// the string form has to survive a round trip through java.util.UUID unchanged
			assertEquals(uuid, UUID.fromString(uuid).toString());
		}
	}

	/**
	 * @see UuidUtil#newUuid()
	 */
	@Test
	public void newUuid_shouldReturnARandomlyGeneratedUuidOfTheIetfVariant() {
		for (int i = 0; i < 1000; i++) {
			UUID uuid = UuidUtil.newUuid();

			assertEquals(4, uuid.version(), "expected a version 4 (random) uuid");
			assertEquals(2, uuid.variant(), "expected the IETF RFC 4122 variant");
		}
	}

	/**
	 * @see UuidUtil#newUuidString()
	 */
	@Test
	public void newUuidString_shouldNotRepeatItselfOverALargeNumberOfUuids() {
		int count = 200000;
		Set<String> uuids = new HashSet<>(count * 2);

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
					Set<String> generated = new HashSet<>(perThread * 2);
					for (int j = 0; j < perThread; j++) {
						generated.add(UuidUtil.newUuidString());
					}
					return generated;
				});
			}

			Set<String> uuids = new HashSet<>(threads * perThread * 2);
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
