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
import org.openmrs.annotation.Authorized;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Proves what happens when one method carries both {@code @Authorized} and {@code @PreAuthorize}:
 * both advisors match independently - {@link org.openmrs.aop.AuthorizationAdvice}'s pointcut is any
 * {@code @Service} method, with the annotation looked up inside {@code before()} - and both run,
 * {@code @PreAuthorize} first at order -500, then {@code @Authorized} at order 1. They combine with
 * AND, not OR, and the first to fail short-circuits the second.
 * <p>
 * {@code admin}, the default principal, is a superuser, and {@link Context#hasPrivilege(String)}'s
 * bypass is unconditional - which now covers {@code hasAuthority(...)} too, since the factory
 * routes through the same method. So the "{@code @PreAuthorize} fails first" case uses
 * {@code denyAll()}, and the opposite direction logs out (dropping to the empty Anonymous role) and
 * grants a proxy privilege, which {@code hasAuthority(...)} honors while {@code @Authorized} still
 * denies.
 */
public class CombinedAuthorizedAndPreAuthorizeTest extends BaseContextSensitiveTest {

	private static final String PRE_AUTHORIZE_PRIVILEGE = "Combined Test PreAuthorize Privilege";

	private static final String PROXY_ONLY_PRIVILEGE = "Combined Test Proxy Only Privilege";

	@Autowired
	private CombinedAnnotationTestService combinedAnnotationTestService;

	@Test
	public void invoke_shouldAllowWhenBothAnnotationsAreSatisfied() {
		// admin is a superuser, so Context.hasPrivilege's bypass satisfies both annotations - the
		// privilege needs no Privilege row, since hasAuthority(...) resolves through that same method
		assertEquals("ok", combinedAnnotationTestService.requireBothWithAPrivilege());
	}

	@Test
	public void invoke_shouldDenyWithAccessDeniedExceptionWhenPreAuthorizeFailsFirst() {
		// @Authorized(GET_CONCEPTS) alone would allow this for the superuser test principal - proving
		// the combination is stricter than either annotation alone (AND, not OR) - and @PreAuthorize's
		// interceptor (order -500) runs before AuthorizationAdvice (order 1), so AccessDeniedException
		// surfaces and AuthorizationAdvice never even runs.
		//
		// denyAll() rather than a privilege name, because no privilege name can deny a superuser: both
		// annotations resolve through Context.hasPrivilege(String) (hasAuthority via
		// OpenmrsAuthorizationManagerFactory), whose superuser bypass is unconditional.
		assertThrows(AccessDeniedException.class, combinedAnnotationTestService::requireBothWithADeniedPreAuthorize);
	}

	@Test
	public void invoke_shouldDenyWithAccessDeniedExceptionWhenPreAuthorizePassesButAuthorizedFails() {
		// logged out entirely: Context.hasPrivilege(GET_CONCEPTS) falls through to the empty Anonymous
		// role and denies, but the proxy privilege is included in hasAuthority(...) regardless of
		// authentication status, so @PreAuthorize (order -500) passes and AuthorizationAdvice (order 1)
		// runs next and is the one that denies - proving the ordering, not just that both are enforced
		Context.getUserContext().logout();
		Context.addProxyPrivilege(PROXY_ONLY_PRIVILEGE);
		try {
			assertThrows(AccessDeniedException.class,
			    combinedAnnotationTestService::requireBothWhereOnlyPreAuthorizeIsSatisfiedByAProxyPrivilege);
		} finally {
			Context.removeProxyPrivilege(PROXY_ONLY_PRIVILEGE);
		}
	}

	@Service
	public static class CombinedAnnotationTestService {

		@Authorized(org.openmrs.util.PrivilegeConstants.GET_CONCEPTS)
		@PreAuthorize("hasAuthority('" + PRE_AUTHORIZE_PRIVILEGE + "')")
		public String requireBothWithAPrivilege() {
			return "ok";
		}

		@Authorized(org.openmrs.util.PrivilegeConstants.GET_CONCEPTS)
		@PreAuthorize("denyAll()")
		public String requireBothWithADeniedPreAuthorize() {
			return "ok";
		}

		@Authorized(org.openmrs.util.PrivilegeConstants.GET_CONCEPTS)
		@PreAuthorize("hasAuthority('" + PROXY_ONLY_PRIVILEGE + "')")
		public String requireBothWhereOnlyPreAuthorizeIsSatisfiedByAProxyPrivilege() {
			return "ok";
		}
	}
}
