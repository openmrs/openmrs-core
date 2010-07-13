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
package org.openmrs.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.openmrs.api.ConceptService;
import org.openmrs.module.ModuleConstants;
import org.openmrs.module.ModuleFactory;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.scheduler.SchedulerConstants;

/**
 * Constants used in OpenMRS. Contents built from build properties (version, version_short, and
 * expected_database). Some are set at runtime (database, database version). This file should
 * contain all privilege names and global property names. Those strings added to the static CORE_*
 * methods will be written to the database at startup if they don't exist yet.
 */
public final class OpenmrsConstants {
	
	//private static Log log = LogFactory.getLog(OpenmrsConstants.class);
	
	/**
	 * This is the hard coded primary key of the order type for DRUG. This has to be done because
	 * some logic in the API acts on this order type
	 */
	public static final int ORDERTYPE_DRUG = 2;
	
	/**
	 * This is the hard coded primary key of the concept class for DRUG. This has to be done because
	 * some logic in the API acts on this concept class
	 */
	public static final int CONCEPT_CLASS_DRUG = 3;
	
	/**
	 * hack alert: During an ant build, the openmrs api jar manifest file is loaded with these
	 * values. When constructing the OpenmrsConstants class file, the api jar is read and the values
	 * are copied in as constants
	 */
	private static final Package THIS_PACKAGE = OpenmrsConstants.class.getPackage();
	
	/**
	 * This holds the current openmrs code version. This version is a string containing spaces and
	 * words.<br/>
	 * The format is:<br/>
	 * <i>major</i>.<i>minor</i>.<i>maintenance</i> <i>suffix</i> Build <i>buildNumber</i>
	 */
	public static final String OPENMRS_VERSION = THIS_PACKAGE.getSpecificationVendor();
	
	/**
	 * This holds the current openmrs code version in a short space-less string.<br/>
	 * The format is:<br/>
	 * <i>major</i>.<i>minor</i>.<i>maintenance</i>.<i>revision</i>-<i>suffix</i>
	 */
	public static final String OPENMRS_VERSION_SHORT = THIS_PACKAGE.getSpecificationVersion();
	
	/**
	 * See {@link DatabaseUpdater#updatesRequired()} to see what changesets in the
	 * liquibase-update-to-latest.xml file in the openmrs api jar file need to be run to bring the
	 * db up to date with what the api requires.
	 * 
	 * @deprecated the database doesn't have just one main version now that we are using liquibase.
	 */
	@Deprecated
	public static final String DATABASE_VERSION_EXPECTED = THIS_PACKAGE.getImplementationVersion();
	
	public static String DATABASE_NAME = "openmrs";
	
	public static String DATABASE_BUSINESS_NAME = "openmrs";
	
	/**
	 * See {@link DatabaseUpdater#updatesRequired()} to see what changesets in the
	 * liquibase-update-to-latest.xml file in the openmrs api jar file need to be run to bring the
	 * db up to date with what the api requires.
	 * 
	 * @deprecated the database doesn't have just one main version now that we are using liquibase.
	 */
	@Deprecated
	public static String DATABASE_VERSION = null;
	
	/**
	 * Set true from runtime configuration to obscure patients for system demonstrations
	 */
	public static boolean OBSCURE_PATIENTS = false;
	
	public static String OBSCURE_PATIENTS_GIVEN_NAME = "Demo";
	
	public static String OBSCURE_PATIENTS_MIDDLE_NAME = null;
	
	public static String OBSCURE_PATIENTS_FAMILY_NAME = "Person";
	
	public static final String REGEX_LARGE = "[!\"#\\$%&'\\(\\)\\*,+-\\./:;<=>\\?@\\[\\\\\\\\\\]^_`{\\|}~]";
	
	public static final String REGEX_SMALL = "[!\"#\\$%&'\\(\\)\\*,\\./:;<=>\\?@\\[\\\\\\\\\\]^_`{\\|}~]";
	
	public static final Integer CIVIL_STATUS_CONCEPT_ID = 1054;
	
	/**
	 * The directory that will store filesystem data about openmrs like module omods, generated data
	 * exports, etc. This shouldn't be accessed directory, the
	 * OpenmrsUtil.getApplicationDataDirectory() should be used. This should be null here. This
	 * constant will hold the value of the user's runtime property for the
	 * application_data_directory and is set programmatically at startup. This value is set in the
	 * openmrs startup method If this is null, the getApplicationDataDirectory() uses some OS
	 * heuristics to determine where to put an app data dir.
	 * 
	 * @see #APPLICATION_DATA_DIRECTORY_RUNTIME_PROPERTY
	 * @see OpenmrsUtil#getApplicationDataDirectory()
	 * @see OpenmrsUtil#startup(java.util.Properties)
	 */
	public static String APPLICATION_DATA_DIRECTORY = null;
	
	/**
	 * The name of the runtime property that a user can set that will specify where openmrs's
	 * application directory is
	 * 
	 * @see #APPLICATION_DATA_DIRECTORY
	 */
	public static String APPLICATION_DATA_DIRECTORY_RUNTIME_PROPERTY = "application_data_directory";
	
	/**
	 * The name of the runtime property that a user can set that will specify whether the database
	 * is automatically updated on startup
	 */
	public static String AUTO_UPDATE_DATABASE_RUNTIME_PROPERTY = "auto_update_database";
	
	/**
	 * These words are ignored in concept and patient searches
	 * 
	 * @return Collection<String> of words that are ignored
	 */
	public static final Collection<String> STOP_WORDS() {
		List<String> stopWords = new Vector<String>();
		stopWords.add("A");
		stopWords.add("AND");
		stopWords.add("AT");
		stopWords.add("BUT");
		stopWords.add("BY");
		stopWords.add("FOR");
		stopWords.add("HAS");
		stopWords.add("OF");
		stopWords.add("THE");
		stopWords.add("TO");
		
		return stopWords;
	}
	
	/**
	 * A gender character to gender name map<br/>
	 * TODO issues with localization. How should this be handled?
	 * 
	 * @return Map<String, String> of gender character to gender name
	 */
	public static final Map<String, String> GENDER() {
		Map<String, String> genders = new LinkedHashMap<String, String>();
		genders.put("M", "Male");
		genders.put("F", "Female");
		return genders;
	}
	
	// Baked in Privileges:
	
	public static final String PRIV_VIEW_CONCEPTS = "View Concepts";
	
	public static final String PRIV_MANAGE_CONCEPTS = "Manage Concepts";
	
	public static final String PRIV_PURGE_CONCEPTS = "Purge Concepts";
	
	public static final String PRIV_MANAGE_CONCEPT_NAME_TAGS = "Manage Concept Name tags";
	
	public static final String PRIV_VIEW_CONCEPT_PROPOSALS = "View Concept Proposals";
	
	public static final String PRIV_ADD_CONCEPT_PROPOSALS = "Add Concept Proposals";
	
	public static final String PRIV_EDIT_CONCEPT_PROPOSALS = "Edit Concept Proposals";
	
	public static final String PRIV_DELETE_CONCEPT_PROPOSALS = "Delete Concept Proposals";
	
	public static final String PRIV_PURGE_CONCEPT_PROPOSALS = "Purge Concept Proposals";
	
	public static final String PRIV_VIEW_USERS = "View Users";
	
	public static final String PRIV_ADD_USERS = "Add Users";
	
	public static final String PRIV_EDIT_USERS = "Edit Users";
	
	public static final String PRIV_DELETE_USERS = "Delete Users";
	
	public static final String PRIV_PURGE_USERS = "Purge Users";
	
	public static final String PRIV_EDIT_USER_PASSWORDS = "Edit User Passwords";
	
	public static final String PRIV_VIEW_ENCOUNTERS = "View Encounters";
	
	public static final String PRIV_ADD_ENCOUNTERS = "Add Encounters";
	
	public static final String PRIV_EDIT_ENCOUNTERS = "Edit Encounters";
	
	public static final String PRIV_DELETE_ENCOUNTERS = "Delete Encounters";
	
	public static final String PRIV_PURGE_ENCOUNTERS = "Purge Encounters";
	
	public static final String PRIV_VIEW_ENCOUNTER_TYPES = "View Encounter Types";
	
	public static final String PRIV_MANAGE_ENCOUNTER_TYPES = "Manage Encounter Types";
	
	public static final String PRIV_PURGE_ENCOUNTER_TYPES = "Purge Encounter Types";
	
	public static final String PRIV_VIEW_LOCATIONS = "View Locations";
	
	public static final String PRIV_MANAGE_LOCATIONS = "Manage Locations";
	
	public static final String PRIV_PURGE_LOCATIONS = "Purge Locations";
	
	public static final String PRIV_MANAGE_LOCATION_TAGS = "Manage Location Tags";
	
	public static final String PRIV_PURGE_LOCATION_TAGS = "Purge Location Tags";
	
	public static final String PRIV_VIEW_OBS = "View Observations";
	
	public static final String PRIV_ADD_OBS = "Add Observations";
	
	public static final String PRIV_EDIT_OBS = "Edit Observations";
	
	public static final String PRIV_DELETE_OBS = "Delete Observations";
	
