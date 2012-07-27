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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.web.WebConstants;

/**
 * The {@link InitializationFilter} uses this model object to hold all properties that are edited by
 * the user in the wizard. All attributes on this model object are added to all templates rendered
 * by the {@link InitializationFilter}.
 */
public class InitializationWizardModel {
	
	// automatically given to the .vm files and used there
	public static final String headerTemplate = "org/openmrs/web/filter/initialization/header.vm";
	
	// automatically given to the .vm files and used there
	public static final String footerTemplate = "org/openmrs/web/filter/initialization/footer.vm";
	
	/**
	 * Records completed tasks and are displayed at the top of the page upon error
	 */
	public List<String> workLog = new ArrayList<String>();
	
	/**
	 * Whether the runtime properties file could possible be created. (only read by the velocity
	 * scripts)
	 */
	
	public boolean canCreate = true;
	
	/**
	 * Error message from not being able to create the runtime properties file (only read by the
	 * velocity scripts)
	 */
	
	public String cannotCreateErrorMessage = "";
	
	/**
	 * Whether the runtime file can be edited (only read by the velocity scripts)
	 */
	
	public boolean canWrite = true;
	
	/**
	 * The location of the runtime properties file (only read by the velocity scripts)
	 */
	
	public String runtimePropertiesPath = "";
	
	/**
	 * True/false marker for the question "Do you currently have an OpenMRS database installed"
	 */
	public Boolean hasCurrentOpenmrsDatabase = true;
	
	/**
	 * True/false marker for the
	 * question"Do you currently have a database user other than root that has read/write access"
	 */
	public Boolean hasCurrentDatabaseUser = true;
	
	/**
	 * Filled out by the user on the databasesetup.vm page
	 */
	public String databaseName = "openmrs";
	
	/**
	 * Filled out by user on the databasesetup.vm page Looks like:
	 */
	public String databaseConnection = "jdbc:mysql://localhost:3306/@DBNAME@?autoReconnect=true&sessionVariables=storage_engine=InnoDB&useUnicode=true&characterEncoding=UTF-8";
	
	/**
	 * Filled in on databasesetup.vm
	 */
	public String createDatabaseUsername = "root";
	
	/**
	 * Filled in on databasesetup.vm
	 */
	public String createDatabasePassword = "";
	
	/**
	 * DB user that can create an openmrs db user Filled in on databasetablesanduser.vm
	 */
	public String createUserUsername = "root";
	
	/**
	 * DB user that can create an openmrs db user Filled in on databasetablesanduser.vm
	 */
	public String createUserPassword = "";
	
	/**
	 * The username of a user that exists that can read/write to openmrs. Entered on
	 * databasetablesanduser page
	 */
	public String currentDatabaseUsername = "";
	
	/**
	 * The password of a user that exists that can read/write to openmrs. Entered on
	 * databasetablesanduser page
	 */
	public String currentDatabasePassword = "";
	
	/**
	 * Asked for on the databasetablesanduser.vm page to know if their existing database has the
	 * tables or not
	 */
	public Boolean createTables = Boolean.FALSE;
	
	/**
	 * if the user asked us to create the user for openmrs
	 */
	public Boolean createDatabaseUser = Boolean.FALSE;
	
	/**
	 * Does the user want to add the demo data to the database?
	 */
	public Boolean addDemoData = Boolean.FALSE;
	
	/**
	 * Asked for on the otherproperties.vm page to know if the allow_web_admin runtime property is
	 * true/false
	 */
	public Boolean moduleWebAdmin = Boolean.TRUE;
	
	/**
	 * Asked for on otherproperties.vm page to know if the runtime property for auto updating their
	 * db is true/false
	 */
	public Boolean autoUpdateDatabase = Boolean.FALSE;
	
	/**
	 * Password for the admin user if the database was created now
	 */
	public String adminUserPassword = "";
	
	/**
	 * Implementation name.
	 */
	public String implementationIdName = "";
	
	/**
	 * Implementation ID.
	 */
	public String implementationId = "";
	
	/**
	 * Pass phrase used to validate who uses your implementation ID.
	 */
	public String implementationIdPassPhrase = "";
	
	/**
	 * Text describing the implementation.
	 */
	public String implementationIdDescription = "";
	
	public String setupPageUrl = WebConstants.SETUP_PAGE_URL;
	
	/**
	 * The tasks to be executed that the user selected from the wizard's prompts
	 */
	public List<WizardTask> tasksToExecute;
}
