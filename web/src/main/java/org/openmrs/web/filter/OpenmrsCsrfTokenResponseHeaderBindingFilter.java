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

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.web.security.OpenmrsCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

@Component
public class OpenmrsCsrfTokenResponseHeaderBindingFilter extends OncePerRequestFilter {
	
	protected static final String REQUEST_ATTRIBUTE_NAME = "_csrf";
	
	protected static final String RESPONSE_HEADER_NAME = "X-CSRF-HEADER";
	
	protected static final String RESPONSE_PARAM_NAME = "X-CSRF-PARAM";
	
	protected static final String RESPONSE_TOKEN_NAME = OpenmrsCsrfTokenRepository.class.getName().concat(".CSRF_TOKEN");
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	        javax.servlet.FilterChain filterChain) throws ServletException, IOException {
		CsrfToken csrfToken = (CsrfToken) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
		
		if (csrfToken != null) {
			response.setHeader(RESPONSE_HEADER_NAME, csrfToken.getHeaderName());
			response.setHeader(RESPONSE_PARAM_NAME, csrfToken.getParameterName());
			response.setHeader(RESPONSE_TOKEN_NAME, csrfToken.getToken());
			Cookie cookie = WebUtils.getCookie(request, RESPONSE_TOKEN_NAME);
			
			String token = csrfToken.getToken();
			if (cookie == null || token != null && !token.equals(cookie.getValue())) {
				cookie = new Cookie(RESPONSE_TOKEN_NAME, token);
				cookie.setPath("/");
				response.addCookie(cookie);
			}
        }
		filterChain.doFilter(request, response);
    }
}
