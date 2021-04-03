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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class captures all of the information needed to create and initialize a Filter included in a
 * Module. This object is initialized from an xml element that has the following syntax. Expected
 * XML Format:
 * 
 * <pre>
 * 	&lt;filter&gt;
 * 		&lt;filter-name&gt;MyFilterName&lt;/filter-name&gt;
 * 		&lt;filter-class&gt;Fully qualified classname of the Filter class&lt;/filter-class&gt;
 * 		&lt;init-param&gt;
 * 			&lt;param-name&gt;filterParameterName1&lt;/param-name&gt;
 * 			&lt;param-value&gt;filterParameterValue1&lt;/param-value&gt;
 * 		&lt;/init-param&gt;
 * 	&lt;/filter&gt;
 * </pre>
 */
public class ModuleFilterDefinition implements Serializable {
	
	public static final long serialVersionUID = 1;
	
	private static final Logger log = LoggerFactory.getLogger(ModuleFilterDefinition.class);
	
	// Properties
	private Module module;
	
	private String filterName;
	
	private String filterClass;
	
	private Map<String, String> initParameters = new HashMap<>();
	
	/**
	 * Default constructor, requires a Module
	 * 
	 * @param module - The Module to use to construct this {@link ModuleFilterDefinition}
	 */
	public ModuleFilterDefinition(Module module) {
		this.module = module;
	}
	
	/**
	 * @return - The {@link Module} that registered this FilterDefinition
	 */
	public Module getModule() {
		return module;
	}
	
	/**
	 * @param module the {@link Module} to set
	 */
	public void setModule(Module module) {
		this.module = module;
	}
	
	/**
	 * @return - the name of the Filter
	 */
	public String getFilterName() {
		return filterName;
	}
	
	/**
	 * @param filterName the name of the filter
	 */
	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}
	
	/**
	 * @return - the class name of the filter
	 */
	public String getFilterClass() {
		return filterClass;
	}
	
	/**
	 * @param filterClass the class name of the filter
	 */
	public void setFilterClass(String filterClass) {
		this.filterClass = filterClass;
	}
	
	/**
	 * @return - A map of parameters to use to initialize the filter
	 */
	public Map<String, String> getInitParameters() {
		return initParameters;
	}
	
	/**
	 * #param - A map of parameters to use to initialize the filter
	 */
	public void setInitParameters(Map<String, String> initParameters) {
		this.initParameters = initParameters;
	}
	
	/**
	 * Adds a Parameter that should be passed in to initialize this Filter
	 * 
	 * @param parameterName - The name of the parameter
	 * @param parameterValue - The value of the parameter
	 */
	public void addInitParameter(String parameterName, String parameterValue) {
		this.initParameters.put(parameterName, parameterValue);
	}
	
	// Static methods
	
	/**
	 * Static method to parse through a Module's configuration file and return a List of
	 * ModuleFilterDefinition objects for which there are configuration elements. Expected XML
	 * Format:
	 * 
	 * <pre>
	 * 	&lt;filter&gt;
	 * 		&lt;filter-name&gt;MyFilterName&lt;/filter-name&gt;
	 * 		&lt;filter-class&gt;Fully qualified classname of the Filter class&lt;/filter-class&gt;
	 * 		&lt;init-param&gt;
	 * 			&lt;param-name&gt;filterParameterName1&lt;/param-name&gt;
	 * 			&lt;param-value&gt;filterParameterValue1&lt;/param-value&gt;
	 * 		&lt;/init-param&gt;
	 * 	&lt;/filter&gt;
	 * </pre>
	 * 
	 * @param module - The {@link Module} for which to retrieve filter the defined
	 *            {@link ModuleFilterDefinition}s
	 * @return List of {@link ModuleFilterDefinition}s that have been defined for the passed
	 *         {@link Module}
	 */
	public static List<ModuleFilterDefinition> retrieveFilterDefinitions(Module module)  {
		
		List<ModuleFilterDefinition> filters = new ArrayList<>();
		
		try {
			Element rootNode = module.getConfig().getDocumentElement();
			NodeList filterNodes = rootNode.getElementsByTagName("filter");
			if (filterNodes.getLength() > 0) {
				for (int i = 0; i < filterNodes.getLength(); i++) {
					ModuleFilterDefinition filter = new ModuleFilterDefinition(module);
					Node node = filterNodes.item(i);
					NodeList configNodes = node.getChildNodes();
					for (int j = 0; j < configNodes.getLength(); j++) {
						Node configNode = configNodes.item(j);
						if ("filter-name".equals(configNode.getNodeName())) {
							filter.setFilterName(configNode.getTextContent().trim());
						} else if ("filter-class".equals(configNode.getNodeName())) {
							filter.setFilterClass(configNode.getTextContent().trim());
						} else if ("init-param".equals(configNode.getNodeName())) {
							NodeList paramNodes = configNode.getChildNodes();
							String paramName = "";
							String paramValue = "";
							for (int k = 0; k < paramNodes.getLength(); k++) {
								Node paramNode = paramNodes.item(k);
								if ("param-name".equals(paramNode.getNodeName())) {
									paramName = paramNode.getTextContent().trim();
								} else if ("param-value".equals(paramNode.getNodeName())) {
									paramValue = paramNode.getTextContent().trim();
								}
							}
							filter.addInitParameter(paramName, paramValue);
						}
					}
					filters.add(filter);
				}
			}
		}
		catch (Exception e) {
			throw new ModuleException("Unable to parse filters in module configuration.", e);
		}
		log.debug("Retrieved " + filters.size() + " filters for " + module + ": " + filters);
		return filters;
	}
}
