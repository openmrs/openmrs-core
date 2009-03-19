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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.web.WebConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This is the custom OpenMRS filter. It is defined as the filter of choice in the web.xml file. All
 * page/object calls run through the doFilter method so we can wrap every session with the user's
 * userContext (which holds the user's authenticated info)
 */
public class OpenmrsFilter implements Filter {
	
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
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	                                                                                         ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpSession httpSession = httpRequest.getSession();
		UserContext userContext = null;
		
		Object val = httpRequest.getAttribute(WebConstants.INIT_REQ_UNIQUE_ID);
		
		//the request will not have the value if this is the initial request
		boolean initialRequest = (val == null);
		
		if (log.isDebugEnabled()) {
			log.debug("initial Request? " + initialRequest);
			log.debug("requestURI " + httpRequest.getRequestURI());
			log.debug("requestURL " + httpRequest.getRequestURL());
			log.debug("request path info " + httpRequest.getPathInfo());
		}
		
		//set/forward the request init attribute
		if (initialRequest)
			httpRequest.setAttribute(WebConstants.INIT_REQ_UNIQUE_ID, String.valueOf(new Date().getTime()));
		
		//context = (Context)httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		//context = (Context)httpRequest.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		if (initialRequest) {
			// default the session username attribute to anonymous
			httpSession.setAttribute("username", "-anonymous user-");
			
			// User context is created if it doesn't already exist and added to the session
			// note: this usercontext storage logic is copied to webinf/view/uncaughtexception.jsp to 
			// 		 prevent stack traces being shown to non-authenticated users
			userContext = (UserContext) httpSession.getAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR);
			
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
				User user;
				if ((user = userContext.getAuthenticatedUser()) != null)
					httpSession.setAttribute("username", user.getUsername());
			}
			
			// set the locale on the session (for the servlet container as well)
			httpSession.setAttribute("locale", userContext.getLocale());
			
			// Add the user context to the current thread 
			Context.setUserContext(userContext);
		}
		
		log.debug("before doFilter");
		
		// continue the filter chain (going on to spring, authorization, etc)
		try {
			chain.doFilter(request, response);
		}
		finally {
			if (initialRequest) {
				// Clear the context so there's no user information left on the thread
				Context.clearUserContext();
				log.debug("This was considered an initial request");
			}
		}
		
		// TODO why are we setting the userContext here again?
		//httpSession.setAttribute(WebConstants.OPENMRS_USER_CONTEXT_HTTPSESSION_ATTR, userContext);
		
		log.debug("after doFilter");
		
	}
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Initializating filter");
	}
	
	/**
	 * Get the application context.
	 * 
	 * @param httpRequest
	 * @return
	 */
	public ApplicationContext getApplicationContext(HttpServletRequest httpRequest) {
		ServletContext servletContext = httpRequest.getSession().getServletContext();
		return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
	}
	
}
