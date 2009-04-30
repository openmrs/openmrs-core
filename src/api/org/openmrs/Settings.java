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
package org.openmrs;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;

/**
 *
 */
public class Settings {

	private List<GlobalProperty> globalProperties;
	
	private Collection<Module> modules;
	
    /**
     * 
     */
    public Settings() {
    	globalProperties = Context.getAdministrationService().getAllGlobalProperties();
    	modules = ModuleFactory.getLoadedModules();
    }

	/**
     * @return the globalProperties
     */
    public List<GlobalProperty> getGlobalProperties() {
    	return globalProperties;
    }
	
    /**
     * @return the modules
     */
    public Collection<Module> getModules() {
    	return modules;
    }
	
}
