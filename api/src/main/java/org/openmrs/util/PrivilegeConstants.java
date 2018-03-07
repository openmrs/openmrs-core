/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.openmrs.annotation.AddOnStartup;
import org.openmrs.annotation.HasAddOnStartupPrivileges;

/**
 * Contains all privilege names and their descriptions. Some of privilege names may be marked with
 * AddOnStartup annotation.
 * 
 * @see org.openmrs.annotation.AddOnStartup
 * @since 1.8
 */
@HasAddOnStartupPrivileges
public class PrivilegeConstants {

	private PrivilegeConstants() {
	}

	@AddOnStartup(description = "Able to get concept entries")
	public static final String GET_CONCEPTS = "Get Concepts";
	
	@AddOnStartup(description = "Able to get concept proposals to the system")
	public static final String GET_CONCEPT_PROPOSALS = "Get Concept Proposals";
	
	@AddOnStartup(description = "Able to get users in OpenMRS")
	public static final String GET_USERS = "Get Users";
	
	@AddOnStartup(description = "Able to get patient encounters")
	public static final String GET_ENCOUNTERS = "Get Encounters";
	
	@AddOnStartup(description = "Able to get encounter types")
	public static final String GET_ENCOUNTER_TYPES = "Get Encounter Types";
	
	@AddOnStartup(description = "Able to get locations")
	public static final String GET_LOCATIONS = "Get Locations";
	
	@AddOnStartup(description = "Able to get patient observations")
	public static final String GET_OBS = "Get Observations";
	
	@AddOnStartup(description = "Able to get patient notes")
	public static final String GET_NOTE = "Get Notes";
	
	@AddOnStartup(description = "Able to get patients")
	public static final String GET_PATIENTS = "Get Patients";
	
	@AddOnStartup(description = "Able to get patient identifiers")
	public static final String GET_PATIENT_IDENTIFIERS = "Get Patient Identifiers";
	
	@AddOnStartup(description = "Able to get patient cohorts")
	public static final String GET_PATIENT_COHORTS = "Get Patient Cohorts";
	
	@AddOnStartup(description = "Able to get orders")
	public static final String GET_ORDERS = "Get Orders";
	
	@AddOnStartup(description = "Able to get forms")
	public static final String GET_FORMS = "Get Forms";
	
	@AddOnStartup(description = "Able to get patient identifier types")
	public static final String GET_IDENTIFIER_TYPES = "Get Identifier Types";
	
	@AddOnStartup(description = "Able to get concept classes")
	public static final String GET_CONCEPT_CLASSES = "Get Concept Classes";
	
	@AddOnStartup(description = "Able to get concept datatypes")
	public static final String GET_CONCEPT_DATATYPES = "Get Concept Datatypes";
	
	@AddOnStartup(description = "Able to get user privileges")
	public static final String GET_PRIVILEGES = "Get Privileges";
	
	@AddOnStartup(description = "Able to get user roles")
	public static final String GET_ROLES = "Get Roles";
	
	@AddOnStartup(description = "Able to get field types")
	public static final String GET_FIELD_TYPES = "Get Field Types";
	
	@AddOnStartup(description = "Able to get order types")
	public static final String GET_ORDER_TYPES = "Get Order Types";
	
	@AddOnStartup(description = "Able to get relationship types")
	public static final String GET_RELATIONSHIP_TYPES = "Get Relationship Types";
	
	@AddOnStartup(description = "Able to get concept sources")
	public static final String GET_CONCEPT_SOURCES = "Get Concept Sources";
	
	@AddOnStartup(description = "Able to get concept map types")
	public static final String GET_CONCEPT_MAP_TYPES = "Get Concept Map Types";
	
	@AddOnStartup(description = "Able to get concept reference terms")
	public static final String GET_CONCEPT_REFERENCE_TERMS = "Get Concept Reference Terms";
	
	@AddOnStartup(description = "Able to get patient programs")
	public static final String GET_PROGRAMS = "Get Programs";
	
	@AddOnStartup(description = "Able to get which programs that patients are in")
	public static final String GET_PATIENT_PROGRAMS = "Get Patient Programs";
	
	@AddOnStartup(description = "Able to get global properties on the administration screen")
	public static final String GET_GLOBAL_PROPERTIES = "Get Global Properties";
	
	@AddOnStartup(description = "Able to get person attribute types")
	public static final String GET_PERSON_ATTRIBUTE_TYPES = "Get Person Attribute Types";
	
