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

import java.util.function.Supplier;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The {@link Authentication} a method-security expression is evaluated against, taken from the
 * legacy {@link UserContext} when {@link SecurityContextHolder} holds none.
 * <p>
 * Needed because the two holders are independent thread-locals that can disagree.
 * {@link Context#setUserContext(UserContext)} publishes an {@link OpenmrsAuthenticationToken} into
 * the {@code SecurityContextHolder}, but the {@code UserContext} is the store of record, and a
 * thread can hold one without the other - a {@code SecurityContextHolder.clearContext()} by a
 * filter or a test leaves the session open behind it.
 * <p>
 * Without this, the two ways of naming a privilege would disagree exactly there.
 * {@code hasAuthority('X')} never reads the {@code Authentication}:
 * {@link OpenmrsAuthorizationManagerFactory} answers it from {@link Context#hasPrivilege(String)},
 * which reads the {@code UserContext}. {@code hasPermission(...)} is a SpEL method on
 * {@code SecurityExpressionRoot}, so evaluating it has to materialize the {@code Authentication}
 * first, and {@code AuthorizationManagerBeforeMethodInterceptor} throws
 * {@link AuthenticationCredentialsNotFoundException} rather than return {@code null}. The privilege
 * each form then resolves is identical, so a call that {@code hasAuthority} decides would fail
 * before being decided at all when written as {@code hasPermission} - breaking the equivalence
 * {@link OpenmrsPermissionEvaluator} documents and making the addition of a target to an existing
 * annotation a behavioural change on any thread whose holders have diverged.
 * <p>
 * The {@code SecurityContextHolder} still wins where it holds anything, so a caller that installed
 * its own token keeps it. Only its absence falls through to the {@code UserContext}, and with no
 * session either {@link Context#getUserContext()} raises its usual
 * {@link org.openmrs.api.APIException} - deliberately, rather than letting Spring Security's
 * {@link AuthenticationCredentialsNotFoundException} escape. That is the exception every other
 * OpenMRS privilege path raises with no session open, {@code hasAuthority} included (by way of the
 * {@code Context.addProxyPrivilege} in {@link PrivilegeResolution}), and callers are written
 * against it: {@code PersonName.getFullName()} catches {@code APIException} to fall back to an
 * unformatted name, and an {@code AuthenticationException} would sail straight through it.
 *
 * @since 3.0.0
 */
final class LegacyContextAuthentication {

	private LegacyContextAuthentication() {
	}

	/**
	 * Wraps <code>authentication</code> so a missing {@link Authentication} is answered from the
	 * current {@link UserContext}. Resolved per evaluation rather than once, because the root holds the
	 * supplier and reads it lazily - the session can be opened between the expression being compiled
	 * and the privilege being checked.
	 *
	 * @param authentication the interceptor's own supplier, which throws when the
	 *            {@link SecurityContextHolder} is empty
	 * @return a supplier that falls back to the legacy context, raising
	 *         {@link org.openmrs.api.APIException} when there is no session either
	 */
	static Supplier<Authentication> orCurrentUserContext(Supplier<? extends Authentication> authentication) {
		return () -> {
			Authentication resolved = null;
			try {
				resolved = authentication.get();
			} catch (AuthenticationCredentialsNotFoundException e) {
				// nothing published on this thread; the UserContext below is the store of record
			}

			return resolved != null ? resolved : new OpenmrsAuthenticationToken(Context.getUserContext());
		};
	}
}
