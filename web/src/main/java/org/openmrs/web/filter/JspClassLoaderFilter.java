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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.openmrs.util.OpenmrsClassLoader;

/**
 * Simple filter class to set the OpenMRS class loader as the context class loader of the current
 * thread so that JSPs can use EL functions defined in modules
 */
public class JspClassLoaderFilter implements Filter {
	
	/**
	 * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
	}
	
	/**
	 * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest,
	 *      jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	        ServletException {
		// Set thread's class loader
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		
		// Carry on up the chain
		chain.doFilter(request, response);
	}
	
	/**
	 * @see jakarta.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
	}
}
