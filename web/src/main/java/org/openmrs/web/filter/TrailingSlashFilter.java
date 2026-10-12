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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.springframework.web.filter.UrlHandlerFilter;

/**
 * Handles a request whose path ends in a slash as if it had been made without the slash, so
 * {@code /ws/rest/v1/patient/} reaches the same handler as {@code /ws/rest/v1/patient}. Spring MVC
 * matched trailing slashes by default before Spring 6, and Platform 2.x clients rely on that. The
 * request is wrapped rather than redirected so that the method and body of a POST are kept.
 *
 * @since 3.0.0
 */
public class TrailingSlashFilter implements Filter {

	private final Filter delegate = UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		delegate.init(filterConfig);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		delegate.doFilter(request, response, chain);
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}
}
