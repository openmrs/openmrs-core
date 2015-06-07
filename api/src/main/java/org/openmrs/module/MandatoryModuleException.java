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
public class MandatoryModuleException extends ModuleMustStartException {
	
	public static final long serialVersionUID = 236472655L;
	
	private String moduleId;
	
	/**
	 * This constructor is used when a user tries to stop a mandatory module.
	 * 
	 * @param moduleId the module id that is trying to be stopped
	 */
	public MandatoryModuleException(String moduleId) {
		this(moduleId, "");
	}
	
	/**
	 * This constructor is used when a user tries to stop a mandatory module.
	 * 
	 * @param moduleId the module id that is trying to be stopped
	 * @param extraErrorMessage extra data to provide in the error's message
	 */
	public MandatoryModuleException(String moduleId, String extraErrorMessage) {
		super("The " + moduleId + " module is marked as 'mandatory' and so cannot be stopped or unloaded. "
		        + extraErrorMessage);
		this.moduleId = moduleId;
	}
	
	/**
	 * @param moduleIds list of module ids that are mandatory and didn't start
	 */
	public MandatoryModuleException(List<String> moduleIds) {
		super("The following modules are marked as 'mandatory' but were unable to start: "
		        + OpenmrsUtil.join(moduleIds, ","));
		this.moduleId = OpenmrsUtil.join(moduleIds, ",");
	}
	
	/**
	 * The id (or ids) of the module that is mandatory
	 * 
	 * @return the module id (or ids) that caused this exception
	 */
	public String getModuleId() {
		return moduleId;
	}
}
