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
	
	private ModuleEventType eventType;
	
	private String moduleName;
	
	private boolean isSuccess;
	
	public ModuleActionEvent(Object source, ModuleEventType eventType, String moduleName, boolean isSuccess) {
		super(source);
		this.eventType = eventType;
		this.moduleName = moduleName;
		this.isSuccess = isSuccess;
	}
	
	public ModuleEventType getActionType() {
		return eventType;
	}
	
	public String getModuleName() {
		return moduleName;
	}
	
	public boolean isSuccess() {
		return isSuccess;
	}
	
}
