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

import org.openmrs.api.context.Context;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;

/**
 * Wraps the {@code @PreAuthorize}/{@code @PostAuthorize} {@link AuthorizationManager} so that a
 * denial caused by a missing OpenMRS privilege says which privilege was missing, using the same
 * {@code error.privilegesRequired} message {@code AuthorizationAdvice} produces for a denied
 * {@code @Authorized} call, instead of {@code AuthorizationManager}'s own generic "Access Denied".
 * Installed by {@link OpenmrsSecurityConfig}; see
 * {@code OpenmrsSecurityConfig#preAuthorizeAuthorizationManagerPostProcessor()} for how, and why
 * that bean is {@code @Primary}.
 * <p>
 * For method security the naming has to happen here, outside the expression, rather than in
 * {@link OpenmrsPermissionEvaluator#hasPermission(Authentication, Object, Object)} itself: SpEL
 * composes {@code hasPermission(...)} with {@code or}, {@code and} and {@code !}, so a throw from
 * inside the expression decides the outcome before the rest of it has been evaluated (see that
 * class's javadoc). By the time this manager has a result, the whole expression has had its say, so
 * the privilege names {@link OpenmrsPermissionEvaluator} collected are only turned into an
 * exception when the expression actually denied.
 * <p>
 * A denial with no recorded privilege is left alone and reported by Spring Security as usual: an
 * expression can deny for reasons that name no privilege at all ({@code isAuthenticated()},
 * {@code hasAuthority(...)}, a hand-written {@code @PreAuthorize}), and inventing a
 * privileges-required message for those would be worse than the generic one.
 *
 * @param <T> what the wrapped manager authorizes - a {@code MethodInvocation} for
 *            {@code @PreAuthorize}, a {@code MethodInvocationResult} for {@code @PostAuthorize}
 * @since 3.0.0
 */
public class PrivilegeNamingAuthorizationManager<T> implements AuthorizationManager<T> {

	private final AuthorizationManager<T> delegate;

	public PrivilegeNamingAuthorizationManager(AuthorizationManager<T> delegate) {
		this.delegate = delegate;
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
			// joined the same way AuthorizationAdvice joins a multi-privilege @Authorized
			throw new AuthorizationDeniedException(Context.getMessageSourceService().getMessage("error.privilegesRequired",
			    new Object[] { String.join(",", missingPrivileges) }, Locale.getDefault()), result);
		}

		return result;
	}
}