	@AddOnStartup(description = "Able to get person objects")
	public static final String GET_PERSONS = "Get People";
	
	@AddOnStartup(description = "Able to get relationships")
	public static final String GET_RELATIONSHIPS = "Get Relationships";
	
	@AddOnStartup(description = "Able to get database changes from the admin screen")
	public static final String GET_DATABASE_CHANGES = "Get Database Changes";
	
	@AddOnStartup(description = "Able to get problems")
	public static final String GET_PROBLEMS = "Get Problems";
	
	@AddOnStartup(description = "Able to get allergies")
	public static final String GET_ALLERGIES = "Get Allergies";
	
	@AddOnStartup(description = "Able to add/edit/delete HL7 messages")
	public static final String MANAGE_HL7_MESSAGES = "Manage HL7 Messages";
	
	public static final String PURGE_HL7_MESSAGES = "Purge HL7 Messages";
	
	@AddOnStartup(description = "Able to get an HL7 Source")
	public static final String GET_HL7_SOURCE = "Get HL7 Source";
	
	@AddOnStartup(description = "Able to get an HL7 Queue item")
	public static final String GET_HL7_IN_QUEUE = "Get HL7 Inbound Queue";
	
	@AddOnStartup(description = "Able to get an HL7 archive item")
	public static final String GET_HL7_IN_ARCHIVE = "Get HL7 Inbound Archive";
	
	@AddOnStartup(description = "Able to get an HL7 error item")
	public static final String GET_HL7_IN_EXCEPTION = "Get HL7 Inbound Exception";
	
	@AddOnStartup(description = "Able to get visit types")
	public static final String GET_VISIT_TYPES = "Get Visit Types";
	
	@AddOnStartup(description = "Able to get visits")
	public static final String GET_VISITS = "Get Visits";
	
	@AddOnStartup(description = "Able to get visit attribute types")
	public static final String GET_VISIT_ATTRIBUTE_TYPES = "Get Visit Attribute Types";
	
	@AddOnStartup(description = "Able to get location attribute types")
	public static final String GET_LOCATION_ATTRIBUTE_TYPES = "Get Location Attribute Types";
	
	@AddOnStartup(description = "Able to get Provider")
	public static final String GET_PROVIDERS = "Get Providers";
	
	@AddOnStartup(description = "Able to get encounter roles")
	public static final String GET_ENCOUNTER_ROLES = "Get Encounter Roles";
	
	@AddOnStartup(description = "Able to add/edit/delete concept entries")
	public static final String MANAGE_CONCEPTS = "Manage Concepts";
	
	public static final String PURGE_CONCEPTS = "Purge Concepts";
	
	@AddOnStartup(description = "Able to add/edit/delete concept name tags")
	public static final String MANAGE_CONCEPT_NAME_TAGS = "Manage Concept Name tags";
	
	@AddOnStartup(description = "Able to add concept proposals to the system")
	public static final String ADD_CONCEPT_PROPOSALS = "Add Concept Proposals";
	
	@AddOnStartup(description = "Able to edit concept proposals in the system")
	public static final String EDIT_CONCEPT_PROPOSALS = "Edit Concept Proposals";
	
	@AddOnStartup(description = "Able to delete concept proposals from the system")
	public static final String DELETE_CONCEPT_PROPOSALS = "Delete Concept Proposals";
	
	public static final String PURGE_CONCEPT_PROPOSALS = "Purge Concept Proposals";
	
	@AddOnStartup(description = "Able to add users to OpenMRS")
	public static final String ADD_USERS = "Add Users";
	
	@AddOnStartup(description = "Able to edit users in OpenMRS")
	public static final String EDIT_USERS = "Edit Users";
	
	@AddOnStartup(description = "Able to delete users in OpenMRS")
	public static final String DELETE_USERS = "Delete Users";
	
	public static final String PURGE_USERS = "Purge Users";
	
	@AddOnStartup(description = "Able to change the passwords of users in OpenMRS")
	public static final String EDIT_USER_PASSWORDS = "Edit User Passwords";
	
	@AddOnStartup(description = "Able to add patient encounters")
	public static final String ADD_ENCOUNTERS = "Add Encounters";
	
	@AddOnStartup(description = "Able to edit patient encounters")
	public static final String EDIT_ENCOUNTERS = "Edit Encounters";
	
	@AddOnStartup(description = "Able to delete patient encounters")
	public static final String DELETE_ENCOUNTERS = "Delete Encounters";
	
	public static final String PURGE_ENCOUNTERS = "Purge Encounters";
	
