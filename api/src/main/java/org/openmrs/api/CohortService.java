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
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.CohortDAO;
import org.openmrs.util.PrivilegeConstants;

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
 */
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
	@Authorized( { PrivilegeConstants.ADD_COHORTS, PrivilegeConstants.EDIT_COHORTS })
	public Cohort saveCohort(Cohort cohort) throws APIException;
	
	/**
	 * @see #saveCohort(Cohort)
	 * @deprecated replaced by saveCohort(Cohort)
	 */
	@Deprecated
	@Authorized( { PrivilegeConstants.ADD_COHORTS })
	public Cohort createCohort(Cohort cohort) throws APIException;
	
	/**
	 * @see #saveCohort(Cohort)
	 * @deprecated replaced by saveCohort(Cohort)
	 */
	@Deprecated
	@Authorized( { PrivilegeConstants.EDIT_COHORTS })
	public Cohort updateCohort(Cohort cohort) throws APIException;
	
	/**
	 * Voids the given cohort, deleting it from the perspective of the typical end user.
	 * 
	 * @param cohort the cohort to delete
	 * @param reason the reason this cohort is being retired
	 * @return The cohort that was passed in
	 * @throws APIException
	 * @should fail if reason is null
	 * @should fail if reason is empty
	 * @should void cohort
	 * @should not change an already voided cohort
	 */
	@Authorized( { PrivilegeConstants.DELETE_COHORTS })
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
	@Authorized( { PrivilegeConstants.VIEW_PATIENT_COHORTS })
	public Cohort getCohort(Integer id) throws APIException;
	
	/**
	 * Gets a non voided Cohort by its name
	 * 
	 * @param name
	 * @return the Cohort with the given name, or null if none exists
	 * @throws APIException
	 * @should get cohort given a name
	 * @should get the nonvoided cohort if two exist with same name
	 * @should only get non voided cohorts by name
	 */
	@Authorized( { PrivilegeConstants.VIEW_PATIENT_COHORTS })
	public Cohort getCohort(String name) throws APIException;
	
	/**
	 * Gets all Cohorts (not including voided ones)
	 * 
	 * @return All Cohorts in the database (not including voided ones)
	 * @throws APIException
	 * @should get all nonvoided cohorts in database
	 * @should not return any voided cohorts
	 */
	@Authorized( { PrivilegeConstants.VIEW_PATIENT_COHORTS })
	public List<Cohort> getAllCohorts() throws APIException;
	
	/**
	 * Gets all Cohorts, possibly including the voided ones
	 * 
	 * @param includeVoided whether or not to include voided Cohorts
	 * @return All Cohorts, maybe including the voided ones
	 * @throws APIException
	 * @should return all cohorts and voided
	 */
	@Authorized( { PrivilegeConstants.VIEW_PATIENT_COHORTS })
	public List<Cohort> getAllCohorts(boolean includeVoided) throws APIException;
	
	/**
	 * @see #getAllCohorts()
	 * @deprecated replaced by getAllCohorts()
	 */
	@Deprecated
	@Authorized( { PrivilegeConstants.VIEW_PATIENT_COHORTS })
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
	@Authorized( { PrivilegeConstants.VIEW_PATIENT_COHORTS })
	public List<Cohort> getCohortsContainingPatient(Patient patient) throws APIException;
	
	/**
	 * Find all Cohorts that contain the given patientId. (Not including voided Cohorts)
	 * 
	 * @param patientId
	 * @return All non-voided Cohorts that contain the given patientId
	 * @throws APIException
	 */
	@Authorized( { PrivilegeConstants.VIEW_PATIENT_COHORTS })
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
	@Authorized( { PrivilegeConstants.EDIT_COHORTS })
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
	@Authorized( { PrivilegeConstants.EDIT_COHORTS })
	public Cohort removePatientFromCohort(Cohort cohort, Patient patient) throws APIException;
	
	/**
	 * Get Cohort by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized( { PrivilegeConstants.VIEW_PATIENT_COHORTS })
	public Cohort getCohortByUuid(String uuid);
	
}
