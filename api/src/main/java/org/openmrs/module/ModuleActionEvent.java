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

import org.springframework.context.ApplicationEvent;

/**
 * A POJO representing a module action event which
 * carries data for module-related events so they can be published across the app.
 * 
 * @since 2.7.10
 */
public class ModuleActionEvent extends ApplicationEvent {
	
	private final ModuleEventType eventType;
	
	private final String moduleId;
	
	private final String moduleName;
	
	private final String moduleVersion;
	
	private final boolean isSuccess;
	
	private final String failureReason;
	
	public ModuleActionEvent(Object source, ModuleEventType eventType, String moduleId, String moduleName,
	    String moduleVersion, boolean isSuccess, String failureReason) {
		super(source);
		this.eventType = eventType;
		this.moduleId = moduleId;
		this.moduleName = moduleName;
		this.moduleVersion = moduleVersion;
		this.isSuccess = isSuccess;
		this.failureReason = failureReason;
	}
	
	public ModuleEventType getEventType() {
		return eventType;
	}
	
	public String getModuleId() {
		return moduleId;
	}
	
	public String getModuleName() {
		return moduleName;
	}
	
	public String getModuleVersion() {
		return moduleVersion;
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}
	
	public String getFailureReason() {
		return failureReason;
	}
	
}
