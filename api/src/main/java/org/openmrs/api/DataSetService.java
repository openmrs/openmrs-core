/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.report.DataSet;
import org.openmrs.report.DataSetDefinition;
import org.openmrs.report.DataSetProvider;
import org.openmrs.report.EvaluationContext;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public interface DataSetService {
	
	/**
	 * Add the given list of DataSetProviders to the providers on this service. This is used by
	 * Spring and its config files. See /metadata/spring/applicationContext-service.xml file for how
	 * to add objects to this. This should delegate to {@link #registerProvider(DataSetProvider)}
	 * for each provider
	 * 
	 * @param providers list of DataSetProvider objects to add on service
	 */
	public void setProviders(List<DataSetProvider> providers) throws APIException;
	
	/**
	 * Gets the list of providers that have been previously set on this service
	 * 
	 * @return the list of registered providers
	 * @see #setProviders(List)
	 */
	public List<DataSetProvider> getProviders() throws APIException;
	
	/**
	 * Registered a DataSetProvider and make it available to users of the data set service
	 * 
	 * @param newProvider DataSetProvider to add
	 */
	public void registerProvider(DataSetProvider newProvider);
	
	/**
	 * Remove a registered DataSetProvider
	 * 
	 * @param provider DataSetProvider to remove
	 */
	public void removeProvider(DataSetProvider provider) throws APIException;
	
	/**
	 * @param definition
	 * @return A registered DataSetProvider capable of evaluating the given definition
	 */
	public DataSetProvider getProvider(DataSetDefinition definition) throws APIException;
	
	/**
	 * Evaluate a data set definition to get turn it into a DataSet
	 * 
	 * @param definition
	 * @param inputCohort Input cohort optionally specified by the user. May be ignored by some data
	 *            sets.
	 * @param evalContext EvaluationContext containing parameter values, etc
	 * @return a DataSet matching the parameters
	 * @throws APIException when no DataSetProvider is found in the registered providers for the
	 *             given definition
	 * @see #setProviders(List)
	 */
	@SuppressWarnings("unchecked")
	public DataSet evaluate(DataSetDefinition definition, Cohort inputCohort, EvaluationContext evalContext)
	        throws APIException;
	
}
