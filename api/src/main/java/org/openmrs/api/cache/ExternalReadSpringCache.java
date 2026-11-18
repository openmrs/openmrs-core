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

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.infinispan.Cache;
import org.infinispan.commons.util.NullValue;
import org.infinispan.context.Flag;
import org.infinispan.spring.common.provider.SpringCache;
import org.springframework.cache.support.SimpleValueWrapper;

/**
 * A {@link SpringCache} that writes values with {@link Cache#putForExternalRead}, for caches whose
 * values are read from the database. In an invalidation cluster a plain put invalidates the key on
 * every other node, so nodes reading the same key would keep evicting each other's entries;
 * {@code putForExternalRead} leaves them in place. Evictions and clears still reach every node.
 * <p>
 * {@code putForExternalRead} does nothing if the key is already cached, so {@code put} does not
 * overwrite an entry. That suits {@code @Cacheable}, which only puts on a miss, but not
 * {@code @CachePut}; evict the entry instead of overwriting it.
 *
 * @since 2.8.10
 */
public class ExternalReadSpringCache extends SpringCache {

	private final Cache<Object, Object> nativeCache;

	/** Per-key locks, so a synchronized {@code @Cacheable} load runs once per key on this node. */
	private final ConcurrentMap<Object, ReentrantLock> loadLocks = new ConcurrentHashMap<>();

	public ExternalReadSpringCache(Cache<Object, Object> nativeCache) {
		super(nativeCache);
		this.nativeCache = nativeCache;
	}

	@Override
	public void put(Object key, Object value) {
		nativeCache.putForExternalRead(key, value != null ? value : NullValue.NULL);
	}

	/**
	 * Writes the value on this node only, so that, like {@link #put}, it does not invalidate the entry
	 * on other nodes.
	 */
	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		Object existing = nativeCache.getAdvancedCache().withFlags(Flag.CACHE_MODE_LOCAL).putIfAbsent(key,
		    value != null ? value : NullValue.NULL);
		if (existing == null) {
			return null;
		}
		return new SimpleValueWrapper(existing instanceof NullValue ? null : existing);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Callable<T> valueLoader) {
		ValueWrapper cached = get(key);
		if (cached != null) {
			return (T) cached.get();
		}

		ReentrantLock lock = loadLocks.computeIfAbsent(key, k -> new ReentrantLock());
		lock.lock();
		try {
			cached = get(key);
			if (cached != null) {
				return (T) cached.get();
			}

			T value;
			try {
				value = valueLoader.call();
			} catch (Exception e) {
				throw new ValueRetrievalException(key, valueLoader, e);
			}
			put(key, value);
			return value;
		} finally {
			lock.unlock();
			loadLocks.remove(key, lock);
		}
	}
}
