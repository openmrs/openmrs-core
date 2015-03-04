/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web.filter;

import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * This class is an implementation of FilterConfig for use in instantiating Filters from Modules
 */
public class ModuleFilterConfig implements FilterConfig {
	
	// Properties
	private ModuleFilterDefinition filterDefinition;
	
	private ServletContext servletContext;
	
	/**
	 * Private constructor which sets all required properties
	 * 
	 * @param filterDefinition The ModuleFilterDefinition to store in this ModuleFilterConfig
	 * @param servletContext The {@link ServletContext} to store in this ModuleFilterConfig
	 */
	private ModuleFilterConfig(ModuleFilterDefinition filterDefinition, ServletContext servletContext) {
		this.filterDefinition = filterDefinition;
		this.servletContext = servletContext;
	}
	
	/**
	 * Factory method to construct and return a ModuleFilterConfig
	 * 
	 * @param filterDefinition The ModuleFilterDefinition to store in this ModuleFilterConfig
	 * @param servletContext The {@link ServletContext} to store in this ModuleFilterConfig
	 * @return The ModuleFilterConfig that is fully initialized with the passed parameters
	 */
	public static ModuleFilterConfig getInstance(ModuleFilterDefinition filterDefinition, ServletContext servletContext) {
		return new ModuleFilterConfig(filterDefinition, servletContext);
	}
	
	/**
	 * @see javax.servlet.FilterConfig#getFilterName()
	 */
	public String getFilterName() {
		return filterDefinition.getFilterName();
	}
	
	/**
	 * @see javax.servlet.FilterConfig#getInitParameter(java.lang.String)
	 */
	public String getInitParameter(String paramName) {
		return filterDefinition.getInitParameters().get(paramName);
	}
	
	/**
	 * @see javax.servlet.FilterConfig#getInitParameterNames()
	 */
	public Enumeration<String> getInitParameterNames() {
		Vector<String> v = new Vector<String>(filterDefinition.getInitParameters().keySet());
		return v.elements();
	}
	
	//******************
	// Property access
	//******************
	
	public ModuleFilterDefinition getFilterDefinition() {
		return filterDefinition;
	}
	
	public void setFilterDefinition(ModuleFilterDefinition filterDefinition) {
		this.filterDefinition = filterDefinition;
	}
	
	public ServletContext getServletContext() {
		return servletContext;
	}
	
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
}