	public static final String PRIV_PURGE_OBS = "Purge Observations";
	
	@Deprecated
	public static final String PRIV_VIEW_MIME_TYPES = "View Mime Types";
	
	@Deprecated
	public static final String PRIV_PURGE_MIME_TYPES = "Purge Mime Types";
	
	public static final String PRIV_VIEW_PATIENTS = "View Patients";
	
	public static final String PRIV_ADD_PATIENTS = "Add Patients";
	
	public static final String PRIV_EDIT_PATIENTS = "Edit Patients";
	
	public static final String PRIV_DELETE_PATIENTS = "Delete Patients";
	
	public static final String PRIV_PURGE_PATIENTS = "Purge Patients";
	
	public static final String PRIV_VIEW_PATIENT_IDENTIFIERS = "View Patient Identifiers";
	
	public static final String PRIV_ADD_PATIENT_IDENTIFIERS = "Add Patient Identifiers";
	
	public static final String PRIV_EDIT_PATIENT_IDENTIFIERS = "Edit Patient Identifiers";
	
	public static final String PRIV_DELETE_PATIENT_IDENTIFIERS = "Delete Patient Identifiers";
	
	public static final String PRIV_PURGE_PATIENT_IDENTIFIERS = "Purge Patient Identifiers";
	
	public static final String PRIV_VIEW_PATIENT_COHORTS = "View Patient Cohorts";
	
	public static final String PRIV_ADD_COHORTS = "Add Cohorts";
	
	public static final String PRIV_EDIT_COHORTS = "Edit Cohorts";
	
	public static final String PRIV_DELETE_COHORTS = "Delete Cohorts";
	
	public static final String PRIV_PURGE_COHORTS = "Purge Cohorts";
	
	public static final String PRIV_VIEW_ORDERS = "View Orders";
	
	public static final String PRIV_ADD_ORDERS = "Add Orders";
	
	public static final String PRIV_EDIT_ORDERS = "Edit Orders";
	
	public static final String PRIV_DELETE_ORDERS = "Delete Orders";
	
	public static final String PRIV_PURGE_ORDERS = "Purge Orders";
	
	public static final String PRIV_VIEW_FORMS = "View Forms";
	
	public static final String PRIV_MANAGE_FORMS = "Manage Forms";
	
	public static final String PRIV_PURGE_FORMS = "Purge Forms";
	
	// This name is historic, since that's what it was originally called in the infopath formentry module
	public static final String PRIV_FORM_ENTRY = "Form Entry";
	
	@Deprecated
	public static final String PRIV_VIEW_REPORTS = "View Reports";
	
	@Deprecated
	public static final String PRIV_ADD_REPORTS = "Add Reports";
	
	@Deprecated
	public static final String PRIV_EDIT_REPORTS = "Edit Reports";
	
	@Deprecated
	public static final String PRIV_DELETE_REPORTS = "Delete Reports";
	
	@Deprecated
	public static final String PRIV_RUN_REPORTS = "Run Reports";
	
	@Deprecated
	public static final String PRIV_VIEW_REPORT_OBJECTS = "View Report Objects";
	
	@Deprecated
	public static final String PRIV_ADD_REPORT_OBJECTS = "Add Report Objects";
	
	@Deprecated
	public static final String PRIV_EDIT_REPORT_OBJECTS = "Edit Report Objects";
	
	@Deprecated
	public static final String PRIV_DELETE_REPORT_OBJECTS = "Delete Report Objects";
	
	public static final String PRIV_MANAGE_IDENTIFIER_TYPES = "Manage Identifier Types";
	
	public static final String PRIV_VIEW_IDENTIFIER_TYPES = "View Identifier Types";
	
	public static final String PRIV_PURGE_IDENTIFIER_TYPES = "Purge Identifier Types";
	
	@Deprecated
	public static final String PRIV_MANAGE_MIME_TYPES = "Manage Mime Types";
	
	public static final String PRIV_VIEW_CONCEPT_CLASSES = "View Concept Classes";
	
	public static final String PRIV_MANAGE_CONCEPT_CLASSES = "Manage Concept Classes";
	
	public static final String PRIV_PURGE_CONCEPT_CLASSES = "Purge Concept Classes";
	
	public static final String PRIV_VIEW_CONCEPT_DATATYPES = "View Concept Datatypes";
	
	public static final String PRIV_MANAGE_CONCEPT_DATATYPES = "Manage Concept Datatypes";
	
	public static final String PRIV_PURGE_CONCEPT_DATATYPES = "Purge Concept Datatypes";
	
	public static final String PRIV_VIEW_PRIVILEGES = "View Privileges";
	
	public static final String PRIV_MANAGE_PRIVILEGES = "Manage Privileges";
	
	public static final String PRIV_PURGE_PRIVILEGES = "Purge Privileges";
	
	public static final String PRIV_VIEW_ROLES = "View Roles";
	
	public static final String PRIV_MANAGE_ROLES = "Manage Roles";
	
	public static final String PRIV_PURGE_ROLES = "Purge Roles";
	
	public static final String PRIV_VIEW_FIELD_TYPES = "View Field Types";
	
	public static final String PRIV_MANAGE_FIELD_TYPES = "Manage Field Types";
	
	public static final String PRIV_PURGE_FIELD_TYPES = "Purge Field Types";
	
	public static final String PRIV_VIEW_ORDER_TYPES = "View Order Types";
	
	public static final String PRIV_MANAGE_ORDER_TYPES = "Manage Order Types";
	
	public static final String PRIV_PURGE_ORDER_TYPES = "Purge Order Types";
	
	public static final String PRIV_VIEW_RELATIONSHIP_TYPES = "View Relationship Types";
	
	public static final String PRIV_MANAGE_RELATIONSHIP_TYPES = "Manage Relationship Types";
	
	public static final String PRIV_PURGE_RELATIONSHIP_TYPES = "Purge Relationship Types";
	
	public static final String PRIV_MANAGE_ALERTS = "Manage Alerts";
	
	public static final String PRIV_MANAGE_CONCEPT_SOURCES = "Manage Concept Sources";
	
	public static final String PRIV_VIEW_CONCEPT_SOURCES = "View Concept Sources";
	
	public static final String PRIV_PURGE_CONCEPT_SOURCES = "Purge Concept Sources";
	
	public static final String PRIV_VIEW_NAVIGATION_MENU = "View Navigation Menu";
	
	public static final String PRIV_VIEW_ADMIN_FUNCTIONS = "View Administration Functions";
	
	public static final String PRIV_VIEW_UNPUBLISHED_FORMS = "View Unpublished Forms";
	
	public static final String PRIV_VIEW_PROGRAMS = "View Programs";
	
	public static final String PRIV_MANAGE_PROGRAMS = "Manage Programs";
	
	public static final String PRIV_VIEW_PATIENT_PROGRAMS = "View Patient Programs";
	
	public static final String PRIV_ADD_PATIENT_PROGRAMS = "Add Patient Programs";
	
	public static final String PRIV_EDIT_PATIENT_PROGRAMS = "Edit Patient Programs";
	
	public static final String PRIV_DELETE_PATIENT_PROGRAMS = "Delete Patient Programs";
	
	public static final String PRIV_PURGE_PATIENT_PROGRAMS = "Add Patient Programs";
	
	public static final String PRIV_DASHBOARD_OVERVIEW = "Patient Dashboard - View Overview Section";
	
	public static final String PRIV_DASHBOARD_REGIMEN = "Patient Dashboard - View Regimen Section";
	
	public static final String PRIV_DASHBOARD_ENCOUNTERS = "Patient Dashboard - View Encounters Section";
	
	public static final String PRIV_DASHBOARD_DEMOGRAPHICS = "Patient Dashboard - View Demographics Section";
	
	public static final String PRIV_DASHBOARD_GRAPHS = "Patient Dashboard - View Graphs Section";
	
	public static final String PRIV_DASHBOARD_FORMS = "Patient Dashboard - View Forms Section";
	
	public static final String PRIV_DASHBOARD_SUMMARY = "Patient Dashboard - View Patient Summary";
	
	public static final String PRIV_VIEW_GLOBAL_PROPERTIES = "View Global Properties";
	
	public static final String PRIV_MANAGE_GLOBAL_PROPERTIES = "Manage Global Properties";
	
	public static final String PRIV_PURGE_GLOBAL_PROPERTIES = "Purge Global Properties";
	
	public static final String PRIV_MANAGE_MODULES = "Manage Modules";
	
	public static final String PRIV_MANAGE_SCHEDULER = "Manage Scheduler";
	
	public static final String PRIV_VIEW_PERSON_ATTRIBUTE_TYPES = "View Person Attribute Types";
	
	public static final String PRIV_MANAGE_PERSON_ATTRIBUTE_TYPES = "Manage Person Attribute Types";
	
	public static final String PRIV_PURGE_PERSON_ATTRIBUTE_TYPES = "Purge Person Attribute Types";
	
	public static final String PRIV_VIEW_PERSONS = "View People";
	
	public static final String PRIV_ADD_PERSONS = "Add People";
	
	public static final String PRIV_EDIT_PERSONS = "Edit People";
	