	@AddOnStartup(description = "Able to add/edit/retire encounter types")
	public static final String MANAGE_ENCOUNTER_TYPES = "Manage Encounter Types";
	
	public static final String PURGE_ENCOUNTER_TYPES = "Purge Encounter Types";
	
	@AddOnStartup(description = "Able to choose encounter visit handler and enable/disable encounter visits")
	public static final String CONFIGURE_VISITS = "Configure Visits";
	
	@AddOnStartup(description = "Able to add/edit/delete locations")
	public static final String MANAGE_LOCATIONS = "Manage Locations";
	
	public static final String PURGE_LOCATIONS = "Purge Locations";
	
	@AddOnStartup(description = "Able to add/edit/delete location tags")
	public static final String MANAGE_LOCATION_TAGS = "Manage Location Tags";
	
	@AddOnStartup(description = "Able to add/edit/delete address templates")
	public static final String MANAGE_ADDRESS_TEMPLATES = "Manage Address Templates";
	
	public static final String PURGE_LOCATION_TAGS = "Purge Location Tags";
	
	@AddOnStartup(description = "Able to add patient observations")
	public static final String ADD_OBS = "Add Observations";
	
	@AddOnStartup(description = "Able to edit patient observations")
	public static final String EDIT_OBS = "Edit Observations";
	
	@AddOnStartup(description = "Able to delete patient observations")
	public static final String DELETE_OBS = "Delete Observations";
	
	public static final String PURGE_OBS = "Purge Observations";
	
	@AddOnStartup(description = "Able to edit patient notes")
	public static final String EDIT_NOTE = "Edit Notes";
	
	@AddOnStartup(description = "Able to delete patient notes")
	public static final String DELETE_NOTE = "Delete Notes";
	
	@AddOnStartup(description = "Able to add patients")
	public static final String ADD_PATIENTS = "Add Patients";
	
	@AddOnStartup(description = "Able to edit patients")
	public static final String EDIT_PATIENTS = "Edit Patients";
	
	@AddOnStartup(description = "Able to delete patients")
	public static final String DELETE_PATIENTS = "Delete Patients";
	
	public static final String PURGE_PATIENTS = "Purge Patients";
	
	@AddOnStartup(description = "Able to add patient identifiers")
	public static final String ADD_PATIENT_IDENTIFIERS = "Add Patient Identifiers";
	
	@AddOnStartup(description = "Able to edit patient identifiers")
	public static final String EDIT_PATIENT_IDENTIFIERS = "Edit Patient Identifiers";
	
	@AddOnStartup(description = "Able to delete patient identifiers")
	public static final String DELETE_PATIENT_IDENTIFIERS = "Delete Patient Identifiers";
	
	public static final String PURGE_PATIENT_IDENTIFIERS = "Purge Patient Identifiers";
	
	@AddOnStartup(description = "Able to add a cohort to the system")
	public static final String ADD_COHORTS = "Add Cohorts";
	
	@AddOnStartup(description = "Able to add a cohort to the system")
	public static final String EDIT_COHORTS = "Edit Cohorts";
	
	@AddOnStartup(description = "Able to add a cohort to the system")
	public static final String DELETE_COHORTS = "Delete Cohorts";
	
	public static final String PURGE_COHORTS = "Purge Cohorts";
	
	@AddOnStartup(description = "Able to add orders")
	public static final String ADD_ORDERS = "Add Orders";
	
	@AddOnStartup(description = "Able to edit orders")
	public static final String EDIT_ORDERS = "Edit Orders";
	
	@AddOnStartup(description = "Able to delete orders")
	public static final String DELETE_ORDERS = "Delete Orders";
	
	public static final String PURGE_ORDERS = "Purge Orders";
	
	@AddOnStartup(description = "Able to add/edit/delete forms")
	public static final String MANAGE_FORMS = "Manage Forms";
	
	public static final String PURGE_FORMS = "Purge Forms";
	
	// This name is historic, since that's what it was originally called in the
	// infopath formentry module
	@AddOnStartup(description = "Able to fill out forms")
	public static final String FORM_ENTRY = "Form Entry";
	
	@AddOnStartup(description = "Able to add/edit/retire patient identifier types")
	public static final String MANAGE_IDENTIFIER_TYPES = "Manage Identifier Types";
	
	public static final String PURGE_IDENTIFIER_TYPES = "Purge Identifier Types";
	
