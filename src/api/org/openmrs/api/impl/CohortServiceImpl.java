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
package org.openmrs.api.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.CohortDAO;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.cohort.CohortDefinitionProvider;
import org.openmrs.report.EvaluationContext;
import org.openmrs.reporting.PatientCharacteristicFilter;
import org.openmrs.reporting.PatientSearch;

/**
 *
 */
public class CohortServiceImpl implements CohortService {

	protected Log log = LogFactory.getLog(this.getClass());
	private CohortDAO dao;
	
	private static Map<Class<? extends CohortDefinition>, CohortDefinitionProvider> cohortDefinitionProviders = null;
	
	public CohortServiceImpl() {
	}
	
	private CohortDAO getCohortDAO() {
		return dao;
	}
	
	public void setCohortDAO(CohortDAO dao) {
		this.dao = dao;
	}
	
	public void createCohort(Cohort cohort) {
		if (cohort.getCreator() == null)
			cohort.setCreator(Context.getAuthenticatedUser());
		if (cohort.getDateCreated() == null)
			cohort.setDateCreated(new java.util.Date());
		if (cohort.getName() == null)
			throw new IllegalArgumentException("Missing Name");
		getCohortDAO().createCohort(cohort);
	}

	public Cohort getCohort(Integer id) {
		return getCohortDAO().getCohort(id); 
	}
	
	public List<Cohort> getCohorts() {
		return getCohortDAO().getCohorts();
	}
	
	public void voidCohort(Cohort cohort, String reason) {
		cohort.setVoided(true);
		cohort.setVoidedBy(Context.getAuthenticatedUser());
		cohort.setVoidReason(reason);
		updateCohort(cohort);
	}

	/**
     * @see org.openmrs.api.CohortService#addPatientToCohort(org.openmrs.Cohort, org.openmrs.Patient)
     */
    public void addPatientToCohort(Cohort cohort, Patient patient) {
	    cohort.getMemberIds().add(patient.getPatientId());
	    updateCohort(cohort);
    }

	/**
     * @see org.openmrs.api.CohortService#removePatientFromCohort(org.openmrs.Cohort, org.openmrs.Patient)
     */
    public void removePatientFromCohort(Cohort cohort, Patient patient) {
	    cohort.getMemberIds().remove(patient.getPatientId());
	    updateCohort(cohort);
    }

	/**
     * @see org.openmrs.api.CohortService#updateCohort(org.openmrs.Cohort)
     */
    public void updateCohort(Cohort cohort) {
		if (cohort.getCreator() == null)
			cohort.setCreator(Context.getAuthenticatedUser());
		if (cohort.getDateCreated() == null)
			cohort.setDateCreated(new java.util.Date());
		if (cohort.getName() == null)
			throw new IllegalArgumentException("Missing Name");
		// TODO: Add modifiedBy and dateModified to Cohort
		//cohort.setDateModified(new java.util.Date());
		//cohort.setModifiedBy(Context.getAuthenticatedUser());
		getCohortDAO().updateCohort(cohort);
	}

	/**
     * @see org.openmrs.api.CohortService#getCohortsContainingPatient(org.openmrs.Patient)
     */
    public List<Cohort> getCohortsContainingPatient(Patient patient) {
	    return getCohortDAO().getCohortsContainingPatientId(patient.getPatientId());
    }
    
    public List<Cohort> getCohortsContainingPatientId(Integer patientId) {
    	return getCohortDAO().getCohortsContainingPatientId(patientId);
    }

    /**
     * Auto generated method comment
     * 
     * @param definitionClass
     * @return
     * @throws APIException
     */
    private CohortDefinitionProvider getCohortDefinitionProvider(Class<? extends CohortDefinition> definitionClass) throws APIException {
    	CohortDefinitionProvider ret = cohortDefinitionProviders.get(definitionClass);
    	if (ret == null)
    		throw new APIException("No CohortDefinitionProvider registered for " + definitionClass);
    	else
    		return ret;
    }
	
	/**
     * @see org.openmrs.api.CohortService#evaluate(org.openmrs.cohort.CohortDefinition, org.openmrs.report.EvaluationContext)
     */
    public Cohort evaluate(CohortDefinition definition, EvaluationContext evalContext) throws APIException {
	    CohortDefinitionProvider provider = getCohortDefinitionProvider(definition.getClass());
	    return provider.evaluate(definition, evalContext);
    }



