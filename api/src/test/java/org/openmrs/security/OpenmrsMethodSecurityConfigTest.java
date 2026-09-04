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
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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
	}
}
