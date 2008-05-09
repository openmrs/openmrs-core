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
package org.openmrs.cohort.impl;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.cohort.CohortDefinitionProvider;
import org.openmrs.report.EvaluationContext;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * This class provides access to {@link org.openmrs.reporting.PatientSearch} objects that
 * are saved in the report_object table, but exposes them as {@link CohortDefinition}
 */
public class PatientSearchCohortDefinitionProvider implements
        CohortDefinitionProvider {

	/**
	 * TODO: this is potentially not necessary because its set via spring's
	 * application context
     * @see org.openmrs.cohort.CohortDefinitionProvider#getClassHandled()
     */
    public Class<? extends CohortDefinition> getClassHandled() {
	    return PatientSearch.class;
    }
    
    /**
	 * @see org.openmrs.cohort.CohortDefinitionProvider#evaluate(org.openmrs.cohort.CohortDefinition, org.openmrs.report.EvaluationContext)
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
	public List<CohortDefinition> getAllCohortDefinitions() {
		List<CohortDefinition> ret = new ArrayList<CohortDefinition>();
		for (AbstractReportObject o : Context.getReportObjectService().getReportObjectsByType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH)) {
			PatientSearchReportObject s = (PatientSearchReportObject) o;
			ret.add(s.getPatientSearch());
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
