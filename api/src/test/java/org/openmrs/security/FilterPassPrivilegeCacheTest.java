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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.openmrs.PrivilegeListener;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves {@link FilterPassPrivilegeCache} resolves the privilege a filter expression names once per
 * pass instead of once per element, without changing any verdict - including the case the cache
 * must not collapse, a filter whose permission varies per element.
 */
public class FilterPassPrivilegeCacheTest extends BaseContextSensitiveTest {

	private static final String HELD = "Filter Pass Cache Held Privilege";

	private static final String WITHHELD = "Filter Pass Cache Withheld Privilege";

	@Autowired
	private FilterPassTestService service;

	@Autowired
	private CountingPrivilegeListener listener;

	@Test
	public void shouldResolveTheFilterPrivilegeOnceForTheWholePass() {
		Context.getUserContext().logout();
		Context.addProxyPrivilege(HELD);
		try {
			listener.reset();

			assertEquals(List.of("a", "b", "c", "d", "e"), service.fiveElements());
			assertEquals(1, listener.countFor(HELD), "the privilege should be resolved once, not once per element");
		} finally {
			Context.removeProxyPrivilege(HELD);
		}
	}

	@Test
	public void shouldStillResolveEveryDistinctPrivilegeWhenThePermissionVariesPerElement() {
		Context.getUserContext().logout();
		Context.addProxyPrivilege(HELD);
		try {
			listener.reset();

			// each element is its own privilege name, so collapsing to one verdict would be wrong
			assertEquals(List.of(HELD), service.heldOfBothPrivileges());
			assertEquals(1, listener.countFor(HELD));
			assertEquals(1, listener.countFor(WITHHELD));
		} finally {
			Context.removeProxyPrivilege(HELD);
		}
	}

	@Test
	public void shouldNotCacheAcrossSeparatePasses() {
		Context.getUserContext().logout();
		try {
			assertTrue(service.fiveElements().isEmpty(), "nothing is held yet, so nothing survives");

			Context.addProxyPrivilege(HELD);
			assertEquals(5, service.fiveElements().size(), "a later pass must see the privilege now held");
		} finally {
			Context.removeProxyPrivilege(HELD);
		}
	}

	@Test
	public void shouldResolveDirectlyWhenNoPassIsOpen() {
		AtomicInteger resolutions = new AtomicInteger();

		assertTrue(FilterPassPrivilegeCache.holdsPrivilege(HELD, privilege -> {
			resolutions.incrementAndGet();
			return true;
		}));
		assertTrue(FilterPassPrivilegeCache.holdsPrivilege(HELD, privilege -> {
			resolutions.incrementAndGet();
			return true;
		}));

		assertEquals(2, resolutions.get(), "with no pass open every check resolves");
	}

	@Test
	public void shouldRestoreTheEnclosingPassSoNestingDoesNotLeak() {
		AtomicInteger resolutions = new AtomicInteger();
		Map<String, Boolean> outer = FilterPassPrivilegeCache.begin();
		try {
			assertFalse(FilterPassPrivilegeCache.holdsPrivilege(HELD, privilege -> {
				resolutions.incrementAndGet();
				return false;
			}));

			// a nested pass resolves on its own and must not inherit or overwrite the outer verdict
			Map<String, Boolean> inner = FilterPassPrivilegeCache.begin();
			try {
				assertTrue(FilterPassPrivilegeCache.holdsPrivilege(HELD, privilege -> {
					resolutions.incrementAndGet();
					return true;
				}));
			} finally {
				FilterPassPrivilegeCache.end(inner);
			}

			assertFalse(FilterPassPrivilegeCache.holdsPrivilege(HELD, privilege -> {
				resolutions.incrementAndGet();
				return true;
			}), "the outer pass's own verdict should still stand");
			assertEquals(2, resolutions.get());
		} finally {
			FilterPassPrivilegeCache.end(outer);
		}

		assertTrue(FilterPassPrivilegeCache.holdsPrivilege(HELD, privilege -> true),
		    "the outermost pass should be closed again");
	}

	@Service
	public static class FilterPassTestService {

		@PostFilter("hasPermission(filterObject, '" + HELD + "')")
		public List<String> fiveElements() {
			return new ArrayList<>(List.of("a", "b", "c", "d", "e"));
		}

		@PostFilter("hasPermission(null, filterObject)")
		public List<String> heldOfBothPrivileges() {
			return new ArrayList<>(List.of(HELD, WITHHELD));
		}
	}

	/**
	 * Counts resolutions that actually reached {@code UserContext.hasPrivilege}, which is what the
	 * cache is meant to reduce.
	 */
	@Component
	public static class CountingPrivilegeListener implements PrivilegeListener {

		private final Map<String, AtomicInteger> counts = new java.util.concurrent.ConcurrentHashMap<>();

		@Override
		public void privilegeChecked(User user, String privilege, boolean hasPrivilege) {
			counts.computeIfAbsent(privilege, name -> new AtomicInteger()).incrementAndGet();
		}

		void reset() {
			counts.clear();
		}

		int countFor(String privilege) {
			AtomicInteger count = counts.get(privilege);
			return count == null ? 0 : count.get();
		}
	}
}
