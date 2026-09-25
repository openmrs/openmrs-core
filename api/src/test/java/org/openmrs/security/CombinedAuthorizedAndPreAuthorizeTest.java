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
import org.openmrs.Privilege;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.test.context.transaction.TestTransaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Proves what happens when a single method carries both {@code @Authorized} and
 * {@code @PreAuthorize}: both advisors match it independently -
 * {@link org.openmrs.aop.AuthorizationAdvice}'s pointcut is "any {@code @Service} method", not
 * gated on {@code @Authorized} being present (the annotation is looked up inside {@code before()}
 * instead), while {@code @PreAuthorize}'s interceptor is only woven onto methods that actually
 * carry it - and both run, in order: {@code @PreAuthorize} at order 0
 * ({@link org.openmrs.security.OpenmrsSecurityConfig}'s {@code offset = -200}), then
 * {@code @Authorized} at order 1 ({@link org.openmrs.aop.AOPConfig#authorizationAdvisor}). The two
 * combine with AND semantics, not OR - the caller must satisfy both - and whichever runs first to
 * fail is the one whose exception type surfaces; the second advisor never even executes in that
 * case.
 * <p>
 * {@code admin}, the default test principal, is a superuser, for whom
 * {@link Context#hasPrivilege(String)}'s bypass is unconditional regardless of the privilege named
 * - so {@code @Authorized} alone can never be made to fail for this principal by naming an
 * unregistered or made-up privilege. Proving the "{@code @PreAuthorize} passes, {@code @Authorized}
 * fails" direction therefore logs out entirely (stripping superuser status, so
 * {@code Context.hasPrivilege(String)} falls through to the empty Anonymous role and denies) and
 * uses a proxy privilege - included in {@code hasAuthority(...)} regardless of authentication
 * status - to satisfy only the {@code @PreAuthorize} side.
 */
public class CombinedAuthorizedAndPreAuthorizeTest extends BaseContextSensitiveTest {

	private static final String PRE_AUTHORIZE_PRIVILEGE = "Combined Test PreAuthorize Privilege";

	private static final String UNREGISTERED_PRIVILEGE = "Combined Test Unregistered Privilege";

	private static final String PROXY_ONLY_PRIVILEGE = "Combined Test Proxy Only Privilege";

	@Autowired
	private UserService userService;

	@Autowired
	private CombinedAnnotationTestService combinedAnnotationTestService;

	@Test
	public void invoke_shouldAllowWhenBothAnnotationsAreSatisfied() {
		// admin is a superuser: Context.hasPrivilege's bypass satisfies @Authorized unconditionally,
		// and superuser status satisfies hasAuthority(...) for any *registered* privilege - which
		// PRE_AUTHORIZE_PRIVILEGE must actually be, hence registerAndCommit()
		registerAndCommit();
		try {
			assertEquals("ok", combinedAnnotationTestService.requireBothWithARegisteredPrivilege());
		} finally {
			purgeAndCommit();
		}
	}

	@Test
	public void invoke_shouldDenyWithAccessDeniedExceptionWhenPreAuthorizeFailsFirst() {
		// @Authorized(GET_CONCEPTS) alone would allow this for the superuser test principal - proving
		// the combination is stricter than either annotation alone (AND, not OR) - but hasAuthority(...)
		// denies an unregistered privilege even for a superuser, and @PreAuthorize's interceptor (order
		// 0) runs before AuthorizationAdvice (order 1), so AccessDeniedException surfaces and
		// AuthorizationAdvice never even runs
		assertThrows(AccessDeniedException.class,
		    combinedAnnotationTestService::requireBothWithAnUnregisteredPreAuthorizePrivilege);
	}

	@Test
	public void invoke_shouldDenyWithAPIAuthenticationExceptionWhenPreAuthorizePassesButAuthorizedFails() {
		// logged out entirely: Context.hasPrivilege(GET_CONCEPTS) falls through to the empty Anonymous
		// role and denies, but the proxy privilege is included in hasAuthority(...) regardless of
		// authentication status, so @PreAuthorize (order 0) passes and AuthorizationAdvice (order 1)
		// runs next and is the one that denies - proving the ordering, not just that both are enforced
		Context.getUserContext().logout();
		Context.addProxyPrivilege(PROXY_ONLY_PRIVILEGE);
		try {
			assertThrows(APIAuthenticationException.class,
			    combinedAnnotationTestService::requireBothWhereOnlyPreAuthorizeIsSatisfiedByAProxyPrivilege);
		} finally {
			Context.removeProxyPrivilege(PROXY_ONLY_PRIVILEGE);
		}
	}

	/**
	 * Saves {@link #PRE_AUTHORIZE_PRIVILEGE} and force-commits the transaction, so the daemon-thread
	 * reload behind {@code OpenmrsAuthenticationToken#getAuthorities()} can see it - same reasoning as
	 * {@link OpenmrsAuthenticationTokenTest#registerAndCommit()}. Leaves a fresh transaction started
	 * for the rest of the test method.
	 */
	private void registerAndCommit() {
		userService.savePrivilege(new Privilege(PRE_AUTHORIZE_PRIVILEGE, "for testing"));
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	/**
	 * Undoes {@link #registerAndCommit()}.
	 */
	private void purgeAndCommit() {
		Privilege privilege = userService.getPrivilege(PRE_AUTHORIZE_PRIVILEGE);
		if (privilege != null) {
			userService.purgePrivilege(privilege);
		}
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	@Service
	public static class CombinedAnnotationTestService {

		@Authorized(org.openmrs.util.PrivilegeConstants.GET_CONCEPTS)
		@PreAuthorize("hasAuthority('" + PRE_AUTHORIZE_PRIVILEGE + "')")
		public String requireBothWithARegisteredPrivilege() {
			return "ok";
		}

		@Authorized(org.openmrs.util.PrivilegeConstants.GET_CONCEPTS)
		@PreAuthorize("hasAuthority('" + UNREGISTERED_PRIVILEGE + "')")
		public String requireBothWithAnUnregisteredPreAuthorizePrivilege() {
			return "ok";
		}

		@Authorized(org.openmrs.util.PrivilegeConstants.GET_CONCEPTS)
		@PreAuthorize("hasAuthority('" + PROXY_ONLY_PRIVILEGE + "')")
		public String requireBothWhereOnlyPreAuthorizeIsSatisfiedByAProxyPrivilege() {
			return "ok";
		}
	}
}
