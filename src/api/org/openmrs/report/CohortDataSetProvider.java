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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinition;

/**
 * The logic that evaluates a {@link CohortDataSetDefinition} and produces a {@link CohortDataSet}
 * 
 * @see CohortDataSetDefinition
 * @see CohortDataSet
 */
public class CohortDataSetProvider implements DataSetProvider {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	public CohortDataSetProvider() {
	}
	
	/**
	 * @see org.openmrs.report.DataSetProvider#canEvaluate(org.openmrs.report.DataSetDefinition)
	 */
	public boolean canEvaluate(DataSetDefinition dataSetDefinition) {
		return (dataSetDefinition instanceof CohortDataSetDefinition);
	}
	
	/**
	 * @see org.openmrs.report.DataSetProvider#evaluate(org.openmrs.report.DataSetDefinition,
	 *      org.openmrs.Cohort)
	 */
	@SuppressWarnings("unchecked")
    public DataSet evaluate(DataSetDefinition dataSetDefinition, Cohort inputCohort, EvaluationContext evalContext) {
		CohortDataSetDefinition def = (CohortDataSetDefinition) dataSetDefinition;
		
		CohortDataSet data = new CohortDataSet();
		data.setDefinition(def);
		data.setEvaluationContext(evalContext);
		data.setName(def.getName());
		
		Map<String, Cohort> results = new LinkedHashMap<String, Cohort>();
		CohortService cs = Context.getCohortService();
		for (Map.Entry<String, CohortDefinition> e : def.getStrategies().entrySet()) {
			Cohort temp = cs.evaluate(e.getValue(), evalContext);
			if (inputCohort != null)
				temp = Cohort.intersect(temp, inputCohort);
			results.put(e.getKey(), temp);
		}
		data.setCohortData(results);
		
		return data;
	}
	
}
