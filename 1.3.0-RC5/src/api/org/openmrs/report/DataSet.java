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

import java.util.Iterator;
import java.util.Map;

import org.simpleframework.xml.Root;

/**
 * Implementations of this interface represent the equivalent of a spreadsheet of data, i.e. columns and rows.
 *
 * Typically subclasses will implement DataSet<Object>, but if all the cells in a table have the same
 * datatype, the subclass could implement DataSet<ThatType>, like for example {@link CohortDataSet}.
 * 
 * This is one of three interfaces that work together to define and evaluate an OpenMRS DataSet.
 * You need to implement all three of DataSetProvider, {@link DataSetDefinition}, and {@link DataSet}
 * in order to get useful behavior. For example: {@link RowPerObsDataSetProvider},
 * {@link RowPerObsDataSetDefinition}, and {@link RowPerObsDataSet}
 * 
 * The metadata that describes what data will be produced is defined in {@link DataSetDefinition}
 * 
 * The logic that evaluates that metadata goes in an implementation of {@link DataSetProvider}.
 * 
 * After evaluation, the data is represented by an implementation of this interface.
 *
 * @see DataSetProvider
 * @see DataSetDefinition
 */
@Root
public interface DataSet<T extends Object> extends Iterable<Map<String, T>> {
	
	/**
	 * @return The definition that was evaluated to produce this data set.
	 */
	public DataSetDefinition getDefinition();
	
	/**
	 * @return The evaluationContext where this DataSet was evaluated. 
	 */
	public EvaluationContext getEvaluationContext();

	/**
	 * The keys of the maps that this iterator returns are given by this.getDefinition().getColumnKeys() 
	 * @return an iterator over the rows in this dataset.
	 */
	public Iterator<Map<String, T>> iterator();

}
