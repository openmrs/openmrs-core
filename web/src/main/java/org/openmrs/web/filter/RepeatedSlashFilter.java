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
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Collapses each run of slashes in the request URI into one before anything later in
 * {@code web.xml} reads it, so {@code /openmrs//ws/rest/v1/visit} is handled as
 * {@code /openmrs/ws/rest/v1/visit}, the path the container dispatched it on.
 * <p>
 * The container normalizes the servlet path and path info when it picks the servlet, but
 * {@link HttpServletRequest#getRequestURI()} keeps the slashes the client sent. Spring's
 * {@code ServletRequestPathUtils} needs the context path and servlet path to start that URI, so a
 * repeated slash at or before {@code /ws} failed the request with a 500 in Spring Security's
 * {@code ServletRequestPathFilter}, before {@code FilterChainProxy}'s firewall could answer 400 as
 * it does for one further in. Clients send such URLs: the O3 patient chart's visit history requests
 * {@code /openmrs//ws/rest/v1/visit}, which OpenMRS 2.x served.
 * <p>
 * {@code WebSecurityConfig} leaves {@code StrictHttpFirewall} at its defaults because a URL rule
 * matches the request URI, not the path the container dispatches on. Once this filter has run, the
 * two agree about repeated slashes, so one cannot walk around a rule. Encoded slashes, path
 * parameters and dot segments are left as they are, for the firewall to reject as before.
 *
 * @since 3.0.0
 */
public class RepeatedSlashFilter extends OncePerRequestFilter {

	private static final Pattern REPEATED_SLASHES = Pattern.compile("/{2,}");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {
		// the context path starts the request URI, so a repeated slash in either shows here
		boolean repeated = request.getRequestURI().contains("//");
		filterChain.doFilter(repeated ? new CollapsedSlashesRequest(request) : request, response);
	}

	private static String collapse(String path) {
		return (path == null) ? null : REPEATED_SLASHES.matcher(path).replaceAll("/");
	}

	/**
	 * The request with the slashes in its URI, URL and context path collapsed. The servlet path and
	 * path info are left alone, since the container normalized them already.
	 */
	private static class CollapsedSlashesRequest extends HttpServletRequestWrapper {

		CollapsedSlashesRequest(HttpServletRequest request) {
			super(request);
		}

		@Override
		public String getRequestURI() {
			return collapse(super.getRequestURI());
		}

		@Override
		public StringBuffer getRequestURL() {
			StringBuffer url = super.getRequestURL();
			// the path starts at the first slash after "://", whose two slashes must stay
			int pathStart = url.indexOf("/", url.indexOf("://") + 3);
			if (pathStart < 0) {
				return url;
			}
			return new StringBuffer(url.substring(0, pathStart)).append(collapse(url.substring(pathStart)));
		}

		@Override
		public String getContextPath() {
			return collapse(super.getContextPath());
		}
	}
}
