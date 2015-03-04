/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.cohort.impl;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.cohort.CohortDefinitionItemHolder;
import org.openmrs.cohort.CohortDefinitionProvider;
import org.openmrs.report.EvaluationContext;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * This class provides access to {@link org.openmrs.reporting.PatientSearch} objects that are saved
 * in the report_object table, but exposes them as {@link CohortDefinition}
 * 
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class PatientSearchCohortDefinitionProvider implements CohortDefinitionProvider {
	
	/**
	 * TODO: this is potentially not necessary because its set via spring's application context
	 * 
	 * @see org.openmrs.cohort.CohortDefinitionProvider#getClassHandled()
	 */
	public Class<? extends CohortDefinition> getClassHandled() {
		return PatientSearch.class;
	}
	
	/**
	 * @see org.openmrs.cohort.CohortDefinitionProvider#evaluate(org.openmrs.cohort.CohortDefinition,
	 *      org.openmrs.report.EvaluationContext)
	 */
	public Cohort evaluate(CohortDefinition cohortDefinition, EvaluationContext evaluationContext) {
		PatientSearch search = (PatientSearch) cohortDefinition;
		PatientFilter filter = OpenmrsUtil.toPatientFilter(search, null, evaluationContext);
		Cohort ret = filter.filter(null, evaluationContext);
		ret.setCohortDefinition(cohortDefinition);
		ret.setEvaluationContext(evaluationContext);
		return ret;
	}
	
	/**
	 * @see org.openmrs.cohort.CohortDefinitionProvider#getAllCohortDefinitions()
	 */
	public List<CohortDefinitionItemHolder> getAllCohortDefinitions() {
		List<CohortDefinitionItemHolder> ret = new ArrayList<CohortDefinitionItemHolder>();
		
		List<AbstractReportObject> patientSearches = Context.getReportObjectService().getReportObjectsByType(
		    OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);
		
		for (AbstractReportObject o : patientSearches) {
			PatientSearchReportObject psro = (PatientSearchReportObject) o;
			CohortDefinitionItemHolder item = new CohortDefinitionItemHolder();
			item.setKey(psro.getReportObjectId() + ":" + psro.getPatientSearch().getClass().getCanonicalName());
			item.setName(psro.getName());
			item.setCohortDefinition(psro.getPatientSearch());
			ret.add(item);
		}
		return ret;
	}
	
	/**
	 * @see org.openmrs.cohort.CohortDefinitionProvider#getCohortDefinition(java.lang.Integer)
	 */
	public CohortDefinition getCohortDefinition(Integer id) {
		PatientSearchReportObject psro = (PatientSearchReportObject) Context.getReportObjectService().getReportObject(id);
		return psro.getPatientSearch();
	}
	
	/**
	 * @see org.openmrs.cohort.CohortDefinitionProvider#purgeCohortDefinition(org.openmrs.cohort.CohortDefinition)
	 */
	public void purgeCohortDefinition(CohortDefinition cohortDefinition) {
		throw new APIException("Not Yet Implemented");
	}
	
	/**
	 * @see org.openmrs.cohort.CohortDefinitionProvider#saveCohortDefinition(org.openmrs.cohort.CohortDefinition)
	 */
	public CohortDefinition saveCohortDefinition(CohortDefinition cohortDefinition) {
		throw new APIException("Not Yet Implemented");
	}
	
}
