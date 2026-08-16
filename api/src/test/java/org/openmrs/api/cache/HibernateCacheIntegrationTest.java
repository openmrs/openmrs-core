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

import org.hibernate.SessionFactory;
import org.hibernate.cache.spi.CacheImplementor;
import org.hibernate.cache.spi.Region;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.infinispan.AdvancedCache;
import org.infinispan.hibernate.cache.commons.InfinispanBaseRegion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HibernateCacheIntegrationTest extends BaseContextSensitiveTest {

	private CacheImplementor cacheImplementor;

	@BeforeEach
	public void setup() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		SessionFactoryImplementor sessionFactoryImplementor = (SessionFactoryImplementor) sessionFactory;
		cacheImplementor = sessionFactoryImplementor.getCache();
	}

	@Test
	public void secondLevelCaches_shouldHaveExpectedConfiguration() {
		assertCacheConfiguration("org.openmrs.Concept", 10_000);
		assertCacheConfiguration("org.openmrs.GlobalProperty", 1_000);
		assertCacheConfiguration("org.openmrs.User", 100);
		assertCacheConfiguration("org.openmrs.Role", 100);
		assertCacheConfiguration("org.openmrs.Privilege", 500);
		assertCacheConfiguration("org.openmrs.Person", 100);
		assertCacheConfiguration("org.openmrs.PersonName", 100);
		assertCacheConfiguration("org.openmrs.PersonAddress", 100);
		assertCacheConfiguration("org.openmrs.PersonAttribute", 100);
		assertCacheConfiguration("org.openmrs.ConceptDatatype", 100);
		assertCacheConfiguration("org.openmrs.ConceptClass", 100);
		assertCacheConfiguration("org.openmrs.Location", 100);
	}

	private void assertCacheConfiguration(String regionName, long maxEntries) {
		Region region = cacheImplementor.getRegion(regionName);

		assertNotNull(region, "Cache region should be configured: " + regionName);

		InfinispanBaseRegion infinispanRegion = (InfinispanBaseRegion) region;
		AdvancedCache nativeCache = infinispanRegion.getCache();

		assertEquals(-1L, nativeCache.getCacheConfiguration().expiration().maxIdle());
		assertEquals(-1L, nativeCache.getCacheConfiguration().expiration().lifespan());
		assertEquals(maxEntries, nativeCache.getCacheConfiguration().memory().maxCount());
	}
}
