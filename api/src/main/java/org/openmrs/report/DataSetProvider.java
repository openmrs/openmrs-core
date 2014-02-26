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

import org.openmrs.Cohort;

/**
 * Implementations of this interface perform the work of converting from a {@link DataSetDefinition}
 * to a {@link DataSet}.
 * <p>
 * This is one of three interfaces that work together to define and evaluate an OpenMRS DataSet. You
 * need to implement all three of DataSetProvider, {@link DataSetDefinition} , and {@link DataSet}
 * in order to get useful behavior. For example: {@link RowPerObsDataSetProvider},
 * {@link RowPerObsDataSetDefinition}, and {@link RowPerObsDataSet}
 * <p>
 * The metadata that describes what data will be produced is defined in {@link DataSetDefinition}
 * The logic that evaluates that metadata goes in an implementation of this interface. After
 * evaluation, the data is represented by a {@link DataSet}.
 * 
 * @see DataSetDefinition
 * @see DataSet
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public interface DataSetProvider {
	
	/**
	 * @param dataSetDefinition
	 * @return Whether this provider can evaluate the given definition
	 */
	public boolean canEvaluate(DataSetDefinition dataSetDefinition);
	
	/**
	 * Evaluate a dataset on a given input cohort (which may be null, and may be ignored by some
	 * data sets)
	 * 
	 * @param dataSetDefinition
	 * @param inputCohort
	 * @return the evaluated <code>DataSet</code>
	 */
	@SuppressWarnings("unchecked")
	public DataSet evaluate(DataSetDefinition dataSetDefinition, Cohort inputCohort, EvaluationContext evalContext);
}
