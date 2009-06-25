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
package org.openmrs.web.controller.maintenance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;

/**
 *
 */
public class GlobalPropertyDTO {

	/** Logger for this class */
	protected final Log log = LogFactory.getLog(getClass());

	private List<Module> modules;

	private List<GlobalProperty> systemProperties;
	
	private List<GlobalProperty> allProperties;

	private List<GlobalProperty> modulesProperties;
	
    /**
     * 
     */
    public GlobalPropertyDTO() {
    	
    	allProperties = Context.getAdministrationService().getAllGlobalProperties();
    	systemProperties = new ArrayList<GlobalProperty>();
    	modulesProperties = new ArrayList<GlobalProperty>();

    	for (GlobalProperty property : Context.getAdministrationService().getAllGlobalProperties()) {
    		systemProperties.add(property);
    	}
    	
    	log.debug("System Properties initial count: " + systemProperties.size());
    	
    	modules = new ArrayList<Module>(ModuleFactory.getStartedModules());
    	
    	Collections.sort(modules, new Comparator<Module>() {
            public int compare(Module m1, Module m2) {
	            return m1.getName().compareTo(m2.getName());
            }
    	});

    	for (Module module : modules) {

    		systemProperties.removeAll(module.getGlobalProperties());
    		modulesProperties.addAll(module.getGlobalProperties());
    		
    		log.debug(module.getGlobalProperties().size()  + " properties from module "  + module.getName() + " was removed from System Properties");
        	log.debug("System Properties count: " + systemProperties.size());
    	}
    }
    
    /**
     * @return the allProperties
     */
    public List<GlobalProperty> getAllProperties() {
    	return allProperties;
    }

	/**
     * @return the globalProperties
     */
    public List<GlobalProperty> getSystemProperties() {
    	return systemProperties;
    }
    
    /**
     * Return namespaces of given global properties
     * 
     * @param properties - the List of global properties. Each property in OpenMRS has the folowing format <namespace>.<name>
     * @return List of namespace. 
     */
    public List<Namespace> getPropertiesNamespaces(List<GlobalProperty> properties) {
    	List<Namespace> result = new ArrayList<Namespace>();
    	List<String> namespaceNames = new ArrayList<String>();
    	for (GlobalProperty property : properties) {
    		String[] splitedProperty = property.getProperty().split("\\.");
    		if (splitedProperty.length == 2) {
        		if (!namespaceNames.contains(splitedProperty[0])) {
        			namespaceNames.add(splitedProperty[0]);
        			result.add(new Namespace(splitedProperty[0]));
        		}
    		}
    	}
    	return result;
    }
    
    public List<Namespace> getSystemNamespaces() {
	    return getPropertiesNamespaces(systemProperties);
    }
	
    /**
     * @return the modulesProperties
     */
    public List<GlobalProperty> getModulesProperties() {
    	return modulesProperties;
    }

	/**
     * @return the modules
     */
    public List<Module> getModules() {
    	return modules;
    }
    
}
