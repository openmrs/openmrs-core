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
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.cohort.CohortDefinitionItemHolder;
import org.openmrs.cohort.CohortDefinitionProvider;
import org.openmrs.report.EvaluationContext;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * API methods related to Cohorts and CohortDefinitions
 * <ul>
 * <li>A Cohort is a list of patient ids.</li>
 * <li>A CohortDefinition is a search strategy which can be used to arrive at a cohort. Therefore,
 * the patients returned by running a CohortDefinition can be different depending on the data that
 * is stored elsewhere in the database.</li>
 * </ul>
 * 
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
	 * Save a cohort to the database (create if new, or update if changed) This method will throw an
	 * exception if any patientIds in the Cohort don't exist.
	 * 
	 * @param cohort the cohort to be saved to the database
	 * @return The cohort that was passed in
	 * @throws APIException
	 * @should create new cohorts
	 * @should update an existing cohort
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_COHORTS, OpenmrsConstants.PRIV_EDIT_COHORTS })
	public Cohort saveCohort(Cohort cohort) throws APIException;
	
	/**
	 * @see #saveCohort(Cohort)
	 * @deprecated replaced by saveCohort(Cohort)
	 */
	@Authorized( { OpenmrsConstants.PRIV_ADD_COHORTS })
	public Cohort createCohort(Cohort cohort) throws APIException;
	
	/**
	 * @see #saveCohort(Cohort)
	 * @deprecated replaced by saveCohort(Cohort)
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_COHORTS })
	public Cohort updateCohort(Cohort cohort) throws APIException;
	
	/**
	 * Voids the given cohort, deleting it from the perspective of the typical end user.
	 * 
	 * @param cohort the cohort to delete
	 * @param reason the reason this cohort is being retired
	 * @return The cohort that was passed in
	 * @throws APIException
	 * @should fail with if reason is null or empty
	 * @should void cohort
	 * @should not change an already voided cohort
	 */
	@Authorized( { OpenmrsConstants.PRIV_DELETE_COHORTS })
	public Cohort voidCohort(Cohort cohort, String reason) throws APIException;
	
	/**
	 * Completely removes a Cohort from the database (not reversible)
	 * 
	 * @param cohort the Cohort to completely remove from the database
	 * @throws APIException
	 * @should delete cohort from database
	 */
	public Cohort purgeCohort(Cohort cohort) throws APIException;
	
	/**
	 * Gets a Cohort by its database primary key
	 * 
	 * @param id
	 * @return the Cohort with the given primary key, or null if none exists
	 * @throws APIException
	 * @should get cohort by id
	 */
	@Transactional(readOnly = true)
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS })
	public Cohort getCohort(Integer id) throws APIException;
	
	/**
	 * Gets a non voided Cohort by its name
	 * 
	 * @param name
	 * @return the Cohort with the given name, or null if none exists
	 * @throws APIException
	 * @should get cohort given a name
	 * @should get the nonvoided cohort if two exist with same name
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS })
	public Cohort getCohort(String name) throws APIException;
	
	/**
	 * Gets all Cohorts (not including voided ones)
	 * 
	 * @return All Cohorts in the database (not including voided ones)
	 * @throws APIException
	 * @should get all cohorts in database
	 * @should not return any voided cohorts
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS })
	public List<Cohort> getAllCohorts() throws APIException;
	
	/**
	 * Gets all Cohorts, possibly including the voided ones
	 * 
	 * @param includeVoided whether or not to include voided Cohorts
	 * @return All Cohorts, maybe including the voided ones
	 * @throws APIException
	 * @should return all cohorts and voided
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS })
	public List<Cohort> getAllCohorts(boolean includeVoided) throws APIException;
	
	/**
	 * @see #getAllCohorts()
	 * @deprecated replaced by getAllCohorts()
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS })
	public List<Cohort> getCohorts() throws APIException;
	
	/**
	 * Returns Cohorts whose names match the given string. Returns an empty list in the case of no
	 * results. Returns all Cohorts in the case of null or empty input
	 * 
	 * @param nameFragment
	 * @return list of cohorts matching the name fragment
	 * @throws APIException
	 * @should never return null
	 * @should match cohorts by partial name
	 */
	public List<Cohort> getCohorts(String nameFragment) throws APIException;
	
	/**
	 * Find all Cohorts that contain the given patient. (Not including voided Cohorts)
	 * 
	 * @param patient
	 * @return All non-voided Cohorts that contain the given patient
	 * @throws APIException
	 * @should not return voided cohorts
	 * @should return cohorts that have given patient
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS })
	public List<Cohort> getCohortsContainingPatient(Patient patient) throws APIException;
	
	/**
	 * Find all Cohorts that contain the given patientId. (Not including voided Cohorts)
	 * 
	 * @param patientId
	 * @return All non-voided Cohorts that contain the given patientId
	 * @throws APIException
	 */
	@Authorized( { OpenmrsConstants.PRIV_VIEW_PATIENT_COHORTS })
	public List<Cohort> getCohortsContainingPatientId(Integer patientId) throws APIException;
	
	/**
	 * Adds a new patient to a Cohort. If the patient is not already in the Cohort, then they are
	 * added, and the Cohort is saved, marking it as changed.
	 * 
	 * @param cohort the cohort to receive the given patient
	 * @param patient the patient to insert into the cohort
	 * @return The cohort that was passed in with the new patient in it
	 * @throws APIException
	 * @should add a patient and save the cohort
	 * @should add a patient and insert the cohort to database
	 * @should not fail if cohort already contains patient
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_COHORTS })
	public Cohort addPatientToCohort(Cohort cohort, Patient patient) throws APIException;
	
	/**
	 * Removes a patient from a Cohort. If the patient is in the Cohort, then they are removed, and
	 * the Cohort is saved, marking it as changed.
	 * 
	 * @param cohort the cohort containing the given patient
	 * @param patient the patient to remove from the given cohort
	 * @return The cohort that was passed in with the patient removed
	 * @throws APIException
	 * @should not fail if cohort doesn't contain patient
	 * @should save cohort after removing patient
	 */
	@Authorized( { OpenmrsConstants.PRIV_EDIT_COHORTS })
	public Cohort removePatientFromCohort(Cohort cohort, Patient patient) throws APIException;
	
	/**
	 * Set the given CohortDefinitionProviders as the providers for this service. These are set via
	 * spring injection in /metadata/spring/applicationContext-service.xml . This method acts more
	 * like an "add" than a "set". All entries in the <code>providerClassMap</code> are added to the
	 * already set list of providers. This allows multiple Spring application context files to call
	 * this method with their own providers
	 * 
	 * @param providerClassMap mapping from CohortDefinition to its provider
	 * @should not overwrite previously set providers if called twice
	 */
	@Transactional(readOnly = true)
	public void setCohortDefinitionProviders(
	                                         Map<Class<? extends CohortDefinition>, CohortDefinitionProvider> providerClassMap)
	                                                                                                                           throws APIException;
	
	/**
	 * Adds the given cohort definition provider to this service's list of providers
	 * 
	 * @param cohortDefClass the type of cohort definition that this provider works on
	 * @param cohortDef the provider
	 * @throws APIException
	 * @should overwrite provider if duplicate CcohortDefinition class
	 */
	@Transactional(readOnly = true)
	public void registerCohortDefinitionProvider(Class<? extends CohortDefinition> cohortDefClass,
	                                             CohortDefinitionProvider cohortDef) throws APIException;
	
	/**
	 * Gets all the providers registered to this service. Will return an empty list instead of null.
	 * 
	 * @return this service's providers
	 * @throws APIException
	 * @see #setCohortDefinitionProviders(Map)
	 * @should not return null if not providers have been set
	 */
	@Transactional(readOnly = true)
	public Map<Class<? extends CohortDefinition>, CohortDefinitionProvider> getCohortDefinitionProviders()
	                                                                                                      throws APIException;
	
	/**
	 * Removing any mapping from CohortDefinition to provider in this server where the given
	 * <code>providerClass</class> is the
	 * CohortDefinitionProvider
	 * 
	 * @param providerClass the provider to remove
	 * @throws APIException
	 * @should not fail if providerClass not set
	 */
	@Transactional(readOnly = true)
	public void removeCohortDefinitionProvider(Class<? extends CohortDefinitionProvider> providerClass) throws APIException;
	
	/**
	 * Get every cohort definition that every registered provider knows about. This is typically
	 * used for selection by an end user, and so a list of Holders are returned instead of a list of
	 * definitions so that the system knows which provider the definition came from
	 * 
	 * @return a list of CohortDefinitionItemHolder defined
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<CohortDefinitionItemHolder> getAllCohortDefinitions() throws APIException;
	
	@Transactional(readOnly = true)
	public List<CohortDefinitionItemHolder> getCohortDefinitions(Class<? extends CohortDefinitionProvider> providerClass)
	                                                                                                                     throws APIException;
	
	@Transactional(readOnly = true)
	public CohortDefinition getCohortDefinition(Class<CohortDefinition> clazz, Integer id) throws APIException;
	
	@Transactional(readOnly = true)
	public CohortDefinition getCohortDefinition(String cohortKey) throws APIException;
	
	@Transactional(readOnly = true)
	public CohortDefinition saveCohortDefinition(CohortDefinition definition) throws APIException;
	
	@Transactional
	public void purgeCohortDefinition(CohortDefinition definition) throws APIException;
	
	@Transactional(readOnly = true)
	public Cohort evaluate(CohortDefinition definition, EvaluationContext evalContext) throws APIException;
	
	@Transactional(readOnly = true)
	public CohortDefinition getAllPatientsCohortDefinition() throws APIException;
	
}
