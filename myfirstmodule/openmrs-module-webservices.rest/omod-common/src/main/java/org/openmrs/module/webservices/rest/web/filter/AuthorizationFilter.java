/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.filter;

import java.io.IOException;
import java.nio.charset.Charset;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter intended for all /ws/rest calls that allows the user to authenticate via Basic
 * authentication. (It will not fail on invalid or missing credentials. We count on the API to throw
 * exceptions if an unauthenticated user tries to do something they are not allowed to do.) <br/>
 * <br/>
 * IP address authorization is also performed based on the global property:
 * {@link RestConstants#ALLOWED_IPS_GLOBAL_PROPERTY_NAME}
 */
public class AuthorizationFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(AuthorizationFilter.class);
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		log.debug("Initializing REST WS Authorization filter");
	}
	
	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		log.debug("Destroying REST WS Authorization filter");
	}
	
	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
	 *      javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		
		// check the IP address first.  If its not valid, return a 403
		if (!RestUtil.isIpAllowed(request.getRemoteAddr())) {
			// the ip address is not valid, set a 403 http error code
			HttpServletResponse httpresponse = (HttpServletResponse) response;
			httpresponse.sendError(HttpServletResponse.SC_FORBIDDEN,
			    "IP address '" + request.getRemoteAddr() + "' is not authorized");
			return;
		}
		
		// skip if the session has timed out, we're already authenticated, or it's not an HTTP request
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			if (httpRequest.getRequestedSessionId() != null && !httpRequest.isRequestedSessionIdValid()) {
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session timed out");
			}
			
			if (!Context.isAuthenticated()) {
				String basicAuth = httpRequest.getHeader("Authorization");
				if (basicAuth != null) {
					// check that header is in format "Basic ${base64encode(username + ":" + password)}"
					if (basicAuth.startsWith("Basic")) {
						try {
							// remove the leading "Basic "
							basicAuth = basicAuth.substring(6);
							if (StringUtils.isBlank(basicAuth)) {
								HttpServletResponse httpResponse = (HttpServletResponse) response;
								httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid credentials provided");
								return;
							}
							
							String decoded = new String(Base64.decodeBase64(basicAuth), Charset.forName("UTF-8"));
							if (StringUtils.isBlank(decoded) || !decoded.contains(":")) {
								HttpServletResponse httpResponse = (HttpServletResponse) response;
								httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid credentials provided");
								return;
							}
							
							String[] userAndPass = decoded.split(":");
							Context.authenticate(userAndPass[0], userAndPass[1]);
							log.debug("authenticated [{}]", userAndPass[0]);
						}
						catch (Exception ex) {
							// This filter never stops execution. If the user failed to
							// authenticate, that will be caught later.
							log.debug("authentication exception ", ex);
						}
					}
				}
			}
		}
		
		// continue with the filter chain (unless IP is not allowed)
		chain.doFilter(request, response);
	}
}
