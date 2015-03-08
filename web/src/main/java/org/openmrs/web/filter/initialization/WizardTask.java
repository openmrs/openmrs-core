/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.initialization;

/**
 * The different tasks that the wizard could be executing at a given time during the initialization
 * process.
 */
public enum WizardTask {
	
	CREATE_SCHEMA("install.progress.tasks.create.schema"),
	CREATE_DB_USER("install.progress.tasks.create.user"),
	
	CREATE_TABLES("install.progress.tasks.create.tables"),
	ADD_CORE_DATA("install.progress.tasks.add.coreData"),
	
	ADD_DEMO_DATA("install.progress.tasks.add.demoData"),
	UPDATE_TO_LATEST("install.progress.tasks.update"),
	
	IMPORT_TEST_DATA("install.progress.tasks.test"),
	ADD_MODULES("install.progress.tasks.addModules");
	
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
