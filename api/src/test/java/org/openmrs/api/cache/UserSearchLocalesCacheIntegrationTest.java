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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserSearchLocalesCacheIntegrationTest extends BaseContextSensitiveTest {

	private CacheManager cacheManager;

	@BeforeEach
	public void setup() {
		cacheManager = Context.getRegisteredComponent("apiCacheManager", CacheManager.class);
	}

	@Test
	public void userSearchLocalesCache_shouldHaveExpectedConfiguration() {
		Cache cache = cacheManager.getCache("userSearchLocales");

		assertNotNull(cache, "userSearchLocales cache should be configured");

		// Verify the underlying Infinispan configuration.
		org.infinispan.Cache<?, ?> nativeCache = (org.infinispan.Cache<?, ?>) cache.getNativeCache();

		assertEquals(300000L, nativeCache.getCacheConfiguration().expiration().maxIdle());
		assertEquals(300000L, nativeCache.getCacheConfiguration().expiration().lifespan());
		assertEquals(500L, nativeCache.getCacheConfiguration().memory().maxCount());
	}
}
