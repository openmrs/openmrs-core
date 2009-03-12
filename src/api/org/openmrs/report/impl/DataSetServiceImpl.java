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
package org.openmrs.report.impl;

import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.APIException;
import org.openmrs.api.DataSetService;
import org.openmrs.report.DataSet;
import org.openmrs.report.DataSetDefinition;
import org.openmrs.report.DataSetProvider;
import org.openmrs.report.EvaluationContext;

/**
 * Default implementation of the data set service.
 */
public class DataSetServiceImpl implements DataSetService {
	
	public Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Saved list of DataSetProviders allowed to be used by the dataset service
	 */
	private static List<DataSetProvider> providers;
	
	/**
	 * Default no-arg constructor
	 */
	public DataSetServiceImpl() {
	}
	
	/**
	 * @see org.openmrs.api.DataSetService#setProviders(java.util.Collection)
	 */
	public void setProviders(List<DataSetProvider> newProviders) {
		for (DataSetProvider provider : newProviders) {
			registerProvider(provider);
		}
	}
	
	/**
	 * @see org.openmrs.api.DataSetService#getProviders()
	 */
	public List<DataSetProvider> getProviders() {
		if (providers == null)
			providers = new Vector<DataSetProvider>();
		
		return providers;
	}
	
	/**
	 * @see org.openmrs.api.DataSetService#registerProvider(org.openmrs.report.DataSetProvider)
	 */
	public void registerProvider(DataSetProvider newProvider) {
		for (DataSetProvider currentProvider : getProviders()) {
			if (currentProvider.getClass().equals(newProvider.getClass()))
				return;
		}
		
		// we only get here if there isn't already a provider registered for this class
		providers.add(newProvider);
	}
	
	/**
	 * @see org.openmrs.api.DataSetService#removeProvider(org.openmrs.report.DataSetProvider)
	 */
	public void removeProvider(DataSetProvider provider) {
		getProviders().remove(provider);
	}
	
	/**
	 * @see org.openmrs.api.DataSetService#getProvider(org.openmrs.report.DataSetDefinition)
	 */
	public DataSetProvider getProvider(DataSetDefinition definition) {
		for (DataSetProvider p : getProviders())
			if (p.canEvaluate(definition))
				return p;
		return null;
	}
	
	/**
	 * @see org.openmrs.api.DataSetService#evaluate(org.openmrs.report.DataSetDefinition,
	 *      org.openmrs.Cohort, org.openmrs.report.EvaluationContext)
	 */
	@SuppressWarnings("unchecked")
    public DataSet evaluate(DataSetDefinition definition, Cohort inputCohort, EvaluationContext evalContext) {
		DataSetProvider provider = getProvider(definition);
		if (provider == null)
			throw new APIException("No DataSetProvider found for (" + definition.getClass() + ") " + definition.getName());
		return provider.evaluate(definition, inputCohort, evalContext);
	}
	
}
