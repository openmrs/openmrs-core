/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.security;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins {@code @PostAuthorize}/{@code @PostFilter} running <em>ahead</em> of the cache interceptor
 * (order 4 in {@link org.openmrs.aop.AOPConfig}), which is what {@link OpenmrsSecurityConfig}'s
 * {@code offset = -700} buys: an offset large enough for {@code @PreAuthorize} alone would leave
 * these two numerically behind caching, i.e. nested inside it, and a cache hit would then return
 * before either check ran - handing the first caller's cached result to every later caller with the
 * same arguments regardless of their own privileges. Correctly ordered, a cached value is still
 * checked and filtered on every call.
 */
public class CachedPostAuthorizeOrderingTest extends BaseContextSensitiveTest {

	@Autowired
	private CachedAuthorizationTestService service;

	@Test
	public void postAuthorize_shouldDenyEvenWhenTheUnderlyingValueIsCached() {
		// populate the cache as the default (authenticated, superuser) test principal
		assertEquals("secret", service.getCachedSecret("postAuthorizeOrderingTestKey"));

		// same cache key, but no longer authenticated - must still be denied, not served the value the
		// first call already cached
		Context.getUserContext().logout();
		assertThrows(AccessDeniedException.class, () -> service.getCachedSecret("postAuthorizeOrderingTestKey"));
	}

	@Test
	public void postFilter_shouldFilterEvenWhenTheUnderlyingListIsCached() {
		// populate the cache as the default (authenticated) test principal - both entries pass
		List<String> whileAuthenticated = service.getCachedList("postFilterOrderingTestKey");
		assertEquals(2, whileAuthenticated.size());

		// same cache key, but no longer authenticated - the filter must still strip every entry, not
		// hand back the first call's cached, unfiltered list
		Context.getUserContext().logout();
		List<String> whenLoggedOut = service.getCachedList("postFilterOrderingTestKey");
		assertTrue(whenLoggedOut.isEmpty());
	}

	@Service
	public static class CachedAuthorizationTestService {

		@Cacheable("testCache")
		@PostAuthorize("isAuthenticated()")
		public String getCachedSecret(String key) {
			return "secret";
		}

		// isAuthenticated() does not reference filterObject, so it evaluates the same for every
		// element - keeping all of them while authenticated, stripping all of them once logged out -
		// which is all this test needs to prove the interceptor still runs against a cached list.
		@Cacheable("testCache")
		@PostFilter("isAuthenticated()")
		public List<String> getCachedList(String key) {
			return new ArrayList<>(List.of("a", "b"));
		}
	}
}
