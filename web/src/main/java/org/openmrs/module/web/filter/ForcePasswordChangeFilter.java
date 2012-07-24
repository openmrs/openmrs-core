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
package org.openmrs.module.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.web.user.UserProperties;

/**
 * This filter checks if an authenticated user has been flagged by the admin to change his password
 * on first/subsequent login. It will intercept any requests made to a *.html or a *.form to force
 * the user to change his password.
 */
public class ForcePasswordChangeFilter implements Filter {
	
	private String excludeURL;
	
	private String changePasswordForm;
	
	private FilterConfig config;
	
	private String[] excludedURLs;
	
	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}
	
	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	        ServletException {
		String requestURI = ((HttpServletRequest) request).getRequestURI();
		
		if (Context.isAuthenticated()
		        && new UserProperties(Context.getAuthenticatedUser().getUserProperties()).isSupposedToChangePassword()
		        && shouldNotAllowAccessToUrl(requestURI)) {
			config.getServletContext().getRequestDispatcher(changePasswordForm).forward(request, response);
		} else {
			chain.doFilter(request, response);
		}
	}
	
	/**
	 * Method to check if the request url is an excluded url.
	 * 
	 * @param requestURI
	 * @param excludeURL
	 * @return
	 */
	private boolean shouldNotAllowAccessToUrl(String requestURI) {
		
		for (String url : excludedURLs) {
			if (requestURI.endsWith(url)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
		excludeURL = config.getInitParameter("excludeURL");
		excludedURLs = excludeURL.split(",");
		changePasswordForm = config.getInitParameter("changePasswordForm");
	}
	
}
