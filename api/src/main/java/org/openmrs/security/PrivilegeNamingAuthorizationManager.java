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
 * {@code AuthorizationAdvice} uses, rather than a generic "Access Denied". Installed by
 * {@link OpenmrsSecurityConfig}.
 * <p>
 * The named exception is <em>returned</em> as the {@link AuthorizationResult}, not thrown:
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
 * recorded no privilege is left to Spring Security, since expressions can deny for reasons that
 * name none.
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
		Set<String> enclosing = MissingPrivilegeRecorder.begin();
		AuthorizationResult result;
		Set<String> missingPrivileges;
		try {
			result = delegate.authorize(authentication, object);
		} finally {
			missingPrivileges = MissingPrivilegeRecorder.end(enclosing);
		}

		if (result != null && !result.isGranted() && !missingPrivileges.isEmpty()) {
			// returned rather than thrown: see the class javadoc. Joined the same way
			// AuthorizationAdvice joins a multi-privilege @Authorized.
			return new AuthorizationDeniedException(Context.getMessageSourceService().getMessage("error.privilegesRequired",
			    new Object[] { String.join(",", missingPrivileges) }, Locale.getDefault()), result);
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
