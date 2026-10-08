/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.security;

import org.openmrs.security.OpenmrsAuthenticationToken;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

/**
 * Counts a not-yet-logged-in caller as anonymous, which {@link AuthenticationTrustResolverImpl}
 * cannot do here: it decides by token class, and OpenMRS always installs an
 * {@link OpenmrsAuthenticationToken} with Spring's anonymous filter disabled, so nothing ever looks
 * anonymous to it. Everything else is delegated, so remember-me and fully-authenticated keep
 * Spring's semantics.
 * <p>
 * The distinction decides the status code: {@code ExceptionTranslationFilter} sends an anonymous
 * caller's denial to the entry point (401, inviting credentials) and an authenticated caller's to
 * the access-denied handler (403). Without it a caller with no session got a bare 403 from a URL
 * rule, where the same privilege on a service method answers 401 - reachable in practice because
 * webservices.rest's filter carries on when Basic credentials are wrong.
 * <p>
 * Installed on both {@code ExceptionTranslationFilter}s. The {@link WebSecurityConfig} one needs
 * {@link OpenmrsSecurityContextFilter} ahead of it, since that filter clears
 * {@code SecurityContextHolder} in a {@code finally}.
 *
 * @since 3.0.0
 */
public class OpenmrsAuthenticationTrustResolver implements AuthenticationTrustResolver {

	private final AuthenticationTrustResolver delegate = new AuthenticationTrustResolverImpl();

	@Override
	public boolean isAnonymous(Authentication authentication) {
		if (authentication instanceof OpenmrsAuthenticationToken) {
			// OpenmrsAuthenticationToken#isAuthenticated() already accounts for a Daemon thread and for
			// proxy privileges, not just for a logged-in user
			return !authentication.isAuthenticated();
		}

		return delegate.isAnonymous(authentication);
	}

	@Override
	public boolean isRememberMe(Authentication authentication) {
		return delegate.isRememberMe(authentication);
	}
}
