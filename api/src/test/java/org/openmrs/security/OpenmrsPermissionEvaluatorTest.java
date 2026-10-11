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

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

/**
 * Proves {@code @PreAuthorize("isAuthenticated()")} - Spring Security's own built-in expression -
 * is a faithful equivalent of a no-value {@code @Authorized}, across all three ways
 * {@code AuthorizationAdvice.before} can let a no-value {@code @Authorized} call through:
 * authenticated, holding a proxy privilege while logged out, or running on a {@link Daemon} thread
 * while logged out. Each test runs the same scenario through both annotations, on twin methods, to
 * show they agree.
 * <p>
 * This works because {@link OpenmrsAuthenticationToken#isAuthenticated()} - which Spring's
 * {@code isAuthenticated()} SpEL consults directly via {@code Authentication#isAuthenticated()} -
 * itself accounts for the Daemon-thread and proxy-privilege cases, not just
 * {@code UserContext#isAuthenticated()}'s plain {@code user != null}. No dedicated bean method is
 * needed the way {@code hasPermission(null, '&lt;privilege&gt;')} is for named privileges.
 */
public class OpenmrsPermissionEvaluatorTest extends BaseContextSensitiveTest {

	private static final String PROXY_PRIVILEGE = "OpenmrsPermissionEvaluatorTest Proxy Privilege";

	@Autowired
	private RequireLoggedInTestService requireLoggedInTestService;

	@Test
	public void bothAnnotations_shouldAllowWhenAuthenticated() {
		assertEquals("ok", requireLoggedInTestService.viaAuthorized());
		assertEquals("ok", requireLoggedInTestService.viaPreAuthorize());
	}

	@Test
	public void bothAnnotations_shouldDenyWhenLoggedOut() {
		Context.getUserContext().logout();
		try {
			assertThrows(Exception.class, requireLoggedInTestService::viaAuthorized);
			assertThrows(AccessDeniedException.class, requireLoggedInTestService::viaPreAuthorize);
		} finally {
			Context.authenticate("admin", "test");
		}
	}

	@Test
	public void bothAnnotations_shouldAllowWhenLoggedOutButProxied() {
		// the gap OpenmrsAuthenticationToken#isAuthenticated() closes: a proxy privilege alone
		// satisfies a no-value @Authorized, and now also isAuthenticated()
		Context.getUserContext().logout();
		Context.addProxyPrivilege(PROXY_PRIVILEGE);
		try {
			assertEquals("ok", requireLoggedInTestService.viaAuthorized());
			assertEquals("ok", requireLoggedInTestService.viaPreAuthorize());
		} finally {
			Context.removeProxyPrivilege(PROXY_PRIVILEGE);
			Context.authenticate("admin", "test");
		}
	}

	@Test
	public void bothAnnotations_shouldAllowOnADaemonThreadEvenWhenLoggedOutAndNotProxied() {
		Context.getUserContext().logout();
		try {
			// CALLS_REAL_METHODS: only isDaemonThread() is overridden; RolePrivilegeCache/AuthorizationAdvice
			// genuinely need the rest of Daemon to keep working underneath these calls
			try (MockedStatic<Daemon> daemon = mockStatic(Daemon.class, Mockito.CALLS_REAL_METHODS)) {
				daemon.when(Daemon::isDaemonThread).thenReturn(true);

				assertEquals("ok", requireLoggedInTestService.viaAuthorized());
				assertEquals("ok", requireLoggedInTestService.viaPreAuthorize());
			}
		} finally {
			Context.authenticate("admin", "test");
		}
	}

	@Service
	public static class RequireLoggedInTestService {

		@Authorized
		public String viaAuthorized() {
			return "ok";
		}

		@PreAuthorize("isAuthenticated()")
		public String viaPreAuthorize() {
			return "ok";
		}
	}
}