	@AddOnStartup(description = "Able to add/edit/retire concept classes")
	public static final String MANAGE_CONCEPT_CLASSES = "Manage Concept Classes";
	
	public static final String PURGE_CONCEPT_CLASSES = "Purge Concept Classes";
	
	@AddOnStartup(description = "Able to add/edit/retire concept datatypes")
	public static final String MANAGE_CONCEPT_DATATYPES = "Manage Concept Datatypes";
	
	public static final String PURGE_CONCEPT_DATATYPES = "Purge Concept Datatypes";
	
	@AddOnStartup(description = "Able to add/edit/delete privileges")
	public static final String MANAGE_PRIVILEGES = "Manage Privileges";
	
	public static final String PURGE_PRIVILEGES = "Purge Privileges";
	
	@AddOnStartup(description = "Able to add/edit/delete user roles")
	public static final String MANAGE_ROLES = "Manage Roles";
	
	public static final String PURGE_ROLES = "Purge Roles";
	
	@AddOnStartup(description = "Able to add/edit/retire field types")
	public static final String MANAGE_FIELD_TYPES = "Manage Field Types";
	
	public static final String PURGE_FIELD_TYPES = "Purge Field Types";
	
	@AddOnStartup(description = "Able to add/edit/retire order types")
	public static final String MANAGE_ORDER_TYPES = "Manage Order Types";
	
	public static final String PURGE_ORDER_TYPES = "Purge Order Types";
	
	@AddOnStartup(description = "Able to add/edit/retire relationship types")
	public static final String MANAGE_RELATIONSHIP_TYPES = "Manage Relationship Types";
	
	public static final String PURGE_RELATIONSHIP_TYPES = "Purge Relationship Types";
	
	@AddOnStartup(description = "Able to add/edit/delete user alerts")
	public static final String MANAGE_ALERTS = "Manage Alerts";
	
	@AddOnStartup(description = "Able to add/edit/delete concept sources")
	public static final String MANAGE_CONCEPT_SOURCES = "Manage Concept Sources";
	
	@AddOnStartup(description = "Able to add/edit/retire concept map types")
	public static final String MANAGE_CONCEPT_MAP_TYPES = "Manage Concept Map Types";
	
	public static final String PURGE_CONCEPT_MAP_TYPES = "Purge Concept Map Types";
	
	@AddOnStartup(description = "Able to add/edit/retire reference terms")
	public static final String MANAGE_CONCEPT_REFERENCE_TERMS = "Manage Concept Reference Terms";
	
	public static final String CREATE_REFERENCE_TERMS_WHILE_EDITING_CONCEPTS = "Create Reference Terms While Editing Concepts";
	
	public static final String PURGE_CONCEPT_REFERENCE_TERMS = "Purge Concept Reference Terms";
	
	public static final String PURGE_CONCEPT_SOURCES = "Purge Concept Sources";
	
	@AddOnStartup(description = "Able to view the navigation menu (Home, View Patients, Dictionary, Administration, My Profile")
	public static final String VIEW_NAVIGATION_MENU = "View Navigation Menu";
	
	@AddOnStartup(description = "Able to view the 'Administration' link in the navigation bar")
	public static final String VIEW_ADMIN_FUNCTIONS = "View Administration Functions";
	
	@AddOnStartup(description = "Able to view and fill out unpublished forms")
	public static final String VIEW_UNPUBLISHED_FORMS = "View Unpublished Forms";
	
	@AddOnStartup(description = "Able to add/view/delete patient programs")
	public static final String MANAGE_PROGRAMS = "Manage Programs";
	
	@AddOnStartup(description = "Able to add patients to programs")
	public static final String ADD_PATIENT_PROGRAMS = "Add Patient Programs";
	
	@AddOnStartup(description = "Able to edit patients in programs")
	public static final String EDIT_PATIENT_PROGRAMS = "Edit Patient Programs";
	
	@AddOnStartup(description = "Able to delete patients from programs")
	public static final String DELETE_PATIENT_PROGRAMS = "Delete Patient Programs";
	
	public static final String PURGE_PATIENT_PROGRAMS = "Purge Patient Programs";
	
	@AddOnStartup(description = "Able to add/edit global properties")
	public static final String MANAGE_GLOBAL_PROPERTIES = "Manage Global Properties";
	
	public static final String PURGE_GLOBAL_PROPERTIES = "Purge Global Properties";
	
	@AddOnStartup(description = "Able to add/remove modules to the system")
	public static final String MANAGE_MODULES = "Manage Modules";
	
