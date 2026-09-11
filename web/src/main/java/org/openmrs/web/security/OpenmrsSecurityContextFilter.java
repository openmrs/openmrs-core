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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.web.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Registered as part of {@link WebSecurityConfig}'s {@code SecurityFilterChain} rather than as a
 * plain {@code web.xml} filter - superseding {@code org.openmrs.web.filter.OpenmrsFilter}, which
 * did the same job from outside Spring Security's own filter chain. Every page/object request runs
 * through {@link #doFilterInternal}, wrapping it with the user's {@link UserContext} (holding the
 * user's authenticated info) so the OpenMRS API, which keeps authentication information on the
 * current {@link Thread}, sees it: web applications use a different thread per request, so this
 * filter makes sure the {@code UserContext} is on the thread before the rest of the chain runs.
 * <p>
 * Positioned via {@link WebSecurityConfig} to run before Spring Security's own authorization filter
 * ({@code authorizeHttpRequests(...)}), since that needs the
 * {@link org.springframework.security.core.Authentication} this filter installs (via
 * {@link Context#setUserContext(UserContext)}) to already be in place - see
 * {@link OpenmrsAuthenticationToken}.
 *
 * @since 3.0.0
 */
public class OpenmrsSecurityContextFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(OpenmrsSecurityContextFilter.class);

	/**
	 * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(HttpServletRequest,
	 *      HttpServletResponse, FilterChain)
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain)
	        throws ServletException, IOException {

		HttpSession httpSession = httpRequest.getSession();

		// used by htmlInclude tag
		httpRequest.setAttribute(WebConstants.INIT_REQ_UNIQUE_ID, String.valueOf(System.currentTimeMillis()));

		log.debug("requestURI {}", httpRequest.getRequestURI());
		log.debug("requestURL {}", httpRequest.getRequestURL());
		log.debug("request path info {}", httpRequest.getPathInfo());

		// User context is created if it doesn't already exist and added to the session
		// note: this usercontext storage logic is copied to webinf/view/uncaughtexception.jsp to
		// 		 prevent stack traces being shown to non-authenticated users
		UserContext userContext = (UserContext) httpSession.getAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR);

		// default the session username attribute to anonymous
		httpSession.setAttribute("username", "-anonymous user-");

		// if there isn't a userContext on the session yet, create one
		// and set it onto the session
		if (userContext == null) {
			userContext = new UserContext(Context.getAuthenticationScheme());
			httpSession.setAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR, userContext);

			log.debug("Just set user context {} as attribute on session", userContext);
		} else {
			// set username as attribute on session so parent servlet container
			// can identify sessions easier
			User user = userContext.getAuthenticatedUser();
			if (user != null) {
				httpSession.setAttribute("username", user.getUsername());
			}
		}

		// set the locale on the session (for the servlet container as well)
		httpSession.setAttribute("locale", userContext.getLocale());

		// We do not cache the csrfguard javascript file because it contains the csrf token that is
		// dynamically embedded in forms. For this to work, this filter must run before CSRFGuard,
		// which it does: web.xml positions springSecurityFilterChain (which this filter is part of)
		// ahead of the CSRFGuard filter.
		if (httpRequest.getRequestURI().endsWith("csrfguard")) {
			httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
			httpResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			httpResponse.setHeader("Expires", "0"); // Proxies.
		}

		// Add the user context to the current thread
		Context.setUserContext(userContext);
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());

		log.debug("before chain.Filter");

		// continue the filter chain (going on to the rest of Spring Security, authorization, etc)
		try {
			chain.doFilter(httpRequest, httpResponse);
		} finally {
			Context.clearUserContext();
		}

		log.debug("after chain.doFilter");
	}
}