	public static final String PRIV_DELETE_PERSONS = "Delete People";
	
	public static final String PRIV_PURGE_PERSONS = "Purge People";
	
	/**
	 * @deprecated replacing with ADD/EDIT/DELETE privileges
	 */
	public static final String PRIV_MANAGE_RELATIONSHIPS = "Manage Relationships";
	
	public static final String PRIV_VIEW_RELATIONSHIPS = "View Relationships";
	
	public static final String PRIV_ADD_RELATIONSHIPS = "Add Relationships";
	
	public static final String PRIV_EDIT_RELATIONSHIPS = "Edit Relationships";
	
	public static final String PRIV_DELETE_RELATIONSHIPS = "Delete Relationships";
	
	public static final String PRIV_PURGE_RELATIONSHIPS = "Purge Relationships";
	
	public static final String PRIV_VIEW_DATABASE_CHANGES = "View Database Changes";
	
	public static final String PRIV_MANAGE_IMPLEMENTATION_ID = "Manage Implementation Id";
	
	public static final String PRIV_SQL_LEVEL_ACCESS = "SQL Level Access";
	
	public static final String PRIV_VIEW_PROBLEMS = "View Problems";
	
	public static final String PRIV_ADD_PROBLEMS = "Add Problems";
	
	public static final String PRIV_EDIT_PROBLEMS = "Edit Problems";
	
	public static final String PRIV_DELETE_PROBLEMS = "Remove Problems";

	public static final String PRIV_VIEW_ALLERGIES = "View Allergies";
	
	public static final String PRIV_ADD_ALLERGIES = "Add Allergies";
	
	public static final String PRIV_EDIT_ALLERGIES = "Edit Allergies";
	
	public static final String PRIV_DELETE_ALLERGIES = "Remove Allergies";

	/**
	 * Cached list of core privileges
	 */
	private static Map<String, String> CORE_PRIVILEGES = null;
	
