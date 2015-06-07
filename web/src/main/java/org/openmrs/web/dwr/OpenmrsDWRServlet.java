/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
