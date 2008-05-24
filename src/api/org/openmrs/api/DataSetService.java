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
package org.openmrs.api;

import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.report.DataSet;
import org.openmrs.report.DataSetDefinition;
import org.openmrs.report.DataSetProvider;
import org.openmrs.report.EvaluationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Transactional;

/**
 *  
 */
@Transactional( readOnly = true)
public interface DataSetService {
	
	/**
	 * Add the given list of DataSetProviders to the providers on this service.
	 * 
	 * This is used by Spring and its config files.  See
	 * /metadata/spring/applicationContext-service.xml file for how to
	 * add objects to this.
	 * 
	 * This should delegate to {@link #registerProvider(DataSetProvider)} for each 
	 * provider
	 * 
	 * @param providers list of DataSetProvider objects to add on service
	 */
	public void setProviders(List<DataSetProvider> providers) throws APIException;
	
	/**
	 * Gets the list of providers that have been previously set on this service
	 * 
	 * @return the list of registered providers
	 * 
	 * @see #setProviders(List)
	 */
	public List<DataSetProvider> getProviders() throws APIException;
	
	/**
	 * Registered a DataSetProvider and make it available to users of the
	 * data set service
	 * 
	 * @param provider to add
	 */
	public void registerProvider(DataSetProvider newProvider);
	
	/**
	 * Remove a registered DataSetProvider
	 * 
	 * @param provider
	 */
	public void removeProvider(DataSetProvider provider) throws APIException;
	
	/**
	 * @param definition
	 * @return A registered DataSetProvider capable of evaluating the given definition 
	 */
	public DataSetProvider getProvider(DataSetDefinition definition) throws APIException;
	
	/**
	 * Evaluate a data set definition and return a 
	 * 
	 * @param definition
	 * @param inputCohort Input cohort optionally specified by the user. May be ignored by some data sets.
	 * @param evalContext EvaluationContext containing parameter values, etc
	 * @return
	 * @throws APIException when no DataSetProvider is found in the registered providers for the given definition
	 * 
	 * @see #setProviders(List)
	 */
	public DataSet evaluate(DataSetDefinition definition, Cohort inputCohort, EvaluationContext evalContext) throws APIException;
	
}
