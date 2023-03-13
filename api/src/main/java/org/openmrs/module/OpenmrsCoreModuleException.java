/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import java.util.Map;

import org.openmrs.api.context.Context;

/**
 * This error is thrown when OpenMRS is started but there is a module that is core to OpenMRS and
 * that module is not present or did not start <br>
 * <br>
 * This error is also thrown when trying to stop a module that is marked as core.
 * 
 * @see Context#startup(java.util.Properties)
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
	 * @param modules map from module id to version that is core and didn't start
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
		StringBuilder msg = new StringBuilder(
		        "The following modules are marked as 'core' by OpenMRS but were unable to start: ");
		for (Map.Entry<String, String> entry : modules.entrySet()) {
			msg.append(entry.getKey());
			msg.append(" v");
			msg.append(entry.getValue());
			msg.append(", ");
		}
		
		return msg.toString();
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
