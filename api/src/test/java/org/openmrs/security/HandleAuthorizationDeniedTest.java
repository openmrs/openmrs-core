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

import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.method.HandleAuthorizationDenied;
import org.springframework.security.authorization.method.MethodAuthorizationDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pins that {@code @HandleAuthorizationDenied} still works once
 * {@link PrivilegeNamingAuthorizationManager} wraps the pre/post authorization managers.
 * <p>
 * It is easy to break and silent when broken. Both method-security interceptors consult a method's
 * handler only when the {@code AuthorizationManager} they hold implements
 * {@link MethodAuthorizationDeniedHandler}; a wrapper that does not implement it sends every denial
 * to the default throwing handler instead, so the annotation stops having any effect on every
 * {@code @PreAuthorize}/{@code @PostAuthorize} method in the application at once, without anything
 * failing. The {@code @PostAuthorize} case additionally requires the wrapper to <em>return</em> its
 * named exception rather than throw it, since {@code AuthorizationManagerAfterMethodInterceptor}
 * does not catch what {@code authorize(...)} throws.
 * <p>
 * The last test covers the other half of that contract: with no handler on the method, the returned
 * exception must still surface with its privilege name rather than a generic "Access Denied".
 */
public class HandleAuthorizationDeniedTest extends BaseContextSensitiveTest {

	private static final String MASKED = "masked";

	@Autowired
	private HandlerTestService service;

	@Test
	public void handler_shouldRunForADeniedHasPermission() {
		Context.getUserContext().logout();

		assertEquals(MASKED, service.handledHasPermission());
	}

	@Test
	public void handler_shouldRunForADeniedHasAuthority() {
		Context.getUserContext().logout();

		assertEquals(MASKED, service.handledHasAuthority());
	}

	@Test
	public void handler_shouldRunForADeniedDenyAll() {
		// denyAll() returns a denied result the naming wrapper never touches - it still has to reach
		// the method's handler, which it only does because the wrapper forwards the interface
		assertEquals(MASKED, service.handledDenyAll());
	}

	@Test
	public void handler_shouldRunForADeniedPostAuthorize() {
		Context.getUserContext().logout();

		assertEquals(MASKED, service.handledPostAuthorize());
	}

	@Test
	public void withoutAHandler_shouldStillReportTheMissingPrivilege() {
		Context.getUserContext().logout();

		AccessDeniedException exception = assertThrows(AccessDeniedException.class, service::unhandled);
		assertEquals(Context.getMessageSourceService().getMessage("error.privilegesRequired",
		    new Object[] { PrivilegeConstants.GET_CONCEPTS }, Locale.getDefault()), exception.getMessage());
	}

	@Component("maskingDeniedHandler")
	public static class MaskingDeniedHandler implements MethodAuthorizationDeniedHandler {

		@Override
		public Object handleDeniedInvocation(MethodInvocation methodInvocation, AuthorizationResult result) {
			return MASKED;
		}
	}

	@Service
	public static class HandlerTestService {

		@HandleAuthorizationDenied(handlerClass = MaskingDeniedHandler.class)
		@PreAuthorize("hasPermission(null, '" + PrivilegeConstants.GET_CONCEPTS + "')")
		public String handledHasPermission() {
			return "ok";
		}

		@HandleAuthorizationDenied(handlerClass = MaskingDeniedHandler.class)
		@PreAuthorize("hasAuthority('" + PrivilegeConstants.GET_CONCEPTS + "')")
		public String handledHasAuthority() {
			return "ok";
		}

		@HandleAuthorizationDenied(handlerClass = MaskingDeniedHandler.class)
		@PreAuthorize("denyAll()")
		public String handledDenyAll() {
			return "ok";
		}

		@HandleAuthorizationDenied(handlerClass = MaskingDeniedHandler.class)
		@PostAuthorize("hasPermission(returnObject, '" + PrivilegeConstants.GET_CONCEPTS + "')")
		public String handledPostAuthorize() {
			return "ok";
		}

		@PreAuthorize("hasPermission(null, '" + PrivilegeConstants.GET_CONCEPTS + "')")
		public String unhandled() {
			return "ok";
		}
	}
}
