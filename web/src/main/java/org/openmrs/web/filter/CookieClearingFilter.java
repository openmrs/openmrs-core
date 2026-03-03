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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * This servlet filter exists to remove session cookies when a user logs out.
 * <p/>
 * This filter is configurable at runtime using the following runtime properties:
 * <ul>
 *     <li><tt>cookieClearingFilter.toClear = comma separated list of cookies to clear</tt>
 *     determines the cookies we will try to clear. If unset, will default to just clearing the JSESSIONID cookie.</li>
 * </ul>
 */
public class CookieClearingFilter extends OncePerRequestFilter {
	
	private static final Logger log = LoggerFactory.getLogger(CookieClearingFilter.class);
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		
		// if an earlier filter has already written a response, we cannot do anything
		if (response.isCommitted()) {
			filterChain.doFilter(request, response);
			return;
		}
		
		String[] cookiesToClear = new String[0];
		
		// the try-catch here is defensive; if, for whatever reason, we cannot parse this setting, this filter should not
		// stop the request
		try {
			Properties properties = Context.getRuntimeProperties();
			String cookiesToClearSetting = properties.getProperty("cookieClearingFilter.toClear", "JSESSIONID");
			
			if (StringUtils.isNotBlank(cookiesToClearSetting)) {
				cookiesToClear = Arrays.stream(cookiesToClearSetting.split("\\s*,\\s*")).map(String::trim).toArray(
					String[]::new);
			}
		}
		catch (Exception e) {
			log.warn("Caught exception while trying to determine cookies to clear", e);
		}
		
		boolean requestHasSession = false;
		if (cookiesToClear.length > 0) {
			// we need to track whether this request initially was part of a session
			// if it was and there is no valid request at the end of the session, we clear the session cookies
			requestHasSession = request.getRequestedSessionId() != null;
		}
		
		// handle the request
		try {
			filterChain.doFilter(request, response);
		}
		finally {
			if (cookiesToClear.length > 0 && !response.isCommitted()) {
				HttpSession session = request.getSession(false);
				// session was invalidated
				if (session == null && requestHasSession) {
					for (Cookie cookie : request.getCookies()) {
						for (String cookieToClear : cookiesToClear) {
							if (cookieToClear.equalsIgnoreCase(cookie.getName())) {
								Cookie clearedCookie = new Cookie(cookie.getName(), null);
								String contextPath = request.getContextPath();
								clearedCookie.setPath(
									contextPath == null || contextPath.trim().equals("") ? "/" : contextPath);
								clearedCookie.setMaxAge(0);
								clearedCookie.setHttpOnly(true);
								clearedCookie.setSecure(request.isSecure());
								response.addCookie(clearedCookie);
								break;
							}
						}
					}
				}
			}
		}
	}
}
