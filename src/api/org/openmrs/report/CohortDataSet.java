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
 */
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
