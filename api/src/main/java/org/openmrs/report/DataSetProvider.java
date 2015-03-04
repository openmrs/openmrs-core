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
