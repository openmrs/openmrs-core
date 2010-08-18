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

/**
 * Enum used to mark a Module with pending action
 */
public enum ModuleAction {
	PENDING_START("started"), PENDING_STOP("stopped"), PENDING_UPGRADE("upgraded"), PENDING_UNLOAD("unloaded"), PENDING_NONE("none");
	
	/*
	 * action type attribute
	 */
	private String action;
	
	private ModuleAction(String action) {
		this.action = action;
	}
	
	public String getAction() {
		return action;
	}
}
