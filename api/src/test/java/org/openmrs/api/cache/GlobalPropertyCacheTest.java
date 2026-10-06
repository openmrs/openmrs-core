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
import java.util.concurrent.atomic.AtomicInteger;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.infinispan.spring.common.provider.SpringCache;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
	public void get_shouldOnlyServeAnEntryForTheSpellingItWasLoadedFor() {
		givenProperty("some.property", "value");
		givenProperty("SOME.Property", "other");
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertEquals("other", globalPropertyCache.get("SOME.Property").getValue());
		globalPropertyCache.awaitFills();

		// read by the caller only, since a fill cannot replace the entry for the other spelling
		verify(dao, times(1)).getGlobalPropertyObject("SOME.Property");
		assertEquals("value", cached("some.property").getValue());
	}

	@Test
	public void get_shouldNotCacheAPropertyStoredUnderANameThatLowerCasesDifferently() {
		// for example a collation that ignores trailing spaces
		when(dao.getGlobalPropertyObject("some.property ")).thenReturn(new GlobalProperty("some.property", "value"));
		when(dao.getStoredGlobalPropertyName("some.property ")).thenReturn("some.property");

		assertEquals("value", globalPropertyCache.get("some.property ").getValue());
		globalPropertyCache.awaitFills();

		assertNull(cache.get("some.property "));
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
			return null;
		});

		globalPropertyCache.get("some.property");
		globalPropertyCache.get("some.property");

		assertEquals(1, started.size());
	}

	@Test
	public void get_shouldNotRunMoreThanTheMaximumNumberOfFillsAtOnce() {
		List<Runnable> started = new ArrayList<>();
		globalPropertyCache = new GlobalPropertyCache(cacheManager, dao, mock(PlatformTransactionManager.class), task -> {
			started.add(task);
			return null;
		});

		for (int i = 0; i <= GlobalPropertyCache.MAX_CONCURRENT_FILLS; i++) {
			globalPropertyCache.get("property." + i);
		}
		assertEquals(GlobalPropertyCache.MAX_CONCURRENT_FILLS, started.size());

		// a finished fill frees its permit for a later miss
		started.get(0).run();
		globalPropertyCache.get("property." + GlobalPropertyCache.MAX_CONCURRENT_FILLS);
		assertEquals(GlobalPropertyCache.MAX_CONCURRENT_FILLS + 1, started.size());
	}

	@Test
	public void get_shouldReleaseTheFillPermitIfTheFillCannotBeStarted() {
		List<Runnable> started = new ArrayList<>();
		AtomicInteger attempts = new AtomicInteger();
		globalPropertyCache = new GlobalPropertyCache(cacheManager, dao, mock(PlatformTransactionManager.class), task -> {
			if (attempts.incrementAndGet() <= GlobalPropertyCache.MAX_CONCURRENT_FILLS) {
				throw new IllegalStateException("no threads");
			}
			started.add(task);
			return null;
		});

		for (int i = 0; i <= GlobalPropertyCache.MAX_CONCURRENT_FILLS; i++) {
			globalPropertyCache.get("property." + i);
		}

		assertEquals(1, started.size());
	}

	@Test
	public void get_shouldReleaseTheFillPermitIfTheFillsTaskEndsWithoutRunningIt() {
		List<Runnable> started = new ArrayList<>();
		AtomicInteger attempts = new AtomicInteger();
		globalPropertyCache = new GlobalPropertyCache(cacheManager, dao, mock(PlatformTransactionManager.class), task -> {
			if (attempts.incrementAndGet() <= GlobalPropertyCache.MAX_CONCURRENT_FILLS) {
				return CompletableFuture.failedFuture(new IllegalStateException("no session"));
			}
			started.add(task);
			return null;
		});

		for (int i = 0; i <= GlobalPropertyCache.MAX_CONCURRENT_FILLS; i++) {
			globalPropertyCache.get("property." + i);
		}

		assertEquals(1, started.size());
	}

	@Test
	public void get_shouldFillAgainAfterAFillThatRanOnTheCallingThread() {
		globalPropertyCache = new GlobalPropertyCache(cacheManager, dao, mock(PlatformTransactionManager.class), task -> {
			task.run();
			return null;
		});
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		assertEquals("value", cached("some.property").getValue());

		globalPropertyCache.evict("some.property");
		globalPropertyCache.get("some.property");

		assertEquals("value", cached("some.property").getValue());
	}

	@Test
	public void get_shouldNotCacheAPropertyEvictedWhileTheFillIsLoadingIt() {
		GlobalProperty property = new GlobalProperty("some.property", "old");
		// the first read is the caller's; the second, on the fill's thread, races an eviction
		when(dao.getGlobalPropertyObject("some.property")).thenReturn(property).thenAnswer(invocation -> {
			globalPropertyCache.evict("some.property");
			return property;
		});
		when(dao.getStoredGlobalPropertyName("some.property")).thenReturn("some.property");

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
	public void get_shouldNotCacheAPropertyClearedWhileTheFillIsLoadingIt() {
		GlobalProperty property = new GlobalProperty("some.property", "old");
		when(dao.getGlobalPropertyObject("some.property")).thenReturn(property).thenAnswer(invocation -> {
			globalPropertyCache.clear();
			return property;
		});
		when(dao.getStoredGlobalPropertyName("some.property")).thenReturn("some.property");

		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertNull(cache.get("some.property"));
	}

	@Test
	public void get_shouldNotCacheAPropertyIfTheGenerationTokenExpiresWhileTheFillIsLoadingIt() {
		GlobalProperty property = new GlobalProperty("some.property", "old");
		when(dao.getGlobalPropertyObject("some.property")).thenReturn(property).thenAnswer(invocation -> {
			cache.evict(CacheInvalidation.GENERATION);
			return property;
		});
		when(dao.getStoredGlobalPropertyName("some.property")).thenReturn("some.property");

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
		seed("some.property", "old");

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

		assertEquals("value", globalPropertyCache.getIfCached("some.property").getValue());
		verify(dao, times(2)).getGlobalPropertyObject("some.property");
	}

	@Test
	public void getIfCached_shouldReturnNullForAnotherSpellingOfACachedProperty() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		assertNull(globalPropertyCache.getIfCached("SOME.Property"));
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
	public void evict_shouldEvictTheEntryCachedForAnySpelling() {
		givenProperty("SOME.Property", "value");
		globalPropertyCache.get("SOME.Property");
		globalPropertyCache.awaitFills();

		globalPropertyCache.evict("some.property");

		assertNull(cache.get("some.property"));
	}

	@Test
	public void evict_shouldClearTheCacheIfTheCaseSensitivityPropertyChanges() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		globalPropertyCache.evict(OpenmrsConstants.GP_CASE_SENSITIVE_DATABASE_STRING_COMPARISON.toUpperCase());

		assertNull(cache.get("some.property"));
	}

	@Test
	public void evict_shouldKeepServingOtherTransactionsUntilTheTransactionCompletes() {
		seed("some.property", "old");
		TransactionSynchronizationManager.initSynchronization();

		globalPropertyCache.evict("some.property");

		assertNotNull(cache.get("some.property"));
		commit();
		assertNull(cache.get("some.property"));
	}

	@Test
	public void evict_shouldEvictWhenTheTransactionCommits() {
		TransactionSynchronizationManager.initSynchronization();
		globalPropertyCache.evict("some.property");

		// another transaction fills the cache while this one is still running
		seed("some.property", "old");

		commit();

		assertNull(cache.get("some.property"));
	}

	@Test
	public void evict_shouldEvictWhenTheTransactionRollsBack() {
		TransactionSynchronizationManager.initSynchronization();
		globalPropertyCache.evict("some.property");

		cache.put("some.property", new GlobalPropertyCache.CachedEntry("some.property", GlobalPropertyCache.Entry.ABSENT));

		rollback();

		assertNull(cache.get("some.property"));
	}

	@Test
	public void evict_shouldEvictATransactionsWritesTogether() {
		seed("some.property", "old");
		seed("other.property", "old");
		TransactionSynchronizationManager.initSynchronization();

		globalPropertyCache.evict("some.property");
		globalPropertyCache.evict("other.property");
		globalPropertyCache.evict("SOME.Property");

		assertEquals(1, TransactionSynchronizationManager.getSynchronizations().size());
		commit();
		assertNull(cache.get("some.property"));
		assertNull(cache.get("other.property"));
	}

	@Test
	public void evict_shouldEvictTheWritesOfATransactionStartedWhileAnotherIsSuspended() {
		seed("some.property", "old");
		seed("other.property", "old");
		TransactionSynchronizationManager.initSynchronization();
		globalPropertyCache.evict("some.property");

		List<TransactionSynchronization> suspended = suspend();
		assertEquals("old", globalPropertyCache.getIfCached("some.property").getValue());
		globalPropertyCache.evict("other.property");
		commit();
		assertNull(cache.get("other.property"));
		assertNotNull(cache.get("some.property"));

		resume(suspended);
		assertNull(globalPropertyCache.getIfCached("some.property"));
		commit();
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
	public void clear_shouldKeepServingOtherTransactionsUntilTheTransactionCompletes() {
		seed("some.property", "old");
		TransactionSynchronizationManager.initSynchronization();

		globalPropertyCache.clear();

		assertNotNull(cache.get("some.property"));
		commit();
		assertNull(cache.get("some.property"));
	}

	@Test
	public void clearNow_shouldEvictEveryPropertyImmediatelyWithinATransaction() {
		seed("some.property", "old");
		TransactionSynchronizationManager.initSynchronization();

		globalPropertyCache.clearNow();

		assertNull(cache.get("some.property"));
		seed("some.property", "old");
		assertNull(globalPropertyCache.getIfCached("some.property"));
	}

	@Test
	public void onApplicationEvent_shouldEvictEveryProperty() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.awaitFills();

		globalPropertyCache.onApplicationEvent(new ContextRefreshedEvent(new GenericApplicationContext()));

		assertNull(cache.get("some.property"));
	}

	@Test
	public void evict_shouldClearThisNodesCacheIfTheEvictionFails() {
		seed("some.property", "old");
		seed("other.property", "old");
		failGenerationWrites();

		globalPropertyCache.evict("some.property");

		assertNull(cache.get("some.property"));
		assertNull(cache.get("other.property"));
	}

	@Test
	public void evict_shouldClearThisNodesCacheIfTheEvictionFailsWhenTheTransactionCompletes() {
		seed("some.property", "old");
		seed("other.property", "old");
		TransactionSynchronizationManager.initSynchronization();
		globalPropertyCache.evict("some.property");
		failGenerationWrites();

		commit();

		assertNull(cache.get("some.property"));
		assertNull(cache.get("other.property"));
	}

	@Test
	public void destroy_shouldStopListeningForPartitionMerges() {
		EmbeddedCacheManager nativeCacheManager = mock(EmbeddedCacheManager.class);
		SpringEmbeddedCacheManager springCacheManager = mock(SpringEmbeddedCacheManager.class);
		when(springCacheManager.getNativeCacheManager()).thenReturn(nativeCacheManager);
		GlobalPropertyCache cacheWithMockedManager = new GlobalPropertyCache(springCacheManager, dao,
		        mock(PlatformTransactionManager.class), fillExecutor::submit);
		ArgumentCaptor<Object> listener = ArgumentCaptor.forClass(Object.class);
		verify(nativeCacheManager).addListener(listener.capture());
		assertTrue(listener.getValue() instanceof CacheInvalidation.PartitionMergeListener);

		cacheWithMockedManager.destroy();

		verify(nativeCacheManager).removeListener(listener.getValue());
	}

	/**
	 * Makes every replacement of the generation token fail, as it would if the cluster were
	 * unreachable.
	 */
	private void failGenerationWrites() {
		((org.infinispan.Cache<?, ?>) cache.getNativeCache()).addListener(new FailGenerationWrites());
	}

	@Listener
	public static class FailGenerationWrites {

		@CacheEntryCreated
		@CacheEntryModified
		public void written(CacheEntryEvent<Object, Object> event) {
			if (event.isPre() && CacheInvalidation.GENERATION.equals(event.getKey())) {
				throw new IllegalStateException("the cluster is unreachable");
			}
		}
	}

	private GlobalProperty givenProperty(String name, String value) {
		GlobalProperty property = new GlobalProperty(name, value);
		when(dao.getGlobalPropertyObject(name)).thenReturn(property);
		when(dao.getStoredGlobalPropertyName(name)).thenReturn(name);
		return property;
	}

	/** Caches a value as a fill of <code>name</code> by another transaction would. */
	private void seed(String name, String value) {
		cache.put(name.toLowerCase(),
		    new GlobalPropertyCache.CachedEntry(name, GlobalPropertyCache.Entry.of(new GlobalProperty(name, value))));
	}

	private GlobalPropertyCache.Entry cached(String key) {
		GlobalPropertyCache.CachedEntry cached = cache.get(key, GlobalPropertyCache.CachedEntry.class);
		assertNotNull(cached, "expected " + key + " to be cached");
		return cached.getEntry();
	}

	/**
	 * Suspends the current transaction's synchronizations and starts a new, empty set, as starting a
	 * new transaction would.
	 */
	private static List<TransactionSynchronization> suspend() {
		List<TransactionSynchronization> suspended = TransactionSynchronizationManager.getSynchronizations();
		suspended.forEach(TransactionSynchronization::suspend);
		TransactionSynchronizationManager.clearSynchronization();
		TransactionSynchronizationManager.initSynchronization();
		return suspended;
	}

	private static void resume(List<TransactionSynchronization> suspended) {
		TransactionSynchronizationManager.clearSynchronization();
		TransactionSynchronizationManager.initSynchronization();
		suspended.forEach(synchronization -> {
			synchronization.resume();
			TransactionSynchronizationManager.registerSynchronization(synchronization);
		});
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
