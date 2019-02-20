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
import javax.servlet.http.HttpSession;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.web.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * This is the custom OpenMRS filter. It is defined as the filter of choice in the web.xml file. All
 * page/object calls run through the doFilter method so we can wrap every session with the user's
 * userContext (which holds the user's authenticated info). This is needed because the OpenMRS API
 * keeps authentication information on the current Thread. Web applications use a different thread
 * per request, so before each request this filter will make sure that the UserContext (the
 * authentication information) is on the Thread.
 */
public class OpenmrsFilter extends OncePerRequestFilter {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsFilter.class);
	
	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		log.debug("Destroying filter");
	}
	
	/**
	 * This method is called for every request for a page/image/javascript file/etc The main point
	 * of this is to make sure the user's current userContext is on the session and on the current
	 * thread
	 *
	 * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest httpRequest, HttpServletResponse httpResponse, FilterChain chain)
	        throws ServletException, IOException {
		
		HttpSession httpSession = httpRequest.getSession();
		
		// used by htmlInclude tag
		httpRequest.setAttribute(WebConstants.INIT_REQ_UNIQUE_ID, String.valueOf(System.currentTimeMillis()));
		
		if (log.isDebugEnabled()) {
			log.debug("requestURI " + httpRequest.getRequestURI());
			log.debug("requestURL " + httpRequest.getRequestURL());
			log.debug("request path info " + httpRequest.getPathInfo());
		}
		
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
			
			if (log.isDebugEnabled()) {
				log.debug("Just set user context " + userContext + " as attribute on session");
			}
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
		
		// Add the user context to the current thread 
		Context.setUserContext(userContext);
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		
		log.debug("before chain.Filter");
		
		// continue the filter chain (going on to spring, authorization, etc)
		try {
			chain.doFilter(httpRequest, httpResponse);
		}
		finally {
			Context.clearUserContext();
		}
		
		log.debug("after chain.doFilter");
		
	}
	
}
