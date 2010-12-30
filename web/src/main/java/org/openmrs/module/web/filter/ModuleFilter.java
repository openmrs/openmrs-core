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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.web.WebModuleUtil;

/**
 * This filter provides a mechanism for modules to plug-in their own custom filters. It is started
 * automatically, and will iterate through all filters that have been added through Modules.
 */
public class ModuleFilter implements Filter {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Initializating ModuleFilter");
	}
	
	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		log.debug("Destroying the ModuleFilter");
	}
	
	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	                                                                                         ServletException {
		ModuleFilterChain moduleChain = ModuleFilterChain.getInstance(WebModuleUtil.getFiltersForRequest(request), chain);
		moduleChain.doFilter(request, response);
	}
	
}
