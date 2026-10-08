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
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.savedrequest.NullRequestCache;

/**
 * Enforces every {@link AuthorizedUrlMatcher} rule via {@link OpenmrsAuthorizationManager},
 * registered as its own {@code <filter>} in {@code web.xml} <em>after</em> {@code ModuleFilter}
 * rather than inside the composite {@code springSecurityFilterChain} (see
 * {@link WebSecurityConfig}). That chain runs before {@code ModuleFilter}, and
 * {@code @EnableWebSecurity} cannot place one of several {@code SecurityFilterChain} beans
 * elsewhere in the servlet order - which is wrong for URL rules, because webservices.rest and fhir2
 * authenticate Basic credentials from inside their own {@code ModuleFilter}-dispatched filter. A
 * rule enforced ahead of those would deny valid credentials.
 * <p>
 * The trade-off: a module filter that completes a request without calling
 * {@code chain.doFilter(...)} never reaches this filter, so no URL rule is evaluated for it. A
 * privilege that must hold whatever modules are installed therefore belongs on the service method,
 * with the URL rule as an extra restriction. {@link OpenmrsSecurityContextFilter} does not move,
 * since {@code ModuleFilter}'s own filters need the {@code Authentication} it installs from an
 * existing session.
 * <p>
 * Built from Spring Security's own filters rather than through {@code HttpSecurity}:
 * {@link ExceptionTranslationFilter} wraps {@link AuthorizationFilter}, whose
 * {@code AuthorizationManager<HttpServletRequest>} shape the adapter below supplies. The adapter
 * throws the {@link PrivilegeNamingAuthorizationManager} denial it is handed, because that wrapper
 * returns rather than throws (see its javadoc) and {@code AuthorizationFilter} would otherwise
 * replace the privilege name with its own "Access Denied".
 * <p>
 * A denial then takes one of two paths, told apart by {@link OpenmrsAuthenticationTrustResolver}
 * since Spring's own resolver decides by token class: a caller with no session gets 401 from
 * {@code HttpStatusEntryPoint}, an authenticated one lacking the privilege gets 403 from
 * {@link OpenmrsAccessDeniedHandler}, which publishes the reason as the
 * {@code WebAttributes.ACCESS_DENIED_403} attribute. Only the 403 path publishes it, so no
 * privilege name reaches a caller who has not identified itself.
 *
 * @since 3.0.0
 */
public class OpenmrsAuthorizationFilter implements Filter {

	private final ExceptionTranslationFilter exceptionTranslationFilter;

	private final AuthorizationFilter authorizationFilter;

	public OpenmrsAuthorizationFilter(List<AuthorizedUrlMatchers> authorizedUrlMatchers) {
		OpenmrsAuthorizationManager rules = new OpenmrsAuthorizationManager(authorizedUrlMatchers);
		AuthorizationManager<RequestAuthorizationContext> naming = new PrivilegeNamingAuthorizationManager<>(rules);
		AuthorizationManager<HttpServletRequest> adapter = (authentication, request) -> {
			AuthorizationResult result = naming.authorize(authentication, new RequestAuthorizationContext(request));
			// the naming manager returns its exception rather than throwing it, so that method security
			// can route a denial through @HandleAuthorizationDenied (see that class's javadoc).
			// AuthorizationFilter would swap a returned denial for its own "Access Denied", so throw here
			// to keep the privilege name.
			if (result instanceof AuthorizationDeniedException denied) {
				throw denied;
			}
			return result;
		};

		// NullRequestCache: the single-argument constructor would save every challenged request into the
		// HttpSession for RequestCacheAwareFilter to replay, and that filter is disabled
		this.exceptionTranslationFilter = new ExceptionTranslationFilter(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
		        new NullRequestCache());
		this.exceptionTranslationFilter.setAccessDeniedHandler(new OpenmrsAccessDeniedHandler());
		this.exceptionTranslationFilter.setAuthenticationTrustResolver(new OpenmrsAuthenticationTrustResolver());
		this.authorizationFilter = new AuthorizationFilter(adapter);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		exceptionTranslationFilter.doFilter(request, response,
		    (innerRequest, innerResponse) -> authorizationFilter.doFilter(innerRequest, innerResponse, chain));
	}
}
