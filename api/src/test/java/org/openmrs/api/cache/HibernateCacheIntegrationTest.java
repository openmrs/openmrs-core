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
import org.infinispan.configuration.cache.ExpirationConfiguration;
import org.infinispan.hibernate.cache.commons.InfinispanBaseRegion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HibernateCacheIntegrationTest extends BaseContextSensitiveTest {

	private static final long FIFTEEN_MINUTES = 900000L;

	private static final long ONE_HOUR = 3600000L;

	private CacheImplementor cacheImplementor;

	@BeforeEach
	public void setup() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		SessionFactoryImplementor sessionFactoryImplementor = (SessionFactoryImplementor) sessionFactory;
		cacheImplementor = sessionFactoryImplementor.getCache();
	}

	@Test
	public void secondLevelCaches_shouldHaveExpectedConfiguration() {
		assertCacheConfiguration("org.openmrs.Concept", 10000);
		assertCacheConfiguration("org.openmrs.User", 10000);
		assertCacheConfiguration("org.openmrs.Privilege", 10000);
		assertCacheConfiguration("org.openmrs.Person", 10000);
		assertCacheConfiguration("org.openmrs.ConceptDatatype", 100);
		assertCacheConfiguration("org.openmrs.ConceptClass", 100);
		assertCacheConfiguration("org.openmrs.Location", 10000);
	}

	@Test
	public void personDetailCaches_shouldHaveExpectedConfiguration() {
		assertCacheConfiguration("org.openmrs.PersonName", FIFTEEN_MINUTES, ONE_HOUR, 10000);
		assertCacheConfiguration("org.openmrs.PersonAddress", FIFTEEN_MINUTES, ONE_HOUR, 10000);
		assertCacheConfiguration("org.openmrs.PersonAttribute", FIFTEEN_MINUTES, ONE_HOUR, 10000);

		assertCacheExpiration("org.openmrs.Person.names", FIFTEEN_MINUTES, ONE_HOUR);
		assertCacheExpiration("org.openmrs.Person.addresses", FIFTEEN_MINUTES, ONE_HOUR);
		assertCacheExpiration("org.openmrs.Person.attributes", FIFTEEN_MINUTES, ONE_HOUR);
	}

	@Test
	public void userRolesCache_shouldExpireAfterOneHour() {
		assertCacheExpiration("org.openmrs.User.roles", -1, ONE_HOUR);
	}

	/**
	 * GlobalPropertyCache and RolePrivilegeCache refill through these regions, so they must expire for
	 * those caches' lifespans to bound staleness from changes made outside Hibernate.
	 */
	@Test
	public void regionsBehindGlobalPropertyAndRolePrivilegeCaches_shouldExpire() {
		assertExpires("org.openmrs.GlobalProperty");
		assertExpires("org.openmrs.Role");
		assertExpires("org.openmrs.Role.privileges");
		assertExpires("org.openmrs.Role.inheritedRoles");
		assertExpires("org.openmrs.Role.childRoles");
	}

	private void assertCacheConfiguration(String regionName, long maxEntries) {
		assertCacheConfiguration(regionName, -1, -1, maxEntries);
	}

	private void assertCacheConfiguration(String regionName, long maxIdle, long lifespan, long maxEntries) {
		assertCacheExpiration(regionName, maxIdle, lifespan);
		assertEquals(maxEntries, getNativeCache(regionName).getCacheConfiguration().memory().maxCount());
	}

	private void assertCacheExpiration(String regionName, long maxIdle, long lifespan) {
		AdvancedCache nativeCache = getNativeCache(regionName);

		assertEquals(maxIdle, nativeCache.getCacheConfiguration().expiration().maxIdle());
		assertEquals(lifespan, nativeCache.getCacheConfiguration().expiration().lifespan());
	}

	private AdvancedCache getNativeCache(String regionName) {
		Region region = cacheImplementor.getRegion(regionName);

		assertNotNull(region, "Cache region should be configured: " + regionName);

		return ((InfinispanBaseRegion) region).getCache();
	}

	private void assertExpires(String regionName) {
		ExpirationConfiguration expiration = getNativeCache(regionName).getCacheConfiguration().expiration();

		assertTrue(expiration.maxIdle() > 0 || expiration.lifespan() > 0, regionName + " should expire");
	}

}
