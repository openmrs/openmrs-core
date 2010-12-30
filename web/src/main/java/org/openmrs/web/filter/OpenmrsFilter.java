/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.filter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.web.WebConstants;
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
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see javax.servlet.Filter#destroy()
	 */
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
		httpRequest.setAttribute(WebConstants.INIT_REQ_UNIQUE_ID, String.valueOf(new Date().getTime()));
		
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
			userContext = new UserContext();
			httpSession.setAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR, userContext);
			
			if (log.isDebugEnabled())
				log.debug("Just set user context " + userContext + " as attribute on session");
		} else {
			// set username as attribute on session so parent servlet container 
			// can identify sessions easier
			User user = userContext.getAuthenticatedUser();
			if (user != null)
				httpSession.setAttribute("username", user.getUsername());
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
