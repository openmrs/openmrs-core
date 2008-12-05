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
package org.openmrs.web.dwr;

import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.cohort.CohortUtil;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.Parameter;
import org.openmrs.util.OpenmrsConstants;

/**
 * This class exposes some of the methods in {@link org.openmrs.api.CohortService}
 * via the dwr package
 */
public class DWRCohortService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Adds the {@link Patient} identified by <code>patientId</code> to the
	 * {@link Cohort} identified by <code>cohortId</code>
	 * @param cohortId - Identifies the {@link Cohort} to add to
	 * @param patientId - Identifies the {@link Patient} to add
	 */
	public void addPatientToCohort(Integer cohortId, Integer patientId) {
		Patient p = Context.getPatientService().getPatient(patientId);
		Cohort c = Context.getCohortService().getCohort(cohortId);
		Context.getCohortService().addPatientToCohort(c, p);
	}
	
	/**
	 * Removes the {@link Patient} identified by <code>patientId</code> from the
	 * {@link Cohort} identified by <code>cohortId</code>
	 * @param cohortId - Identifies the {@link Cohort} to remove from
	 * @param patientId - Identifies the {@link Patient} to remove
	 */
	public void removePatientFromCohort(Integer cohortId, Integer patientId) {
		Patient p = Context.getPatientService().getPatient(patientId);
		Cohort c = Context.getCohortService().getCohort(cohortId);
		Context.getCohortService().removePatientFromCohort(c, p);
	}

	/**
	 * Returns a Vector<ListItem> of all saved Cohorts
	 * @return Vector<ListItem> - all saved Cohorts
	 */
	public Vector<ListItem> getCohorts() {
		Vector<ListItem> ret = new Vector<ListItem>();
		for (Cohort c : Context.getCohortService().getAllCohorts()) {
			ret.add(new ListItem(c.getCohortId(), c.getName(), c.getDescription()));
		}
		return ret;
	}
	
	/**
	 * Returns a Vector<ListItem> of all saved Cohorts containing 
	 * the {@link Patient} identified by <code>patientId</code>
	 * 
	 * @param patientId - Identifies the {@link Patient} to lookup in each {@link Cohort}
	 * @return Vector<ListItem> - of all saved Cohorts containing the {@link Patient} 
	 * 							  identified by <code>patientId</code>
	 */
	public Vector<ListItem> getCohortsContainingPatient(Integer patientId) {
		Vector<ListItem> ret = new Vector<ListItem>();
		for (Cohort c : Context.getCohortService().getCohortsContainingPatientId(patientId)) {
			ret.add(new ListItem(c.getCohortId(), c.getName(), c.getDescription()));
		}
		return ret;
	}
	
	/**
	 * Accepts an input cohortSpecification String, which represents a Cohort Definition to evaluate.
	 * Returns a Vector of Parameters that are required to evaluate the given CohortDefinition.
	 * Any parameter value that is an expression as determined by {@link EvaluationContext#isExpression(String)}
	 * will be returned.
	 * 
	 * For example:
	 * For a cohortSpecification of [Male], an empty Vector is returned, as there are no parameters.
	 * For a cohortSpecification of [PregnantOnDate|effectiveDate=${?}], a Vector containing "effectiveDate" is returned
	 * 
	 * @param cohortSpecification - This input String represents the Cohort Definition to evaluate
	 * @return Vector<Parameter> containing all Parameters that need to be provided to evaluate the input cohortSpecification
	 */
	public Vector<Parameter> getMissingParameters(String cohortSpecification) {
		Vector<Parameter> ret = new Vector<Parameter>();
		CohortDefinition def = CohortUtil.parse(cohortSpecification);
		for (Parameter p : def.getParameters()) {
			ret.add(p);
		}
		return ret;
	}
	
	/**
	 * Accepts an input cohortSpecification String, which represents a Cohort Definition to evaluate, along
	 * with a Map<Parameter, Object> which provides values for each missing parameter
	 * @param cohortSpecification - This input String represents the Cohort Definition to evaluate
	 * @return Cohort - The Cohort of patients that are returned
	 */
	public Cohort evaluateCohortDefinition(String cohortSpecification, Map<Parameter, Object> parameterValues) {
		CohortDefinition def = CohortUtil.parse(cohortSpecification);
		EvaluationContext evalContext = new EvaluationContext();
		if (parameterValues != null) {
			for (Parameter p : parameterValues.keySet()) {
				Object v = parameterValues.get(p);
				evalContext.addParameterValue(p, v);
			}
		}
		return Context.getCohortService().evaluate(def, evalContext);
	}

}
