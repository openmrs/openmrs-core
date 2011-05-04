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

import org.openmrs.annotation.AddOnStartup;

/**
 * Contains all privilege names and their descriptions. Some of privilege names may be marked with
 * AddOnStartup annotation.
 * 
 * @see org.openmrs.annotation.AddOnStartup
 */
public class PrivilegeConstants {
	
	@AddOnStartup(description = "Able to view concept entries")
	public static final String VIEW_CONCEPTS = "View Concepts";
	
	@AddOnStartup(description = "Able to add/edit/delete concept entries")
	public static final String MANAGE_CONCEPTS = "Manage Concepts";
	
	public static final String PURGE_CONCEPTS = "Purge Concepts";
	
	@AddOnStartup(description = "Able to add/edit/delete concept name tags")
	public static final String MANAGE_CONCEPT_NAME_TAGS = "Manage Concept Name tags";
	
	@AddOnStartup(description = "Able to view concept proposals to the system")
	public static final String VIEW_CONCEPT_PROPOSALS = "View Concept Proposals";
	
	@AddOnStartup(description = "Able to add concept proposals to the system")
	public static final String ADD_CONCEPT_PROPOSALS = "Add Concept Proposals";
	
	@AddOnStartup(description = "Able to edit concept proposals in the system")
	public static final String EDIT_CONCEPT_PROPOSALS = "Edit Concept Proposals";
	
	@AddOnStartup(description = "Able to delete concept proposals from the system")
	public static final String DELETE_CONCEPT_PROPOSALS = "Delete Concept Proposals";
	
	public static final String PURGE_CONCEPT_PROPOSALS = "Purge Concept Proposals";
	
	@AddOnStartup(description = "Able to view users in OpenMRS")
	public static final String VIEW_USERS = "View Users";
	
	@AddOnStartup(description = "Able to add users to OpenMRS")
	public static final String ADD_USERS = "Add Users";
	
	@AddOnStartup(description = "Able to edit users in OpenMRS")
	public static final String EDIT_USERS = "Edit Users";
	
	@AddOnStartup(description = "Able to delete users in OpenMRS")
	public static final String DELETE_USERS = "Delete Users";
	
	public static final String PURGE_USERS = "Purge Users";
	
	@AddOnStartup(description = "Able to change the passwords of users in OpenMRS")
	public static final String EDIT_USER_PASSWORDS = "Edit User Passwords";
	
	@AddOnStartup(description = "Able to view patient encounters")
	public static final String VIEW_ENCOUNTERS = "View Encounters";
	
	@AddOnStartup(description = "Able to add patient encounters")
	public static final String ADD_ENCOUNTERS = "Add Encounters";
	
	@AddOnStartup(description = "Able to edit patient encounters")
	public static final String EDIT_ENCOUNTERS = "Edit Encounters";
	
	@AddOnStartup(description = "Able to delete patient encounters")
	public static final String DELETE_ENCOUNTERS = "Delete Encounters";
	
	public static final String PURGE_ENCOUNTERS = "Purge Encounters";
	
	@AddOnStartup(description = "Able to view encounter types")
	public static final String VIEW_ENCOUNTER_TYPES = "View Encounter Types";
	
	@AddOnStartup(description = "Able to add/edit/retire encounter types")
	public static final String MANAGE_ENCOUNTER_TYPES = "Manage Encounter Types";
	
	public static final String PURGE_ENCOUNTER_TYPES = "Purge Encounter Types";
	
	@AddOnStartup(description = "Able to view locations")
	public static final String VIEW_LOCATIONS = "View Locations";
	
	@AddOnStartup(description = "Able to add/edit/delete locations")
	public static final String MANAGE_LOCATIONS = "Manage Locations";
	
	public static final String PURGE_LOCATIONS = "Purge Locations";
	
	@AddOnStartup(description = "Able to add/edit/delete location tags")
	public static final String MANAGE_LOCATION_TAGS = "Manage Location Tags";
	
