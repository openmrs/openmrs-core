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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.openmrs.util.OpenmrsClassLoader;

/**
 * Simple filter class to set the OpenMRS class loader as the context class loader of the current
 * thread so that JSPs can use EL functions defined in modules
 */
public class JspClassLoaderFilter implements Filter {
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
	}
	
	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
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
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
	}
}
