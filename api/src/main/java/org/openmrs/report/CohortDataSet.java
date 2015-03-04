/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.report;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.Cohort;

/**
 * A dataset which represents a list of cohorts, each of which has a name. For example a
 * CohortDataset might represent: "1. Total # of Patients" -> 123 "1.a. Male Adults" -> Cohort of 54
 * patients "1.b. Female Adults" -> Cohort of 43 patients "1.c. Male Children" -> Cohort of 12
 * patients "1.d. Female Children" -> Cohort of 14 patient ...
 * 
 * @see CohortDataSetDefinition
 * @see CohortDataSetProvider
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class CohortDataSet implements MapDataSet<Cohort> {
	
	private CohortDataSetDefinition definition;
	
	private EvaluationContext evaluationContext;
	
	private String name;
	
	private Map<String, Cohort> cohortData;
	
	public CohortDataSet() {
		cohortData = new LinkedHashMap<String, Cohort>();
	}
	
	/**
	 * @see org.openmrs.report.MapDataSet#getData()
	 */
	public Map<String, Cohort> getData() {
		return cohortData;
	}
	
	/**
	 * Returns this map as a single-row data set
	 * 
	 * @see org.openmrs.report.DataSet#iterator()
	 */
	public Iterator<Map<String, Cohort>> iterator() {
		return Collections.singleton(cohortData).iterator();
	}
	
	public Map<String, Cohort> getCohortData() {
		return cohortData;
	}
	
	public void setCohortData(Map<String, Cohort> cohortData) {
		this.cohortData = cohortData;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @see DataSet#getDefinition()
	 */
	public DataSetDefinition getDefinition() {
		return definition;
	}
	
	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(CohortDataSetDefinition definition) {
		this.definition = definition;
	}
	
	/**
	 * @see DataSet#getEvaluationContext()
	 */
	public EvaluationContext getEvaluationContext() {
		return evaluationContext;
	}
	
	/**
	 * @param evaluationContext the evaluationContext to set
	 */
	public void setEvaluationContext(EvaluationContext evaluationContext) {
		this.evaluationContext = evaluationContext;
	}
	
}
