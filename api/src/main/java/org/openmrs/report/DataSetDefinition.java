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

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Implementations of this interface describe the metadata that can be evaluated to produce a
 * {@link DataSet}. This is one of three interfaces that work together to define and evaluate an
 * OpenMRS DataSet. You need to implement all three of DataSetProvider, {@link DataSetDefinition},
 * and {@link DataSet} in order to get useful behavior. For example:
 * {@link RowPerObsDataSetProvider}, {@link RowPerObsDataSetDefinition}, and
 * {@link RowPerObsDataSet} The metadata that describes what data will be produced is defined in
 * this interface The logic that evaluates that metadata goes in an implementation of
 * {@link DataSetProvider}. After evaluation, the data is represented by a {@link DataSet}.
 * 
 * @see DataSetProvider
 * @see DataSet
 * @deprecated see reportingcompatibility module
 */
@Root(strict = false)
@Deprecated
public interface DataSetDefinition extends Parameterizable {
	
	/**
	 * Gets the name
	 * 
	 * @return <code>String</code> name of the DataSetDefinition
	 */
	@Attribute(required = true)
	public String getName();
	
	/**
	 * Sets the name of this data set definition
	 * 
	 * @param name the descriptive name of this definition
	 */
	@Attribute(required = true)
	public void setName(String name);
	
	/**
	 * Gets a list of column keys.
	 * 
	 * @return <code>List<String></code> of the column keys
	 */
	public List<String> getColumnKeys();
	
	/**
	 * Gets a list of the datatype of the columns
	 * 
	 * @return <code>List<Class></code> of the column datatypes
	 */
	@SuppressWarnings("unchecked")
	public List<Class> getColumnDatatypes();
	
}
