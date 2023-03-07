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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter for /ws/rest endpoints to prevent XML Content-Types due to security concerns
 */
public class ContentTypeFilter implements Filter {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		log.debug("Initializing REST WS Content-Type filter");
	}
	
	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		log.debug("Destroying REST WS Content-Type filter");
	}
	
	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	        ServletException {
		
		// check content-type (do not allow xml)
		if (isUnsupportedContentType(request.getContentType())) {
			HttpServletResponse httpresponse = (HttpServletResponse) response;
			httpresponse.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
			    "Content-Type " + request.getContentType() + " not supported");
			return;
		}
		
		// continue with the filter chain
		chain.doFilter(request, response);
	}
	
	private boolean isUnsupportedContentType(String contentType) {
		
		// contentType will be null for GET requests
		// blacklist approach
		return contentType != null
				&& !contentType.isEmpty()
				&& contentType.toLowerCase().contains("xml");
	}
}
