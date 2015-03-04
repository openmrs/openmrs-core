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
 * @deprecated see reportingcompatibility module
 */
@Deprecated
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
	 * @see org.openmrs.report.DataSetProvider#evaluate(DataSetDefinition, Cohort,
	 *      EvaluationContext)
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