	/**
     * @see org.openmrs.api.CohortService#getAllPatientsCohortDefinition()
     */
    public CohortDefinition getAllPatientsCohortDefinition() {
	    PatientSearch ps = new PatientSearch();
	    ps.setFilterClass(PatientCharacteristicFilter.class);	    
	    return ps;
    }

	/**
     * @see org.openmrs.api.CohortService#getCohortDefinition(java.lang.Class, java.lang.Integer)
     */
    public CohortDefinition getCohortDefinition(Class<CohortDefinition> clazz, Integer id) {
    	CohortDefinitionProvider provider = getCohortDefinitionProvider(clazz);
    	return provider.getCohortDefinition(id);
    }

	/**
     * @see org.openmrs.api.CohortService#getCohortDefinitions()
     */
    public List<CohortDefinition> getAllCohortDefinitions() {
	    List<CohortDefinition> ret = new ArrayList<CohortDefinition>();
	    for (CohortDefinitionProvider p : cohortDefinitionProviders.values()) {
	    	ret.addAll(p.getAllCohortDefinitions());
	    }
	    return ret;
    }

	/**
     * @see org.openmrs.api.CohortService#purgeCohortDefinition(org.openmrs.cohort.CohortDefinition)
     */
    public void purgeCohortDefinition(CohortDefinition definition) {
    	CohortDefinitionProvider provider = getCohortDefinitionProvider(definition.getClass());
	    provider.purgeCohortDefinition(definition);
    }
    
    /**
     * @see org.openmrs.api.CohortService#setCohortDefinitionProviders(Map)
     */
    public void setCohortDefinitionProviders(Map<Class<? extends CohortDefinition>, CohortDefinitionProvider> providerClassMap) {
    	for (Map.Entry<Class<? extends CohortDefinition>, CohortDefinitionProvider> entry : providerClassMap.entrySet()) {
    		registerCohortDefinitionProvider(entry.getKey(), entry.getValue());
    	}
    }
    
    /**
     * @see org.openmrs.api.CohortService#getCohortDefinitionProviders()
     */
    public Map<Class<? extends CohortDefinition>, CohortDefinitionProvider> getCohortDefinitionProviders() {
    	if (cohortDefinitionProviders == null)
    		cohortDefinitionProviders = new LinkedHashMap<Class<? extends CohortDefinition>, CohortDefinitionProvider>();
    	
    	return cohortDefinitionProviders;
    }
    
    /**
     * @see org.openmrs.api.CohortService#registerCohortDefinitionProvider(java.lang.Class, org.openmrs.api.CohortDefinitionService)
     */
    public void registerCohortDefinitionProvider(Class<? extends CohortDefinition> defClass, CohortDefinitionProvider cohortDefProvider) throws APIException {
    	getCohortDefinitionProviders().put(defClass, cohortDefProvider);
    }
    
	/**
     * @see org.openmrs.api.CohortService#removeCohortDefinitionProvider(java.lang.Class)
     */
    public void removeCohortDefinitionProvider(Class<? extends CohortDefinitionProvider> providerClass) {
    	
    	// TODO: should this be looking through the values or the keys?
    	for (Iterator<CohortDefinitionProvider> i = cohortDefinitionProviders.values().iterator(); i.hasNext(); ) {
    		if (i.next().getClass().equals(providerClass))
    			i.remove();
    	}
    }

	/**
     * @see org.openmrs.api.CohortService#saveCohortDefinition(org.openmrs.cohort.CohortDefinition)
     */
    public CohortDefinition saveCohortDefinition(CohortDefinition definition) throws APIException {
    	CohortDefinitionProvider provider = getCohortDefinitionProvider(definition.getClass());
    	return provider.saveCohortDefinition(definition);
    }

	/**
     * @see org.openmrs.api.CohortService#getCohortDefinitions(java.lang.Class)
     */
    public List<CohortDefinition> getCohortDefinitions(Class providerClass) {
    	CohortDefinitionProvider provider = getCohortDefinitionProvider(providerClass);
	    return provider.getAllCohortDefinitions();
    }
}
