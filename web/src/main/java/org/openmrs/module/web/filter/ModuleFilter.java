/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