	/**
	 * These are the privileges that are required by OpenMRS. Upon startup, if any of these
	 * privileges do not exist in the database, they are inserted. These privileges are not allowed
	 * to be deleted. They are marked as 'locked' in the administration screens.
	 * 
	 * @return privileges core to the system
	 */
	public static final Map<String, String> CORE_PRIVILEGES() {
		
		// if we don't have a cache, create one
		if (CORE_PRIVILEGES == null) {
			CORE_PRIVILEGES = new HashMap<String, String>();
			
			CORE_PRIVILEGES.put(PRIV_VIEW_PROGRAMS, "Able to view patient programs");
			CORE_PRIVILEGES.put(PRIV_MANAGE_PROGRAMS, "Able to add/view/delete patient programs");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_PATIENT_PROGRAMS, "Able to see which programs that patients are in");
			CORE_PRIVILEGES.put(PRIV_ADD_PATIENT_PROGRAMS, "Able to add patients to programs");
			CORE_PRIVILEGES.put(PRIV_EDIT_PATIENT_PROGRAMS, "Able to edit patients in programs");
			CORE_PRIVILEGES.put(PRIV_DELETE_PATIENT_PROGRAMS, "Able to delete patients from programs");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_UNPUBLISHED_FORMS, "Able to view and fill out unpublished forms");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_CONCEPTS, "Able to view concept entries");
			CORE_PRIVILEGES.put(PRIV_MANAGE_CONCEPTS, "Able to add/edit/delete concept entries");
			
			CORE_PRIVILEGES.put(PRIV_MANAGE_CONCEPT_NAME_TAGS, "Able to add/edit/delete concept name tags");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_CONCEPT_PROPOSALS, "Able to view concept proposals to the system");
			CORE_PRIVILEGES.put(PRIV_ADD_CONCEPT_PROPOSALS, "Able to add concept proposals to the system");
			CORE_PRIVILEGES.put(PRIV_EDIT_CONCEPT_PROPOSALS, "Able to edit concept proposals in the system");
			CORE_PRIVILEGES.put(PRIV_DELETE_CONCEPT_PROPOSALS, "Able to delete concept proposals from the system");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_USERS, "Able to view users in OpenMRS");
			CORE_PRIVILEGES.put(PRIV_ADD_USERS, "Able to add users to OpenMRS");
			CORE_PRIVILEGES.put(PRIV_EDIT_USERS, "Able to edit users in OpenMRS");
			CORE_PRIVILEGES.put(PRIV_DELETE_USERS, "Able to delete users in OpenMRS");
			CORE_PRIVILEGES.put(PRIV_EDIT_USER_PASSWORDS, "Able to change the passwords of users in OpenMRS");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_ENCOUNTERS, "Able to view patient encounters");
			CORE_PRIVILEGES.put(PRIV_ADD_ENCOUNTERS, "Able to add patient encounters");
			CORE_PRIVILEGES.put(PRIV_EDIT_ENCOUNTERS, "Able to edit patient encounters");
			CORE_PRIVILEGES.put(PRIV_DELETE_ENCOUNTERS, "Able to delete patient encounters");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_OBS, "Able to view patient observations");
			CORE_PRIVILEGES.put(PRIV_ADD_OBS, "Able to add patient observations");
			CORE_PRIVILEGES.put(PRIV_EDIT_OBS, "Able to edit patient observations");
			CORE_PRIVILEGES.put(PRIV_DELETE_OBS, "Able to delete patient observations");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_PATIENTS, "Able to view patients");
			CORE_PRIVILEGES.put(PRIV_ADD_PATIENTS, "Able to add patients");
			CORE_PRIVILEGES.put(PRIV_EDIT_PATIENTS, "Able to edit patients");
			CORE_PRIVILEGES.put(PRIV_DELETE_PATIENTS, "Able to delete patients");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_PATIENT_IDENTIFIERS, "Able to view patient identifiers");
			CORE_PRIVILEGES.put(PRIV_ADD_PATIENT_IDENTIFIERS, "Able to add patient identifiers");
			CORE_PRIVILEGES.put(PRIV_EDIT_PATIENT_IDENTIFIERS, "Able to edit patient identifiers");
			CORE_PRIVILEGES.put(PRIV_DELETE_PATIENT_IDENTIFIERS, "Able to delete patient identifiers");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_PATIENT_COHORTS, "Able to view patient cohorts");
			CORE_PRIVILEGES.put(PRIV_ADD_COHORTS, "Able to add a cohort to the system");
			CORE_PRIVILEGES.put(PRIV_EDIT_COHORTS, "Able to add a cohort to the system");
			CORE_PRIVILEGES.put(PRIV_DELETE_COHORTS, "Able to add a cohort to the system");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_ORDERS, "Able to view orders");
			CORE_PRIVILEGES.put(PRIV_ADD_ORDERS, "Able to add orders");
			CORE_PRIVILEGES.put(PRIV_EDIT_ORDERS, "Able to edit orders");
			CORE_PRIVILEGES.put(PRIV_DELETE_ORDERS, "Able to delete orders");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_FORMS, "Able to view forms");
			CORE_PRIVILEGES.put(PRIV_MANAGE_FORMS, "Able to add/edit/delete forms");
			CORE_PRIVILEGES.put(PRIV_FORM_ENTRY, "Able to fill out forms");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_REPORTS, "Able to view reports");
			CORE_PRIVILEGES.put(PRIV_ADD_REPORTS, "Able to add reports");
			CORE_PRIVILEGES.put(PRIV_EDIT_REPORTS, "Able to edit reports");
			CORE_PRIVILEGES.put(PRIV_DELETE_REPORTS, "Able to delete reports");
			CORE_PRIVILEGES.put(PRIV_RUN_REPORTS, "Able to run reports");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_REPORT_OBJECTS, "Able to view report objects");
			CORE_PRIVILEGES.put(PRIV_ADD_REPORT_OBJECTS, "Able to add report objects");
			CORE_PRIVILEGES.put(PRIV_EDIT_REPORT_OBJECTS, "Able to edit report objects");
			CORE_PRIVILEGES.put(PRIV_DELETE_REPORT_OBJECTS, "Able to delete report objects");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_IDENTIFIER_TYPES, "Able to view patient identifier types");
			CORE_PRIVILEGES.put(PRIV_MANAGE_RELATIONSHIPS, "Able to add/edit/delete relationships");
			CORE_PRIVILEGES.put(PRIV_MANAGE_IDENTIFIER_TYPES, "Able to add/edit/retire patient identifier types");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_LOCATIONS, "Able to view locations");
			CORE_PRIVILEGES.put(PRIV_MANAGE_LOCATIONS, "Able to add/edit/delete locations");
			CORE_PRIVILEGES.put(PRIV_MANAGE_LOCATION_TAGS, "Able to add/edit/delete location tags");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_CONCEPT_CLASSES, "Able to view concept classes");
			CORE_PRIVILEGES.put(PRIV_MANAGE_CONCEPT_CLASSES, "Able to add/edit/retire concept classes");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_CONCEPT_DATATYPES, "Able to view concept datatypes");
			CORE_PRIVILEGES.put(PRIV_MANAGE_CONCEPT_DATATYPES, "Able to add/edit/retire concept datatypes");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_ENCOUNTER_TYPES, "Able to view encounter types");
			CORE_PRIVILEGES.put(PRIV_MANAGE_ENCOUNTER_TYPES, "Able to add/edit/retire encounter types");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_PRIVILEGES, "Able to view user privileges");
			CORE_PRIVILEGES.put(PRIV_MANAGE_PRIVILEGES, "Able to add/edit/delete privileges");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_FIELD_TYPES, "Able to view field types");
			CORE_PRIVILEGES.put(PRIV_MANAGE_FIELD_TYPES, "Able to add/edit/retire field types");
			CORE_PRIVILEGES.put(PRIV_PURGE_FIELD_TYPES, "Able to purge field types");
			
			CORE_PRIVILEGES.put(PRIV_MANAGE_ORDER_TYPES, "Able to add/edit/retire order types");
			CORE_PRIVILEGES.put(PRIV_VIEW_ORDER_TYPES, "Able to view order types");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_RELATIONSHIP_TYPES, "Able to view relationship types");
			CORE_PRIVILEGES.put(PRIV_MANAGE_RELATIONSHIP_TYPES, "Able to add/edit/retire relationship types");
			
			CORE_PRIVILEGES.put(PRIV_MANAGE_ALERTS, "Able to add/edit/delete user alerts");
			
			CORE_PRIVILEGES.put(PRIV_MANAGE_CONCEPT_SOURCES, "Able to add/edit/delete concept sources");
			CORE_PRIVILEGES.put(PRIV_VIEW_CONCEPT_SOURCES, "Able to view concept sources");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_ROLES, "Able to view user roles");
			CORE_PRIVILEGES.put(PRIV_MANAGE_ROLES, "Able to add/edit/delete user roles");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_NAVIGATION_MENU,
			    "Able to view the navigation menu (Home, View Patients, Dictionary, Administration, My Profile)");
			CORE_PRIVILEGES.put(PRIV_VIEW_ADMIN_FUNCTIONS, "Able to view the 'Administration' link in the navigation bar");
			
			CORE_PRIVILEGES.put(PRIV_DASHBOARD_OVERVIEW, "Able to view the 'Overview' tab on the patient dashboard");
			CORE_PRIVILEGES.put(PRIV_DASHBOARD_REGIMEN, "Able to view the 'Regimen' tab on the patient dashboard");
			CORE_PRIVILEGES.put(PRIV_DASHBOARD_ENCOUNTERS, "Able to view the 'Encounters' tab on the patient dashboard");
			CORE_PRIVILEGES.put(PRIV_DASHBOARD_DEMOGRAPHICS, "Able to view the 'Demographics' tab on the patient dashboard");
			CORE_PRIVILEGES.put(PRIV_DASHBOARD_GRAPHS, "Able to view the 'Graphs' tab on the patient dashboard");
			CORE_PRIVILEGES.put(PRIV_DASHBOARD_FORMS, "Able to view the 'Forms' tab on the patient dashboard");
			CORE_PRIVILEGES.put(PRIV_DASHBOARD_SUMMARY, "Able to view the 'Summary' tab on the patient dashboard");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_GLOBAL_PROPERTIES, "Able to view global properties on the administration screen");
			CORE_PRIVILEGES.put(PRIV_MANAGE_GLOBAL_PROPERTIES, "Able to add/edit global properties");
			CORE_PRIVILEGES.put(PRIV_MANAGE_MODULES, "Able to add/remove modules to the system");
			
			CORE_PRIVILEGES.put(PRIV_MANAGE_SCHEDULER, "Able to add/edit/remove scheduled tasks");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_PERSON_ATTRIBUTE_TYPES, "Able to view person attribute types");
			CORE_PRIVILEGES.put(PRIV_MANAGE_PERSON_ATTRIBUTE_TYPES, "Able to add/edit/retire person attribute types");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_PERSONS, "Able to view person objects");
			CORE_PRIVILEGES.put(PRIV_ADD_PERSONS, "Able to add person objects");
			CORE_PRIVILEGES.put(PRIV_EDIT_PERSONS, "Able to edit person objects");
			CORE_PRIVILEGES.put(PRIV_DELETE_PERSONS, "Able to delete objects");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_RELATIONSHIPS, "Able to view relationships");
			CORE_PRIVILEGES.put(PRIV_ADD_RELATIONSHIPS, "Able to add relationships");
			CORE_PRIVILEGES.put(PRIV_EDIT_RELATIONSHIPS, "Able to edit relationships");
			CORE_PRIVILEGES.put(PRIV_DELETE_RELATIONSHIPS, "Able to delete relationships");
			
			CORE_PRIVILEGES.put(PRIV_VIEW_DATABASE_CHANGES, "Able to view database changes from the admin screen");
			CORE_PRIVILEGES.put(PRIV_MANAGE_IMPLEMENTATION_ID, "Able to view/add/edit the implementation id for the system");
			
			CORE_PRIVILEGES.put(PRIV_ADD_ALLERGIES, "Add allergies");
			CORE_PRIVILEGES.put(PRIV_EDIT_ALLERGIES, "Able to edit allergies");
			CORE_PRIVILEGES.put(PRIV_DELETE_ALLERGIES, "Remove allergies");
			CORE_PRIVILEGES.put(PRIV_ADD_PROBLEMS, "Add problems");
			CORE_PRIVILEGES.put(PRIV_EDIT_PROBLEMS, "Able to edit problems");
			CORE_PRIVILEGES.put(PRIV_DELETE_PROBLEMS, "Remove problems");
			CORE_PRIVILEGES.put(PRIV_VIEW_ALLERGIES, "Able to view allergies");
			CORE_PRIVILEGES.put(PRIV_VIEW_PROBLEMS, "Able to view problems");
		}
		
		// always add the module core privileges back on
		for (Privilege privilege : ModuleFactory.getPrivileges()) {
			CORE_PRIVILEGES.put(privilege.getPrivilege(), privilege.getDescription());
		}
		
		return CORE_PRIVILEGES;
	}
	
	// Baked in Roles:
	public static final String SUPERUSER_ROLE = "System Developer";
	
	public static final String ANONYMOUS_ROLE = "Anonymous";
	
	public static final String AUTHENTICATED_ROLE = "Authenticated";
	
	public static final String PROVIDER_ROLE = "Provider";
	
	/**
	 * All roles returned by this method are inserted into the database if they do not exist
	 * already. These roles are also forbidden to be deleted from the administration screens.
	 * 
	 * @return roles that are core to the system
	 */
	public static final Map<String, String> CORE_ROLES() {
		Map<String, String> roles = new HashMap<String, String>();
		
		roles.put(SUPERUSER_ROLE,
		    "Assigned to Administrators of OpenMRS. Gives additional access to change core aspects of the system.");
		roles.put(ANONYMOUS_ROLE, "Privileges for non-authenticated users.");
		roles.put(AUTHENTICATED_ROLE, "Privileges gained once authentication has been established.");
		roles.put(PROVIDER_ROLE, "All users with the 'Provider' role will appear as options in the default Infopath ");
		
		return roles;
	}
	
	/**
	 * These roles are given to a user automatically and cannot be assigned
	 * 
	 * @return <code>Collection<String></code> of the auto-assigned roles
	 */
	public static final Collection<String> AUTO_ROLES() {
		List<String> roles = new Vector<String>();
		
		roles.add(ANONYMOUS_ROLE);
		roles.add(AUTHENTICATED_ROLE);
		
		return roles;
	}
	
	public static final String GLOBAL_PROPERTY_DRUG_FREQUENCIES = "dashboard.regimen.displayFrequencies";
	
	public static final String GLOBAL_PROPERTY_CONCEPTS_LOCKED = "concepts.locked";
	
	public static final String GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES = "patient.listingAttributeTypes";
	
	public static final String GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES = "patient.viewingAttributeTypes";
	
	public static final String GLOBAL_PROPERTY_PATIENT_HEADER_ATTRIBUTES = "patient.headerAttributeTypes";
	
	public static final String GLOBAL_PROPERTY_USER_LISTING_ATTRIBUTES = "user.listingAttributeTypes";
	
	public static final String GLOBAL_PROPERTY_USER_VIEWING_ATTRIBUTES = "user.viewingAttributeTypes";
	
	public static final String GLOBAL_PROPERTY_USER_HEADER_ATTRIBUTES = "user.headerAttributeTypes";
	
	public static final String GLOBAL_PROPERTY_HL7_ARCHIVE_DIRECTORY = "hl7_archive.dir";
	
	public static final String GLOBAL_PROPERTY_DEFAULT_THEME = "default_theme";

	/**
	 * Array of all core global property names that represent comma-separated lists of
	 * PersonAttributeTypes. (If you rename a PersonAttributeType then these global properties are
	 * potentially modified.)
	 */
	public static final String[] GLOBAL_PROPERTIES_OF_PERSON_ATTRIBUTES = { GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES,
	        GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES, GLOBAL_PROPERTY_PATIENT_HEADER_ATTRIBUTES,
	        GLOBAL_PROPERTY_USER_LISTING_ATTRIBUTES, GLOBAL_PROPERTY_USER_VIEWING_ATTRIBUTES,
	        GLOBAL_PROPERTY_USER_HEADER_ATTRIBUTES };
	
	public static final String GLOBAL_PROPERTY_PATIENT_IDENTIFIER_REGEX = "patient.identifierRegex";
	
	public static final String GLOBAL_PROPERTY_PATIENT_IDENTIFIER_PREFIX = "patient.identifierPrefix";
	
	public static final String GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SUFFIX = "patient.identifierSuffix";
	
	public static final String GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_PATTERN = "patient.identifierSearchPattern";
	
	public static final String GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS = "person.searchMaxResults";
	
	public static final int GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS_DEFAULT_VALUE = 1000;
	
	public static final String GLOBAL_PROPERTY_GZIP_ENABLED = "gzip.enabled";
	
	public static final String GLOBAL_PROPERTY_MEDICAL_RECORD_OBSERVATIONS = "concept.medicalRecordObservations";
	
	public static final String GLOBAL_PROPERTY_PROBLEM_LIST = "concept.problemList";
	
	@Deprecated
	public static final String GLOBAL_PROPERTY_REPORT_XML_MACROS = "report.xmlMacros";
	
	public static final String GLOBAL_PROPERTY_STANDARD_DRUG_REGIMENS = "dashboard.regimen.standardRegimens";
	
	public static final String GLOBAL_PROPERTY_SHOW_PATIENT_NAME = "dashboard.showPatientName";
	
	public static final String GLOBAL_PROPERTY_DEFAULT_PATIENT_IDENTIFIER_VALIDATOR = "patient.defaultPatientIdentifierValidator";
	
	public static final String GLOBAL_PROPERTY_PATIENT_IDENTIFIER_IMPORTANT_TYPES = "patient_identifier.importantTypes";
	
	public static final String GLOBAL_PROPERTY_ENCOUNTER_FORM_OBS_SORT_ORDER = "encounterForm.obsSortOrder";
	
	public static final String GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST = "locale.allowed.list";
	
	public static final String GLOBAL_PROPERTY_IMPLEMENTATION_ID = "implementation_id";
	
	public static final String GLOBAL_PROPERTY_NEWPATIENTFORM_RELATIONSHIPS = "newPatientForm.relationships";
	
	public static final String GLOBAL_PROPERTY_COMPLEX_OBS_DIR = "obs.complex_obs_dir";
	
	public static final String GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS = "minSearchCharacters";
	
	public static final String GLOBAL_PROPERTY_DEFAULT_LOCALE = "default_locale";
	
	public static final String GLOBAL_PROPERTY_DEFAULT_LOCALE_DEFAULT_VALUE = "en_GB";
	
	public static final String GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_MODE = "patientSearch.matchMode";
	
	public static final String GLOBAL_PROPERTY_PATIENT_SEARCH_MATCH_ANYWHERE = "ANYWHERE";
	
	public static final String GLOBAL_PROPERTY_DEFAULT_SERIALIZER = "serialization.defaultSerializer";
	
	public static final String GLOBAL_PROPERTY_IGNORE_MISSING_NONLOCAL_PATIENTS = "hl7_processor.ignore_missing_patient_non_local";
	
	public static final String GLOBAL_PROPERTY_TRUE_CONCEPT = "concept.true";
	
	public static final String GLOBAL_PROPERTY_FALSE_CONCEPT = "concept.false";
	
	public static final String GLOBAL_PROPERTY_LOCATION_WIDGET_TYPE = "location.field.style";
	
	/**
	 * Global property name that allows specification of whether user passwords must contain both
	 * upper and lower case characters. Allowable values are "true", "false", and null
	 */
	public static String GP_PASSWORD_REQUIRES_UPPER_AND_LOWER_CASE = "security.passwordRequiresUpperAndLowerCase";
	
	/**
	 * Global property name that allows specification of whether user passwords require non-digits.
	 * Allowable values are "true", "false", and null
	 */
	public static String GP_PASSWORD_REQUIRES_NON_DIGIT = "security.passwordRequiresNonDigit";
	
	/**
	 * Global property name that allows specification of whether user passwords must contain digits.
	 * Allowable values are "true", "false", and null
	 */
	public static String GP_PASSWORD_REQUIRES_DIGIT = "security.passwordRequiresDigit";
	
	/**
	 * Global property name that allows specification of whether user passwords can match username
	 * or system id. Allowable values are "true", "false", and null
	 */
	public static String GP_PASSWORD_CANNOT_MATCH_USERNAME_OR_SYSTEMID = "security.passwordCannotMatchUsername";
	
	/**
	 * Global property name that allows specification of whether user passwords have a minimum
	 * length requirement Allowable values are any integer
	 */
	public static String GP_PASSWORD_MINIMUM_LENGTH = "security.passwordMinimumLength";
	
	/**
	 * Global property name that allows specification of a regular expression that passwords must
	 * adhere to
	 */
	public static String GP_PASSWORD_CUSTOM_REGEX = "security.passwordCustomRegex";
	
	/**
	 * Global property name for absolute color for patient graphs.
	 */
	public static final String GP_GRAPH_COLOR_ABSOLUTE = "graph.color.absolute";
	
	/**
	 * Global property name for normal color for patient graphs.
	 */
	public static final String GP_GRAPH_COLOR_NORMAL = "graph.color.normal";
	
	/**
	 * Global property name for critical color for patient graphs.
	 */
	public static final String GP_GRAPH_COLOR_CRITICAL = "graph.color.critical";
	
	/**
	 * At OpenMRS startup these global properties/default values/descriptions are inserted into the
	 * database if they do not exist yet.
	 * 
	 * @return List<GlobalProperty> of the core global properties
	 */
	public static final List<GlobalProperty> CORE_GLOBAL_PROPERTIES() {
		List<GlobalProperty> props = new Vector<GlobalProperty>();
		
		props.add(new GlobalProperty("use_patient_attribute.healthCenter", "false",
		        "Indicates whether or not the 'health center' attribute is shown when viewing/searching for patients"));
		props.add(new GlobalProperty("use_patient_attribute.mothersName", "false",
		        "Indicates whether or not mother's name is able to be added/viewed for a patient"));
		
		props.add(new GlobalProperty("new_patient_form.showRelationships", "false",
		        "true/false whether or not to show the relationship editor on the addPatient.htm screen"));
		
		props.add(new GlobalProperty("dashboard.overview.showConcepts", "",
		        "Comma delimited list of concepts ids to show on the patient dashboard overview tab"));
		props.add(new GlobalProperty("dashboard.encounters.showEmptyFields", "true",
		        "true/false whether or not to show empty fields on the 'View Encounter' window"));
		props
		        .add(new GlobalProperty(
		                "dashboard.encounters.usePages",
		                "smart",
		                "true/false/smart on how to show the pages on the 'View Encounter' window.  'smart' means that if > 50% of the fields have page numbers defined, show data in pages"));
		props.add(new GlobalProperty("dashboard.encounters.showViewLink", "true",
		        "true/false whether or not to show the 'View Encounter' link on the patient dashboard"));
		props.add(new GlobalProperty("dashboard.encounters.showEditLink", "true",
		        "true/false whether or not to show the 'Edit Encounter' link on the patient dashboard"));
		props
		        .add(new GlobalProperty(
		                "dashboard.header.programs_to_show",
		                "",
		                "List of programs to show Enrollment details of in the patient header. (Should be an ordered comma-separated list of program_ids or names.)"));
		props
		        .add(new GlobalProperty(
		                "dashboard.header.workflows_to_show",
		                "",
		                "List of programs to show Enrollment details of in the patient header. List of workflows to show current status of in the patient header. These will only be displayed if they belong to a program listed above. (Should be a comma-separated list of program_workflow_ids.)"));
		props.add(new GlobalProperty("dashboard.relationships.show_types", "",
		        "Types of relationships separated by commas.  Doctor/Patient,Parent/Child"));
		props.add(new GlobalProperty("FormEntry.enableDashboardTab", "true",
		        "true/false whether or not to show a Form Entry tab on the patient dashboard"));
		props.add(new GlobalProperty("FormEntry.enableOnEncounterTab", "false",
		        "true/false whether or not to show a Enter Form button on the encounters tab of the patient dashboard"));
		props
		        .add(new GlobalProperty(
		                "dashboard.regimen.displayDrugSetIds",
		                "ANTIRETROVIRAL DRUGS,TUBERCULOSIS TREATMENT DRUGS",
		                "Drug sets that appear on the Patient Dashboard Regimen tab. Comma separated list of name of concepts that are defined as drug sets."));
		
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_DRUG_FREQUENCIES,
		                "7 days/week,6 days/week,5 days/week,4 days/week,3 days/week,2 days/week,1 days/week",
		                "Frequency of a drug order that appear on the Patient Dashboard. Comma separated list of name of concepts that are defined as drug frequencies."));
		
		props.add(new GlobalProperty(GP_GRAPH_COLOR_ABSOLUTE, "rgb(20,20,20)",
		        "Color of the 'invalid' section of numeric graphs on the patient dashboard."));
		
		props.add(new GlobalProperty(GP_GRAPH_COLOR_NORMAL, "rgb(255,126,0)",
		        "Color of the 'normal' section of numeric graphs on the patient dashboard."));
		
		props.add(new GlobalProperty(GP_GRAPH_COLOR_CRITICAL, "rgb(200,0,0)",
		        "Color of the 'critical' section of numeric graphs on the patient dashboard."));
		
		props.add(new GlobalProperty(GLOBAL_PROPERTY_LOCATION_WIDGET_TYPE, "default",
		        "Type of widget to use for location fields"));
		
		String standardRegimens = "<list>" + "  <regimenSuggestion>" + "    <drugComponents>" + "      <drugSuggestion>"
		        + "        <drugId>2</drugId>" + "        <dose>1</dose>" + "        <units>tab(s)</units>"
		        + "        <frequency>2/day x 7 days/week</frequency>" + "        <instructions></instructions>"
		        + "      </drugSuggestion>" + "    </drugComponents>"
		        + "    <displayName>3TC + d4T(30) + NVP (Triomune-30)</displayName>"
		        + "    <codeName>standardTri30</codeName>" + "    <canReplace>ANTIRETROVIRAL DRUGS</canReplace>"
		        + "  </regimenSuggestion>" + "  <regimenSuggestion>" + "    <drugComponents>" + "      <drugSuggestion>"
		        + "        <drugId>3</drugId>" + "        <dose>1</dose>" + "        <units>tab(s)</units>"
		        + "        <frequency>2/day x 7 days/week</frequency>" + "        <instructions></instructions>"
		        + "      </drugSuggestion>" + "    </drugComponents>"
		        + "    <displayName>3TC + d4T(40) + NVP (Triomune-40)</displayName>"
		        + "    <codeName>standardTri40</codeName>" + "    <canReplace>ANTIRETROVIRAL DRUGS</canReplace>"
		        + "  </regimenSuggestion>" + "  <regimenSuggestion>" + "    <drugComponents>" + "      <drugSuggestion>"
		        + "        <drugId>39</drugId>" + "        <dose>1</dose>" + "        <units>tab(s)</units>"
		        + "        <frequency>2/day x 7 days/week</frequency>" + "        <instructions></instructions>"
		        + "      </drugSuggestion>" + "      <drugSuggestion>" + "        <drugId>22</drugId>"
		        + "        <dose>200</dose>" + "        <units>mg</units>"
		        + "        <frequency>2/day x 7 days/week</frequency>" + "        <instructions></instructions>"
		        + "      </drugSuggestion>" + "    </drugComponents>" + "    <displayName>AZT + 3TC + NVP</displayName>"
		        + "    <codeName>standardAztNvp</codeName>" + "    <canReplace>ANTIRETROVIRAL DRUGS</canReplace>"
		        + "  </regimenSuggestion>" + "  <regimenSuggestion>" + "    <drugComponents>"
		        + "      <drugSuggestion reference=\"../../../regimenSuggestion[3]/drugComponents/drugSuggestion\"/>"
		        + "      <drugSuggestion>" + "        <drugId>11</drugId>" + "        <dose>600</dose>"
		        + "        <units>mg</units>" + "        <frequency>1/day x 7 days/week</frequency>"
		        + "        <instructions></instructions>" + "      </drugSuggestion>" + "    </drugComponents>"
		        + "    <displayName>AZT + 3TC + EFV(600)</displayName>" + "    <codeName>standardAztEfv</codeName>"
		        + "    <canReplace>ANTIRETROVIRAL DRUGS</canReplace>" + "  </regimenSuggestion>" + "  <regimenSuggestion>"
		        + "    <drugComponents>" + "      <drugSuggestion>" + "        <drugId>5</drugId>"
		        + "        <dose>30</dose>" + "        <units>mg</units>"
		        + "        <frequency>2/day x 7 days/week</frequency>" + "        <instructions></instructions>"
		        + "      </drugSuggestion>" + "      <drugSuggestion>" + "        <drugId>42</drugId>"
		        + "        <dose>150</dose>" + "        		<units>mg</units>"
		        + "        <frequency>2/day x 7 days/week</frequency>" + "        <instructions></instructions>"
		        + "      </drugSuggestion>"
		        + "      <drugSuggestion reference=\"../../../regimenSuggestion[4]/drugComponents/drugSuggestion[2]\"/>"
		        + "    </drugComponents>" + "    <displayName>d4T(30) + 3TC + EFV(600)</displayName>"
		        + "    <codeName>standardD4t30Efv</codeName>" + "    <canReplace>ANTIRETROVIRAL DRUGS</canReplace>"
		        + "  </regimenSuggestion>" + "  <regimenSuggestion>" + "    <drugComponents>" + "      <drugSuggestion>"
		        + "        <drugId>6</drugId>" + "        <dose>40</dose>" + "        <units>mg</units>"
		        + "        <frequency>2/day x 7 days/week</frequency>" + "        <instructions></instructions>"
		        + "      </drugSuggestion>"
		        + "      <drugSuggestion reference=\"../../../regimenSuggestion[5]/drugComponents/drugSuggestion[2]\"/>"
		        + "      <drugSuggestion reference=\"../../../regimenSuggestion[4]/drugComponents/drugSuggestion[2]\"/>"
		        + "    </drugComponents>" + "    <displayName>d4T(40) + 3TC + EFV(600)</displayName>"
		        + "    <codeName>standardD4t40Efv</codeName>" + "    <canReplace>ANTIRETROVIRAL DRUGS</canReplace>"
		        + "  </regimenSuggestion>" + "</list>";
		props.add(new GlobalProperty(GLOBAL_PROPERTY_STANDARD_DRUG_REGIMENS, standardRegimens,
		        "XML description of standard drug regimens, to be shown as shortcuts on the dashboard regimen entry tab"));
		
		props.add(new GlobalProperty("concept.weight", "5089", "Concept id of the concept defining the WEIGHT concept"));
		props.add(new GlobalProperty("concept.height", "5090", "Concept id of the concept defining the HEIGHT concept"));
		props
		        .add(new GlobalProperty("concept.cd4_count", "5497",
		                "Concept id of the concept defining the CD4 count concept"));
		props.add(new GlobalProperty("concept.causeOfDeath", "5002",
		        "Concept id of the concept defining the CAUSE OF DEATH concept"));
		props.add(new GlobalProperty("concept.none", "1107", "Concept id of the concept defining the NONE concept"));
		props.add(new GlobalProperty("concept.otherNonCoded", "5622",
		        "Concept id of the concept defining the OTHER NON-CODED concept"));
		props.add(new GlobalProperty("concept.patientDied", "1742",
		        "Concept id of the concept defining the PATIENT DIED concept"));
		props.add(new GlobalProperty("concept.reasonExitedCare", "1811",
		        "Concept id of the concept defining the REASON EXITED CARE concept"));
		props.add(new GlobalProperty("concept.reasonOrderStopped", "1812",
		        "Concept id of the concept defining the REASON ORDER STOPPED concept"));
		
		props.add(new GlobalProperty("mail.transport_protocol", "smtp",
		        "Transport protocol for the messaging engine. Valid values: smtp"));
		props.add(new GlobalProperty("mail.smtp_host", "localhost", "SMTP host name"));
		props.add(new GlobalProperty("mail.smtp_port", "25", "SMTP port"));
		props.add(new GlobalProperty("mail.from", "info@openmrs.org", "Email address to use as the default from address"));
		props.add(new GlobalProperty("mail.debug", "false",
		        "true/false whether to print debugging information during mailing"));
		props.add(new GlobalProperty("mail.smtp_auth", "false", "true/false whether the smtp host requires authentication"));
		props.add(new GlobalProperty("mail.user", "test", "Username of the SMTP user (if smtp_auth is enabled)"));
		props.add(new GlobalProperty("mail.password", "test", "Password for the SMTP user (if smtp_auth is enabled)"));
		props.add(new GlobalProperty("mail.default_content_type", "text/plain",
		        "Content type to append to the mail messages"));
		
		props.add(new GlobalProperty(ModuleConstants.REPOSITORY_FOLDER_PROPERTY,
		        ModuleConstants.REPOSITORY_FOLDER_PROPERTY_DEFAULT, "Name of the folder in which to store the modules"));
		
		props
		        .add(new GlobalProperty("layout.address.format", "general",
		                "Format in which to display the person addresses.  Valid values are general, kenya, rwanda, usa, and lesotho"));
		props.add(new GlobalProperty("layout.name.format", "short",
		        "Format in which to display the person names.  Valid values are short, long"));
		
		// TODO should be changed to text defaults and constants should be removed
		props.add(new GlobalProperty("scheduler.username", SchedulerConstants.SCHEDULER_DEFAULT_USERNAME,
		        "Username for the OpenMRS user that will perform the scheduler activities"));
		props.add(new GlobalProperty("scheduler.password", SchedulerConstants.SCHEDULER_DEFAULT_PASSWORD,
		        "Password for the OpenMRS user that will perform the scheduler activities"));
		
		props.add(new GlobalProperty(GLOBAL_PROPERTY_CONCEPTS_LOCKED, "false",
		        "true/false whether or not concepts can be edited in this database."));
		
		props.add(new GlobalProperty(GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES, "",
		        "A comma delimited list of PersonAttributeType names that should be displayed for patients in _lists_"));
		props
		        .add(new GlobalProperty(GLOBAL_PROPERTY_PATIENT_VIEWING_ATTRIBUTES, "",
		                "A comma delimited list of PersonAttributeType names that should be displayed for patients when _viewing individually_"));
		props.add(new GlobalProperty(GLOBAL_PROPERTY_PATIENT_HEADER_ATTRIBUTES, "",
		        "A comma delimited list of PersonAttributeType names that will be shown on the patient dashboard"));
		
		props.add(new GlobalProperty(GLOBAL_PROPERTY_USER_LISTING_ATTRIBUTES, "",
		        "A comma delimited list of PersonAttributeType names that should be displayed for users in _lists_"));
		props
		        .add(new GlobalProperty(GLOBAL_PROPERTY_USER_VIEWING_ATTRIBUTES, "",
		                "A comma delimited list of PersonAttributeType names that should be displayed for users when _viewing individually_"));
		props
		        .add(new GlobalProperty(GLOBAL_PROPERTY_USER_HEADER_ATTRIBUTES, "",
		                "A comma delimited list of PersonAttributeType names that will be shown on the user dashboard. (not used in v1.5)"));
		
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_PATIENT_IDENTIFIER_REGEX,
		                "",
		                "WARNING: Using this search property can cause a drop in mysql performance with large patient sets.  A MySQL regular expression for the patient identifier search strings.  The @SEARCH@ string is replaced at runtime with the user's search string.  An empty regex will cause a simply 'like' sql search to be used. Example: ^0*@SEARCH@([A-Z]+-[0-9])?$"));
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_PATIENT_IDENTIFIER_PREFIX,
		                "",
		                "This property is only used if "
		                        + GLOBAL_PROPERTY_PATIENT_IDENTIFIER_REGEX
		                        + " is empty.  The string here is prepended to the sql indentifier search string.  The sql becomes \"... where identifier like '<PREFIX><QUERY STRING><SUFFIX>';\".  Typically this value is either a percent sign (%) or empty."));
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SUFFIX,
		                "",
		                "This property is only used if "
		                        + GLOBAL_PROPERTY_PATIENT_IDENTIFIER_REGEX
		                        + " is empty.  The string here is prepended to the sql indentifier search string.  The sql becomes \"... where identifier like '<PREFIX><QUERY STRING><SUFFIX>';\".  Typically this value is either a percent sign (%) or empty."));
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_PATIENT_IDENTIFIER_SEARCH_PATTERN,
		                "",
		                "If this is empty, the regex or suffix/prefix search is used.  Comma separated list of identifiers to check.  Allows for faster searching of multiple options rather than the slow regex. e.g. @SEARCH@,0@SEARCH@,@SEARCH-1@-@CHECKDIGIT@,0@SEARCH-1@-@CHECKDIGIT@ would turn a request for \"4127\" into a search for \"in ('4127','04127','412-7','0412-7')\""));
		
		props.add(new GlobalProperty(GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS, String
		        .valueOf(GLOBAL_PROPERTY_PERSON_SEARCH_MAX_RESULTS_DEFAULT_VALUE),
		        "The maximum number of results returned by patient searches"));
		
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_GZIP_ENABLED,
		                "false",
		                "Set to 'true' to turn on OpenMRS's gzip filter, and have the webapp compress data before sending it to any client that supports it. Generally use this if you are running Tomcat standalone. If you are running Tomcat behind Apache, then you'd want to use Apache to do gzip compression."));
		props
		        .add(new GlobalProperty(GLOBAL_PROPERTY_REPORT_XML_MACROS, "",
		                "Macros that will be applied to Report Schema XMLs when they are interpreted. This should be java.util.properties format."));
		
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_MEDICAL_RECORD_OBSERVATIONS,
		                "1238",
		                "The concept id of the MEDICAL_RECORD_OBSERVATIONS concept.  This concept_id is presumed to be the generic grouping (obr) concept in hl7 messages.  An obs_group row is not created for this concept."));
		
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_PROBLEM_LIST,
		                "1284",
		                "The concept id of the PROBLEM LIST concept.  This concept_id is presumed to be the generic grouping (obr) concept in hl7 messages.  An obs_group row is not created for this concept."));
		
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_LOG_LEVEL,
		                LOG_LEVEL_INFO,
		                "log level used by the logger 'org.openmrs'. This value will override the log4j.xml value. Valid values are trace, debug, info, warn, error or fatal"));
		
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_DEFAULT_PATIENT_IDENTIFIER_VALIDATOR,
		                LUHN_IDENTIFIER_VALIDATOR,
		                "This property sets the default patient identifier validator.  The default validator is only used in a handful of (mostly legacy) instances.  For example, it's used to generate the isValidCheckDigit calculated column and to append the string \"(default)\" to the name of the default validator on the editPatientIdentifierType form."));
		
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_PATIENT_IDENTIFIER_IMPORTANT_TYPES,
		                "",
		                "A comma delimited list of PatientIdentifier names : PatientIdentifier locations that will be displayed on the patient dashboard.  E.g.: TRACnet ID:Rwanda,ELDID:Kenya"));
		
		props.add(new GlobalProperty(GLOBAL_PROPERTY_COMPLEX_OBS_DIR, "complex_obs",
		        "Default directory for storing complex obs."));
		
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_ENCOUNTER_FORM_OBS_SORT_ORDER,
		                "number",
		                "The sort order for the obs listed on the encounter edit form.  'number' sorts on the associated numbering from the form schema.  'weight' sorts on the order displayed in the form schema."));
		
		props.add(new GlobalProperty(GLOBAL_PROPERTY_LOCALE_ALLOWED_LIST, "en, es, fr, it, pt",
		        "Comma delimited list of locales allowed for use on system"));
		
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_NEWPATIENTFORM_RELATIONSHIPS,
		                "",
		                "Comma separated list of the RelationshipTypes to show on the new/short patient form.  The list is defined like '3a, 4b, 7a'.  The number is the RelationshipTypeId and the 'a' vs 'b' part is which side of the relationship is filled in by the user."));
		
		props.add(new GlobalProperty(GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS, "3",
		        "Number of characters user must input before searching is started."));
		
		props
		        .add(new GlobalProperty(
		                OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE,
		                OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE_DEFAULT_VALUE,
		                "Specifies the default locale. You can specify both the language code(ISO-639) and the country code(ISO-3166), e.g. 'en_GB' or just country: e.g. 'en'"));
		
		props.add(new GlobalProperty(GP_PASSWORD_CANNOT_MATCH_USERNAME_OR_SYSTEMID, "true",
		        "Configure whether passwords must not match user's username or system id"));
		
		props.add(new GlobalProperty(GP_PASSWORD_CUSTOM_REGEX, "",
		        "Configure a custom regular expression that a password must match"));
		
		props.add(new GlobalProperty(GP_PASSWORD_MINIMUM_LENGTH, "8",
		        "Configure the minimum length required of all passwords"));
		
		props.add(new GlobalProperty(GP_PASSWORD_REQUIRES_DIGIT, "true",
		        "Configure whether passwords must contain at least one digit"));
		
		props.add(new GlobalProperty(GP_PASSWORD_REQUIRES_NON_DIGIT, "true",
		        "Configure whether passwords must contain at least one non-digit"));
		
		props.add(new GlobalProperty(GP_PASSWORD_REQUIRES_UPPER_AND_LOWER_CASE, "true",
		        "Configure whether passwords must contain both upper and lower case characters"));
		
		props.add(new GlobalProperty(GLOBAL_PROPERTY_IGNORE_MISSING_NONLOCAL_PATIENTS, "false",
		        "If true, hl7 messages for patients that are not found and are non-local will silently be dropped/ignored"));
		
		props
		        .add(new GlobalProperty(
		                GLOBAL_PROPERTY_SHOW_PATIENT_NAME,
		                "false",
		                "Whether or not to display the patient name in the patient dashboard title. Note that enabling this could be security risk if multiple users operate on the same computer."));
		
		props.add(new GlobalProperty(GLOBAL_PROPERTY_DEFAULT_THEME, "",
		        "Default theme for users.  OpenMRS ships with themes of 'green', 'orange', 'purple', and 'legacy'"));
		
		for (GlobalProperty gp : ModuleFactory.getGlobalProperties()) {
			props.add(gp);
		}
		
		return props;
	}
	
	// ConceptProposal proposed concept identifier keyword
	public static final String PROPOSED_CONCEPT_IDENTIFIER = "PROPOSED";
	
	// ConceptProposal states
	public static final String CONCEPT_PROPOSAL_UNMAPPED = "UNMAPPED";
	
	public static final String CONCEPT_PROPOSAL_CONCEPT = "CONCEPT";
	
	public static final String CONCEPT_PROPOSAL_SYNONYM = "SYNONYM";
	
	public static final String CONCEPT_PROPOSAL_REJECT = "REJECT";
	
	public static final Collection<String> CONCEPT_PROPOSAL_STATES() {
		Collection<String> states = new Vector<String>();
		
		states.add(CONCEPT_PROPOSAL_UNMAPPED);
		states.add(CONCEPT_PROPOSAL_CONCEPT);
		states.add(CONCEPT_PROPOSAL_SYNONYM);
		states.add(CONCEPT_PROPOSAL_REJECT);
		
		return states;
	}
	
	public static Locale SPANISH_LANGUAGE = new Locale("es");
	
	public static Locale PORTUGUESE_LANGUAGE = new Locale("pt");
	
	public static Locale ITALIAN_LANGUAGE = new Locale("it");
	
	/**
	 * @return Collection of locales available to openmrs
	 * @deprecated
	 */
	public static final Collection<Locale> OPENMRS_LOCALES() {
		List<Locale> languages = new Vector<Locale>();
		
		languages.add(Locale.US);
		languages.add(Locale.UK);
		languages.add(Locale.FRENCH);
		languages.add(SPANISH_LANGUAGE);
		languages.add(PORTUGUESE_LANGUAGE);
		languages.add(ITALIAN_LANGUAGE);
		
		return languages;
	}
	
	/**
	 * @deprecated use {@link LocaleUtility#getDefaultLocale()}
	 */
	public static final Locale GLOBAL_DEFAULT_LOCALE = LocaleUtility.DEFAULT_LOCALE;
	
	/**
	 * @return Collection of locales that the concept dictionary should be aware of
	 * @see ConceptService#getLocalesOfConceptNames()
	 * @deprecated
	 */
	public static final Collection<Locale> OPENMRS_CONCEPT_LOCALES() {
		List<Locale> languages = new Vector<Locale>();
		
		languages.add(Locale.ENGLISH);
		languages.add(Locale.FRENCH);
		languages.add(SPANISH_LANGUAGE);
		languages.add(PORTUGUESE_LANGUAGE);
		languages.add(ITALIAN_LANGUAGE);
		
		return languages;
	}
	
	@Deprecated
	private static Map<String, String> OPENMRS_LOCALE_DATE_PATTERNS = null;
	
	/**
	 * @return Mapping of Locales to locale specific date pattern
	 * @deprecated use the {@link org.openmrs.api.context.Context#getDateFormat()}
	 */
	public static final Map<String, String> OPENMRS_LOCALE_DATE_PATTERNS() {
		if (OPENMRS_LOCALE_DATE_PATTERNS == null) {
			Map<String, String> patterns = new HashMap<String, String>();
			
			patterns.put(Locale.US.toString().toLowerCase(), "MM/dd/yyyy");
			patterns.put(Locale.UK.toString().toLowerCase(), "dd/MM/yyyy");
			patterns.put(Locale.FRENCH.toString().toLowerCase(), "dd/MM/yyyy");
			patterns.put(Locale.GERMAN.toString().toLowerCase(), "MM.dd.yyyy");
			patterns.put(SPANISH_LANGUAGE.toString().toLowerCase(), "dd/MM/yyyy");
			patterns.put(PORTUGUESE_LANGUAGE.toString().toLowerCase(), "dd/MM/yyyy");
			patterns.put(ITALIAN_LANGUAGE.toString().toLowerCase(), "dd/MM/yyyy");
			
			OPENMRS_LOCALE_DATE_PATTERNS = patterns;
		}
		
		return OPENMRS_LOCALE_DATE_PATTERNS;
	}
	
	/*
	 * User property names
	 */
	public static final String USER_PROPERTY_CHANGE_PASSWORD = "forcePassword";
	
	public static final String USER_PROPERTY_DEFAULT_LOCALE = "defaultLocale";
	
	public static final String USER_PROPERTY_DEFAULT_LOCATION = "defaultLocation";
	
	public static final String USER_PROPERTY_SHOW_RETIRED = "showRetired";
	
	public static final String USER_PROPERTY_SHOW_VERBOSE = "showVerbose";
	
	public static final String USER_PROPERTY_NOTIFICATION = "notification";
	
	public static final String USER_PROPERTY_NOTIFICATION_ADDRESS = "notificationAddress";
	
	public static final String USER_PROPERTY_NOTIFICATION_FORMAT = "notificationFormat"; // text/plain, text/html
	
	/**
	 * Name of the user_property that stores the number of unsuccessful login attempts this user has
	 * made
	 */
	public static final String USER_PROPERTY_LOGIN_ATTEMPTS = "loginAttempts";
	
	/**
	 * Name of the user_property that stores the time the user was locked out due to too many login
	 * attempts
	 */
	public static final String USER_PROPERTY_LOCKOUT_TIMESTAMP = "lockoutTimestamp";
	
	/**
	 * A user property name. The value should be a comma-separated ordered list of fully qualified
	 * locales within which the user is a proficient speaker. The list should be ordered from the
	 * most to the least proficiency. Example:
	 * <code>proficientLocales = en_US, en_GB, en, fr_RW</code>
	 */
	public static final String USER_PROPERTY_PROFICIENT_LOCALES = "proficientLocales";
	
	/**
	 * Report object properties
	 */
	@Deprecated
	public static final String REPORT_OBJECT_TYPE_PATIENTFILTER = "Patient Filter";
	
	@Deprecated
	public static final String REPORT_OBJECT_TYPE_PATIENTSEARCH = "Patient Search";
	
	@Deprecated
	public static final String REPORT_OBJECT_TYPE_PATIENTDATAPRODUCER = "Patient Data Producer";
	
	// Used for differences between windows/linux upload capabilities)
	// Used for determining where to find runtime properties
	public static final String OPERATING_SYSTEM_KEY = "os.name";
	
	public static final String OPERATING_SYSTEM = System.getProperty(OPERATING_SYSTEM_KEY);
	
	public static final String OPERATING_SYSTEM_WINDOWS_XP = "Windows XP";
	
	public static final String OPERATING_SYSTEM_WINDOWS_VISTA = "Windows Vista";
	
	public static final String OPERATING_SYSTEM_LINUX = "Linux";
	
	public static final String OPERATING_SYSTEM_SUNOS = "SunOS";
	
	public static final String OPERATING_SYSTEM_FREEBSD = "FreeBSD";
	
	public static final String OPERATING_SYSTEM_OSX = "Mac OS X";
	
	/**
	 * URL to the concept source id verification server
	 */
	public static final String IMPLEMENTATION_ID_REMOTE_CONNECTION_URL = "http://resources.openmrs.org/tools/implementationid";
	
	/**
	 * Shortcut booleans used to make some OS specific checks more generic; note the *nix flavored
	 * check is missing some less obvious choices
	 */
	public static final boolean UNIX_BASED_OPERATING_SYSTEM = (OPERATING_SYSTEM.indexOf(OPERATING_SYSTEM_LINUX) > -1
	        || OPERATING_SYSTEM.indexOf(OPERATING_SYSTEM_SUNOS) > -1
	        || OPERATING_SYSTEM.indexOf(OPERATING_SYSTEM_FREEBSD) > -1 || OPERATING_SYSTEM.indexOf(OPERATING_SYSTEM_OSX) > -1);
	
	public static final boolean WINDOWS_BASED_OPERATING_SYSTEM = OPERATING_SYSTEM.indexOf("Windows") > -1;
	
	public static final boolean WINDOWS_VISTA_OPERATING_SYSTEM = OPERATING_SYSTEM.equals(OPERATING_SYSTEM_WINDOWS_VISTA);
	
	/**
	 * Marker put into the serialization session map to tell @Replace methods whether or not to do
	 * just the very basic serialization
	 */
	public static final String SHORT_SERIALIZATION = "isShortSerialization";
	
	// Global property key for global logger level
	public static final String GLOBAL_PROPERTY_LOG_LEVEL = "log.level.openmrs";
	
	// Global logger category
	public static final String LOG_CLASS_DEFAULT = "org.openmrs";
	
	// Log levels
	public static final String LOG_LEVEL_TRACE = "trace";
	
	public static final String LOG_LEVEL_DEBUG = "debug";
	
	public static final String LOG_LEVEL_INFO = "info";
	
	public static final String LOG_LEVEL_WARN = "warn";
	
	public static final String LOG_LEVEL_ERROR = "error";
	
	public static final String LOG_LEVEL_FATAL = "fatal";
	
	/**
	 * These enumerations should be used in ObsService and PersonService getters to help determine
	 * which type of object to restrict on
	 * 
	 * @see org.openmrs.api.ObsService
	 * @see org.openmrs.api.PersonService
	 */
	public static enum PERSON_TYPE {
		PERSON, PATIENT, USER
	}
	
	//Patient Identifier Validators
	public static final String LUHN_IDENTIFIER_VALIDATOR = LuhnIdentifierValidator.class.getName();
	
	// ComplexObsHandler views
	public static final String RAW_VIEW = "RAW_VIEW";
	
	public static final String TEXT_VIEW = "TEXT_VIEW";
	
	public static final String JIRA_CREATE_PATCH_URL = "http://tickets.openmrs.org/secure/CreateIssueDetails!init.jspa";
	
	public static final String JIRA_SIGN_UP = "http://tickets.openmrs.org/secure/Signup!default.jspa";
	
	public static final String JIRA_OPENMRS_TRUNK_PID = "10000";
	
	public static final String JIRA_BUG = "1";
	
}