	@AddOnStartup(description = "Able to add/edit/remove scheduled tasks")
	public static final String MANAGE_SCHEDULER = "Manage Scheduler";
	
	@AddOnStartup(description = "Able to add/edit/retire person attribute types")
	public static final String MANAGE_PERSON_ATTRIBUTE_TYPES = "Manage Person Attribute Types";
	
	public static final String PURGE_PERSON_ATTRIBUTE_TYPES = "Purge Person Attribute Types";
	
	@AddOnStartup(description = "Able to add person objects")
	public static final String ADD_PERSONS = "Add People";
	
	@AddOnStartup(description = "Able to add person objects")
	public static final String EDIT_PERSONS = "Edit People";
	
	@AddOnStartup(description = "Able to delete objects")
	public static final String DELETE_PERSONS = "Delete People";
	
	public static final String PURGE_PERSONS = "Purge People";
	
	@AddOnStartup(description = "Able to add relationships")
	public static final String ADD_RELATIONSHIPS = "Add Relationships";
	
	@AddOnStartup(description = "Able to edit relationships")
	public static final String EDIT_RELATIONSHIPS = "Edit Relationships";
	
	@AddOnStartup(description = "Able to delete relationships")
	public static final String DELETE_RELATIONSHIPS = "Delete Relationships";
	
	public static final String PURGE_RELATIONSHIPS = "Purge Relationships";
	
	@AddOnStartup(description = "Able to view/add/edit the implementation id for the system")
	public static final String MANAGE_IMPLEMENTATION_ID = "Manage Implementation Id";
	
	public static final String SQL_LEVEL_ACCESS = "SQL Level Access";
	
	@AddOnStartup(description = "Add problems")
	public static final String ADD_PROBLEMS = "Add Problems";
	
	@AddOnStartup(description = "Able to edit problems")
	public static final String EDIT_PROBLEMS = "Edit Problems";
	
	@AddOnStartup(description = "Remove problems")
	public static final String DELETE_PROBLEMS = "Remove Problems";
	
	@AddOnStartup(description = "Add allergies")
	public static final String ADD_ALLERGIES = "Add Allergies";
	
	@AddOnStartup(description = "Able to edit allergies")
	public static final String EDIT_ALLERGIES = "Edit Allergies";
	
	@AddOnStartup(description = "Remove allergies")
	public static final String DELETE_ALLERGIES = "Remove Allergies";
	
	@AddOnStartup(description = "Able to view/add/remove the concept stop words")
	public static final String MANAGE_CONCEPT_STOP_WORDS = "Manage Concept Stop Words";
	
	@AddOnStartup(description = "Able to add an HL7 Source")
	public static final String PRIV_ADD_HL7_SOURCE = "Add HL7 Source";
	
	@AddOnStartup(description = "Able to update an HL7 Source")
	public static final String PRIV_UPDATE_HL7_SOURCE = "Update HL7 Source";
	
	public static final String PRIV_PURGE_HL7_SOURCE = "Purge HL7 Source";
	
	@AddOnStartup(description = "Able to add an HL7 Queue item")
	public static final String PRIV_ADD_HL7_IN_QUEUE = "Add HL7 Inbound Queue";
	
	@AddOnStartup(description = "Able to update an HL7 Queue item")
	public static final String PRIV_UPDATE_HL7_IN_QUEUE = "Update HL7 Inbound Queue";
	
	@AddOnStartup(description = "Able to delete an HL7 Queue item")
	public static final String PRIV_DELETE_HL7_IN_QUEUE = "Delete HL7 Inbound Queue";
	
	public static final String PRIV_PURGE_HL7_IN_QUEUE = "Purge HL7 Inbound Queue";
	
	@AddOnStartup(description = "Able to add an HL7 archive item")
	public static final String PRIV_ADD_HL7_IN_ARCHIVE = "Add HL7 Inbound Archive";
	
	@AddOnStartup(description = "Able to update an HL7 archive item")
	public static final String PRIV_UPDATE_HL7_IN_ARCHIVE = "Update HL7 Inbound Archive";
	
	@AddOnStartup(description = "Able to delete/retire an HL7 archive item")
	public static final String PRIV_DELETE_HL7_IN_ARCHIVE = "Delete HL7 Inbound Archive";
	
	public static final String PRIV_PURGE_HL7_IN_ARCHIVE = "Purge HL7 Inbound Archive";
	
	@AddOnStartup(description = "Able to add an HL7 error item")
	public static final String PRIV_ADD_HL7_IN_EXCEPTION = "Add HL7 Inbound Exception";
	
