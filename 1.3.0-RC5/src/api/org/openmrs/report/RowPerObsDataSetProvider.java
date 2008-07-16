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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;

/**
 *
 */
public class RowPerObsDataSetProvider implements DataSetProvider {

	protected Log log = LogFactory.getLog(this.getClass());
	
	public RowPerObsDataSetProvider() { }
	
	/**
	 * @see org.openmrs.report.DataSetProvider#canEvaluate(org.openmrs.report.DataSetDefinition)
	 */
	public boolean canEvaluate(DataSetDefinition dataSetDefinition) {
		return dataSetDefinition instanceof RowPerObsDataSetDefinition;
	}

	/**
	 * @see org.openmrs.report.DataSetProvider#evaluate(org.openmrs.report.DataSetDefinition, org.openmrs.Cohort, org.openmrs.report.EvaluationContext)
	 */
	public DataSet evaluate(DataSetDefinition dataSetDefinition,
	        Cohort inputCohort, EvaluationContext evalContext) {
	
		RowPerObsDataSetDefinition definition = (RowPerObsDataSetDefinition) dataSetDefinition;
		Cohort patients = inputCohort;
		if (definition.getFilter() != null) {
			if (patients != null)
				patients = Cohort.intersect(patients, Context.getCohortService().evaluate(definition.getFilter(), evalContext));
			else
				patients = Context.getCohortService().evaluate(definition.getFilter(), evalContext);
		}

		RowPerObsDataSet ret = new RowPerObsDataSet();
		ret.setDefinition(definition);
		ret.setEvaluationContext(evalContext);
		List<Concept> concepts = new ArrayList<Concept>(definition.getQuestions());
		List<Obs> list = Context.getObsService().getObservations(patients, concepts, definition.getFromDate(), definition.getToDate());
		ret.setData(list);
		return ret;
	}

}
