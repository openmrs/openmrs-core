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
package org.openmrs.module.web;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;

public class ModuleServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1239820102030303L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("In service method for module servlet: " + request.getPathInfo());
		String servletName = request.getPathInfo();
		int end = servletName.indexOf("/", 1);
		
		String moduleId = null;
		if (end > 0)
			moduleId = servletName.substring(1, end);
		
		log.debug("ModuleId: " + moduleId);
		Module mod = ModuleFactory.getModuleById(moduleId);
		
		int start = 1; // where in the path to start trimming
		if (mod != null) {
			log.debug("Module with id " + moduleId + " found.  Looking for servlet name after " + moduleId + " in url path");
			start = moduleId.length() + 2;
			// this skips over the moduleId that is in the path
		}
		
		end = servletName.indexOf("/", start);
		if (end == -1 || end > servletName.length())
			end = servletName.length();
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
		
		public String getServletName() {
			return name;
		}
		
		public ServletContext getServletContext() {
			return servletContext;
		}
		
		// not implemented in a module's config.xml yet
		public String getInitParameter(String paramName) {
			return null;
		}
		
		// not implemented in a module's config.xml yet
		@SuppressWarnings("unchecked")
        public Enumeration getInitParameterNames() {
			return null;
		}
	}
}
