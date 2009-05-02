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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;

/**
 *
 */
public class SettingsDTO {

	/** Logger for this class */
	protected final Log log = LogFactory.getLog(getClass());

	private Collection<Module> modules;

	private List<GlobalProperty> systemSettings;
	
	private List<GlobalProperty> allSettings;
	
    /**
     * 
     */
    public SettingsDTO() {
    	allSettings = new ArrayList<GlobalProperty>();
    	systemSettings = new ArrayList<GlobalProperty>();
    	for (GlobalProperty property : Context.getAdministrationService().getAllGlobalProperties()) {
    		allSettings.add(property);
    		systemSettings.add(property);
    	}
    	
    	modules = ModuleFactory.getLoadedModules();
    	
    	log.debug("System Settings count before: " + systemSettings.size());
    	for (Module module : modules) {
    		log.debug("Removing " + module.getName() + "properties. Count: " + module.getGlobalProperties().size());
    		systemSettings.removeAll(module.getGlobalProperties());
    		if (!allSettings.containsAll(module.getGlobalProperties())) {
    			allSettings.addAll(module.getGlobalProperties());
    		}
        	log.debug("System Settings count: " + systemSettings.size());
    	}
    	
    	Collections.sort(allSettings, new Comparator<GlobalProperty>() {
    		public int compare(GlobalProperty p1, GlobalProperty p2){
    			return p1.getProperty().compareTo(p2.getProperty());
    		}
    	});
    }
    
    /**
     * @return the allSettings
     */
    public List<GlobalProperty> getAllSettings() {
    	return allSettings;
    }

	/**
     * @return the globalProperties
     */
    public List<GlobalProperty> getSystemSettings() {
    	return systemSettings;
    }
	
    /**
     * @return the modules
     */
    public Collection<Module> getModules() {
    	return modules;
    }
	
}
