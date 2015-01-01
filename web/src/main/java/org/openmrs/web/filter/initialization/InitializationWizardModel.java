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
	
	// Values for installMethod field.
	public static final String INSTALL_METHOD_SIMPLE = "simple";
	
	public static final String INSTALL_METHOD_ADVANCED = "advanced";
	
	public static final String INSTALL_METHOD_TESTING = "testing";
	
	public static final String INSTALL_METHOD_AUTO = "auto";
	
	// Default OpenMRS admin password set by the simple installation.
	public static final String ADMIN_DEFAULT_PASSWORD = "Admin123";
	
	/**
	 * Default database name to use unless user specifies another in the wizard or they are creating
	 * a test installation
	 */
	public static final String DEFAULT_DATABASE_NAME = WebConstants.WEBAPP_NAME;
	
	/**
	 * Records completed tasks and are displayed at the top of the page upon error
	 */
	private List<String> workLog = new ArrayList<String>();
	
	/**
	 * Whether the runtime properties file could possible be created. (only read by the velocity
	 * scripts)
	 */
	
	private boolean canCreate = true;
	
	/**
	 * Error message from not being able to create the runtime properties file (only read by the
	 * velocity scripts)
	 */
	
	private String cannotCreateErrorMessage = "";
	
	/**
	 * Whether the runtime file can be edited (only read by the velocity scripts)
	 */
	
	private boolean canWrite = true;
	
	/**
	 * The location of the runtime properties file (only read by the velocity scripts)
	 */
	
	private String runtimePropertiesPath = "";
	
	private String installMethod = INSTALL_METHOD_SIMPLE;
	
	/**
	 * True/false marker for the question "Do you currently have an OpenMRS database installed"
	 */
	private Boolean hasCurrentOpenmrsDatabase = true;
	
	/**
	 * True/false marker for the
	 * question"Do you currently have a database user other than root that has read/write access"
	 */
	private Boolean hasCurrentDatabaseUser = true;
	
	/**
	 * Filled out by the user on the databasesetup.vm page
	 */
	private String databaseName = DEFAULT_DATABASE_NAME;
	
	/**
	 * Filled out by user on the databasesetup.vm page Looks like:
	 */
	private String databaseConnection = "jdbc:mysql://localhost:3306/@DBNAME@?autoReconnect=true&sessionVariables=storage_engine=InnoDB&useUnicode=true&characterEncoding=UTF-8";
	
	/**
	 * Optional Database Driver string filled in on databasesetup.vm
	 */
	private String databaseDriver = "";
	
	/**
	 * MySQL root account password used for simple installation. Filled in simplesetup.vm.
	 */
	private String databaseRootPassword = "";
	
	/**
	 * Filled in on databasesetup.vm
	 */
	private String createDatabaseUsername = "root";
	
	/**
	 * Filled in on databasesetup.vm
	 */
	private String createDatabasePassword = "";
	
	/**
	 * DB user that can create an openmrs db user Filled in on databasetablesanduser.vm
	 */
	private String createUserUsername = "root";
	
	/**
	 * DB user that can create an openmrs db user Filled in on databasetablesanduser.vm
	 */
	private String createUserPassword = "";
	
	/**
	 * The username of a user that exists that can read/write to openmrs. Entered on
	 * databasetablesanduser page
	 */
	private String currentDatabaseUsername = "";
	
	/**
	 * The password of a user that exists that can read/write to openmrs. Entered on
	 * databasetablesanduser page
	 */
	private String currentDatabasePassword = "";
	
	/**
	 * Asked for on the databasetablesanduser.vm page to know if their existing database has the
	 * tables or not
	 */
	private Boolean createTables = Boolean.FALSE;
	
	/**
	 * if the user asked us to create the user for openmrs
	 */
	private Boolean createDatabaseUser = Boolean.FALSE;
	
	/**
	 * Enables importing test data from the remote server
	 */
	private Boolean importTestData = Boolean.FALSE;
	
	/**
	 * Does the user want to add the demo data to the database?
	 */
	private Boolean addDemoData = Boolean.FALSE;
	
	/**
	 * Asked for on the otherproperties.vm page to know if the allow_web_admin runtime property is
	 * true/false
	 */
	private Boolean moduleWebAdmin = Boolean.TRUE;
	
	/**
	 * Asked for on otherproperties.vm page to know if the runtime property for auto updating their
	 * db is true/false
	 */
	private Boolean autoUpdateDatabase = Boolean.FALSE;
	
	/**
	 * Password for the admin user if the database was created now
	 */
	private String adminUserPassword = ADMIN_DEFAULT_PASSWORD;
	
	/**
	 * Implementation name.
	 */
	private String implementationIdName = "";
	
	/**
	 * Implementation ID.
	 */
	private String implementationId = "";
	
	/**
	 * Pass phrase used to validate who uses your implementation ID.
	 */
	private String implementationIdPassPhrase = "";
	
	/**
	 * Text describing the implementation.
	 */
	private String implementationIdDescription = "";
	
	private String setupPageUrl = WebConstants.SETUP_PAGE_URL;
	
	/**
	 * The tasks to be executed that the user selected from the wizard's prompts
	 */
	private List<WizardTask> tasksToExecute;
	
	private String localeToSave = "";
	
	/**
	 * The url to the remote system
	 */
	private String remoteUrl = "";
	
	/**
	 * The username to use to authenticate to the remote system
	 */
	private String remoteUsername = "";
	
	/**
	 * The password to use to authenticate to the remote system
	 */
	private String remotePassword = "";
	
	/**
	 * The current step. e.g Step 1 of ...
	 */
	private Integer currentStepNumber = 1;
	
	/**
	 * The total number of steps. e.g Step ... of 5
	 */
	private Integer numberOfSteps = 1;
}
