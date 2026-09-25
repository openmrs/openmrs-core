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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import org.infinispan.Cache;
import org.infinispan.counter.EmbeddedCounterManagerFactory;
import org.infinispan.counter.api.CounterConfiguration;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.CounterType;
import org.infinispan.counter.api.Storage;
import org.infinispan.counter.api.SyncStrongCounter;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

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
 * The caller never fills the cache. On a miss, the property is read through the caller's own
 * session, which sees the caller's uncommitted changes, and a fill is started in the background.
 * The fill reads the property in a new transaction on a daemon thread, so it only sees committed
 * data and never touches the caller's transaction. Evictions increment a cluster-wide counter,
 * which the fill reads before loading and checks after writing, so an entry is only kept if nothing
 * was evicted in between. After a write through the API commits, the property is therefore never
 * served from before that write. Properties the current transaction has written, or all of them
 * after a {@link #clear()}, bypass the cache until the transaction completes, so a transaction
 * always reads its own writes.
 * <p>
 * Callers that write a global property must call {@link #evict(String)}. Changes to the
 * {@code global_property} table made outside the API are not detected, but the {@code
 * global-properties} cache template's lifespan limits how long they can be served stale.
 *
 * @since 2.8.10
 */
@Component("globalPropertyCache")
public class GlobalPropertyCache implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger log = LoggerFactory.getLogger(GlobalPropertyCache.class);

	public static final String CACHE_NAME = "globalProperties";

	/**
	 * Caches whether keys are lower-cased. The NUL character keeps it from colliding with a property
	 * name, and a plain string needs no special marshalling in a cluster.
	 */
	static final String KEY_MODE = "\0caseInsensitiveKeys";

	private static final String GENERATION_COUNTER = "globalPropertyCacheGeneration";

	/**
	 * Recorded in the current transaction's written set by {@link #clear()}, meaning every property.
	 */
	private static final String ALL_PROPERTIES = "\0all";

	/** Capability token issued by {@link Daemon}, letting fills run on daemon threads. */
	private static volatile Daemon.CallerKey daemonCallerKey;

	private final SpringEmbeddedCacheManager cacheManager;

	private final AdministrationDAO dao;

	private final TransactionTemplate readOnlyTransaction;

	private final SyncStrongCounter generation;

	/** Starts a fill on another thread and returns a future that completes when it is done. */
	private final Function<Runnable, Future<?>> backgroundRunner;

	/** Fills in progress, by property name, so concurrent misses start a single fill. */
	private final ConcurrentMap<String, Future<?>> fills = new ConcurrentHashMap<>();

	@Autowired
	public GlobalPropertyCache(@Qualifier("apiCacheManager") SpringEmbeddedCacheManager cacheManager, AdministrationDAO dao,
	    @Qualifier("transactionManager") TransactionManager transactionManager) {
		this(cacheManager, dao, (PlatformTransactionManager) transactionManager,
		        task -> Daemon.runNewDaemonTask(task, daemonCallerKey()));
	}

	GlobalPropertyCache(SpringEmbeddedCacheManager cacheManager, AdministrationDAO dao,
	    PlatformTransactionManager transactionManager, Function<Runnable, Future<?>> backgroundRunner) {
		this.cacheManager = cacheManager;
		this.dao = dao;
		this.backgroundRunner = backgroundRunner;

		this.readOnlyTransaction = new TransactionTemplate(transactionManager);
		this.readOnlyTransaction.setReadOnly(true);

		CounterManager counters = EmbeddedCounterManagerFactory.asCounterManager(cacheManager.getNativeCacheManager());
		counters.defineCounter(GENERATION_COUNTER,
		    CounterConfiguration.builder(CounterType.UNBOUNDED_STRONG).storage(Storage.VOLATILE).build());
		this.generation = counters.getStrongCounter(GENERATION_COUNTER).sync();
	}

	/**
	 * Receives the {@link Daemon} caller key. Called only by {@link Daemon} during its initialization.
	 *
	 * @param callerKey the caller key issued by {@link Daemon}
	 */
	public static void setDaemonCallerKey(Daemon.CallerKey callerKey) {
		if (callerKey != null && daemonCallerKey == null) {
			daemonCallerKey = callerKey;
		}
	}

	private static Daemon.CallerKey daemonCallerKey() {
		if (daemonCallerKey == null) {
			// Guarantee Daemon has initialized and therefore handed us the key, regardless of the order in
			// which the two classes were first loaded.
			Daemon.ensureInitialized();
		}
		return daemonCallerKey;
	}

	/**
	 * Returns a snapshot of the named property. On a miss it is read through the caller's session and a
	 * fill of the cache is started in the background. Never returns null; a property that does not
	 * exist is returned as {@link Entry#ABSENT}.
	 * <p>
	 * The snapshot does not record whether the current user may view the property, so callers must
	 * check {@link Entry#getViewPrivilege()} on every call.
	 *
	 * @param propertyName the name of the property, not null
	 * @return a snapshot of the property
	 */
	public Entry get(String propertyName) {
		Entry cached = getIfCached(propertyName);
		if (cached != null) {
			return cached;
		}

		Entry loaded = Entry.of(dao.getGlobalPropertyObject(propertyName));
		if (getCache() != null && !isWrittenInCurrentTransaction(propertyName)) {
			startFill(propertyName);
		}
		return loaded;
	}

	/**
	 * Returns the cached snapshot of the named property without loading it, or null if it is not cached
	 * or the current transaction has written it. Unlike {@link #get(String)}, this needs no transaction
	 * or database access, which makes it suitable for callers that must avoid them. As with
	 * {@link #get(String)}, callers must check whether the current user may view the property.
	 *
	 * @param propertyName the name of the property
	 * @return a snapshot of the property, or null if it is not cached
	 */
	public Entry getIfCached(String propertyName) {
		Cache<Object, Object> cache = getCache();
		if (cache == null || propertyName == null || isWrittenInCurrentTransaction(propertyName)) {
			return null;
		}

		Object caseInsensitive = cache.get(KEY_MODE);
		if (!(caseInsensitive instanceof Boolean)) {
			return null;
		}

		Object cached = cache.get(key((Boolean) caseInsensitive, propertyName));
		return cached instanceof Entry ? (Entry) cached : null;
	}

	/**
	 * Evicts the named property. Must be called whenever a global property is saved or purged.
	 * <p>
	 * The property is evicted immediately and again when the current transaction completes, so that
	 * values filled while it was running are discarded whether it commits or rolls back. Until then the
	 * current transaction reads the property without the cache.
	 *
	 * @param propertyName the name of the property, not null
	 */
	public void evict(String propertyName) {
		Cache<Object, Object> cache = getCache();
		if (cache == null) {
			return;
		}

		if (OpenmrsConstants.GP_CASE_SENSITIVE_DATABASE_STRING_COMPARISON.equalsIgnoreCase(propertyName)
		        || !Boolean.TRUE.equals(cache.get(KEY_MODE))) {
			// the keys may be exact names, or unknown, so other spellings of the name may be cached
			clear();
			return;
		}

		recordWrite(propertyName);
		String key = key(true, propertyName);
		invalidate(() -> cache.remove(key));
	}

	/**
	 * Evicts every property. Use this when global properties may have been changed outside the API, for
	 * example by Liquibase.
	 * <p>
	 * As with {@link #evict(String)}, the cache is cleared immediately and again when the current
	 * transaction completes, and until then the current transaction reads every property without it.
	 */
	public void clear() {
		Cache<Object, Object> cache = getCache();
		if (cache != null) {
			recordWrite(ALL_PROPERTIES);
			invalidate(cache::clear);
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

	/**
	 * Waits for the fills in progress to finish. Fills only affect what later calls find in the cache,
	 * so this is only needed where a caller must observe a fill, for example in tests.
	 */
	void awaitFills() {
		for (Future<?> fill : fills.values()) {
			try {
				fill.get();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			} catch (ExecutionException e) {
				// the fill already logged its failure
			}
		}
	}

	/**
	 * Forgets which properties the current transaction has written, so that it reads through the cache
	 * again. Only for tests that load data behind the API but still need to observe cache hits.
	 */
	void forgetWritesInCurrentTransaction() {
		if (TransactionSynchronizationManager.hasResource(this)) {
			getWrittenInCurrentTransaction().clear();
		}
	}

	private void startFill(String propertyName) {
		try {
			fills.computeIfAbsent(propertyName, name -> backgroundRunner.apply(() -> {
				try {
					fill(name);
				} catch (RuntimeException e) {
					log.warn("Could not fill the global property cache with {}", name, e);
				} finally {
					fills.remove(name);
				}
			}));
		} catch (RuntimeException e) {
			log.warn("Could not start a fill of the global property cache with {}", propertyName, e);
			fills.remove(propertyName);
		}
	}

	/**
	 * Runs on a background thread: reads the property and the key mode in a new read-only transaction
	 * and caches them unless an eviction happens in the meantime.
	 */
	private void fill(String propertyName) {
		Cache<Object, Object> cache = getCache();
		if (cache == null) {
			return;
		}

		long loadedAt = generation.getValue();
		readOnlyTransaction.executeWithoutResult(status -> {
			boolean caseInsensitive = dao.isDatabaseStringComparisonCaseSensitive();
			Entry entry = Entry.of(dao.getGlobalPropertyObject(propertyName));
			putIfCurrent(cache, KEY_MODE, caseInsensitive, loadedAt);
			putIfCurrent(cache, key(caseInsensitive, propertyName), entry, loadedAt);
		});
	}

	/**
	 * Caches <code>value</code> unless an eviction has happened since it was loaded. An eviction
	 * increments the counter before removing entries, so if one runs while the value is being put, the
	 * second check either sees it and removes the value, or the eviction removes it.
	 */
	private void putIfCurrent(Cache<Object, Object> cache, String key, Object value, long loadedAt) {
		if (generation.getValue() != loadedAt) {
			return;
		}

		cache.putForExternalRead(key, value);
		if (generation.getValue() != loadedAt) {
			cache.remove(key);
		}
	}

	private static String key(boolean caseInsensitive, String propertyName) {
		return caseInsensitive ? propertyName.toLowerCase(Locale.ROOT) : propertyName;
	}

	/**
	 * Runs <code>removal</code> now and again when the current transaction completes, each time after
	 * incrementing the generation so that fills already in progress are not kept.
	 */
	private void invalidate(Runnable removal) {
		generation.incrementAndGet();
		removal.run();
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

				@Override
				public void afterCompletion(int status) {
					generation.incrementAndGet();
					removal.run();
				}
			});
		}
	}

	private boolean isWrittenInCurrentTransaction(String propertyName) {
		if (!TransactionSynchronizationManager.hasResource(this)) {
			return false;
		}

		Set<String> written = getWrittenInCurrentTransaction();
		return written.contains(ALL_PROPERTIES) || written.contains(propertyName.toLowerCase(Locale.ROOT));
	}

	/**
	 * Records that the current transaction has written the property, so that it bypasses the cache for
	 * the rest of the transaction. Names are lower-cased, since skipping the cache for another spelling
	 * of the name is harmless.
	 */
	private void recordWrite(String propertyName) {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			return;
		}

		if (!TransactionSynchronizationManager.hasResource(this)) {
			TransactionSynchronizationManager.bindResource(this, new HashSet<String>());
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

				@Override
				public void afterCompletion(int status) {
					TransactionSynchronizationManager.unbindResourceIfPossible(GlobalPropertyCache.this);
				}
			});
		}
		getWrittenInCurrentTransaction()
		        .add(ALL_PROPERTIES.equals(propertyName) ? propertyName : propertyName.toLowerCase(Locale.ROOT));
	}

	@SuppressWarnings("unchecked")
	private Set<String> getWrittenInCurrentTransaction() {
		return (Set<String>) TransactionSynchronizationManager.getResource(this);
	}

	@SuppressWarnings("unchecked")
	private Cache<Object, Object> getCache() {
		org.springframework.cache.Cache cache = cacheManager.getCache(CACHE_NAME);
		return cache == null ? null : (Cache<Object, Object>) cache.getNativeCache();
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
