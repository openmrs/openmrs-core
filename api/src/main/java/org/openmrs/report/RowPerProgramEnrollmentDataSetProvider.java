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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.context.Context;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class RowPerProgramEnrollmentDataSetProvider implements DataSetProvider {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	public RowPerProgramEnrollmentDataSetProvider() {
	}
	
	/**
	 * @see org.openmrs.report.DataSetProvider#canEvaluate(org.openmrs.report.DataSetDefinition)
	 */
	public boolean canEvaluate(DataSetDefinition dataSetDefinition) {
		return dataSetDefinition instanceof RowPerProgramEnrollmentDataSetDefinition;
	}
	
	/**
	 * @see org.openmrs.report.DataSetProvider#evaluate(org.openmrs.report.DataSetDefinition,
	 *      org.openmrs.Cohort, org.openmrs.report.EvaluationContext)
	 */
	public DataSet<?> evaluate(DataSetDefinition dataSetDefinition, Cohort inputCohort, EvaluationContext evalContext) {
		
		RowPerProgramEnrollmentDataSetDefinition definition = (RowPerProgramEnrollmentDataSetDefinition) dataSetDefinition;
		Cohort patients = inputCohort;
		if (definition.getFilter() != null) {
			if (patients != null)
				patients = Cohort.intersect(patients, Context.getCohortService().evaluate(definition.getFilter(),
				    evalContext));
			else
				patients = Context.getCohortService().evaluate(definition.getFilter(), evalContext);
		}
		
		RowPerProgramEnrollmentDataSet ret = new RowPerProgramEnrollmentDataSet();
		ret.setDefinition(definition);
		ret.setEvaluationContext(evalContext);
		List<Program> programs = new ArrayList<Program>(definition.getPrograms());
		List<PatientProgram> list = Context.getProgramWorkflowService().getPatientPrograms(patients, programs);
		ret.setData(list);
		return ret;
	}
	
}
