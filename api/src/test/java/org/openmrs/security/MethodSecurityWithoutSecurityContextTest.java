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
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves an expression is still decided when the {@code SecurityContextHolder} is empty but the
 * legacy {@link UserContext} is not - the state a {@code SecurityContextHolder.clearContext()}
 * leaves behind, since the two holders are independent (see {@link LegacyContextAuthentication}).
 * <p>
 * The regression guarded against is narrow and easy to reintroduce: {@code hasAuthority('X')} never
 * reads the {@code Authentication} and so was unaffected, while {@code hasPermission(...)} has to
 * materialize one and failed with {@link AuthenticationCredentialsNotFoundException} before
 * deciding anything. Merely adding a target to an existing annotation therefore changed behaviour
 * on such a thread. {@code PersonName.getFullName()} reached
 * {@code AdministrationService.getGlobalProperty} this way, which is how it first showed up.
 * <p>
 * Each test asserts the two forms agree, rather than only that {@code hasPermission} works, since
 * the property that matters is the equivalence {@link OpenmrsPermissionEvaluator} documents.
 */
public class MethodSecurityWithoutSecurityContextTest extends BaseContextSensitiveTest {

	private static final String PRIVILEGE = "No Security Context Test Privilege";

	@Autowired
	private NoSecurityContextTestService service;

	/**
	 * Drops the published token while leaving the session open, which is the divergence under test.
	 * {@link Context#clearUserContext()} would clear both and prove nothing.
	 */
	private void clearSecurityContextOnly() {
		Context.getUserContext().logout();
		SecurityContextHolder.clearContext();
		assertTrue(Context.isSessionOpen(), "the UserContext is the state under test and must survive");
	}

	@Test
	public void hasPermission_shouldGrantFromTheUserContextWhenTheSecurityContextIsEmpty() {
		clearSecurityContextOnly();
		Context.addProxyPrivilege(PRIVILEGE);
		try {
			assertEquals("ok", service.requirePermission());
			// the form that was already working, asserted alongside so the two cannot drift apart
			assertEquals("ok", service.requireAuthority());
		} finally {
			Context.removeProxyPrivilege(PRIVILEGE);
		}
	}

	@Test
	public void hasPermission_shouldDenyRatherThanFailToDecideWhenThePrivilegeIsMissing() {
		clearSecurityContextOnly();

		// an AccessDeniedException is a decision; AuthenticationCredentialsNotFoundException is not
		assertThrows(AccessDeniedException.class, service::requirePermission);
		assertThrows(AccessDeniedException.class, service::requireAuthority);
	}

	@Test
	public void postFilter_shouldFilterPerElementWhenTheSecurityContextIsEmpty() {
		clearSecurityContextOnly();
		Context.addProxyPrivilege(PRIVILEGE);
		try {
			assertEquals(List.of(PRIVILEGE), service.heldPrivileges());
		} finally {
			Context.removeProxyPrivilege(PRIVILEGE);
		}
	}

	@Test
	public void hasPermission_shouldRaiseTheSameApiExceptionAsHasAuthorityWhenNoSessionIsOpenEither() {
		UserContext saved = Context.getUserContext();
		Context.clearUserContext();
		try {
			// APIException, not Spring Security's AuthenticationCredentialsNotFoundException: callers
			// catch the former to degrade gracefully with no session - PersonName.getFullName() falls
			// back to an unformatted name that way - and an AuthenticationException passes through
			// such a catch untouched
			assertThrows(APIException.class, service::requirePermission);
			assertThrows(APIException.class, service::requireAuthority);
		} finally {
			Context.setUserContext(saved);
		}
	}

	@Test
	public void getFullName_shouldFallBackRatherThanPropagateWhenNoSessionIsOpen() {
		UserContext saved = Context.getUserContext();
		Context.clearUserContext();
		try {
			// the call path this regression surfaced on: getFullName -> NameSupport
			// -> AdministrationService.getGlobalProperty, whose @PreAuthorize names a target
			PersonName name = new PersonName();
			name.setGivenName("Given");
			name.setFamilyName("Family");

			assertEquals("Given Family", name.getFullName());
		} finally {
			Context.setUserContext(saved);
		}
	}

	@Service
	public static class NoSecurityContextTestService {

		@PreAuthorize("hasPermission(null, '" + PRIVILEGE + "')")
		public String requirePermission() {
			return "ok";
		}

		@PreAuthorize("hasAuthority('" + PRIVILEGE + "')")
		public String requireAuthority() {
			return "ok";
		}

		@PostFilter("hasPermission(null, filterObject)")
		public List<String> heldPrivileges() {
			return new ArrayList<>(List.of(PRIVILEGE, "Some Privilege Nobody Holds"));
		}
	}
}
