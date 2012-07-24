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