	@AddOnStartup(description = "Able to update an HL7 archive item")
	public static final String PRIV_UPDATE_HL7_IN_EXCEPTION = "Update HL7 Inbound Exception";
	
	@AddOnStartup(description = "Able to delete an HL7 archive item")
	public static final String PRIV_DELETE_HL7_IN_EXCEPTION = "Delete HL7 Inbound Exception";
	
	public static final String PRIV_PURGE_HL7_IN_EXCEPTION = "Purge HL7 Inbound Exception";
	
	@AddOnStartup(description = "Able to add/edit/delete visit types")
	public static final String MANAGE_VISIT_TYPES = "Manage Visit Types";
	
	@AddOnStartup(description = "Able to add visits")
	public static final String ADD_VISITS = "Add Visits";
	
	@AddOnStartup(description = "Able to edit visits")
	public static final String EDIT_VISITS = "Edit Visits";
	
	@AddOnStartup(description = "Able to delete visits")
	public static final String DELETE_VISITS = "Delete Visits";
	
	public static final String PURGE_VISITS = "Purge Visits";
	
	@AddOnStartup(description = "Able to add/edit/retire visit attribute types")
	public static final String MANAGE_VISIT_ATTRIBUTE_TYPES = "Manage Visit Attribute Types";
	
	public static final String PURGE_VISIT_ATTRIBUTE_TYPES = "Purge Visit Attribute Types";
	
	@AddOnStartup(description = "Able to add/edit/retire location attribute types")
	public static final String MANAGE_LOCATION_ATTRIBUTE_TYPES = "Manage Location Attribute Types";
	
	public static final String PURGE_LOCATION_ATTRIBUTE_TYPES = "Purge Location Attribute Types";
	
	@AddOnStartup(description = "Able to edit Provider")
	public static final String MANAGE_PROVIDERS = "Manage Providers";
	
	public static final String PURGE_PROVIDERS = "Purge Providers";
	
	public static final String PURGE_ENCOUNTER_ROLES = "Purge Encounter Roles";
	
	@AddOnStartup(description = "Able to add/edit/retire encounter roles")
	public static final String MANAGE_ENCOUNTER_ROLES = "Manage Encounter Roles";
	
	@AddOnStartup(description = "Able to assign System Developer role")
	public static final String ASSIGN_SYSTEM_DEVELOPER_ROLE = "Assign System Developer Role";
	
	@AddOnStartup(description = "Able to get Order Frequencies")
	public static final String GET_ORDER_FREQUENCIES = "Get Order Frequencies";
	
	@AddOnStartup(description = "Able to add/edit/retire Order Frequencies")
	public static final String MANAGE_ORDER_FREQUENCIES = "Manage Order Frequencies";
	
	public static final String PURGE_ORDER_FREQUENCIES = "Purge Order Frequencies";
	
	@AddOnStartup(description = "Able to get Care Settings")
	public static final String GET_CARE_SETTINGS = "Get Care Settings";

	@AddOnStartup(description = "Able to manage the search index")
	public static final String MANAGE_SEARCH_INDEX = "Manage Search Index";

	@AddOnStartup(description = "Able to add, edit, delete allergies")
	public static final String MODIFY_ALLERGIES = "Task: Modify Allergies";

	@AddOnStartup(description = "Able to get order sets")
	public static final String GET_ORDER_SETS = "Get Order Sets";
	
	@AddOnStartup(description = "Able to manage order sets")
	public static final String MANAGE_ORDER_SETS = "Manage Order Sets";

	@AddOnStartup(description = "Able to get concept attribute types")
	public static final String GET_CONCEPT_ATTRIBUTE_TYPES = "Get Concept Attribute Types";

	@AddOnStartup(description = "Able to add/edit/retire concept attribute types")
	public static final String MANAGE_CONCEPT_ATTRIBUTE_TYPES = "Manage Concept Attribute Types";

	public static final String PURGE_CONCEPT_ATTRIBUTE_TYPES = "Purge Concept Attribute Types";

	public static final String EDIT_CONDITIONS = "Edit Conditions";

	public static final String GET_CONDITIONS = "Get Conditions";
	
	public static final String DELETE_CONDITIONS = "Delete Conditions";

	public static final String EDIT_DIAGNOSES = "Edit Diagnoses";

	public static final String GET_DIAGNOSES = "Get Diagnoses";

	public static final String DELETE_DIAGNOSES = "Delete Diagnoses";

}
