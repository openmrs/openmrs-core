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

import java.io.Serializable;
import java.util.Locale;

import org.openmrs.GlobalProperty;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Caches the global property lookups made through
 * {@link org.openmrs.api.AdministrationService#getGlobalProperty(String)}, including lookups of
 * properties that do not exist. Values are immutable {@link Entry} snapshots rather than
 * {@link GlobalProperty} entities, which are mutable and lazily load their privileges.
 * <p>
 * Keys follow the case rules of {@link AdministrationDAO#getGlobalPropertyObject(String)}, so every
 * name returns what the DAO would return for it. When
 * {@value OpenmrsConstants#GP_CASE_SENSITIVE_DATABASE_STRING_COMPARISON} is true the DAO ignores
 * case, so keys are lower-cased and a write evicts a single key. Otherwise whether the DAO ignores
 * case depends on the database, so keys are the exact names requested and a write clears the whole
 * cache, since other spellings of the name may be cached too.
 * <p>
 * Only committed data is cached. A property that exists is written to the cache after the reading
 * transaction commits, so a value written but not yet committed by the same transaction is never
 * cached. A property that does not exist is cached immediately, because in practice absence can
 * only be uncommitted if the reading transaction purged the property itself. In that case the
 * absence is visible to other threads until the purge completes and {@link #evict(String)} evicts
 * the entry.
 * <p>
 * Callers that write a global property must call {@link #evict(String)}. Changes to the
 * {@code global_property} table made outside the API are not detected, but the {@code
 * global-properties} cache template's lifespan limits how long they can be served stale.
 *
 * @since 2.8.10
 */
@Component("globalPropertyCache")
public class GlobalPropertyCache implements ApplicationListener<ContextRefreshedEvent> {

	public static final String CACHE_NAME = "globalProperties";

	/**
	 * Caches whether keys are lower-cased. The NUL character keeps it from colliding with a property
	 * name, and a plain string needs no special marshalling in a cluster.
	 */
	private static final String KEY_MODE = "\0caseInsensitiveKeys";

	private final CacheManager cacheManager;

	private final AdministrationDAO dao;

	@Autowired
	public GlobalPropertyCache(@Qualifier("apiCacheManager") CacheManager cacheManager, AdministrationDAO dao) {
		this.cacheManager = cacheManager;
		this.dao = dao;
	}

	/**
	 * Returns a snapshot of the named property, loading it from the DAO on a miss. Never returns null;
	 * a property that does not exist is returned as {@link Entry#ABSENT}.
	 * <p>
	 * The snapshot does not record whether the current user may view the property, so callers must
	 * check {@link Entry#getViewPrivilege()} on every call.
	 *
	 * @param propertyName the name of the property, not null
	 * @return a snapshot of the property
	 */
	public Entry get(String propertyName) {
		Cache cache = getCache();
		if (cache == null) {
			return load(propertyName);
		}

		String key = key(isKeyedCaseInsensitively(cache), propertyName);
		Entry cached = cache.get(key, Entry.class);
		if (cached != null) {
			return cached;
		}

		Entry loaded = load(propertyName);
		if (loaded.isPresent()) {
			new TransactionAwareCacheDecorator(cache).put(key, loaded);
		} else {
			cache.put(key, loaded);
		}
		return loaded;
	}

	/**
	 * Returns the cached snapshot of the named property without loading it, or null if it is not
	 * cached. Unlike {@link #get(String)}, this needs no transaction or database access, which makes it
	 * suitable for callers that must avoid them. As with {@link #get(String)}, callers must check
	 * whether the current user may view the property.
	 *
	 * @param propertyName the name of the property
	 * @return a snapshot of the property, or null if it is not cached
	 */
	public Entry getIfCached(String propertyName) {
		Cache cache = getCache();
		if (cache == null || propertyName == null) {
			return null;
		}

		Boolean caseInsensitive = cache.get(KEY_MODE, Boolean.class);
		if (caseInsensitive == null) {
			return null;
		}
		return cache.get(key(caseInsensitive, propertyName), Entry.class);
	}

	/**
	 * Evicts the named property. Must be called whenever a global property is saved or purged.
	 * <p>
	 * The property is evicted immediately, so that the current transaction usually reads its own write,
	 * and again when the current transaction completes, so that values cached by other transactions
	 * while it was running are discarded whether it commits or rolls back.
	 *
	 * @param propertyName the name of the property, not null
	 */
	public void evict(String propertyName) {
		Cache cache = getCache();
		if (cache == null) {
			return;
		}

		if (OpenmrsConstants.GP_CASE_SENSITIVE_DATABASE_STRING_COMPARISON.equalsIgnoreCase(propertyName)
		        || !isKeyedCaseInsensitively(cache)) {
			clear();
			return;
		}

		String key = key(true, propertyName);
		cache.evictIfPresent(key);
		afterCompletion(() -> cache.evict(key));
	}

	/**
	 * Evicts every property. Use this when global properties may have been changed outside the API, for
	 * example by Liquibase.
	 * <p>
	 * As with {@link #evict(String)}, the cache is cleared immediately and again when the current
	 * transaction completes, which also discards values the transaction read before the change.
	 */
	public void clear() {
		Cache cache = getCache();
		if (cache != null) {
			cache.invalidate();
			afterCompletion(cache::invalidate);
		}
	}

	/**
	 * Clears the cache whenever the application context is refreshed, which happens whenever modules
	 * are started or stopped.
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		clear();
	}

	private Entry load(String propertyName) {
		return Entry.of(dao.getGlobalPropertyObject(propertyName));
	}

	private static String key(boolean caseInsensitive, String propertyName) {
		return caseInsensitive ? propertyName.toLowerCase(Locale.ROOT) : propertyName;
	}

	/**
	 * Whether the DAO ignores case, cached under {@link #KEY_MODE} so that clearing the cache, on any
	 * node, also discards it. It is put only on commit, like any other existing property.
	 */
	private boolean isKeyedCaseInsensitively(Cache cache) {
		Boolean caseInsensitive = cache.get(KEY_MODE, Boolean.class);
		if (caseInsensitive == null) {
			caseInsensitive = dao.isDatabaseStringComparisonCaseSensitive();
			new TransactionAwareCacheDecorator(cache).put(KEY_MODE, caseInsensitive);
		}
		return caseInsensitive;
	}

	/**
	 * Runs <code>action</code> once the current transaction commits or rolls back. Transactions run
	 * {@code afterCommit} callbacks, including the deferred puts made by {@link #get}, before any
	 * {@code afterCompletion} callback, so the action always runs after those puts.
	 */
	private static void afterCompletion(Runnable action) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

				@Override
				public void afterCompletion(int status) {
					action.run();
				}
			});
		}
	}

	private Cache getCache() {
		return cacheManager == null ? null : cacheManager.getCache(CACHE_NAME);
	}

	/**
	 * An immutable snapshot of a global property's value and view privilege, or {@link #ABSENT} if the
	 * property does not exist.
	 */
	public static final class Entry implements Serializable {

		private static final long serialVersionUID = 1L;

		public static final Entry ABSENT = new Entry(false, null, null);

		private final boolean present;

		private final String value;

		private final String viewPrivilege;

		private Entry(boolean present, String value, String viewPrivilege) {
			this.present = present;
			this.value = value;
			this.viewPrivilege = viewPrivilege;
		}

		/**
		 * @param globalProperty the property to snapshot, or null if it does not exist
		 * @return a snapshot of <code>globalProperty</code>, or {@link #ABSENT} if it is null
		 */
		public static Entry of(GlobalProperty globalProperty) {
			if (globalProperty == null) {
				return ABSENT;
			}

			String viewPrivilege = globalProperty.getViewPrivilege() == null ? null
			        : globalProperty.getViewPrivilege().getPrivilege();
			return new Entry(true, globalProperty.getPropertyValue(), viewPrivilege);
		}

		/**
		 * @return true if the property exists
		 */
		public boolean isPresent() {
			return present;
		}

		/**
		 * @return the property's value, or null if it has none or does not exist
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @return the name of the privilege needed to view the property, or null if none is needed
		 */
		public String getViewPrivilege() {
			return viewPrivilege;
		}
	}
}
