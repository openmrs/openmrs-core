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
package org.openmrs.web.dwr;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.directwebremoting.servlet.DwrServlet;
import org.openmrs.module.web.WebModuleUtil;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * Simply used so that we have a way we can restart the DWR HttpServlet
 */
public class OpenmrsDWRServlet extends DwrServlet {
	
	private static final long serialVersionUID = 121212111335789L;
	
	/**
	 * Overriding the init(ServletConfig) method to save the dwr servlet to the ModuleWebUtil class
	 */
	public void init(ServletConfig config) throws ServletException {
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		super.init(config);
		WebModuleUtil.setDWRServlet(this);
	}
	
	/**
	 * This method is called to remake all of the dwr methods
	 * 
	 * @throws ServletException
	 */
	public void reInitServlet() throws ServletException {
		init(this.getServletConfig());
	}
	
}
