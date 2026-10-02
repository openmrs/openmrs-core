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
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves {@code hasPermission(null, '<privilege>')} behaves as a plain predicate, so SpEL can
 * compose it, while a denial still names the privilege that was missing (see
 * {@link OpenmrsPermissionEvaluator} and {@link PrivilegeNamingAuthorizationManager} for why those
 * two requirements pull in opposite directions).
 * <p>
 * The three shapes covered here all break if the evaluator throws on a miss instead of returning
 * {@code false}:
 * <ul>
 * <li>{@code A or B}, which is the direct translation of a multi-privilege
 * {@code @Authorized({A, B})} and must grant on either privilege. 21 methods in the api module
 * still carry that annotation, so this is the shape TRUNK-6802's conversions will reach for.</li>
 * <li>{@code !A}, which must be able to evaluate to true.</li>
 * <li>{@code @PostFilter}, which evaluates the expression once per element and drops the ones that
 * fail rather than failing the call. {@code CachedPostAuthorizeOrderingTest}'s own
 * {@code @PostFilter} deliberately uses {@code isAuthenticated()}, so it never exercises
 * {@code hasPermission} here.</li>
 * </ul>
 * Each privilege used below is unregistered and granted only as a proxy privilege, so the tests say
 * nothing about role membership: they are about how the expression combines the verdicts.
 */
public class OpenmrsPermissionEvaluatorCompositionTest extends BaseContextSensitiveTest {

	private static final String FIRST = "Composition Test First Privilege";

	private static final String SECOND = "Composition Test Second Privilege";

	@Autowired
	private ComposedExpressionTestService service;

	@Test
	public void or_shouldGrantWhenOnlyTheFirstPrivilegeIsHeld() {
		Context.getUserContext().logout();
		Context.addProxyPrivilege(FIRST);
		try {
			assertEquals("ok", service.requireFirstOrSecond());
		} finally {
			Context.removeProxyPrivilege(FIRST);
		}
	}

	@Test
	public void or_shouldGrantWhenOnlyTheSecondPrivilegeIsHeld() {
		// the case a throwing hasPermission(...) got wrong: the first operand's miss ended the
		// expression before SpEL ever evaluated the second
		Context.getUserContext().logout();
		Context.addProxyPrivilege(SECOND);
		try {
			assertEquals("ok", service.requireFirstOrSecond());
		} finally {
			Context.removeProxyPrivilege(SECOND);
		}
	}

	@Test
	public void or_shouldDenyNamingBothPrivilegesWhenNeitherIsHeld() {
		Context.getUserContext().logout();

		AccessDeniedException exception = assertThrows(AccessDeniedException.class, service::requireFirstOrSecond);
		assertEquals(expectedMessage(FIRST + "," + SECOND), exception.getMessage());
	}

	@Test
	public void not_shouldGrantWhenThePrivilegeIsMissing() {
		Context.getUserContext().logout();

		assertEquals("ok", service.requireNotFirst());
	}

	@Test
	public void not_shouldDenyWhenThePrivilegeIsHeld() {
		Context.getUserContext().logout();
		Context.addProxyPrivilege(FIRST);
		try {
			// nothing was missing, so there is no privilege to name and Spring Security's own generic
			// denial stands
			AccessDeniedException exception = assertThrows(AccessDeniedException.class, service::requireNotFirst);
			assertEquals("Access Denied", exception.getMessage());
		} finally {
			Context.removeProxyPrivilege(FIRST);
		}
	}

	@Test
	public void postFilter_shouldKeepOnlyTheElementsTheCallerHolds() {
		Context.getUserContext().logout();
		Context.addProxyPrivilege(SECOND);
		try {
			assertEquals(List.of(SECOND), service.heldPrivileges());
		} finally {
			Context.removeProxyPrivilege(SECOND);
		}
	}

	@Test
	public void postFilter_shouldReturnAnEmptyListRatherThanThrowWhenNoElementPasses() {
		Context.getUserContext().logout();

		assertTrue(service.heldPrivileges().isEmpty());
	}

	private String expectedMessage(String privileges) {
		return Context.getMessageSourceService().getMessage("error.privilegesRequired", new Object[] { privileges },
		    Locale.getDefault());
	}

	@Service
	public static class ComposedExpressionTestService {

		@PreAuthorize("hasPermission(null, '" + FIRST + "') or hasPermission(null, '" + SECOND + "')")
		public String requireFirstOrSecond() {
			return "ok";
		}

		@PreAuthorize("!hasPermission(null, '" + FIRST + "')")
		public String requireNotFirst() {
			return "ok";
		}

		// each element is itself a privilege name, so the filter keeps exactly the ones the caller
		// holds - a constant permission would evaluate the same for every element and prove nothing
		// about per-element filtering
		@PostFilter("hasPermission(null, filterObject)")
		public List<String> heldPrivileges() {
			return new ArrayList<>(List.of(FIRST, SECOND));
		}
	}
}
