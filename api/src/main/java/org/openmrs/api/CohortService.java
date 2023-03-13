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

import java.util.Date;
import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.User;
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
	 * <strong>Should</strong> create new cohorts
	 * <strong>Should</strong> update an existing cohort
	 */
	@Authorized({ PrivilegeConstants.ADD_COHORTS, PrivilegeConstants.EDIT_COHORTS })
	public Cohort saveCohort(Cohort cohort) throws APIException;
	
	/**
	 * Voids the given cohort, deleting it from the perspective of the typical end user.
	 * 
	 * @param cohort the cohort to delete
	 * @param reason the reason this cohort is being retired
	 * @return The cohort that was passed in
	 * @throws APIException
	 * <strong>Should</strong> fail if reason is null
	 * <strong>Should</strong> fail if reason is empty
	 * <strong>Should</strong> void cohort
	 * <strong>Should</strong> not change an already voided cohort
	 */
	@Authorized({ PrivilegeConstants.DELETE_COHORTS })
	public Cohort voidCohort(Cohort cohort, String reason) throws APIException;
	
	/**
	 * Completely removes a Cohort from the database (not reversible)
	 * 
	 * @param cohort the Cohort to completely remove from the database
	 * @throws APIException
	 * <strong>Should</strong> delete cohort from database
	 */
	public Cohort purgeCohort(Cohort cohort) throws APIException;
	
	/**
	 * Gets a Cohort by its database primary key
	 * 
	 * @param id
	 * @return the Cohort with the given primary key, or null if none exists
	 * @throws APIException
	 * <strong>Should</strong> get cohort by id
	 */
	@Authorized({ PrivilegeConstants.GET_PATIENT_COHORTS })
	public Cohort getCohort(Integer id) throws APIException;
	
	/**
	 * Gets a non voided Cohort by its name
	 *
	 * @param name
	 * @return the Cohort with the given name, or null if none exists
	 * @throws APIException
	 * <strong>Should</strong> get cohort given a name
	 * <strong>Should</strong> get the nonvoided cohort if two exist with same name
	 * <strong>Should</strong> only get non voided cohorts by name
	 * @since 2.1.0
	 */
	@Authorized({ PrivilegeConstants.GET_PATIENT_COHORTS })
	public Cohort getCohortByName(String name) throws APIException;
	
	/**
	 * @deprecated use {@link #getCohortByName(String)}
	 */
	@Deprecated
	@Authorized({ PrivilegeConstants.GET_PATIENT_COHORTS })
	public Cohort getCohort(String name) throws APIException;
	
	/**
	 * Gets all Cohorts (not including voided ones)
	 * 
	 * @return All Cohorts in the database (not including voided ones)
	 * @throws APIException
	 * <strong>Should</strong> get all nonvoided cohorts in database
	 * <strong>Should</strong> not return any voided cohorts
	 */
	@Authorized({ PrivilegeConstants.GET_PATIENT_COHORTS })
	public List<Cohort> getAllCohorts() throws APIException;
	
	/**
	 * Gets all Cohorts, possibly including the voided ones
	 * 
	 * @param includeVoided whether or not to include voided Cohorts
	 * @return All Cohorts, maybe including the voided ones
	 * @throws APIException
	 * <strong>Should</strong> return all cohorts and voided
	 */
	@Authorized({ PrivilegeConstants.GET_PATIENT_COHORTS })
	public List<Cohort> getAllCohorts(boolean includeVoided) throws APIException;
	
	/**
	 * Returns Cohorts whose names match the given string. Returns an empty list in the case of no
	 * results. Returns all Cohorts in the case of null or empty input
	 * 
	 * @param nameFragment
	 * @return list of cohorts matching the name fragment
	 * @throws APIException
	 * <strong>Should</strong> never return null
	 * <strong>Should</strong> match cohorts by partial name
	 */
	public List<Cohort> getCohorts(String nameFragment) throws APIException;
	
	/**
	 * @deprecated use {@link #getCohortsContainingPatientId(Integer)}
	 *
	 * Find all Cohorts that contain the given patient. (Not including voided Cohorts)
	 * 
	 * @param patient patient used to find the cohorts
	 * @return All non-voided Cohorts that contain the given patient
	 * @throws APIException
	 * <strong>Should</strong> not return voided cohorts
	 * <strong>Should</strong> return cohorts that have given patient
	 */
	@Deprecated
	@Authorized({ PrivilegeConstants.GET_PATIENT_COHORTS })
	public List<Cohort> getCohortsContainingPatient(Patient patient) throws APIException;
	
	/**
	 * Find all Cohorts that contain the given patientId right now. (Not including voided Cohorts, or ended memberships)
	 *
	 * @param patientId patient id used to find the cohorts
	 * @return All non-voided Cohorts that contain the given patientId
	 * @throws APIException
	 */
	@Authorized({ PrivilegeConstants.GET_PATIENT_COHORTS })
	public List<Cohort> getCohortsContainingPatientId(Integer patientId) throws APIException;
	
	/**
	 * Adds a new patient to a Cohort. If the patient is not already in the Cohort, then they are
	 * added, and the Cohort is saved, marking it as changed.
	 * 
	 * @param cohort the cohort to receive the given patient
	 * @param patient the patient to insert into the cohort
	 * @return The cohort that was passed in with the new patient in it
	 * @throws APIException
	 * <strong>Should</strong> add a patient and save the cohort
	 * <strong>Should</strong> add a patient and insert the cohort to database
	 * <strong>Should</strong> not fail if cohort already contains patient
	 */
	@Authorized({ PrivilegeConstants.EDIT_COHORTS })
	public Cohort addPatientToCohort(Cohort cohort, Patient patient) throws APIException;
	
	/**
	 * Removes a patient from a Cohort, by voiding their membership. (Has no effect if the patient is not in the cohort.)
	 * (This behavior is provided for consistency with the pre-2.1.0 API, which didn't track cohort membership dates.)
	 *
	 * @deprecated since 2.1.0 you should explicitly call either {@link #endCohortMembership(CohortMembership, Date)} or {@link #voidCohortMembership(CohortMembership, String)}
	 *
	 * @param cohort the cohort containing the given patient
	 * @param patient the patient to remove from the given cohort
	 * @return The cohort that was passed in with the patient removed
	 * @throws APIException
	 * <strong>Should</strong> not fail if cohort doesn't contain patient
	 * <strong>Should</strong> save cohort after removing patient
	 */
	@Deprecated
	@Authorized({ PrivilegeConstants.EDIT_COHORTS })
	public Cohort removePatientFromCohort(Cohort cohort, Patient patient) throws APIException;
	
	/**
	 * Get Cohort by its UUID
	 * 
	 * @param uuid
	 * @return cohort or null
	 * <strong>Should</strong> find object given valid uuid
	 * <strong>Should</strong> return null if no object found with given uuid
	 */
	@Authorized({ PrivilegeConstants.GET_PATIENT_COHORTS })
	public Cohort getCohortByUuid(String uuid);
	
	/**
	 * Get CohortMembership by its UUID
	 * @param uuid
	 * @return cohort membership or null
	 * @since 2.1.0
	 */
	@Authorized({ PrivilegeConstants.GET_PATIENT_COHORTS })
	public CohortMembership getCohortMembershipByUuid(String uuid);
	
	/**
	 * Removes a CohortMembership from its parent Cohort
	 *
	 * @since 2.1.0
	 * @param cohortMembership membership that will be removed from cohort
	 */
	@Authorized({ PrivilegeConstants.EDIT_COHORTS })
	void purgeCohortMembership(CohortMembership cohortMembership);
	
	/**
	 * Marks the specified CohortMembership as voided
	 * 
	 * @param cohortMembership the CohortMembership to void
	 * @param reason void reason
	 * @return the voided CohortMembership
	 * @since 2.1.0
	 */
	@Authorized({ PrivilegeConstants.EDIT_COHORTS })
	CohortMembership voidCohortMembership(CohortMembership cohortMembership, String reason);
	
	/**
	 * Ends the specified CohortMembership i.e. sets its end date to the current date
	 * 
	 * @param cohortMembership the CohortMembership to end
	 * @param onDate when to end the membership (optional, defaults to now)
	 * @return the ended CohortMembership
	 * @since 2.1.0
	 */
	@Authorized({ PrivilegeConstants.EDIT_COHORTS })
	CohortMembership endCohortMembership(CohortMembership cohortMembership, Date onDate);
	
	/**
	 * NOTE: CLIENT CODE SHOULD NEVER CALL THIS METHOD. TREAT THIS AS AN INTERNAL METHOD WHICH MAY CHANGE WITHOUT WARNING.
	 *
	 * Used to notify this service that a patient has been voided, and therefore we should void all cohort memberships
	 * that refer to that patient
	 *
	 * @since 2.1.0
	 * @param patient patient that was voided
	 * <strong>Should</strong> void the membership for the patient that was passed in
	 */
	@Authorized({ PrivilegeConstants.EDIT_COHORTS })
	void notifyPatientVoided(Patient patient);
	
	/**
	 * NOTE: CLIENT CODE SHOULD NEVER CALL THIS METHOD. TREAT THIS AS AN INTERNAL METHOD WHICH MAY CHANGE WITHOUT WARNING.
	 *
	 * Used to notify this service that a patient has been unvoided, and therefore we should unvoid all cohort
	 * memberships that were automatically voided with the patient
	 *
	 * @since 2.1.0
	 * @param patient patient that was unvoided
	 * @param originallyVoidedBy
	 * @param originalDateVoided
	 * <strong>Should</strong> unvoid the membership for the patient that was passed in
	 */
	@Authorized({ PrivilegeConstants.EDIT_COHORTS })
	void notifyPatientUnvoided(Patient patient, User originallyVoidedBy, Date originalDateVoided);
	
	/**
	 * Gets memberships for the given patient, optionally active on a specific date
	 *
	 * @since 2.1.0
	 * @param patientId
	 * @param activeOnDate
	 * @param includeVoided
	 * @return matching memberships
	 */
	@Authorized({ PrivilegeConstants.GET_PATIENT_COHORTS })
	List<CohortMembership> getCohortMemberships(Integer patientId, Date activeOnDate, boolean includeVoided);
}
