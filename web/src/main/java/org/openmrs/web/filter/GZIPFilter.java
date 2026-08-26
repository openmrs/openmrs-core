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
import java.util.regex.PatternSyntaxException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.ConfigUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that compresses output with gzip (assuming that browser supports gzip). Code from <a
 * href="http://www.onjava.com/pub/a/onjava/2003/11/19/filters.html">
 * http://www.onjava.com/pub/a/onjava/2003/11/19/filters.html</a>. &copy; 2003 Jayson Falkner You
 * may freely use the code both commercially and non-commercially.
 */
public class GZIPFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(GZIPFilter.class);

	private volatile AcceptedPathPatterns acceptedPathPatterns = null;

	/**
	 * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		try {
			request = performGZIPRequest(request);
		}
		catch (APIException e) {
			log.debug("Rejecting compressed request for {}: {}", request.getRequestURI(), e.getMessage());
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			return;
		}

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
	 * Supports GZIP requests
	 * @param req request
	 * @return gzipped request
	 */
	public HttpServletRequest performGZIPRequest(HttpServletRequest req) {
		String contentEncoding = req.getHeader("Content-encoding");
		if (contentEncoding != null && contentEncoding.contains("gzip")) {
			if (!isCompressedRequestForPathAccepted(req.getRequestURI())) {
				throw new APIException("Unsupported Media Type");
			}

			log.debug("GZIP request supported");

			try {
				GZIPRequestWrapper wrapperRequest = new GZIPRequestWrapper(req);
				log.debug("GZIP request wrapped successfully");
				return wrapperRequest;
			}
			catch (IOException e) {
				log.error("Error during wrapping GZIP request", e);
				return req;
			}
		} else {
			return req;
		}

	}

	/**
	 * Convenience method to test for GZIP capabilities
	 *
	 * @param req The current user request
	 * @return boolean indicating GZIP support
	 */
	private boolean isGZIPSupported(HttpServletRequest req) {
		String browserEncodings = req.getHeader("accept-encoding");
		return (browserEncodings != null) && browserEncodings.contains("gzip");
	}

	/**
	 * Returns global property gzip.enabled as boolean
	 */
	private boolean isGZIPEnabled() {
		return Boolean.parseBoolean(getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_GZIP_ENABLED));
	}

	/**
	 * Returns true if path matches pattern in gzip.acceptCompressedRequestsForPaths property
	 */
	private boolean isCompressedRequestForPathAccepted(String path) {
		if (path == null) {
			return false;
		}

		String propertyValue = getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_GZIP_ACCEPT_COMPRESSED_REQUESTS_FOR_PATHS);
		if (propertyValue == null) {
			// the property is unset or could not be read, so there is no allowlist to match against
			propertyValue = "";
		}

		AcceptedPathPatterns patterns = acceptedPathPatterns;
		if (patterns == null || !patterns.wasCompiledFrom(propertyValue)) {
			patterns = new AcceptedPathPatterns(propertyValue);
			acceptedPathPatterns = patterns;
		}

		return patterns.accepts(path);
	}

	/**
	 * Reads a global property the way a filter has to: without assuming there is a logged-in user, and
	 * without a service call per request.
	 * <p/>
	 * {@link ConfigUtil} does the caching. It is registered as a
	 * {@link org.openmrs.api.GlobalPropertyListener}, so it answers from its own cache once the
	 * property has been read, and the value it holds is replaced when the property is edited through
	 * the API — which is what lets a change take effect without a restart, where the field this filter
	 * used to cache the accepted paths in never picked one up. Reading a global property is privileged
	 * and this filter serves unauthenticated requests, so the read is proxied; the privilege needs an
	 * open session to hang off, which is what the guard is for.
	 *
	 * @return the property value, or null if it is unset or could not be read
	 */
	private static String getGlobalProperty(String propertyName) {
		if (!Context.isSessionOpen()) {
			return null;
		}

		try {
			Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
			return ConfigUtil.getGlobalProperty(propertyName);
		}
		catch (Exception e) {
			log.warn("Unable to get the global property: {}", propertyName, e);
			return null;
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
		}
	}

	/**
	 * The compiled form of the comma-delimited regexes in
	 * {@link OpenmrsConstants#GLOBAL_PROPERTY_GZIP_ACCEPT_COMPRESSED_REQUESTS_FOR_PATHS}. The property
	 * value they were compiled from is held alongside them so that they can be reused for as long as
	 * that value is unchanged, which is what keeps the regexes off the per-request path.
	 */
	private static final class AcceptedPathPatterns {

		private final String propertyValue;

		/** a null element is an entry that could not be compiled and is skipped when matching */
		private final Pattern[] patterns;

		AcceptedPathPatterns(String propertyValue) {
			this.propertyValue = propertyValue;

			String[] acceptPaths = propertyValue.split(",");
			this.patterns = new Pattern[acceptPaths.length];
			for (int i = 0; i < acceptPaths.length; i++) {
				try {
					patterns[i] = Pattern.compile(acceptPaths[i]);
				}
				catch (PatternSyntaxException e) {
					log.warn("Unable to process the global property: {}. \"{}\" is not a valid regular expression and is "
					        + "ignored, so a compressed request is only accepted for the paths the other entries match",
					    OpenmrsConstants.GLOBAL_PROPERTY_GZIP_ACCEPT_COMPRESSED_REQUESTS_FOR_PATHS, acceptPaths[i], e);
				}
			}
		}

		boolean wasCompiledFrom(String otherPropertyValue) {
			return propertyValue.equals(otherPropertyValue);
		}

		boolean accepts(String path) {
			for (Pattern pattern : patterns) {
				// an entry that would not compile is logged when it is compiled and skipped here
				if (pattern == null) {
					continue;
				}

				if (pattern.matcher(path).matches()) {
					return true;
				}
			}

			return false;
		}
	}
}
