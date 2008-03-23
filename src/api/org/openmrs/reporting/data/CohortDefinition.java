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
package org.openmrs.reporting.data;

import java.io.Serializable;

import org.openmrs.Cohort;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;


/**
 * This class allows us to define a Cohort based on a patient filter or 
 * a cohort instance.  
 * 
 * @author Justin Miranda
 *
 */
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
	 *
	 */
	public CohortDefinition() { }	
	
	/**
	 * Get the cohort instance assigned to this cohort definition
	 * @return
	 */
	public Cohort getCohort() { 
		return cohort;
	}
	
	/**
	 * Set the cohort instance for this cohort definition.
	 * @param cohort
	 */
	public void setCohort(Cohort cohort) { 
		this.cohort = cohort;
	}
	
	
	/**
	 * Get the patient filter assigned to the cohort definition.
	 * @return
	 */
	public PatientFilter getPatientFilter() { 
		return patientFilter;
	}
	
	/**
	 * Set the patient filter.
	 * @param filter
	 */
	public void setPatientFilter(PatientFilter filter) { 
		this.patientFilter = filter;
	}
	
}