	public static final String PURGE_LOCATION_TAGS = "Purge Location Tags";
	
	@AddOnStartup(description = "Able to view patient observations")
	public static final String VIEW_OBS = "View Observations";
	
	@AddOnStartup(description = "Able to add patient observations")
	public static final String ADD_OBS = "Add Observations";
	
	@AddOnStartup(description = "Able to edit patient observations")
	public static final String EDIT_OBS = "Edit Observations";
	
	@AddOnStartup(description = "Able to delete patient observations")
	public static final String DELETE_OBS = "Delete Observations";
	
	public static final String PURGE_OBS = "Purge Observations";
	
	@AddOnStartup(description = "Able to view patients")
	public static final String VIEW_PATIENTS = "View Patients";
	
	@AddOnStartup(description = "Able to add patients")
	public static final String ADD_PATIENTS = "Add Patients";
	
	@AddOnStartup(description = "Able to edit patients")
	public static final String EDIT_PATIENTS = "Edit Patients";
	
	@AddOnStartup(description = "Able to delete patients")
	public static final String DELETE_PATIENTS = "Delete Patients";
	
	public static final String PURGE_PATIENTS = "Purge Patients";
	
	@AddOnStartup(description = "Able to view patient identifiers")
	public static final String VIEW_PATIENT_IDENTIFIERS = "View Patient Identifiers";
	
	@AddOnStartup(description = "Able to add patient identifiers")
	public static final String ADD_PATIENT_IDENTIFIERS = "Add Patient Identifiers";
	
	@AddOnStartup(description = "Able to edit patient identifiers")
	public static final String EDIT_PATIENT_IDENTIFIERS = "Edit Patient Identifiers";
	
	@AddOnStartup(description = "Able to delete patient identifiers")
	public static final String DELETE_PATIENT_IDENTIFIERS = "Delete Patient Identifiers";
	
	public static final String PURGE_PATIENT_IDENTIFIERS = "Purge Patient Identifiers";
	
	@AddOnStartup(description = "Able to view patient cohorts")
	public static final String VIEW_PATIENT_COHORTS = "View Patient Cohorts";
	
	@AddOnStartup(description = "Able to add a cohort to the system")
	public static final String ADD_COHORTS = "Add Cohorts";
	
	@AddOnStartup(description = "Able to add a cohort to the system")
	public static final String EDIT_COHORTS = "Edit Cohorts";
	
	@AddOnStartup(description = "Able to add a cohort to the system")
	public static final String DELETE_COHORTS = "Delete Cohorts";
	
	public static final String PURGE_COHORTS = "Purge Cohorts";
	
	@AddOnStartup(description = "Able to view orders")
	public static final String VIEW_ORDERS = "View Orders";
	
	@AddOnStartup(description = "Able to add orders")
	public static final String ADD_ORDERS = "Add Orders";
	
	@AddOnStartup(description = "Able to edit orders")
	public static final String EDIT_ORDERS = "Edit Orders";
	
	@AddOnStartup(description = "Able to delete orders")
	public static final String DELETE_ORDERS = "Delete Orders";
	
	public static final String PURGE_ORDERS = "Purge Orders";
	
	@AddOnStartup(description = "Able to view forms")
	public static final String VIEW_FORMS = "View Forms";
	
	@AddOnStartup(description = "Able to add/edit/delete forms")
	public static final String MANAGE_FORMS = "Manage Forms";
	
	public static final String PURGE_FORMS = "Purge Forms";
	
	// This name is historic, since that's what it was originally called in the
	// infopath formentry module
	@AddOnStartup(description = "Able to fill out forms")
	public static final String FORM_ENTRY = "Form Entry";
	
	@AddOnStartup(description = "Able to add/edit/retire patient identifier types")
	public static final String MANAGE_IDENTIFIER_TYPES = "Manage Identifier Types";
	
	@AddOnStartup(description = "Able to view patient identifier types")
	public static final String VIEW_IDENTIFIER_TYPES = "View Identifier Types";
	
