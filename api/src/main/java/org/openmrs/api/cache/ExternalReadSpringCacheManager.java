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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.common.provider.SpringCache;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;

/**
 * A {@link SpringEmbeddedCacheManager} whose caches are {@link ExternalReadSpringCache}s, so that
 * filling a cache on one node of a cluster does not invalidate the entry on the others.
 *
 * @since 2.8.10
 */
public class ExternalReadSpringCacheManager extends SpringEmbeddedCacheManager {

	private final ConcurrentMap<String, SpringCache> caches = new ConcurrentHashMap<>();

	public ExternalReadSpringCacheManager(EmbeddedCacheManager nativeCacheManager) {
		super(nativeCacheManager);
	}

	@Override
	public SpringCache getCache(String name) {
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
