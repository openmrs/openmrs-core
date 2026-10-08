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
 * Evaluates every {@link AuthorizedUrlMatcher} rule against the current request, built once from
 * the {@link AuthorizedUrlMatchers} beans core and loaded modules registered (see
 * {@link AuthorizedUrlMatcher} for why building once is safe). Each rule carries its own matcher
 * and {@link AuthorizationManager}, so this only finds the matches and combines them.
 * <p>
 * A request is granted unless a matching rule denies: no matching rule grants access, and where
 * several match, all must be satisfied. A rule that abstains ({@code null}) does not deny, as
 * {@link org.springframework.security.authorization.AuthorizationManagers#allOf} also treats it.
 * Evaluation stops at the first denying rule, so that is the rule whose privilege is named in the
 * denial (see {@code PrivilegeNamingAuthorizationManager}).
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
