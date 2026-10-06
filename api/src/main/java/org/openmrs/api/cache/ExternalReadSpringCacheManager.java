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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.common.provider.SpringCache;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;

/**
 * A {@link SpringEmbeddedCacheManager} whose named caches are {@link ExternalReadSpringCache}s, so
 * that filling one on one node of a cluster does not invalidate the entry on the others. Only
 * caches that are filled on a miss and never overwritten, as with {@code @Cacheable}, may be named,
 * since an {@link ExternalReadSpringCache} does not overwrite an entry. Every other cache,
 * including those of modules, is a plain {@link SpringCache}.
 *
 * @since 2.8.10
 */
public class ExternalReadSpringCacheManager extends SpringEmbeddedCacheManager {

	private final Set<String> externalReadCaches;

	private final ConcurrentMap<String, SpringCache> caches = new ConcurrentHashMap<>();

	/**
	 * @param nativeCacheManager the cache manager to wrap
	 * @param externalReadCaches the names of the caches that are only filled on a miss
	 */
	public ExternalReadSpringCacheManager(EmbeddedCacheManager nativeCacheManager, Set<String> externalReadCaches) {
		super(nativeCacheManager);
		this.externalReadCaches = Set.copyOf(externalReadCaches);
	}

	@Override
	public SpringCache getCache(String name) {
		if (!externalReadCaches.contains(name)) {
			return super.getCache(name);
		}

		return caches.computeIfAbsent(name, n -> {
			Cache<Object, Object> nativeCache = getNativeCacheManager().getCache(n);
			return new ExternalReadSpringCache(nativeCache);
		});
	}

	@Override
	public void stop() {
		caches.clear();
		super.stop();
	}
}