	public static final String PURGE_IDENTIFIER_TYPES = "Purge Identifier Types";
	
	@AddOnStartup(description = "Able to view concept classes")
	public static final String VIEW_CONCEPT_CLASSES = "View Concept Classes";
	
	@AddOnStartup(description = "Able to add/edit/retire concept classes")
	public static final String MANAGE_CONCEPT_CLASSES = "Manage Concept Classes";
	
	public static final String PURGE_CONCEPT_CLASSES = "Purge Concept Classes";
	
	@AddOnStartup(description = "Able to view concept datatypes")
	public static final String VIEW_CONCEPT_DATATYPES = "View Concept Datatypes";
	
	@AddOnStartup(description = "Able to add/edit/retire concept datatypes")
	public static final String MANAGE_CONCEPT_DATATYPES = "Manage Concept Datatypes";
	
	public static final String PURGE_CONCEPT_DATATYPES = "Purge Concept Datatypes";
	
	@AddOnStartup(description = "Able to view user privileges")
	public static final String VIEW_PRIVILEGES = "View Privileges";
	
	@AddOnStartup(description = "Able to add/edit/delete privileges")
	public static final String MANAGE_PRIVILEGES = "Manage Privileges";
	
	public static final String PURGE_PRIVILEGES = "Purge Privileges";
	
	@AddOnStartup(description = "Able to view user roles")
	public static final String VIEW_ROLES = "View Roles";
	
	@AddOnStartup(description = "Able to add/edit/delete user roles")
	public static final String MANAGE_ROLES = "Manage Roles";
	
	public static final String PURGE_ROLES = "Purge Roles";
	
	@AddOnStartup(description = "Able to view field types")
	public static final String VIEW_FIELD_TYPES = "View Field Types";
	
	@AddOnStartup(description = "Able to add/edit/retire field types")
	public static final String MANAGE_FIELD_TYPES = "Manage Field Types";
	
	public static final String PURGE_FIELD_TYPES = "Purge Field Types";
	
	@AddOnStartup(description = "Able to view order types")
	public static final String VIEW_ORDER_TYPES = "View Order Types";
	
	@AddOnStartup(description = "Able to add/edit/retire order types")
	public static final String MANAGE_ORDER_TYPES = "Manage Order Types";
	
	public static final String PURGE_ORDER_TYPES = "Purge Order Types";
	
	@AddOnStartup(description = "Able to view relationship types")
	public static final String VIEW_RELATIONSHIP_TYPES = "View Relationship Types";
	
	@AddOnStartup(description = "Able to add/edit/retire relationship types")
	public static final String MANAGE_RELATIONSHIP_TYPES = "Manage Relationship Types";
	
	public static final String PURGE_RELATIONSHIP_TYPES = "Purge Relationship Types";
	
	@AddOnStartup(description = "Able to add/edit/delete user alerts")
	public static final String MANAGE_ALERTS = "Manage Alerts";
	
	@AddOnStartup(description = "Able to add/edit/delete concept sources")
	public static final String MANAGE_CONCEPT_SOURCES = "Manage Concept Sources";
	
	@AddOnStartup(description = "Able to view concept sources")
	public static final String VIEW_CONCEPT_SOURCES = "View Concept Sources";
	
	public static final String PURGE_CONCEPT_SOURCES = "Purge Concept Sources";
	
	@AddOnStartup(description = "Able to view the navigation menu (Home, View Patients, Dictionary, Administration, My Profile")
	public static final String VIEW_NAVIGATION_MENU = "View Navigation Menu";
	
	@AddOnStartup(description = "Able to view the 'Administration' link in the navigation bar")
	public static final String VIEW_ADMIN_FUNCTIONS = "View Administration Functions";
	
	@AddOnStartup(description = "Able to view and fill out unpublished forms")
	public static final String VIEW_UNPUBLISHED_FORMS = "View Unpublished Forms";
	
