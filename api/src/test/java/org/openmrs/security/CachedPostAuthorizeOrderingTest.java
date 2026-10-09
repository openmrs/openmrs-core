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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
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

	private static final String FILTERED_PRIVILEGE = "Cached PostFilter Ordering Privilege";

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
	public void postFilter_shouldFilterEvenWhenTheUnderlyingListIsCached() throws Exception {
		// The cache key includes the authenticated user (see UserKeyGenerator), so logging out between
		// the two calls would change the key and make the second a miss, proving nothing about
		// ordering. The same user throughout gives a genuine cache hit whose filter verdict has changed
		// underneath it - and it has to be a non-superuser, since Context.hasPrivilege grants a
		// superuser every privilege whatever proxy privileges come and go.
		User sameNonSuperuserThroughout = new User();

		runAs(sameNonSuperuserThroughout, () -> {
			Context.addProxyPrivilege(FILTERED_PRIVILEGE);
			try {
				assertEquals(2, service.getProxyFilteredList("postFilterOrderingTestKey").size());
			} finally {
				Context.removeProxyPrivilege(FILTERED_PRIVILEGE);
			}

			// cache hit on the same key, but the privilege the filter asks for is gone: every element
			// must be stripped rather than the cached list handed back as it stood
			assertTrue(service.getProxyFilteredList("postFilterOrderingTestKey").isEmpty());
		});
	}

	/**
	 * Swaps the authenticated user for a synthetic, non-superuser one, keeping the same instance for
	 * the whole block so the cache key stays constant. Same approach as
	 * {@code UserContextHasPrivilegeTest}.
	 */
	private void runAs(User user, Runnable action) throws IllegalAccessException {
		UserContext userContext = Context.getUserContext();
		User previous = userContext.getAuthenticatedUser();
		try {
			FieldUtils.getField(UserContext.class, "user", true).set(userContext, user);
			action.run();
		} finally {
			FieldUtils.getField(UserContext.class, "user", true).set(userContext, previous);
		}
	}

	@Test
	public void postAuthorize_shouldServeTheCachedValueAgainOnceTheUserLogsBackIn() {
		// the other direction: a denial while logged out must not poison the entry, so the same key
		// still resolves for a caller who is entitled to it. Proves the interceptor is filtering the
		// result per call rather than the cache holding a verdict.
		assertEquals("secret", service.getCachedSecret("logBackInOrderingTestKey"));

		Context.getUserContext().logout();
		assertThrows(AccessDeniedException.class, () -> service.getCachedSecret("logBackInOrderingTestKey"));

		Context.authenticate("admin", "test");
		assertEquals("secret", service.getCachedSecret("logBackInOrderingTestKey"));
	}

	@Test
	public void postFilter_shouldStripOnlyTheCallersOwnCacheEntry() {
		// Why @Cacheable with @PostFilter has to key by user. @PostFilter filters in place -
		// filterCollection does clear() then addAll(retained) on the collection the method returned and
		// gives back that same instance - so the entry really is stripped, permanently. Keyed per user
		// that is harmless: the stripped entry is the one only that caller reads, and the next caller
		// has their own.
		assertEquals(2, service.getCachedList("perUserOrderingTestKey").size());

		// a different caller, so a different key: their own entry, filtered for them
		Context.getUserContext().logout();
		assertTrue(service.getCachedList("perUserOrderingTestKey").isEmpty());

		// back to the first caller, whose entry was never touched by the one above
		Context.authenticate("admin", "test");
		assertEquals(2, service.getCachedList("perUserOrderingTestKey").size(),
		    "another caller's filtering must not have stripped this caller's entry");
	}

	@Service
	public static class CachedAuthorizationTestService {

		// keys by caller, as @PostFilter alongside @Cacheable requires - see UserKeyGenerator
		@Cacheable(value = "testCache", keyGenerator = UserKeyGenerator.BEAN_NAME)
		@PostFilter("hasAuthority('" + FILTERED_PRIVILEGE + "')")
		public List<String> getProxyFilteredList(String key) {
			return new ArrayList<>(List.of("a", "b"));
		}

		@Cacheable("testCache")
		@PostAuthorize("isAuthenticated()")
		public String getCachedSecret(String key) {
			return "secret";
		}

		// isAuthenticated() does not reference filterObject, so it evaluates the same for every
		// element - keeping all of them while authenticated, stripping all of them once logged out -
		// which is all this test needs to prove the interceptor still runs against a cached list.
		//
		// keys by caller because @PostFilter strips the cached collection in place, so a
		// user-independent key would leave the stripped remainder for everyone after the first caller
		@Cacheable(value = "testCache", keyGenerator = UserKeyGenerator.BEAN_NAME)
		@PostFilter("isAuthenticated()")
		public List<String> getCachedList(String key) {
			return new ArrayList<>(List.of("a", "b"));
		}
	}
}
