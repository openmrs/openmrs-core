/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import javax.servlet.ServletException;

import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * This class is only used to get access to the dispatcher servlet that handles static content. <br>
 * <br>
 * After creation, this object is saved to WebModuleUtil for later use. When Spring's root 
 * webApplicationContext is refreshed, this dispatcher servlet needs to be refreshed too.
 */
public class StaticDispatcherServlet extends org.springframework.web.servlet.DispatcherServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(StaticDispatcherServlet.class);
	
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
