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
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.CohortDAO;
import org.openmrs.util.OpenmrsConstants;
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
public interface CohortService extends OpenmrsService {
	
	/**
	 * Sets the CohortDAO for this service to use 
	 * 
	 * @param dao
	 */
	public void setCohortDAO(CohortDAO dao);
	
	/**
	 * Save a cohort to the database (create if new, or update if changed)
	 * This method will throw an exception if any patientIds in the Cohort don't exist.  
	 * 
	 * @param cohort the cohort to be saved to the database
	 * @return The cohort that was passed in
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_ADD_COHORTS, OpenmrsConstants.PRIV_EDIT_COHORTS})
	public Cohort saveCohort(Cohort cohort) throws APIException;
	
	/**
	 * @see #saveCohort(Cohort)
	 * @deprecated replaced by saveCohort(Cohort)
	 */
	@Authorized({OpenmrsConstants.PRIV_ADD_COHORTS})
	public Cohort createCohort(Cohort cohort) throws APIException;
	
	/**
	 * @see #saveCohort(Cohort)
	 * @deprecated replaced by saveCohort(Cohort)
	 */
	@Authorized({OpenmrsConstants.PRIV_EDIT_COHORTS})
	public Cohort updateCohort(Cohort cohort) throws APIException;
	
	/**
	 * Voids the given cohort, deleting it from the perspective of the typical end user.
	 * 
	 * @param cohort the cohort to delete
	 * @param reason the reason this cohort is being retired
	 * @return The cohort that was passed in
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_DELETE_COHORTS})
	public Cohort voidCohort(Cohort cohort, String reason) throws APIException;
	
	/**
	 * Completely removes a Cohort from the database (not reversible)
	 * 
	 * @param cohort the Cohort to completely remove from the database
	 * @throws APIException
	 */
	public Cohort purgeCohort(Cohort cohort) throws APIException;
	
	/**
	 * Gets a Cohort by its database primary key
	 * 
	 * @param id
	 * @return the Cohort with the given primary key, or null if none exists
	 * @throws APIException
	 */
	@Transactional(readOnly=true)
	@Authorized({OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS})
	public Cohort getCohort(Integer id) throws APIException;
	
	/**
	 * Gets a Cohort by its name
	 * 
	 * @param name
	 * @return the Cohort with the given name, or null if none exists
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS})
	public Cohort getCohort(String name) throws APIException;
	
	/**
	 * Gets all Cohorts (not including voided ones)
	 * 
	 * @return All Cohorts in the database (not including voided ones)
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS})
	public List<Cohort> getAllCohorts() throws APIException;
	
	/**
	 * Gets all Cohorts, possibly including the voided ones
	 * 
	 * @param includeVoided whether or not to include voided Cohorts
	 * @return All Cohorts, maybe including the voided ones
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS})
	public List<Cohort> getAllCohorts(boolean includeVoided) throws APIException;
	
	/**
	 * @see #getAllCohorts()
	 * @deprecated replaced by getAllCohorts()
	 */
	@Authorized({OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS})
	public List<Cohort> getCohorts() throws APIException;
	
	/**
	 * Returns Cohorts whose names match the given string.
	 * Returns an empty list in the case of no results.
	 * Returns all Cohorts in the case of null or empty input 
	 * 
	 * @param nameFragment
	 * @return
	 * @throws APIException
	 */
	public List<Cohort> getCohorts(String nameFragment) throws APIException;
	
	/**
	 * Find all Cohorts that contain the given patient. (Not including voided Cohorts)
	 * 
	 * @param patient
	 * @return All non-voided Cohorts that contain the given patient
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS})
	public List<Cohort> getCohortsContainingPatient(Patient patient) throws APIException;
	
	/**
	 * Find all Cohorts that contain the given patientId. (Not including voided Cohorts)
	 * 
	 * @param patientId
	 * @return All non-voided Cohorts that contain the given patientId
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS})
	public List<Cohort> getCohortsContainingPatientId(Integer patientId) throws APIException;
	
	/**
	 * Adds a new patient to a Cohort.
	 * If the patient is not already in the Cohort, then they are added, and the Cohort is saved, marking it as changed.
	 * 
	 * @param cohort
	 * @param patient
	 * @return The cohort that was passed in
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_EDIT_COHORTS})
	public Cohort addPatientToCohort(Cohort cohort, Patient patient) throws APIException;
	
	/**
	 * Removes a patient from a Cohort.
	 * If the patient is in the Cohort, then they are removed, and the Cohort is saved, marking it as changed.
	 * 
	 * @param cohort
	 * @param patient
	 * @return The cohort that was passed in
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_EDIT_COHORTS})
	public Cohort removePatientFromCohort(Cohort cohort, Patient patient) throws APIException;
	
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
