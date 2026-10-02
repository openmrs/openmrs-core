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

import org.openmrs.security.PrivilegeNamingAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

/**
 * Enforces every {@link AuthorizedUrlMatcher} rule via {@link OpenmrsAuthorizationManager},
 * registered as its own {@code <filter>} in {@code web.xml} and positioned <em>after</em>
 * {@code ModuleFilter} - deliberately not inside {@code @EnableWebSecurity}'s single composite
 * {@code springSecurityFilterChain}, where {@code authorizeHttpRequests(...)} would otherwise put
 * it (see {@link WebSecurityConfig}).
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
 * Sitting downstream of {@code ModuleFilter} has one consequence worth being explicit about: a
 * module filter that handles a request itself and does not continue the chain skips this filter
 * altogether, so none of the {@link AuthorizedUrlMatcher} rules are evaluated for that request.
 * {@code ModuleFilterChain.doFilter} only reaches the outer chain once the module filter it is
 * currently running calls {@code chain.doFilter(...)}, so a module filter that writes a response,
 * redirects, or otherwise completes the request takes the URL rules out of it entirely. A rule
 * enforced from the earlier {@code springSecurityFilterChain} seat could not be bypassed that way,
 * which is the price of fixing the authenticate-inside-ModuleFilter case above - judged the better
 * trade, since that case breaks valid credentials outright whereas this one needs a module filter
 * that deliberately ends the request. A URL rule is therefore not quite the unconditional guarantee
 * a service-method {@code @Authorized} is; a privilege that must hold no matter which module is
 * installed belongs on the service method, with the URL rule as an additional restriction rather
 * than the only one.
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
 * <p>
 * That manager is wrapped in a {@link PrivilegeNamingAuthorizationManager} first, so a denial
 * caused by a missing privilege reports {@code error.privilegesRequired} naming it rather than
 * {@code AuthorizationFilter}'s hard-coded "Access Denied" - the same treatment a denied
 * {@code @PreAuthorize} gets. Throwing is safe at this position, unlike inside a method-security
 * expression: nothing composes this manager, {@code AuthorizationFilter} does not catch what
 * {@code authorize(...)} throws, and {@link ExceptionTranslationFilter} is waiting for it.
 * <p>
 * Spring's default {@code AccessDeniedHandlerImpl} would discard that message, answering with
 * {@code sendError(403, "Forbidden")} without ever looking at the exception, so
 * {@link OpenmrsAccessDeniedHandler} is installed in its place: it publishes the exception as the
 * {@code WebAttributes.ACCESS_DENIED_403} request attribute first, leaving the status code and
 * response body untouched. Whatever renders the 403 can then say which privilege was missing.
 *
 * @since 3.0.0
 */
public class OpenmrsAuthorizationFilter implements Filter {

	private final ExceptionTranslationFilter exceptionTranslationFilter;

	private final AuthorizationFilter authorizationFilter;

	public OpenmrsAuthorizationFilter(List<AuthorizedUrlMatchers> authorizedUrlMatchers) {
		OpenmrsAuthorizationManager rules = new OpenmrsAuthorizationManager(authorizedUrlMatchers);
		AuthorizationManager<RequestAuthorizationContext> naming = new PrivilegeNamingAuthorizationManager<>(rules);
		AuthorizationManager<HttpServletRequest> adapter = (authentication, request) -> naming.authorize(authentication,
		    new RequestAuthorizationContext(request));

		this.exceptionTranslationFilter = new ExceptionTranslationFilter(new Http403ForbiddenEntryPoint());
		this.exceptionTranslationFilter.setAccessDeniedHandler(new OpenmrsAccessDeniedHandler());
		this.authorizationFilter = new AuthorizationFilter(adapter);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		exceptionTranslationFilter.doFilter(request, response,
		    (innerRequest, innerResponse) -> authorizationFilter.doFilter(innerRequest, innerResponse, chain));
	}
}
