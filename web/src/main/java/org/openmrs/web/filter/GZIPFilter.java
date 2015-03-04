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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that compresses output with gzip (assuming that browser supports gzip). Code from <a
 * href="http://www.onjava.com/pub/a/onjava/2003/11/19/filters.html">
 * http://www.onjava.com/pub/a/onjava/2003/11/19/filters.html</a>. &copy; 2003 Jayson Falkner You
 * may freely use the code both commercially and non-commercially.
 */
public class GZIPFilter extends OncePerRequestFilter {
	
	private static final Log log = LogFactory.getLog(GZIPFilter.class);
	
	private Boolean cachedGZipEnabledFlag = null;
	
	/**
	 * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		
		if (isGZIPSupported(request) && isGZIPEnabled()) {
			log.debug("GZIP supported and enabled, compressing response");
			
			GZIPResponseWrapper wrappedResponse = new GZIPResponseWrapper(response);
			
			chain.doFilter(request, wrappedResponse);
			wrappedResponse.finishResponse();
			
			return;
		}
		
		chain.doFilter(request, response);
	}
	
	/**
	 * Convenience method to test for GZIP capabilities
	 *
	 * @param req The current user request
	 * @return boolean indicating GZIP support
	 */
	private boolean isGZIPSupported(HttpServletRequest req) {
		
		String browserEncodings = req.getHeader("accept-encoding");
		boolean supported = ((browserEncodings != null) && (browserEncodings.indexOf("gzip") != -1));
		
		String userAgent = req.getHeader("user-agent");
		
		if ((userAgent != null) && userAgent.startsWith("httpunit")) {
			log.debug("httpunit detected, disabling filter...");
			
			return false;
		} else {
			return supported;
		}
	}
	
	/**
	 * Returns global property gzip.enabled as boolean
	 */
	private boolean isGZIPEnabled() {
		if (cachedGZipEnabledFlag != null) {
			return cachedGZipEnabledFlag;
		}
		
		try {
			String gzipEnabled = Context.getAdministrationService().getGlobalProperty(
			    OpenmrsConstants.GLOBAL_PROPERTY_GZIP_ENABLED, "");
			
			boolean isEnabled = gzipEnabled.toLowerCase().equals("true");
			cachedGZipEnabledFlag = isEnabled;
			return cachedGZipEnabledFlag;
		}
		catch (Exception e) {
			log.warn("Unable to get the global property: " + OpenmrsConstants.GLOBAL_PROPERTY_GZIP_ENABLED, e);
			// not caching the enabled flag here in case it becomes available 
			// before the next request
			
			return false;
		}
	}
}
