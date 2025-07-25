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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.openmrs.module.web.WebModuleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter provides a mechanism for modules to plug-in their own custom filters. It is started
 * automatically, and will iterate through all filters that have been added through Modules.
 */
public class ModuleFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(ModuleFilter.class);
	
	/**
	 * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Initializating ModuleFilter");
	}
	
	/**
	 * @see jakarta.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		log.debug("Destroying the ModuleFilter");
	}
	
	/**
	 * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest,
	 *      jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	        ServletException {
		ModuleFilterChain moduleChain = ModuleFilterChain.getInstance(WebModuleUtil.getFiltersForRequest(request), chain);
		moduleChain.doFilter(request, response);
	}
	
}
