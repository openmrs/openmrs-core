/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting.data;

import java.io.Serializable;

import org.openmrs.Cohort;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;

/**
 * This class allows us to define a Cohort based on a patient filter or a cohort instance.
 * 
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class CohortDefinition extends AbstractReportObject implements Serializable {
	
	/* Serial version ID */
	private static final long serialVersionUID = 1L;
	
	/* Type of report object */
	public final static String TYPE_NAME = "Cohort Definition";
	
	/* Subtype (classifier) */
	public final static String SUB_TYPE_NAME = "Cohort Definition";
	
	/* Cohort */
	private Cohort cohort;
	
	/* Patient filter */
	private PatientFilter patientFilter;
	
	/**
	 * Default public constructor
	 */
	public CohortDefinition() {
	}
	
	/**
	 * Get the cohort instance assigned to this cohort definition
	 */
	public Cohort getCohort() {
		return cohort;
	}
	
	/**
	 * Set the cohort instance for this cohort definition.
	 * 
	 * @param cohort
	 */
	public void setCohort(Cohort cohort) {
		this.cohort = cohort;
	}
	
	/**
	 * Get the patient filter assigned to the cohort definition.
	 */
	public PatientFilter getPatientFilter() {
		return patientFilter;
	}
	
	/**
	 * Set the patient filter.
	 * 
	 * @param filter
	 */
	public void setPatientFilter(PatientFilter filter) {
		this.patientFilter = filter;
	}
	
}
