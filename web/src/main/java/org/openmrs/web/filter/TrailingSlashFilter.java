/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.openmrs.web.security.AuthorizedUrlMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.filter.UrlHandlerFilter;

/**
 * Trims the trailing slash from a request to the {@code /ws/*} mapping of the openmrs
 * DispatcherServlet, so {@code /openmrs/ws/rest/v1/session/} reaches the handler for
 * {@code /openmrs/ws/rest/v1/session}, as it did on OpenMRS 2.x. {@code /openmrs/ws/} itself is
 * left as it is, where legacyui serves its index page.
 * <p>
 * Spring 7 has no option to match a handler mapping to a path with a trailing slash.
 * {@link UrlHandlerFilter} is what Spring offers in its place, and this filter hands it the
 * requests it can safely handle:
 * <ul>
 * <li>Only requests the container mapped to {@code /ws}, the prefix mapping through which the
 * openmrs DispatcherServlet serves module controllers such as webservices.rest's.</li>
 * <li>Only a request URI that starts with the context path and {@code /ws/}.
 * {@link UrlHandlerFilter} cannot parse any other, such as {@code /openmrs//ws/x/}, which Tomcat
 * maps on the collapsed path, so those pass on unchanged.</li>
 * <li>Only a request URI that does not end in {@code //}. {@link UrlHandlerFilter} trims one slash,
 * which would leave {@code /openmrs/ws/rest/v1/session//} ending in a single slash, a shape the
 * firewall of {@code springSecurityFilterChain} no longer rejects with 400. Passed on unchanged,
 * the firewall rejects it.</li>
 * </ul>
 * {@code web.xml} runs this ahead of {@code springSecurityFilterChain}, as {@link UrlHandlerFilter}
 * asks, and so ahead of {@code openmrsAuthorizationFilter}, so an {@link AuthorizedUrlMatcher} rule
 * judges the path the handler is chosen on.
 *
 * @since 3.0.0
 */
public class TrailingSlashFilter extends OncePerRequestFilter {

	private final UrlHandlerFilter trailingSlashHandler = UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {
		if (hasTrailingSlashBelowWs(request)) {
			trailingSlashHandler.doFilter(request, response, filterChain);
		} else {
			filterChain.doFilter(request, response);
		}
	}

	private static boolean hasTrailingSlashBelowWs(HttpServletRequest request) {
		String pathInfo = request.getPathInfo();
		return AuthorizedUrlMatcher.WS_PREFIX_PATH.equals(request.getServletPath()) && pathInfo != null
		        && pathInfo.endsWith("/")
		        && request.getRequestURI().startsWith(request.getContextPath() + AuthorizedUrlMatcher.WS_PREFIX_PATH + "/")
		        && !request.getRequestURI().endsWith("//");
	}
}
