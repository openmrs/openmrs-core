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

import java.lang.reflect.Field;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jasper.servlet.JspServlet;

/**
 * This class is only used to get access to the JspServlet. <br/>
 * <br/>
 * When Spring's root webApplicationContext is refreshed, this servlet needs to be refreshed too
 * because it has references (via the objects it creates like JspRuntimeContext, etc) to the openmrs
 * class loader that is being destroyed.
 */
public class OpenmrsJspServlet extends JspServlet {
	
	private static final long serialVersionUID = 1L;
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public static OpenmrsJspServlet jspServlet;
	
	public static void setJspServlet(OpenmrsJspServlet jspServlet) {
		OpenmrsJspServlet.jspServlet = jspServlet;
	}
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		setJspServlet(this);
	}
	
	public void stop() {
		try {
			this.destroy();
		}
		catch (Exception ex) {
			log.error(ex);
		}
	}
	
	public void refresh() {
		try {
			//Get a reference to the private ServletConfig config such that we
			//can initialize the servlet again.
			Field field = this.getClass().getSuperclass().getDeclaredField("config");
			field.setAccessible(true);
			init((ServletConfig) field.get(this));
		}
		catch (Exception ex) {
			log.error(ex);
		}
	}
}
