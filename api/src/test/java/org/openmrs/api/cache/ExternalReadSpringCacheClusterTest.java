/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.cache;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests that the Spring caches {@code apiCacheManager} hands out for caches that are only filled on
 * a miss do not invalidate each other's entries on a two-node cluster, while evictions still reach
 * every node, and that every other cache still overwrites entries.
 */
public class ExternalReadSpringCacheClusterTest {

	/** One of the caches {@link CacheConfig#EXTERNAL_READ_CACHES} names. */
	private static final String CACHE_NAME = "userSearchLocales";

	/** A cache that, like a module's, is not named there. */
	private static final String OTHER_CACHE_NAME = "moduleCache";

	private static ExternalReadSpringCacheManager[] nodes;

	@BeforeAll
	public static void startCluster() throws Exception {
		nodes = InJvmCacheCluster.start("external-read-spring-cache-test", CACHE_NAME, "entity", OTHER_CACHE_NAME, "entity");
	}

	@AfterAll
	public static void stopCluster() {
		InJvmCacheCluster.stop(nodes);
	}

	@AfterEach
	public void tearDown() {
		cache(0).clear();
		otherCache(0).clear();
	}

	@Test
	public void put_shouldNotRemoveTheEntryCachedOnAnotherNode() {
		cache(0).put("key", "value");
		cache(1).put("key", "value");

		assertEquals("value", cache(0).get("key", String.class));
		assertEquals("value", cache(1).get("key", String.class));
	}

	@Test
	public void get_shouldNotRemoveTheEntryCachedOnAnotherNodeWhenLoadingSynchronously() {
		cache(0).get("key", () -> "value");
		cache(1).get("key", () -> "value");

		assertEquals("value", cache(0).get("key", String.class));
		assertEquals("value", cache(1).get("key", String.class));
	}

	@Test
	public void put_shouldCacheNullValues() {
		cache(0).put("key", null);

		Cache.ValueWrapper cached = cache(0).get("key");
		assertNotNull(cached);
		assertNull(cached.get());
	}

	@Test
	public void putIfAbsent_shouldNotRemoveTheEntryCachedOnAnotherNode() {
		cache(0).put("key", "value");

		assertNull(cache(1).putIfAbsent("key", "value"));

		assertEquals("value", cache(0).get("key", String.class));
		assertEquals("value", cache(1).get("key", String.class));
	}

	@Test
	public void putIfAbsent_shouldReturnTheExistingValue() {
		cache(0).put("key", "existing");

		Cache.ValueWrapper existing = cache(0).putIfAbsent("key", "other");

		assertNotNull(existing);
		assertEquals("existing", existing.get());
		assertEquals("existing", cache(0).get("key", String.class));
	}

	@Test
	public void evict_shouldRemoveTheEntryOnEveryNode() {
		cache(0).put("key", "value");
		cache(1).put("key", "value");

		cache(0).evict("key");

		assertNull(cache(0).get("key"));
		assertNull(cache(1).get("key"));
	}

	@Test
	public void clear_shouldRemoveEveryEntryOnEveryNode() {
		cache(0).put("key", "value");
		cache(1).put("key", "value");
		cache(1).put("other", "value");

		cache(0).clear();

		assertNull(cache(1).get("key"));
		assertNull(cache(1).get("other"));
	}

	@Test
	public void put_shouldOverwriteAndInvalidateOtherNodesForACacheThatIsNotOnlyFilledOnAMiss() {
		otherCache(0).put("key", "old");
		otherCache(1).put("key", "old");

		otherCache(0).put("key", "new");

		assertEquals("new", otherCache(0).get("key", String.class));
		assertNull(otherCache(1).get("key"));
	}

	private static Cache cache(int node) {
		return nodes[node].getCache(CACHE_NAME);
	}

	private static Cache otherCache(int node) {
		return nodes[node].getCache(OTHER_CACHE_NAME);
	}
}
