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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.spring.common.provider.SpringCache;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.cache.Cache;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GlobalPropertyCacheTest {

	private AdministrationDAO dao;

	private SpringEmbeddedCacheManager cacheManager;

	private Cache cache;

	private ExecutorService fillExecutor;

	private GlobalPropertyCache globalPropertyCache;

	@BeforeEach
	public void setUp() {
		dao = mock(AdministrationDAO.class);
		when(dao.isDatabaseStringComparisonCaseSensitive()).thenReturn(true);

		DefaultCacheManager nativeCacheManager = new DefaultCacheManager(new GlobalConfigurationBuilder().build());
		nativeCacheManager.defineConfiguration(GlobalPropertyCache.CACHE_NAME,
		    new ConfigurationBuilder().simpleCache(true).build());
		cacheManager = new SpringEmbeddedCacheManager(nativeCacheManager);
		cache = cacheManager.getCache(GlobalPropertyCache.CACHE_NAME);

		fillExecutor = Executors.newSingleThreadExecutor();
		globalPropertyCache = new GlobalPropertyCache(cacheManager, dao, mock(PlatformTransactionManager.class),
		        fillExecutor::submit);
	}

	@AfterEach
	public void tearDown() {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
		TransactionSynchronizationManager.unbindResourceIfPossible(globalPropertyCache);
		fillExecutor.shutdownNow();
		cacheManager.stop();
	}

	@Test
	public void get_shouldReadAMissThroughTheDaoAndFillTheCacheInTheBackground() {
		givenProperty("some.property", "value");

		assertEquals("value", globalPropertyCache.get("some.property").getValue());
		globalPropertyCache.awaitFills();

		assertEquals("value", cached("some.property").getValue());
	}

	@Test
	public void get_shouldNotLoadACachedProperty() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertEquals("value", globalPropertyCache.get("some.property").getValue());

		// once for the caller's read and once for the fill
		verify(dao, times(2)).getGlobalPropertyObject("some.property");
	}

	@Test
	public void get_shouldCacheAbsentProperties() {
		assertSame(GlobalPropertyCache.Entry.ABSENT, globalPropertyCache.get("missing.property"));
		globalPropertyCache.awaitFills();

		assertSame(GlobalPropertyCache.Entry.ABSENT, cached("missing.property"));
	}

	@Test
	public void get_shouldIgnoreCaseIfTheDaoIgnoresCase() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertEquals("value", globalPropertyCache.get("SOME.Property").getValue());
		verify(dao, never()).getGlobalPropertyObject("SOME.Property");
	}

	@Test
	public void get_shouldCacheEachSpellingSeparatelyIfTheDaoMayBeCaseSensitive() {
		when(dao.isDatabaseStringComparisonCaseSensitive()).thenReturn(false);
		givenProperty("some.property", "value");

		assertFalse(globalPropertyCache.get("SOME.Property").isPresent());
		globalPropertyCache.awaitFills();
		assertEquals("value", globalPropertyCache.get("some.property").getValue());
		globalPropertyCache.awaitFills();

		assertFalse(cached("SOME.Property").isPresent());
		assertTrue(cached("some.property").isPresent());
	}

	@Test
	public void get_shouldSnapshotTheViewPrivilege() {
		GlobalProperty property = givenProperty("some.property", "value");
		property.setViewPrivilege(new Privilege("Some Privilege"));

		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertEquals("Some Privilege", cached("some.property").getViewPrivilege());
	}

	@Test
	public void get_shouldStartOneFillForConcurrentMissesOfTheSameProperty() {
		List<Runnable> started = new ArrayList<>();
		globalPropertyCache = new GlobalPropertyCache(cacheManager, dao, mock(PlatformTransactionManager.class), task -> {
			started.add(task);
			return new CompletableFuture<>();
		});

		globalPropertyCache.get("some.property");
		globalPropertyCache.get("some.property");

		assertEquals(1, started.size());
	}

	@Test
	public void get_shouldNotCacheAPropertyEvictedWhileTheFillIsLoadingIt() {
		GlobalProperty property = new GlobalProperty("some.property", "old");
		// the first read is the caller's; the second, on the fill's thread, races an eviction
		when(dao.getGlobalPropertyObject("some.property")).thenReturn(property).thenAnswer(invocation -> {
			globalPropertyCache.evict("some.property");
			return property;
		});

		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertNull(cache.get("some.property"));
	}

	@Test
	public void get_shouldNotCacheAnAbsenceEvictedWhileTheFillIsLoadingIt() {
		when(dao.getGlobalPropertyObject("some.property")).thenReturn(null).thenAnswer(invocation -> {
			globalPropertyCache.evict("some.property");
			return null;
		});

		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertNull(cache.get("some.property"));
	}

	@Test
	public void get_shouldReadTheTransactionsOwnWritesWithoutTheCache() {
		givenProperty("some.property", "old");
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		TransactionSynchronizationManager.initSynchronization();
		givenProperty("some.property", "new");
		globalPropertyCache.evict("some.property");
		// another transaction fills the cache with the committed value
		cache.put("some.property", GlobalPropertyCache.Entry.of(new GlobalProperty("some.property", "old")));

		assertEquals("new", globalPropertyCache.get("some.property").getValue());
		assertNull(globalPropertyCache.getIfCached("some.property"));
	}

	@Test
	public void get_shouldNotFillAPropertyTheTransactionHasWritten() {
		TransactionSynchronizationManager.initSynchronization();
		globalPropertyCache.evict("some.property");

		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertNull(cache.get("some.property"));
	}

	@Test
	public void get_shouldUseTheCacheAgainOnceTheWritingTransactionCompletes() {
		givenProperty("some.property", "value");
		TransactionSynchronizationManager.initSynchronization();
		globalPropertyCache.evict("some.property");
		commit();
		TransactionSynchronizationManager.clearSynchronization();

		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertEquals("value", cached("some.property").getValue());
	}

	@Test
	public void get_shouldLoadEveryTimeIfTheCacheIsUnavailable() {
		globalPropertyCache = new GlobalPropertyCache(new SpringEmbeddedCacheManager(cacheManager.getNativeCacheManager()) {

			@Override
			public SpringCache getCache(String name) {
				return GlobalPropertyCache.CACHE_NAME.equals(name) ? null : super.getCache(name);
			}
		}, dao, mock(PlatformTransactionManager.class), fillExecutor::submit);

		assertFalse(globalPropertyCache.get("missing.property").isPresent());
		assertFalse(globalPropertyCache.get("missing.property").isPresent());

		verify(dao, times(2)).getGlobalPropertyObject("missing.property");
	}

	@Test
	public void getIfCached_shouldReturnACachedPropertyWithoutLoadingIt() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertEquals("value", globalPropertyCache.getIfCached("SOME.Property").getValue());
		verify(dao, times(2)).getGlobalPropertyObject("some.property");
	}

	@Test
	public void getIfCached_shouldReturnNullIfThePropertyIsNotCached() {
		assertNull(globalPropertyCache.getIfCached("some.property"));

		globalPropertyCache.get("other.property");
		globalPropertyCache.awaitFills();
		assertNull(globalPropertyCache.getIfCached("some.property"));

		verify(dao, never()).getGlobalPropertyObject("some.property");
	}

	@Test
	public void getIfCached_shouldReturnNullGivenANullPropertyName() {
		assertNull(globalPropertyCache.getIfCached(null));
	}

	@Test
	public void evict_shouldEvictTheProperty() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.get("other.property");
		globalPropertyCache.awaitFills();

		globalPropertyCache.evict("Some.Property");

		assertNull(cache.get("some.property"));
		assertSame(GlobalPropertyCache.Entry.ABSENT, cached("other.property"));
	}

	@Test
	public void evict_shouldEvictEverySpellingIfTheDaoMayBeCaseSensitive() {
		when(dao.isDatabaseStringComparisonCaseSensitive()).thenReturn(false);
		givenProperty("some.property", "value");
		givenProperty("SOME.Property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();
		globalPropertyCache.get("SOME.Property");
		globalPropertyCache.awaitFills();

		globalPropertyCache.evict("some.property");

		assertNull(cache.get("some.property"));
		assertNull(cache.get("SOME.Property"));
	}

	@Test
	public void evict_shouldReconsiderTheKeysIfTheCaseSensitivityPropertyChanges() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		when(dao.isDatabaseStringComparisonCaseSensitive()).thenReturn(false);
		globalPropertyCache.evict(OpenmrsConstants.GP_CASE_SENSITIVE_DATABASE_STRING_COMPARISON);
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		// with lower-cased keys this would find the entry cached for "some.property"
		assertFalse(globalPropertyCache.get("SOME.Property").isPresent());
	}

	@Test
	public void evict_shouldEvictAgainWhenTheTransactionCommits() {
		TransactionSynchronizationManager.initSynchronization();
		globalPropertyCache.evict("some.property");

		// another transaction fills the cache while this one is still running
		cache.put("some.property", GlobalPropertyCache.Entry.of(new GlobalProperty("some.property", "old")));

		commit();

		assertNull(cache.get("some.property"));
	}

	@Test
	public void evict_shouldEvictAgainWhenTheTransactionRollsBack() {
		TransactionSynchronizationManager.initSynchronization();
		globalPropertyCache.evict("some.property");

		cache.put("some.property", GlobalPropertyCache.Entry.ABSENT);

		rollback();

		assertNull(cache.get("some.property"));
	}

	@Test
	public void clear_shouldEvictEveryProperty() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.get("missing.property");
		globalPropertyCache.awaitFills();

		globalPropertyCache.clear();

		assertNull(cache.get("some.property"));
		assertNull(cache.get("missing.property"));
	}

	@Test
	public void clear_shouldBypassTheCacheForTheRestOfTheTransaction() {
		givenProperty("some.property", "value");
		TransactionSynchronizationManager.initSynchronization();

		globalPropertyCache.clear();
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertNull(cache.get("some.property"));
		assertNull(globalPropertyCache.getIfCached("some.property"));
	}

	@Test
	public void clear_shouldNotKeepTheKeyModeIfItIsClearedWhileTheFillIsLoadingIt() {
		when(dao.isDatabaseStringComparisonCaseSensitive()).thenAnswer(invocation -> {
			globalPropertyCache.clear();
			return true;
		});

		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertNull(globalPropertyCache.getIfCached("some.property"));
		assertNull(cache.get("some.property"));
	}

	@Test
	public void onApplicationEvent_shouldEvictEveryProperty() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		globalPropertyCache.onApplicationEvent(new ContextRefreshedEvent(new GenericApplicationContext()));

		assertNull(cache.get("some.property"));
	}

	private GlobalProperty givenProperty(String name, String value) {
		GlobalProperty property = new GlobalProperty(name, value);
		when(dao.getGlobalPropertyObject(name)).thenReturn(property);
		return property;
	}

	private GlobalPropertyCache.Entry cached(String key) {
		GlobalPropertyCache.Entry entry = cache.get(key, GlobalPropertyCache.Entry.class);
		assertNotNull(entry, "expected " + key + " to be cached");
		return entry;
	}

	private static void commit() {
		List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
		TransactionSynchronizationUtils.invokeAfterCommit(synchronizations);
		TransactionSynchronizationUtils.invokeAfterCompletion(synchronizations, TransactionSynchronization.STATUS_COMMITTED);
	}

	private static void rollback() {
		List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
		TransactionSynchronizationUtils.invokeAfterCompletion(synchronizations,
		    TransactionSynchronization.STATUS_ROLLED_BACK);
	}
}
