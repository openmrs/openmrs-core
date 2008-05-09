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
import java.util.Map;

import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.db.CohortDAO;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.cohort.CohortDefinitionProvider;
import org.openmrs.report.EvaluationContext;
import org.springframework.transaction.annotation.Transactional;

/**
 * API methods related to Cohorts and CohortDefinitions
 * A Cohort is a list of patient ids.
 * A CohortDefinition is a search strategy which can be used to arrive at a cohort.
 * @see org.openmrs.Cohort
 * @see org.openmrs.cohort.CohortDefinition
 * @see org.openmrs.api.CohortDefinitionProvider
 */
@Transactional
public interface CohortService {
	
	public void setCohortDAO(CohortDAO dao);
	
	public void createCohort(Cohort cohort);
	
	public void updateCohort(Cohort cohort);
	
	public void voidCohort(Cohort cohort, String reason);
	
	@Transactional(readOnly=true)
	public Cohort getCohort(Integer id);
	
	@Transactional(readOnly=true)
	public List<Cohort> getCohorts();
	
	@Transactional(readOnly=true)
	public List<Cohort> getCohortsContainingPatient(Patient patient);
	
	@Transactional(readOnly=true)
	public List<Cohort> getCohortsContainingPatientId(Integer patientId);
	
	@Transactional
	public void addPatientToCohort(Cohort cohort, Patient patient);
	
	@Transactional
	public void removePatientFromCohort(Cohort cohort, Patient patient);
	
	/**
	 * Set the given CohortDefinitionProviders as the providers for
	 * this service.  These are set via spring injection in 
	 * /metadata/spring/applicationContext-service.xml
	 * 
	 * @param providerClassMap
	 */
	@Transactional(readOnly=true)
	public void setCohortDefinitionProviders(Map<Class<? extends CohortDefinition>, CohortDefinitionProvider> providerClassMap);
	
	/**
	 * Adds the given cohort definition to this service's providers
	 * 
	 * @param cohortDefClass
	 * @param cohortDef
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	public void registerCohortDefinitionProvider(Class<? extends CohortDefinition> cohortDefClass, CohortDefinitionProvider cohortDef) throws APIException;
	
	/**
	 * Gets all the providers registered to this service
	 * 
	 * @return this service's providers
	 * 
	 * @see #setCohortDefinitionProviders(Map)
	 */
	@Transactional(readOnly=true)
	public Map<Class<? extends CohortDefinition>, CohortDefinitionProvider> getCohortDefinitionProviders();
	
	@Transactional(readOnly=true)
	public void removeCohortDefinitionProvider(Class<? extends CohortDefinitionProvider> providerClass);

	@Transactional(readOnly=true)
	public List<CohortDefinition> getAllCohortDefinitions();
	
	@Transactional(readOnly=true)
	public List<CohortDefinition> getCohortDefinitions(Class<? extends CohortDefinitionProvider> providerClass);

	@Transactional(readOnly=true)
	public CohortDefinition getCohortDefinition(Class<CohortDefinition> clazz, Integer id);
	
	@Transactional(readOnly=true)
	public CohortDefinition saveCohortDefinition(CohortDefinition definition);
	
	@Transactional
	public void purgeCohortDefinition(CohortDefinition definition);
	
	@Transactional(readOnly=true)
	public Cohort evaluate(CohortDefinition definition, EvaluationContext evalContext);

	@Transactional(readOnly=true)
	public CohortDefinition getAllPatientsCohortDefinition();

}
