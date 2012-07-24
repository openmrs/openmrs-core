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
package org.openmrs.module;

import java.util.Map;

import org.openmrs.api.context.Context;

/**
 * This error is thrown when OpenMRS is started but there is a module that is core to OpenMRS and
 * that module is not present or did not start <br/>
 * <br/>
 * This error is also thrown when trying to stop a module that is marked as core.
 * 
 * @see Context#startup()
 * @see ModuleUtil#startup(java.util.Properties)
 * @see ModuleConstants#CORE_MODULES
 */
public class OpenmrsCoreModuleException extends ModuleMustStartException {
	
	public static final long serialVersionUID = 1L;
	
	private Map<String, String> modules;
	
	/**
	 * This constructor is used when a user tries to stop a core module.
	 * 
	 * @param moduleId the module id that is trying to be stopped
	 */
	public OpenmrsCoreModuleException(String moduleId) {
		super("The " + moduleId + " module is set as 'core' by OpenMRS and so cannot be stopped or unloaded.");
	}
	
	/**
	 * @param moduleIds map from module id to version that is core and didn't start
	 */
	public OpenmrsCoreModuleException(Map<String, String> modules) {
		super(createMessage(modules));
		
		// set the moduleIds property for use by the StartupErrorFilter
		this.modules = modules;
	}
	
	/**
	 * Helper method to turn the given modules map into a message string so that it can be passed to
	 * a super constructor
	 * 
	 * @param modules map from module id to the version that is required
	 * @return the string to display to the user
	 */
	private static String createMessage(Map<String, String> modules) {
		String msg = "The following modules are marked as 'core' by OpenMRS but were unable to start: ";
		for (Map.Entry<String, String> entry : modules.entrySet()) {
			msg += entry.getKey() + " v" + entry.getValue() + ", ";
		}
		
		return msg;
	}
	
	/**
	 * The module ids that caused this exception
	 * 
	 * @return the module ids that caused this exception
	 */
	public Map<String, String> getModules() {
		return modules;
	}
}
