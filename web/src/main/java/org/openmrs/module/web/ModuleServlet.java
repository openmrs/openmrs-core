/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1239820102030303L;
	
	private static final Logger log = LoggerFactory.getLogger(ModuleServlet.class);
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("In service method for module servlet: " + request.getPathInfo());
		String servletName = request.getPathInfo();
		int end = servletName.indexOf("/", 1);
		
		String moduleId = null;
		if (end > 0) {
			moduleId = servletName.substring(1, end);
		}
		
		log.debug("ModuleId: " + moduleId);
		Module mod = ModuleFactory.getModuleById(moduleId);
		 
		// where in the path to start trimming
		int start = 1;
		if (mod != null) {
			log.debug("Module with id " + moduleId + " found.  Looking for servlet name after " + moduleId + " in url path");
			start = moduleId.length() + 2;
			// this skips over the moduleId that is in the path
		}
		
		end = servletName.indexOf("/", start);
		if (end == -1 || end > servletName.length()) {
			end = servletName.length();
		}
		servletName = servletName.substring(start, end);
		
		log.debug("Servlet name: " + servletName);
		
		HttpServlet servlet = WebModuleUtil.getServlet(servletName);
		
		if (servlet == null) {
			log.warn("No servlet with name: " + servletName + " was found");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		servlet.service(request, response);
	}
	
	/**
	 * Internal implementation of the ServletConfig interface, to be passed to module servlets when
	 * they are first loaded
	 */
	public static class SimpleServletConfig implements ServletConfig {
		
		private String name;
		
		private ServletContext servletContext;
		
		public SimpleServletConfig(String name, ServletContext servletContext) {
			this.name = name;
			this.servletContext = servletContext;
		}
		
		@Override
		public String getServletName() {
			return name;
		}
		
		@Override
		public ServletContext getServletContext() {
			return servletContext;
		}
		
		// not implemented in a module's config.xml yet
		@Override
		public String getInitParameter(String paramName) {
			return null;
		}
		
		// not implemented in a module's config.xml yet
		@Override
		@SuppressWarnings("unchecked")
		public Enumeration getInitParameterNames() {
			return null;
		}
	}
}
