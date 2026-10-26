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

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.notifications.cachemanagerlistener.event.MergeEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link CacheInvalidation} directly, using a local cache manager built from
 * {@code infinispan-api-local.xml}. Its use by each cache is tested with that cache.
 */
public class CacheInvalidationTest {

	private static final String CACHE_NAME = "cacheInvalidationTest";

	private static DefaultCacheManager cacheManager;

	private CacheInvalidation invalidation;

	@BeforeAll
	public static void startCacheManager() throws Exception {
		ConfigurationBuilderHolder holder = new ParserRegistry().parseFile("infinispan-api-local.xml");
		cacheManager = new DefaultCacheManager(holder, true);
		cacheManager.defineConfiguration(CACHE_NAME,
		    new ConfigurationBuilder().read(cacheManager.getCacheConfiguration("entity")).template(false).build());
	}

	@AfterAll
	public static void stopCacheManager() {
		cacheManager.stop();
	}

	@BeforeEach
	public void setUp() {
		invalidation = new CacheInvalidation("the test cache", CacheInvalidationTest::cache, cacheManager);
	}

	@AfterEach
	public void tearDown() {
		TransactionSynchronizationManager.unbindResourceIfPossible(invalidation);
		invalidation.close();
		cache().clear();
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	public void isWrittenInCurrentTransaction_shouldReportTheKeysAndAllKeysTheTransactionInvalidated() {
		TransactionSynchronizationManager.initSynchronization();

		invalidation.invalidate("some.key");

		assertTrue(invalidation.isWrittenInCurrentTransaction("some.key"));
		assertFalse(invalidation.isWrittenInCurrentTransaction("other.key"));
		assertFalse(invalidation.isWrittenInCurrentTransaction(null));

		invalidation.invalidate(CacheInvalidation.ALL);

		assertTrue(invalidation.isWrittenInCurrentTransaction("other.key"));
		assertTrue(invalidation.isWrittenInCurrentTransaction(null));
	}

	@Test
	public void forgetWritesInCurrentTransaction_shouldNotEvictWhenTheTransactionCompletes() {
		TransactionSynchronizationManager.initSynchronization();
		invalidation.invalidate("some.key");
		cache().put("some.key", "value");

		invalidation.forgetWritesInCurrentTransaction();
		complete();

		assertFalse(invalidation.isWrittenInCurrentTransaction("some.key"));
		assertTrue(cache().containsKey("some.key"));
	}

	@Test
	public void merged_shouldClearThisNodesCache() {
		cache().put("some.key", "value");

		new CacheInvalidation.PartitionMergeListener(invalidation).merged(mock(MergeEvent.class));

		assertFalse(cache().containsKey("some.key"));
	}

	private static void complete() {
		TransactionSynchronizationUtils.invokeAfterCompletion(TransactionSynchronizationManager.getSynchronizations(),
		    TransactionSynchronization.STATUS_COMMITTED);
		TransactionSynchronizationManager.clearSynchronization();
	}

	private static Cache<Object, Object> cache() {
		return cacheManager.getCache(CACHE_NAME);
	}
}
