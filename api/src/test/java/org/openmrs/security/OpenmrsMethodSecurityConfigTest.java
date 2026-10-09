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

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Proves that a {@code @PreAuthorize} method secured via {@link OpenmrsSecurityConfig} and
 * {@link OpenmrsPermissionEvaluator} makes the same authorization decisions as an
 * {@code @Authorized} method (see {@link org.openmrs.aop.AuthorizationAdviceTest}), including proxy
 * privilege and {@code Daemon} thread bypass parity - the two annotation styles are meant to
 * coexist and never disagree for the same privilege name.
 */
public class OpenmrsMethodSecurityConfigTest extends BaseContextSensitiveTest {

	@Autowired
	private PreAuthorizeTestService preAuthorizeTestService;

	@Test
	public void preAuthorize_shouldAllowWhenUserHasPrivilege() {
		assertEquals("ok", preAuthorizeTestService.requireGetConcepts());
	}

	@Test
	public void preAuthorize_shouldDenyWithAccessDeniedExceptionWhenUnauthenticated() {
		Context.getUserContext().logout();
		assertThrows(AccessDeniedException.class, () -> preAuthorizeTestService.requireGetConcepts());
	}

	@Test
	public void preAuthorize_shouldNameTheMissingPrivilegeWhenDenied() {
		Context.getUserContext().logout();
		// the exception AuthorizationAdvice denies a missing privilege with, so a caller catching it
		// recognizes the denial whichever annotation guards the method
		APIAuthenticationException exception = assertThrows(APIAuthenticationException.class,
		    () -> preAuthorizeTestService.requireGetConcepts());
		assertEquals(Context.getMessageSourceService().getMessage("error.privilegesRequired",
		    new Object[] { PrivilegeConstants.GET_CONCEPTS }, Locale.getDefault()), exception.getMessage());
	}

	@Test
	public void postAuthorize_shouldNameTheMissingPrivilegeWhenDenied() {
		Context.getUserContext().logout();
		APIAuthenticationException exception = assertThrows(APIAuthenticationException.class,
		    () -> preAuthorizeTestService.postAuthorizedRequiringGetConcepts());
		assertEquals(Context.getMessageSourceService().getMessage("error.privilegesRequired",
		    new Object[] { PrivilegeConstants.GET_CONCEPTS }, Locale.getDefault()), exception.getMessage());
	}

	@Test
	public void postAuthorize_shouldSayAuthenticationIsRequiredWhenIsAuthenticatedDenies() {
		Context.getUserContext().logout();
		APIAuthenticationException exception = assertThrows(APIAuthenticationException.class,
		    () -> preAuthorizeTestService.postAuthorizedRequiringAuthentication());
		assertEquals("Basic authentication required", exception.getMessage());
	}

	@Test
	public void preAuthorize_shouldNameTheMissingPrivilegeRatherThanAuthenticationWhenBothDenied() {
		// both are missing, and the privilege is what gets named - as it is when hasAuthority(...) alone
		// denies a logged-out caller, and when @Authorized does
		Context.getUserContext().logout();
		APIAuthenticationException exception = assertThrows(APIAuthenticationException.class,
		    () -> preAuthorizeTestService.requireGetConceptsOrAuthentication());
		assertEquals(Context.getMessageSourceService().getMessage("error.privilegesRequired",
		    new Object[] { PrivilegeConstants.GET_CONCEPTS }, Locale.getDefault()), exception.getMessage());
	}

	@Test
	public void preAuthorize_shouldNotSayAuthenticationIsRequiredOfAnAuthenticatedCaller() {
		// isAuthenticated() grants the test context's user, so denyAll() is the only thing that denied
		AuthorizationDeniedException exception = assertThrows(AuthorizationDeniedException.class,
		    () -> preAuthorizeTestService.requireAuthenticationAndDenyEveryone());
		assertEquals(AuthorizationDeniedException.class, exception.getClass());
		assertEquals("Access Denied", exception.getMessage());
	}

	@Test
	public void preAuthorize_shouldLeaveADenialThatRecordedNeitherToSpringSecurity() {
		// denyAll() checks neither a privilege nor authentication, so there is nothing to name even for a
		// caller who is not logged in
		Context.getUserContext().logout();
		AuthorizationDeniedException exception = assertThrows(AuthorizationDeniedException.class,
		    () -> preAuthorizeTestService.denyEveryone());
		assertEquals(AuthorizationDeniedException.class, exception.getClass());
		assertEquals("Access Denied", exception.getMessage());
	}

	@Test
	public void preAuthorize_shouldAllowALoggedOutCallerThroughANegatedIsAuthenticated() {
		// isAuthenticated() denies inside the expression, but the expression as a whole grants
		Context.getUserContext().logout();
		assertEquals("ok", preAuthorizeTestService.requireNotAuthenticated());
	}

	@Test
	public void preAuthorize_shouldAllowViaProxyPrivilegeWhenUnauthenticated() {
		Context.getUserContext().logout();
		Context.addProxyPrivilege(PrivilegeConstants.GET_CONCEPTS);
		try {
			assertEquals("ok", preAuthorizeTestService.requireGetConcepts());
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_CONCEPTS);
		}
	}

	// Daemon-thread bypass is not re-tested here: OpenmrsPermissionEvaluator delegates straight to
	// Context.hasPrivilege(String), whose Daemon short-circuit is already exhaustively covered by
	// DaemonTest - there is no separate bypass path for @PreAuthorize to get wrong.

	@Service
	public static class PreAuthorizeTestService {

		@PreAuthorize("hasPermission(null, '" + PrivilegeConstants.GET_CONCEPTS + "')")
		public String requireGetConcepts() {
			return "ok";
		}

		@PostAuthorize("hasPermission(null, '" + PrivilegeConstants.GET_CONCEPTS + "')")
		public String postAuthorizedRequiringGetConcepts() {
			return "ok";
		}

		@PostAuthorize("isAuthenticated()")
		public String postAuthorizedRequiringAuthentication() {
			return "ok";
		}

		@PreAuthorize("hasAuthority('" + PrivilegeConstants.GET_CONCEPTS + "') or isAuthenticated()")
		public String requireGetConceptsOrAuthentication() {
			return "ok";
		}

		@PreAuthorize("isAuthenticated() and denyAll()")
		public String requireAuthenticationAndDenyEveryone() {
			return "ok";
		}

		@PreAuthorize("denyAll()")
		public String denyEveryone() {
			return "ok";
		}

		@PreAuthorize("!isAuthenticated()")
		public String requireNotAuthenticated() {
			return "ok";
		}
	}
}
