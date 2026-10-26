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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.Merged;
import org.infinispan.notifications.cachemanagerlistener.event.MergeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Invalidation for an API cache whose values are loaded from committed data, possibly on another
 * thread, while the cache may be shared by a cluster. Each cache owns one instance.
 * <p>
 * Every eviction replaces a generation token kept in the cache itself. A load reads the token with
 * {@link #currentGeneration(Cache)} before reading the database and writes its value with
 * {@link #putIfCurrent(Cache, Object, Object, Object)}, which only keeps it if nothing was evicted
 * in between. In a cluster, replacing the token invalidates it on every node, so an eviction
 * anywhere is seen everywhere. A token that expires or is evicted for space also reads as an
 * eviction, which only costs an uncached load. If an eviction cannot reach every node, this node
 * clears its own cache instead, and a node clears its cache when a network partition that cut it
 * off heals.
 * <p>
 * Within a transaction, {@link #invalidate(String)} records the key rather than evicting it, and
 * the keys a transaction records are evicted together when it completes, whether it commits or
 * rolls back. Until then {@link #isWrittenInCurrentTransaction(String)} reports them, so the cache
 * can let the transaction read its own writes without the cache while other transactions keep
 * reading the committed values. Outside a transaction keys are evicted immediately.
 *
 * @since 2.8.10
 */
public final class CacheInvalidation {

	private static final Logger log = LoggerFactory.getLogger(CacheInvalidation.class);

	/**
	 * Key of the generation token. The NUL character keeps it from colliding with a cache's own keys.
	 */
	static final String GENERATION = "\0generation";

	/** Recorded by {@link #invalidate(String)} to mean every key. */
	static final String ALL = "\0all";

	private final String cacheDescription;

	private final Supplier<Cache<Object, Object>> cache;

	private final EmbeddedCacheManager cacheManager;

	private final PartitionMergeListener mergeListener = new PartitionMergeListener(this);

	/**
	 * Creates the invalidation for a cache and starts listening for partition merges; call
	 * {@link #close()} when the cache is destroyed.
	 *
	 * @param cacheDescription how log messages name the cache, for example "the role privilege cache"
	 * @param cache supplies the cache, or null if it is unavailable
	 * @param cacheManager the cache manager the cache belongs to
	 */
	CacheInvalidation(String cacheDescription, Supplier<Cache<Object, Object>> cache, EmbeddedCacheManager cacheManager) {
		this.cacheDescription = cacheDescription;
		this.cache = cache;
		this.cacheManager = cacheManager;
		cacheManager.addListener(mergeListener);
	}

	/** Stops listening for partition merges. */
	void close() {
		cacheManager.removeListener(mergeListener);
	}

	/**
	 * Evicts <code>key</code>, or every key if it is {@link #ALL}, when the current transaction
	 * completes, or immediately outside one.
	 *
	 * @param key the key to evict
	 */
	void invalidate(String key) {
		Cache<Object, Object> current = cache.get();
		if (current == null) {
			return;
		}

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			getWrittenInCurrentTransaction(current).add(key);
		} else {
			evictOrClearLocally(current, Collections.singleton(key));
		}
	}

	/**
	 * @param key the key to check, or null to check only whether every key has been invalidated
	 * @return true if the current transaction has invalidated <code>key</code> or every key
	 */
	boolean isWrittenInCurrentTransaction(String key) {
		Set<?> written = (Set<?>) TransactionSynchronizationManager.getResource(this);
		return written != null && (written.contains(ALL) || (key != null && written.contains(key)));
	}

	/**
	 * Evicts every key immediately, even within a transaction, which still reads without the cache
	 * until it completes. Only for tests that load data behind the API.
	 */
	void clearNow() {
		Cache<Object, Object> current = cache.get();
		if (current == null) {
			return;
		}

		evictOrClearLocally(current, Collections.singleton(ALL));
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			getWrittenInCurrentTransaction(current).add(ALL);
		}
	}

	/**
	 * Forgets which keys the current transaction has invalidated, so that it reads through the cache
	 * again and does not evict them when it completes. Only for tests that load data behind the API but
	 * still need to observe cache hits.
	 */
	void forgetWritesInCurrentTransaction() {
		Set<?> written = (Set<?>) TransactionSynchronizationManager.getResource(this);
		if (written != null) {
			written.clear();
		}
	}

	/**
	 * Returns this node's generation token, first creating one if there is none. The token is created
	 * on this node only, since writing it cluster-wide would invalidate the other nodes' tokens.
	 *
	 * @param cache the cache
	 * @return the token to pass to {@link #putIfCurrent(Cache, Object, Object, Object)}
	 */
	static Object currentGeneration(Cache<Object, Object> cache) {
		Object generation = cache.get(GENERATION);
		if (generation != null) {
			return generation;
		}

		String created = UUID.randomUUID().toString();
		Object existing = cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).putIfAbsent(GENERATION, created);
		return existing != null ? existing : created;
	}

	/**
	 * Caches <code>value</code> unless an eviction has happened since <code>generation</code> was read.
	 * An eviction replaces the generation token before removing entries, so if one runs while the value
	 * is being put, the second check either sees it and removes the value, or the eviction removes it.
	 * The value is written with {@code putForExternalRead}, so it does not invalidate other nodes'
	 * entries, and the removal is local, since the value was only ever written on this node.
	 *
	 * @param cache the cache
	 * @param key the key to cache the value under
	 * @param value the value
	 * @param generation the token {@link #currentGeneration(Cache)} returned before the value was read
	 */
	static void putIfCurrent(Cache<Object, Object> cache, Object key, Object value, Object generation) {
		if (!isCurrent(cache, generation)) {
			return;
		}

		cache.putForExternalRead(key, value);
		if (!isCurrent(cache, generation)) {
			cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).remove(key);
		}
	}

	/**
	 * @param cache the cache
	 * @param generation a token {@link #currentGeneration(Cache)} returned, or null
	 * @return true if nothing has been evicted on this node since <code>generation</code> was read
	 */
	static boolean isCurrent(Cache<Object, Object> cache, Object generation) {
		return generation != null && generation.equals(cache.get(GENERATION));
	}

	/**
	 * Evicts the keys on every node or, if that fails, for example because the cluster is partitioned,
	 * clears this node's cache instead, so that at least this node does not serve values from before
	 * the write. Nodes the eviction did not reach may serve them until the lifespan expires, or, if
	 * they were cut off by a partition, until it heals.
	 */
	private void evictOrClearLocally(Cache<Object, Object> cache, Set<String> keys) {
		try {
			evictNow(cache, keys);
		} catch (RuntimeException e) {
			log.error("Could not evict {} from {} on every node, so clearing it on this node only",
			    keys.contains(ALL) ? "every entry" : keys, cacheDescription, e);
			clearLocally(cache);
		}
	}

	/**
	 * Replaces the generation token and then removes the keys, or every entry if they include
	 * {@link #ALL}. The token must be replaced first: a clear does not lock every key, so a load could
	 * otherwise write behind it and still find its token. Replacing the token with a plain put
	 * invalidates it on every other node before the removals are sent. The removals are sent together,
	 * so evicting several keys costs about one round trip to the other nodes.
	 */
	private static void evictNow(Cache<Object, Object> cache, Set<String> keys) {
		cache.put(GENERATION, UUID.randomUUID().toString());
		if (keys.contains(ALL)) {
			cache.clear();
		} else {
			CompletableFuture.allOf(keys.stream().map(cache::removeAsync).toArray(CompletableFuture[]::new)).join();
		}
	}

	/**
	 * Clears this node's entries, including its generation token, so that loads in progress on this
	 * node are discarded too.
	 */
	private static void clearLocally(Cache<Object, Object> cache) {
		cache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).clear();
	}

	/**
	 * Returns the keys the current transaction has invalidated, first arranging for them to be evicted
	 * when it completes. The set is unbound while the transaction is suspended, since Spring only
	 * suspends its own resources, so a transaction started in the meantime, for example with
	 * {@code REQUIRES_NEW}, records and evicts its own writes.
	 */
	@SuppressWarnings("unchecked")
	private Set<String> getWrittenInCurrentTransaction(Cache<Object, Object> cache) {
		Set<String> bound = (Set<String>) TransactionSynchronizationManager.getResource(this);
		if (bound != null) {
			return bound;
		}

		Set<String> written = new HashSet<>();
		TransactionSynchronizationManager.bindResource(this, written);
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void suspend() {
				TransactionSynchronizationManager.unbindResource(CacheInvalidation.this);
			}

			@Override
			public void resume() {
				TransactionSynchronizationManager.bindResource(CacheInvalidation.this, written);
			}

			@Override
			public void afterCompletion(int status) {
				try {
					if (!written.isEmpty()) {
						evictOrClearLocally(cache, written);
					}
				} finally {
					TransactionSynchronizationManager.unbindResourceIfPossible(CacheInvalidation.this);
				}
			}
		});
		return written;
	}

	/**
	 * Clears this node's cache when a network partition heals, since while it was cut off it missed the
	 * evictions of writes made on the other side. Public only because Infinispan requires listeners to
	 * be.
	 */
	@Listener
	public static final class PartitionMergeListener {

		private final CacheInvalidation owner;

		PartitionMergeListener(CacheInvalidation owner) {
			this.owner = owner;
		}

		@Merged
		public void merged(MergeEvent event) {
			Cache<Object, Object> cache = owner.cache.get();
			if (cache != null) {
				clearLocally(cache);
			}
		}
	}
}