	@AddOnStartup(description = "Able to view patient programs")
	public static final String VIEW_PROGRAMS = "View Programs";
	
	@AddOnStartup(description = "Able to add/view/delete patient programs")
	public static final String MANAGE_PROGRAMS = "Manage Programs";
	
	@AddOnStartup(description = "Able to see which programs that patients are in")
	public static final String VIEW_PATIENT_PROGRAMS = "View Patient Programs";
	
	@AddOnStartup(description = "Able to add patients to programs")
	public static final String ADD_PATIENT_PROGRAMS = "Add Patient Programs";
	
	@AddOnStartup(description = "Able to edit patients in programs")
	public static final String EDIT_PATIENT_PROGRAMS = "Edit Patient Programs";
	
	@AddOnStartup(description = "Able to delete patients from programs")
	public static final String DELETE_PATIENT_PROGRAMS = "Delete Patient Programs";
	
	public static final String PURGE_PATIENT_PROGRAMS = "Purge Patient Programs";
	
	@AddOnStartup(description = "Able to view the 'Overview' tab on the patient dashboard")
	public static final String DASHBOARD_OVERVIEW = "Patient Dashboard - View Overview Section";
	
	@AddOnStartup(description = "Able to view the 'Regimen' tab on the patient dashboard")
	public static final String DASHBOARD_REGIMEN = "Patient Dashboard - View Regimen Section";
	
	@AddOnStartup(description = "Able to view the 'Encounters' tab on the patient dashboard")
	public static final String DASHBOARD_ENCOUNTERS = "Patient Dashboard - View Encounters Section";
	
	@AddOnStartup(description = "Able to view the 'Demographics' tab on the patient dashboard")
	public static final String DASHBOARD_DEMOGRAPHICS = "Patient Dashboard - View Demographics Section";
	
	@AddOnStartup(description = "Able to view the 'Graphs' tab on the patient dashboard")
	public static final String DASHBOARD_GRAPHS = "Patient Dashboard - View Graphs Section";
	
	@AddOnStartup(description = "Able to view the 'Forms' tab on the patient dashboard")
	public static final String DASHBOARD_FORMS = "Patient Dashboard - View Forms Section";
	
	@AddOnStartup(description = "Able to view the 'Summary' tab on the patient dashboard")
	public static final String DASHBOARD_SUMMARY = "Patient Dashboard - View Patient Summary";
	
	@AddOnStartup(description = "Able to view global properties on the administration screen")
	public static final String VIEW_GLOBAL_PROPERTIES = "View Global Properties";
	
	@AddOnStartup(description = "Able to add/edit global properties")
	public static final String MANAGE_GLOBAL_PROPERTIES = "Manage Global Properties";
	
	public static final String PURGE_GLOBAL_PROPERTIES = "Purge Global Properties";
	
	@AddOnStartup(description = "Able to add/remove modules to the system")
	public static final String MANAGE_MODULES = "Manage Modules";
	
	@AddOnStartup(description = "Able to add/edit/remove scheduled tasks")
	public static final String MANAGE_SCHEDULER = "Manage Scheduler";
	
	@AddOnStartup(description = "Able to view person attribute types")
	public static final String VIEW_PERSON_ATTRIBUTE_TYPES = "View Person Attribute Types";
	
	@AddOnStartup(description = "Able to add/edit/retire person attribute types")
	public static final String MANAGE_PERSON_ATTRIBUTE_TYPES = "Manage Person Attribute Types";
	
	public static final String PURGE_PERSON_ATTRIBUTE_TYPES = "Purge Person Attribute Types";
	
	@AddOnStartup(description = "Able to view person objects")
	public static final String VIEW_PERSONS = "View People";
	
	@AddOnStartup(description = "Able to add person objects")
	public static final String ADD_PERSONS = "Add People";
	
	@AddOnStartup(description = "Able to add person objects")
	public static final String EDIT_PERSONS = "Edit People";
	
	@AddOnStartup(description = "Able to delete objects")
	public static final String DELETE_PERSONS = "Delete People";
	
