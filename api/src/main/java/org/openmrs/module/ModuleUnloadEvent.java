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
 * Event published after a module unload attempt completes.
 *
 * @since 2.7.10
 */
public class ModuleUnloadEvent extends AbstractModuleEvent {
	
	private String moduleName;
	
	private String moduleVersion;
	
	public ModuleUnloadEvent(Object source, String moduleId, String moduleName, String moduleVersion) {
		super(source, moduleId);
		this.moduleName = moduleName;
		this.moduleVersion = moduleVersion;
	}
	
	public String getModuleName() {
		return moduleName;
	}
	
	public String getModuleVersion() {
		return moduleVersion;
	}
}
