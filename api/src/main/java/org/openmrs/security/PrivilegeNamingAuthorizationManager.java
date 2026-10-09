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
import java.util.Set;
import java.util.function.Supplier;

import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.method.MethodAuthorizationDeniedHandler;
import org.springframework.security.authorization.method.MethodInvocationResult;
import org.springframework.security.authorization.method.ThrowingMethodAuthorizationDeniedHandler;
import org.springframework.security.core.Authentication;

/**
 * Wraps the {@code @PreAuthorize}/{@code @PostAuthorize} {@link AuthorizationManager} so a denial
 * caused by a missing privilege names it, with the same {@code error.privilegesRequired} message
 * {@code AuthorizationAdvice} uses, rather than a generic "Access Denied". A denial with no
 * privilege to name, in which {@code isAuthenticated()} denied, gets the
 * {@code error.aunthenticationRequired} message {@code AuthorizationAdvice} denies a no-value
 * {@code @Authorized} with. Installed by {@link OpenmrsSecurityConfig}.
 * <p>
 * The named exception is an {@link APIAuthenticationException}, the
 * {@link AuthorizationDeniedException} {@code AuthorizationAdvice} denies {@code @Authorized} with,
 * so converting a method as {@code doc/AUTHORIZATION_MIGRATION.md} translates it does not change
 * what its callers catch. It is <em>returned</em> as the {@link AuthorizationResult}, not thrown:
 * {@link AuthorizationDeniedException} is itself a result, and returning it keeps a method's
 * {@code @HandleAuthorizationDenied} handler in play, since
 * {@code AuthorizationManagerAfterMethodInterceptor} does not catch what {@code authorize(...)}
 * throws. For the same reason this implements {@link MethodAuthorizationDeniedHandler} and forwards
 * - an interceptor only consults the method's handler when its manager does. The URL side is the
 * exception: {@code AuthorizationFilter} replaces a denied result with its own message, so
 * {@code OpenmrsAuthorizationFilter}'s adapter throws instead.
 * <p>
 * Naming happens here, outside the expression, because SpEL composes {@code hasPermission(...)}
 * with {@code or}/{@code and}/{@code !}: throwing from inside would decide the outcome before the
 * rest of the expression was evaluated (see {@link OpenmrsPermissionEvaluator}). A denial that
 * recorded neither a missing privilege nor a denying {@code isAuthenticated()} is left to Spring
 * Security, since expressions can deny for reasons that name none.
 *
 * @param <T> what the wrapped manager authorizes - a {@code MethodInvocation} for
 *            {@code @PreAuthorize}, a {@code MethodInvocationResult} for {@code @PostAuthorize}
 * @since 3.0.0
 */
public class PrivilegeNamingAuthorizationManager<T> implements AuthorizationManager<T>, MethodAuthorizationDeniedHandler {

	private final AuthorizationManager<T> delegate;

	private final MethodAuthorizationDeniedHandler deniedHandler;

	public PrivilegeNamingAuthorizationManager(AuthorizationManager<T> delegate) {
		this.delegate = delegate;
		this.deniedHandler = (delegate instanceof MethodAuthorizationDeniedHandler handler) ? handler
		        : new ThrowingMethodAuthorizationDeniedHandler();
	}

	@Override
	public AuthorizationResult authorize(Supplier<? extends Authentication> authentication, T object) {
		MissingPrivilegeRecorder.Recording enclosing = MissingPrivilegeRecorder.begin();
		AuthorizationResult result;
		MissingPrivilegeRecorder.Recording recorded;
		try {
			result = delegate.authorize(authentication, object);
		} finally {
			recorded = MissingPrivilegeRecorder.end(enclosing);
		}

		Set<String> missingPrivileges = recorded.getMissingPrivileges();
		if (result != null && !result.isGranted() && !missingPrivileges.isEmpty()) {
			// returned rather than thrown: see the class javadoc. Joined the same way
			// AuthorizationAdvice joins a multi-privilege @Authorized.
			return new APIAuthenticationException(Context.getMessageSourceService().getMessage("error.privilegesRequired",
			    new Object[] { String.join(",", missingPrivileges) }, Locale.getDefault()), result);
		}

		if (result != null && !result.isGranted() && recorded.isAuthenticationMissing()) {
			// the message AuthorizationAdvice denies a no-value @Authorized with
			return new APIAuthenticationException(
			        Context.getMessageSourceService().getMessage("error.aunthenticationRequired"), result);
		}

		return result;
	}

	@Override
	public Object handleDeniedInvocation(MethodInvocation methodInvocation, AuthorizationResult authorizationResult) {
		return deniedHandler.handleDeniedInvocation(methodInvocation, authorizationResult);
	}

	@Override
	public Object handleDeniedInvocationResult(MethodInvocationResult methodInvocationResult,
	        AuthorizationResult authorizationResult) {
		return deniedHandler.handleDeniedInvocationResult(methodInvocationResult, authorizationResult);
	}
}
