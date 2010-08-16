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
package org.openmrs.web.filter.initialization;

/**
 * The different tasks that the wizard could be executing at a given time during the initialization
 * process.
 */
public enum WizardTask {
	
	CREATE_SCHEMA("Create database"), CREATE_DB_USER("Create database user"),

	CREATE_TABLES("Create OpenMRS tables"), ADD_CORE_DATA("Add OpenMRS core data"),

	ADD_DEMO_DATA("Add OpenMRS demo data"), UPDATE_TO_LATEST("Update the database");
	
	private final String displayText;
	
	/**
	 * Constructor
	 * 
	 * @param displayText The displayText for the enum value
	 */
	private WizardTask(String displayText) {
		this.displayText = displayText;
	}
	
	/**
	 * Returns the displayText to be printed in the IU
	 * 
	 * @return The displayText for the enum value
	 */
	public String displayText() {
		return this.displayText;
	}
}
