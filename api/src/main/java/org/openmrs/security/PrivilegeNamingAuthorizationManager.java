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
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.ExpressionAuthorizationDecision;
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
 * recorded neither a missing privilege nor a denying {@code isAuthenticated()} keeps Spring
 * Security's generic message, since expressions can deny for reasons that name none, with the
 * denying expression appended so it still says which guard refused.
 * <p>
 * Every denial carries a plain {@link AuthorizationDecision} rather than the delegate's result. The
 * delegate's is an {@link ExpressionAuthorizationDecision} holding the live SpEL expression, and
 * anything serializing the exception - a controller with no handler of its own renders it as the
 * response body, through {@code WebConfig}'s {@code SimpleMappingExceptionResolver} - walks from
 * there into the expression's evaluation context and fails on Jackson. Passing the expression as a
 * string keeps the diagnostic without the object graph.
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
			// returned rather than thrown, and carrying a plain decision: see the class javadoc. Joined
			// the same way AuthorizationAdvice joins a multi-privilege @Authorized. No expression here -
			// the message already names what was missing.
			return new APIAuthenticationException(
			        Context.getMessageSourceService().getMessage("error.privilegesRequired",
			            new Object[] { String.join(",", missingPrivileges) }, Locale.getDefault()),
			        new AuthorizationDecision(false));
		}

		if (result != null && !result.isGranted() && recorded.isAuthenticationMissing()) {
			// the message AuthorizationAdvice denies a no-value @Authorized with, and a plain decision
			// for the same serialization reason
			return new APIAuthenticationException(
			        Context.getMessageSourceService().getMessage("error.aunthenticationRequired"),
			        new AuthorizationDecision(false));
		}

		if (result instanceof ExpressionAuthorizationDecision denied && !result.isGranted()) {
			// Also a plain decision, and for the same serialization reason, but the expression is worth
			// keeping here: this is the denial that names no privilege, so "Access Denied" on its own
			// says nothing about which guard refused. The expression string carries that and is just a
			// String. Which DomainObjectAuthorizationRule denied, when one did, is logged by
			// OpenmrsPermissionEvaluator rather than returned - a module's class name is not the
			// caller's business.
			return new AuthorizationDeniedException("Access Denied: " + denied.getExpression().getExpressionString(),
			        new AuthorizationDecision(false));
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
