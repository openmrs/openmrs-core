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

/**
 * Event published after a module load attempt completes.
 * <p>
 * This event additionally carries {@link #getModuleFileName()}, the filename of uploaded module.
 * It is the only lifecycle event that originates from a file, so the field
 * lives here rather than on the shared base class.
 * </p>
 *
 * @since 2.7.10
 */
public class ModuleLoadEvent extends AbstractModuleEvent {
	
	private final String moduleFileName;
	
	public ModuleLoadEvent(Object source, String moduleId, String moduleName, String moduleVersion, String moduleFileName,
	    boolean isSuccess, String failureReason) {
		super(source, moduleId, moduleName, moduleVersion, isSuccess, failureReason);
		this.moduleFileName = moduleFileName;
	}
	
	public String getModuleFileName() {
		return moduleFileName;
	}
}
