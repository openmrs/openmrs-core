/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
 * 
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class DataSetServiceImpl implements DataSetService {
	
	public Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Saved list of DataSetProviders allowed to be used by the dataset service
	 */
	private static List<DataSetProvider> providers = null;
	
	/**
	 * Default no-arg constructor
	 */
	public DataSetServiceImpl() {
	}
	
	/**
	 * Clean up after this class. Set the static var to null so that the classloader can reclaim the
	 * space.
	 * 
	 * @see org.openmrs.api.impl.BaseOpenmrsService#onShutdown()
	 */
	public void onShutdown() {
		providers = null;
	}
	
	/**
	 * @see org.openmrs.api.DataSetService#setProviders(List)
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
