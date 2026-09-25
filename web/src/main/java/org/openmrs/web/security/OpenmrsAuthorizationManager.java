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

import java.util.List;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * Evaluates every {@link AuthorizedUrlMatcher} rule against the current request, built once (see
 * the class javadoc on {@link AuthorizedUrlMatcher} for why that is safe) from every
 * {@link AuthorizedUrlMatchers} bean core and currently-loaded modules have registered. Each rule
 * already carries its own compiled
 * {@link org.springframework.security.web.util.matcher.RequestMatcher} and
 * {@link AuthorizationManager} - there is nothing left for this class to do beyond finding which
 * rules match and combining their decisions.
 * <p>
 * A request is granted unless some matching rule denies it: no matching rule at all grants access
 * (adding a rule can only narrow access, never be the sole thing granting it), and when more than
 * one rule matches the same path, every one of them must be satisfied. A rule that abstains
 * ({@code null} result, Spring Security's own convention for "no opinion") does not by itself deny
 * the request, matching how
 * {@link org.springframework.security.authorization.AuthorizationManagers#allOf} treats an
 * abstaining delegate.
 *
 * @since 3.0.0
 */
public class OpenmrsAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

	private final List<AuthorizedUrlMatcher> matchers;

	public OpenmrsAuthorizationManager(List<AuthorizedUrlMatchers> authorizedUrlMatchers) {
		this.matchers = authorizedUrlMatchers.stream().flatMap(source -> source.getAuthorizedUrlMatchers().stream())
		        .toList();
	}

	@Override
	public AuthorizationDecision authorize(Supplier<? extends Authentication> authentication,
	        RequestAuthorizationContext context) {
		HttpServletRequest request = context.getRequest();

		for (AuthorizedUrlMatcher matcher : matchers) {
			if (!matcher.getRequestMatcher().matches(request)) {
				continue;
			}

			AuthorizationResult result = matcher.getAuthorizationManager().authorize(authentication, context);
			if (result != null && !result.isGranted()) {
				return new AuthorizationDecision(false);
			}
		}

		return new AuthorizationDecision(true);
	}
}
