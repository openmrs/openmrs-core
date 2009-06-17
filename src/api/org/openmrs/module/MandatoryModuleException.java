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

import java.util.List;

import org.openmrs.util.OpenmrsUtil;

/**
 * This error is thrown when OpenMRS is started but a module marked as 'mandatory' but was unable to
 * start. <br/>
 * <br/>
 * This error is also thrown when trying to stop a module that is marked as mandatory.
 * 
 * @see Context#startup()
 * @see ModuleUtil#getMandatoryModules()
 */
public class MandatoryModuleException extends RuntimeException {
	
	public static final long serialVersionUID = 236472655L;
	
	/**
	 * This constructor is used when a user tries to stop a mandatory module.
	 * 
	 * @param moduleId the module id that is trying to be stopped
	 */
	public MandatoryModuleException(String moduleId) {
		super("The " + moduleId + " module is marked as 'mandatory' and so cannot be stopped or unloaded.");
	}
	
	/**
	 * @param moduleIds list of module ids that are mandatory and didn't start
	 */
	public MandatoryModuleException(List<String> moduleIds) {
		super("The following modules are marked as 'mandatory' but were unable to start: "
		        + OpenmrsUtil.join(moduleIds, ","));
	}
}
