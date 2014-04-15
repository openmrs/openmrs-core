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
package org.openmrs.web;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.BeansException;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * This class is only used to get access to the dispatcher servlet that handles static content. <br/>
 * <br/>
 * After creation, this object is saved to WebModuleUtil for later use. When Spring's root 
 * webApplicationContext is refreshed, this dispatcher servlet needs to be refreshed too.
 * 
 * @see #reInitFrameworkServlet()
 */
public class StaticDispatcherServlet extends org.springframework.web.servlet.DispatcherServlet {
	
	private static final long serialVersionUID = 1L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see org.springframework.web.servlet.FrameworkServlet#initFrameworkServlet()
	 */
	@Override
	protected void initFrameworkServlet() throws ServletException, BeansException {
		
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		
		log.info("Framework being initialized for static content");
		WebModuleUtil.setStaticDispatcherServlet(this);
		
		super.initFrameworkServlet();
	}
	
	/**
	 * Called by the ModuleUtil after adding in a new, updating, starting, or stopping a module.
	 * This needs to be called because each spring dispatcher servlet creates a new application
	 * context, which therefore needs to be refreshed too.
	 * 
	 * @throws ServletException
	 */
	public void refreshApplicationContext() throws ServletException {
		log.info("Application context for the static content dispatcher servlet is being refreshed");
		
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		((XmlWebApplicationContext) getWebApplicationContext()).setClassLoader(OpenmrsClassLoader.getInstance());
		
		refresh();
	}
	
	public void stopAndCloseApplicationContext() {
		try {
			XmlWebApplicationContext ctx = (XmlWebApplicationContext) getWebApplicationContext();
			ctx.stop();
			ctx.close();
		}
		catch (Exception e) {
			log.error("Exception while stopping and closing static content dispatcher servlet context: ", e);
		}
	}
}
