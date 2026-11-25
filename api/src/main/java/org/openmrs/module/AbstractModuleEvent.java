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
 * Base {@link ApplicationEvent} for module lifecycle actions executed by {@link ModuleFactory}.
 * Subclasses represent concrete lifecycle actions such as load, start, stop, and unload.
 * <p>
 * The payload carries the ModuleId to get recognize later by it. 
 * It will use by listener to get the module metadata. 
 * </p>
 * 
 * @since 2.7.10
 */
public abstract class AbstractModuleEvent extends ApplicationEvent {
	
	private final String moduleId;
	
	protected AbstractModuleEvent(Object source, String moduleId) {
		super(source);
		this.moduleId = moduleId;
	}
	
	public String getModuleId() {
		return moduleId;
	}
}
