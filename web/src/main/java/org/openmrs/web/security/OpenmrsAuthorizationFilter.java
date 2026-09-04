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

import java.io.IOException;
import java.util.List;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

/**
 * Enforces every {@link AuthorizedUrlMatcher} rule via {@link OpenmrsAuthorizationManager}, exactly
 * as {@code authorizeHttpRequests(...)} did in {@link WebSecurityConfig} before this class existed
 * - but registered as its own {@code <filter>} in {@code web.xml}, positioned <em>after</em>
 * {@code ModuleFilter}, rather than as part of {@code @EnableWebSecurity}'s single composite
 * {@code springSecurityFilterChain}.
 * <p>
 * That composite chain runs before {@code ModuleFilter} (see the {@code springSecurityFilterChain}
 * entry in {@code web.xml}), and {@code @EnableWebSecurity} offers no way to place one of several
 * {@code SecurityFilterChain} beans at a different point in the servlet filter order - every such
 * bean is folded into the one {@code springSecurityFilterChain} filter, chosen from each other by
 * URL pattern, not by {@code web.xml} position. That is wrong for URL rules specifically: some
 * modules authenticate a request from inside their own {@code ModuleFilter}-dispatched filter
 * rather than relying on an existing session - webservices.rest's {@code AuthorizationFilter} and
 * fhir2's {@code AuthenticationFilter} both call {@code Context.authenticate(...)} against a Basic
 * {@code Authorization} header, deliberately never failing the request themselves ("fail-open at
 * the filter, fail-closed at the service" - see {@code AuthorizationFilter}'s own javadoc). A rule
 * enforced before {@code ModuleFilter} runs would deny that request before either filter had a
 * chance to authenticate it, valid credentials or not - denying with a plain 403, never REST's
 * usual 401 with {@code WWW-Authenticate} or legacyui's redirect to login, since
 * {@code ExceptionTranslationFilter} decides "anonymous" by token type and
 * {@link OpenmrsAuthenticationToken} is never one.
 * <p>
 * {@link OpenmrsSecurityContextFilter} does not move: it stays in the early
 * {@code springSecurityFilterChain} seat, since {@code ModuleFilter}'s own filters (and everything
 * downstream) need the {@code Authentication} it installs from any <em>existing</em> session
 * already in place before they run. Only the rule enforcement moves - which is exactly why this
 * class exists as a second, independent filter rather than by repositioning
 * {@code springSecurityFilterChain} itself.
 * <p>
 * Reuses Spring Security's own filter classes directly, bypassing {@code @EnableWebSecurity}/
 * {@code HttpSecurity} entirely for this piece, so the observable behavior (a denial produces the
 * same response as before) is unaffected by moving where the check runs:
 * {@link ExceptionTranslationFilter} - configured with the same {@link Http403ForbiddenEntryPoint}
 * and default {@code AccessDeniedHandler} {@code HttpSecurity} used implicitly before - wraps
 * {@link AuthorizationFilter}, which is handed an {@link OpenmrsAuthorizationManager} adapted from
 * {@code AuthorizationManager<RequestAuthorizationContext>} to the
 * {@code AuthorizationManager<HttpServletRequest>} that class itself requires.
 *
 * @since 3.0.0
 */
public class OpenmrsAuthorizationFilter implements Filter {

	private final ExceptionTranslationFilter exceptionTranslationFilter;

	private final AuthorizationFilter authorizationFilter;

	public OpenmrsAuthorizationFilter(List<AuthorizedUrlMatchers> authorizedUrlMatchers) {
		OpenmrsAuthorizationManager delegate = new OpenmrsAuthorizationManager(authorizedUrlMatchers);
		AuthorizationManager<HttpServletRequest> adapter = (authentication, request) -> delegate.authorize(authentication,
		    new RequestAuthorizationContext(request));

		this.exceptionTranslationFilter = new ExceptionTranslationFilter(new Http403ForbiddenEntryPoint());
		this.authorizationFilter = new AuthorizationFilter(adapter);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		exceptionTranslationFilter.doFilter(request, response,
		    (innerRequest, innerResponse) -> authorizationFilter.doFilter(innerRequest, innerResponse, chain));
	}
}
