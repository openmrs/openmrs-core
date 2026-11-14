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

import org.infinispan.commons.CacheException;
import org.infinispan.manager.DefaultCacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the runtime exception signature that {@code OpenmrsSecurityContextFilter}'s
 * unreadable-session recovery depends on: reading a stored value whose class the serialization
 * allow list rejects surfaces as an Infinispan {@code CacheException} carrying the ISPN000936
 * marker. If a future Infinispan version changes that signature, the filter's decode-failure
 * detection must be updated with it.
 */
public class SessionsCacheAllowListTest {

	private DefaultCacheManager cacheManager;

	@AfterEach
	public void tearDown() {
		if (cacheManager != null) {
			cacheManager.stop();
		}
	}

	/**
	 * javax.naming.Reference is Serializable but lives outside every admitted namespace (java.util,
	 * java.sql, org.springframework.session, org.openmrs), so storing it succeeds while reading it back
	 * must hit the allow list.
	 */
	@Test
	public void get_shouldThrowCacheExceptionNamingTheBlockedClassWhenTheClassIsNotInAllowList() throws Exception {
		cacheManager = new DefaultCacheManager("infinispan-api-local.xml");

		cacheManager.getCache(CacheConfig.SESSIONS_CACHE_NAME).put("blocked", new javax.naming.Reference("probe"));

		RuntimeException thrown = readBlockedEntry();
		assertInstanceOf(CacheException.class, thrown);
		assertTrue(String.valueOf(thrown.getMessage()).contains("ISPN000936"),
		    "expected the ISPN000936 allow-list rejection, got: " + thrown.getMessage());
		assertTrue(String.valueOf(thrown.getMessage()).contains("javax.naming.Reference"),
		    "the message must name the blocked class");
	}

	private RuntimeException readBlockedEntry() {
		try {
			cacheManager.getCache(CacheConfig.SESSIONS_CACHE_NAME).get("blocked");
			return null;
		} catch (RuntimeException e) {
			return e;
		}
	}
}
