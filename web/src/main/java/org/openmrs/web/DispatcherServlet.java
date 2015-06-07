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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.util.DatabaseUpdater;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.web.filter.initialization.InitializationFilter;
import org.openmrs.web.filter.update.UpdateFilter;
import org.springframework.beans.BeansException;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * This class is only used to get access to the DispatcherServlet. <br/>
 * <br/>
 * After creation, this object is saved to WebUtil for later use. When Spring's
 * webApplicationContext is refreshed, the DispatcherServlet needs to be refreshed too.
 * 
 * @see #reInitFrameworkServlet()
 */
public class DispatcherServlet extends org.springframework.web.servlet.DispatcherServlet {
	
	private static final long serialVersionUID = -6925172744402818729L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see org.springframework.web.servlet.FrameworkServlet#initFrameworkServlet()
	 */
	@Override
	protected void initFrameworkServlet() throws ServletException, BeansException {
		// refresh the application context to look for module xml config files as well
		
		//XmlWebApplicationContext wac = ((XmlWebApplicationContext)getWebApplicationContext());
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		//wac.refresh();
		
		log.debug("Framework being initialized");
		WebModuleUtil.setDispatcherServlet(this);
		
		super.initFrameworkServlet();
	}
	
	/**
	 * Called by the ModuleUtil after adding in a new module. This needs to be called because the
	 * new mappings and advice that a new module adds in are cached by Spring's DispatcherServlet.
	 * This method will reload that cache.
	 * 
	 * @throws ServletException
	 */
	public void reInitFrameworkServlet() throws ServletException {
		log.debug("Framework being REinitialized");
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		((XmlWebApplicationContext) getWebApplicationContext()).setClassLoader(OpenmrsClassLoader.getInstance());
		
		refresh();
		
		// the spring context gets reset by the framework servlet, so we need to 
		// reload the advice points that were lost when refreshing Spring
		for (Module module : ModuleFactory.getStartedModules()) {
			ModuleFactory.loadAdvice(module);
		}
	}
	
	/**
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		// hacky way to know if one of the startup filters needs to be run
		if (UpdateFilter.updatesRequired() && !DatabaseUpdater.allowAutoUpdate()) {
			log.info("DB updates are required, the update wizard must be run");
		}
		if (InitializationFilter.initializationRequired()) {
			log.info("Runtime properties were not found or the database is empty, so initialization is required");
		}
	}
	
	/**
	 * Stops and closes the application context created by this dispatcher servlet.
	 */
	public void stopAndCloseApplicationContext() {
		try {
			XmlWebApplicationContext ctx = (XmlWebApplicationContext) getWebApplicationContext();
			ctx.stop();
			ctx.close();
		}
		catch (Exception e) {
			log.error("Exception while stopping and closing dispatcherServlet context: ", e);
		}
	}
}