	public static final String PURGE_PERSONS = "Purge People";
	
	@AddOnStartup(description = "Able to view relationships")
	public static final String VIEW_RELATIONSHIPS = "View Relationships";
	
	@AddOnStartup(description = "Able to add relationships")
	public static final String ADD_RELATIONSHIPS = "Add Relationships";
	
	@AddOnStartup(description = "Able to edit relationships")
	public static final String EDIT_RELATIONSHIPS = "Edit Relationships";
	
	@AddOnStartup(description = "Able to delete relationships")
	public static final String DELETE_RELATIONSHIPS = "Delete Relationships";
	
	public static final String PURGE_RELATIONSHIPS = "Purge Relationships";
	
	@AddOnStartup(description = "Able to view database changes from the admin screen")
	public static final String VIEW_DATABASE_CHANGES = "View Database Changes";
	
	@AddOnStartup(description = "Able to view/add/edit the implementation id for the system")
	public static final String MANAGE_IMPLEMENTATION_ID = "Manage Implementation Id";
	
	public static final String SQL_LEVEL_ACCESS = "SQL Level Access";
	
	@AddOnStartup(description = "Able to view problems")
	public static final String VIEW_PROBLEMS = "View Problems";
	
	@AddOnStartup(description = "Add problems")
	public static final String ADD_PROBLEMS = "Add Problems";
	
	@AddOnStartup(description = "Able to edit problems")
	public static final String EDIT_PROBLEMS = "Edit Problems";
	
	@AddOnStartup(description = "Remove problems")
	public static final String DELETE_PROBLEMS = "Remove Problems";
	
	@AddOnStartup(description = "Able to view allergies")
	public static final String VIEW_ALLERGIES = "View Allergies";
	
	@AddOnStartup(description = "Add allergies")
	public static final String ADD_ALLERGIES = "Add Allergies";
	
	@AddOnStartup(description = "Able to edit allergies")
	public static final String EDIT_ALLERGIES = "Edit Allergies";
	
	@AddOnStartup(description = "Remove allergies")
	public static final String DELETE_ALLERGIES = "Remove Allergies";
	
	@AddOnStartup(description = "Able to view/add/remove the concept stop words")
	public static final String MANAGE_CONCEPT_STOP_WORDS = "Manage Concept Stop Words";
	
	@AddOnStartup(description = "Able to view visit types")
	public static final String VIEW_VISIT_TYPES = "View Visit Types";
	
	@AddOnStartup(description = "Able to add/edit/delete visit types")
	public static final String MANAGE_VISIT_TYPES = "Manage Visit Types";
	
	@AddOnStartup(description = "Able to view visits")
	public static final String VIEW_VISITS = "View Visits";
	
	@AddOnStartup(description = "Able to add visits")
	public static final String ADD_VISITS = "Add Visits";
	
	@AddOnStartup(description = "Able to edit visits")
	public static final String EDIT_VISITS = "Edit Visits";
	
	@AddOnStartup(description = "Able to delete visits")
	public static final String DELETE_VISITS = "Delete Visits";
	
	@AddOnStartup(description = "Able to purge visits")
	public static final String PURGE_VISITS = "Purge Visits";
	
	@AddOnStartup(description = "Able to view visit attribute types")
	public static final String VIEW_VISIT_ATTRIBUTE_TYPES = "View Visit Attribute Types";
	
	@AddOnStartup(description = "Able to add/edit/retire visit attribute types")
	public static final String MANAGE_VISIT_ATTRIBUTE_TYPES = "Manage Visit Attribute Types";
	
	@AddOnStartup(description = "Able to purge visit attribute types")
	public static final String PURGE_VISIT_ATTRIBUTE_TYPES = "Purge Visit Attribute Types";
	
	@AddOnStartup(description = "Able to view the 'Visits' tab on the patient dashboard")
	public static final String DASHBOARD_VISITS = "Patient Dashboard - View Visits Section";
}
