/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import org.openmrs.annotation.AddOnStartup;
import org.openmrs.annotation.HasAddOnStartupPrivileges;

/**
 * Contains application privilege names and their descriptions. Some of privilege names may be marked with
 * AddOnStartup annotation to indicate that they should be put into the database at app startup.
 * 
 * @see org.openmrs.annotation.AddOnStartup
 * @since 1.10
 */
@HasAddOnStartupPrivileges
public class ApplicationPrivilegeConstants {
	
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
	
	@AddOnStartup(description = "Able to view the 'Visits' tab on the patient dashboard")
	public static final String DASHBOARD_VISITS = "Patient Dashboard - View Visits Section";
	
	@AddOnStartup(description = "Able to view the 'Patient Flags' portlet on the patient dashboard's overview tab")
	public static final String PATIENT_OVERVIEW_PATIENT_FLAGS = "Patient Overview - View Patient Flags";
	
	@AddOnStartup(description = "Able to view the 'Patient Actions' portlet on the patient dashboard's overview tab")
	public static final String PATIENT_OVERVIEW_PATIENT_ACTIONS = "Patient Overview - View Patient Actions";
	
	@AddOnStartup(description = "Able to view the 'Programs' portlet on the patient dashboard's overview tab")
	public static final String PATIENT_OVERVIEW_PROGRAMS = "Patient Overview - View Programs";
	
	@AddOnStartup(description = "Able to view the 'Relationships' portlet on the patient dashboard's overview tab")
	public static final String PATIENT_OVERVIEW_RELATIONSHIPS = "Patient Overview - View Relationships";
	
	@AddOnStartup(description = "Able to view the 'Allergies' portlet on the patient dashboard's overview tab")
	public static final String PATIENT_OVERVIEW_ALLERGIES = "Patient Overview - View Allergies";
	
	@AddOnStartup(description = "Able to view the 'Problem List' portlet on the patient dashboard's overview tab")
	public static final String PATIENT_OVERVIEW_PROBLEM_LIST = "Patient Overview - View Problem List";
}
