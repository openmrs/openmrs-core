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

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GlobalPropertyCacheTest {

	private AdministrationDAO dao;

	private Cache cache;

	private GlobalPropertyCache globalPropertyCache;

	@BeforeEach
	public void setUp() {
		dao = mock(AdministrationDAO.class);
		when(dao.isDatabaseStringComparisonCaseSensitive()).thenReturn(true);

		ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(GlobalPropertyCache.CACHE_NAME);
		cache = cacheManager.getCache(GlobalPropertyCache.CACHE_NAME);
		globalPropertyCache = new GlobalPropertyCache(cacheManager, dao);
	}

	@AfterEach
	public void tearDown() {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}
	}

	@Test
	public void get_shouldOnlyLoadAPropertyOnce() {
		givenProperty("some.property", "value");

		assertEquals("value", globalPropertyCache.get("some.property").getValue());
		assertEquals("value", globalPropertyCache.get("some.property").getValue());

		verify(dao, times(1)).getGlobalPropertyObject("some.property");
	}

	@Test
	public void get_shouldCacheAbsentProperties() {
		assertSame(GlobalPropertyCache.Entry.ABSENT, globalPropertyCache.get("missing.property"));
		assertSame(GlobalPropertyCache.Entry.ABSENT, globalPropertyCache.get("missing.property"));

		verify(dao, times(1)).getGlobalPropertyObject("missing.property");
	}

	@Test
	public void get_shouldIgnoreCaseIfTheDaoIgnoresCase() {
		givenProperty("some.property", "value");

		globalPropertyCache.get("some.property");
		assertEquals("value", globalPropertyCache.get("SOME.Property").getValue());

		verify(dao, times(1)).getGlobalPropertyObject("some.property");
		verify(dao, times(0)).getGlobalPropertyObject("SOME.Property");
	}

	@Test
	public void get_shouldCacheEachSpellingSeparatelyIfTheDaoMayBeCaseSensitive() {
		when(dao.isDatabaseStringComparisonCaseSensitive()).thenReturn(false);
		givenProperty("some.property", "value");

		assertFalse(globalPropertyCache.get("SOME.Property").isPresent());
		assertEquals("value", globalPropertyCache.get("some.property").getValue());
	}

	@Test
	public void get_shouldSnapshotTheViewPrivilege() {
		GlobalProperty property = givenProperty("some.property", "value");
		property.setViewPrivilege(new Privilege("Some Privilege"));

		GlobalPropertyCache.Entry entry = globalPropertyCache.get("some.property");

		assertTrue(entry.isPresent());
		assertEquals("Some Privilege", entry.getViewPrivilege());
	}

	@Test
	public void get_shouldNotCacheAPresentPropertyUntilTheTransactionCommits() {
		givenProperty("some.property", "value");
		TransactionSynchronizationManager.initSynchronization();

		globalPropertyCache.get("some.property");
		assertNull(cache.get("some.property"));

		commit();

		assertEquals("value", cache.get("some.property", GlobalPropertyCache.Entry.class).getValue());
	}

	@Test
	public void get_shouldNotCacheAPresentPropertyIfTheTransactionRollsBack() {
		givenProperty("some.property", "value");
		TransactionSynchronizationManager.initSynchronization();

		globalPropertyCache.get("some.property");

		rollback();

		assertNull(cache.get("some.property"));
	}

	@Test
	public void get_shouldCacheAnAbsentPropertyImmediatelyWithinATransaction() {
		TransactionSynchronizationManager.initSynchronization();

		globalPropertyCache.get("missing.property");

		assertSame(GlobalPropertyCache.Entry.ABSENT, cache.get("missing.property", GlobalPropertyCache.Entry.class));
	}

	@Test
	public void get_shouldLoadEveryTimeIfTheCacheIsUnavailable() {
		globalPropertyCache = new GlobalPropertyCache(new ConcurrentMapCacheManager("someOtherCache") {

			@Override
			public Cache getCache(String name) {
				return GlobalPropertyCache.CACHE_NAME.equals(name) ? null : super.getCache(name);
			}
		}, dao);

		assertFalse(globalPropertyCache.get("missing.property").isPresent());
		assertFalse(globalPropertyCache.get("missing.property").isPresent());

		verify(dao, times(2)).getGlobalPropertyObject("missing.property");
	}

	@Test
	public void getIfCached_shouldReturnACachedPropertyWithoutLoadingIt() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");

		assertEquals("value", globalPropertyCache.getIfCached("SOME.Property").getValue());
		verify(dao, times(1)).getGlobalPropertyObject("some.property");
	}

	@Test
	public void getIfCached_shouldReturnNullIfThePropertyIsNotCached() {
		assertNull(globalPropertyCache.getIfCached("some.property"));

		globalPropertyCache.get("other.property");
		assertNull(globalPropertyCache.getIfCached("some.property"));

		verify(dao, times(0)).getGlobalPropertyObject("some.property");
	}

	@Test
	public void getIfCached_shouldReturnNullGivenANullPropertyName() {
		globalPropertyCache.get("some.property");

		assertNull(globalPropertyCache.getIfCached(null));
	}

	@Test
	public void evict_shouldEvictTheProperty() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.get("other.property");

		globalPropertyCache.evict("Some.Property");

		assertNull(cache.get("some.property"));
		assertSame(GlobalPropertyCache.Entry.ABSENT, cache.get("other.property", GlobalPropertyCache.Entry.class));
	}

	@Test
	public void evict_shouldEvictEverySpellingIfTheDaoMayBeCaseSensitive() {
		when(dao.isDatabaseStringComparisonCaseSensitive()).thenReturn(false);
		givenProperty("some.property", "value");
		givenProperty("SOME.Property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.get("SOME.Property");

		globalPropertyCache.evict("some.property");

		assertNull(cache.get("some.property"));
		assertNull(cache.get("SOME.Property"));
	}

	@Test
	public void evict_shouldReconsiderTheKeysIfTheCaseSensitivityPropertyChanges() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");

		when(dao.isDatabaseStringComparisonCaseSensitive()).thenReturn(false);
		globalPropertyCache.evict(OpenmrsConstants.GP_CASE_SENSITIVE_DATABASE_STRING_COMPARISON);
		globalPropertyCache.get("some.property");

		// with lower-cased keys this would find the entry cached for "some.property"
		assertFalse(globalPropertyCache.get("SOME.Property").isPresent());
	}

	@Test
	public void evict_shouldEvictAgainWhenTheTransactionCommits() {
		TransactionSynchronizationManager.initSynchronization();
		globalPropertyCache.evict("some.property");

		// another transaction caches the old value while this one is still running
		cache.put("some.property", GlobalPropertyCache.Entry.of(new GlobalProperty("some.property", "old")));

		commit();

		assertNull(cache.get("some.property"));
	}

	@Test
	public void evict_shouldEvictAgainWhenTheTransactionRollsBack() {
		TransactionSynchronizationManager.initSynchronization();
		globalPropertyCache.evict("some.property");

		// the purging transaction cached the property as absent before rolling back
		globalPropertyCache.get("some.property");

		rollback();

		assertNull(cache.get("some.property"));
	}

	@Test
	public void clear_shouldEvictEveryProperty() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");
		globalPropertyCache.get("missing.property");

		globalPropertyCache.clear();

		assertNull(cache.get("some.property"));
		assertNull(cache.get("missing.property"));
	}

	@Test
	public void clear_shouldDiscardValuesTheTransactionReadBeforeClearing() {
		givenProperty("some.property", "old");
		TransactionSynchronizationManager.initSynchronization();
		globalPropertyCache.get("some.property");

		globalPropertyCache.clear();
		commit();

		assertNull(cache.get("some.property"));
	}

	@Test
	public void onApplicationEvent_shouldEvictEveryProperty() {
		givenProperty("some.property", "value");
		globalPropertyCache.get("some.property");

		globalPropertyCache.onApplicationEvent(new ContextRefreshedEvent(new GenericApplicationContext()));

		assertNull(cache.get("some.property"));
	}

	private GlobalProperty givenProperty(String name, String value) {
		GlobalProperty property = new GlobalProperty(name, value);
		when(dao.getGlobalPropertyObject(name)).thenReturn(property);
		return property;
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
