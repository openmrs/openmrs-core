/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

import org.openmrs.Cohort
import org.openmrs.CohortMembership
import org.openmrs.Patient
import org.openmrs.User
import org.openmrs.annotation.Authorized
import org.openmrs.api.db.CohortDAO
import org.openmrs.util.PrivilegeConstants
import java.util.Date

/**
 * API methods related to Cohorts and CohortDefinitions.
 *
 * - A Cohort is a list of patient ids.
 * - A CohortDefinition is a search strategy which can be used to arrive at a cohort. Therefore,
 *   the patients returned by running a CohortDefinition can be different depending on the data that
 *   is stored elsewhere in the database.
 *
 * @see org.openmrs.Cohort
 */
interface CohortService : OpenmrsService {

    /**
     * Sets the CohortDAO for this service to use.
     *
     * @param dao the DAO to set
     */
    fun setCohortDAO(dao: CohortDAO)

    /**
     * Save a cohort to the database (create if new, or update if changed). This method will throw an
     * exception if any patientIds in the Cohort don't exist.
     *
     * @param cohort the cohort to be saved to the database
     * @return The cohort that was passed in
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.ADD_COHORTS, PrivilegeConstants.EDIT_COHORTS)
    @Throws(APIException::class)
    fun saveCohort(cohort: Cohort): Cohort

    /**
     * Voids the given cohort, deleting it from the perspective of the typical end user.
     *
     * @param cohort the cohort to delete
     * @param reason the reason this cohort is being retired
     * @return The cohort that was passed in
     * @throws APIException if voiding fails
     */
    @Authorized(PrivilegeConstants.DELETE_COHORTS)
    @Throws(APIException::class)
    fun voidCohort(cohort: Cohort, reason: String): Cohort

    /**
     * Completely removes a Cohort from the database (not reversible).
     *
     * @param cohort the Cohort to completely remove from the database
     * @return the purged Cohort
     * @throws APIException if purging fails
     */
    @Throws(APIException::class)
    fun purgeCohort(cohort: Cohort): Cohort

    /**
     * Gets a Cohort by its database primary key.
     *
     * @param id the cohort id
     * @return the Cohort with the given primary key, or null if none exists
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
    @Throws(APIException::class)
    fun getCohort(id: Int?): Cohort?

    /**
     * Gets a non voided Cohort by its name.
     *
     * @param name the cohort name
     * @return the Cohort with the given name, or null if none exists
     * @throws APIException if retrieval fails
     * @since 2.1.0
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
    @Throws(APIException::class)
    fun getCohortByName(name: String): Cohort?

    /**
     * @deprecated use [getCohortByName]
     */
    @Deprecated("Use getCohortByName instead", ReplaceWith("getCohortByName(name)"))
    @Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
    @Throws(APIException::class)
    fun getCohort(name: String): Cohort?

    /**
     * Gets all Cohorts (not including voided ones).
     *
     * @return All Cohorts in the database (not including voided ones)
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
    @Throws(APIException::class)
    fun getAllCohorts(): List<Cohort>

    /**
     * Gets all Cohorts, possibly including the voided ones.
     *
     * @param includeVoided whether or not to include voided Cohorts
     * @return All Cohorts, maybe including the voided ones
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
    @Throws(APIException::class)
    fun getAllCohorts(includeVoided: Boolean): List<Cohort>

    /**
     * Returns Cohorts whose names match the given string. Returns an empty list in the case of no
     * results. Returns all Cohorts in the case of null or empty input.
     *
     * @param nameFragment the name fragment to search for
     * @return list of cohorts matching the name fragment
     * @throws APIException if retrieval fails
     */
    @Throws(APIException::class)
    fun getCohorts(nameFragment: String?): List<Cohort>

    /**
     * Find all Cohorts that contain the given patient. (Not including voided Cohorts)
     *
     * @deprecated use [getCohortsContainingPatientId]
     * @param patient patient used to find the cohorts
     * @return All non-voided Cohorts that contain the given patient
     * @throws APIException if retrieval fails
     */
    @Deprecated("Use getCohortsContainingPatientId instead", ReplaceWith("getCohortsContainingPatientId(patient.patientId)"))
    @Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
    @Throws(APIException::class)
    fun getCohortsContainingPatient(patient: Patient): List<Cohort>

    /**
     * Find all Cohorts that contain the given patientId right now. (Not including voided Cohorts, or ended memberships)
     *
     * @param patientId patient id used to find the cohorts
     * @return All non-voided Cohorts that contain the given patientId
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
    @Throws(APIException::class)
    fun getCohortsContainingPatientId(patientId: Int?): List<Cohort>

    /**
     * Adds a new patient to a Cohort. If the patient is not already in the Cohort, then they are
     * added, and the Cohort is saved, marking it as changed.
     *
     * @param cohort the cohort to receive the given patient
     * @param patient the patient to insert into the cohort
     * @return The cohort that was passed in with the new patient in it
     * @throws APIException if adding fails
     */
    @Authorized(PrivilegeConstants.EDIT_COHORTS)
    @Throws(APIException::class)
    fun addPatientToCohort(cohort: Cohort, patient: Patient): Cohort

    /**
     * Removes a patient from a Cohort, by voiding their membership. (Has no effect if the patient is not in the cohort.)
     * (This behavior is provided for consistency with the pre-2.1.0 API, which didn't track cohort membership dates.)
     *
     * @deprecated since 2.1.0 you should explicitly call either [endCohortMembership] or [voidCohortMembership]
     * @param cohort the cohort containing the given patient
     * @param patient the patient to remove from the given cohort
     * @return The cohort that was passed in with the patient removed
     * @throws APIException if removal fails
     */
    @Deprecated("Use endCohortMembership or voidCohortMembership instead")
    @Authorized(PrivilegeConstants.EDIT_COHORTS)
    @Throws(APIException::class)
    fun removePatientFromCohort(cohort: Cohort, patient: Patient): Cohort

    /**
     * Get Cohort by its UUID.
     *
     * @param uuid the cohort UUID
     * @return cohort or null
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
    fun getCohortByUuid(uuid: String): Cohort?

    /**
     * Get CohortMembership by its UUID.
     *
     * @param uuid the membership UUID
     * @return cohort membership or null
     * @since 2.1.0
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
    fun getCohortMembershipByUuid(uuid: String): CohortMembership?

    /**
     * Removes a CohortMembership from its parent Cohort.
     *
     * @since 2.1.0
     * @param cohortMembership membership that will be removed from cohort
     */
    @Authorized(PrivilegeConstants.EDIT_COHORTS)
    fun purgeCohortMembership(cohortMembership: CohortMembership)

    /**
     * Marks the specified CohortMembership as voided.
     *
     * @param cohortMembership the CohortMembership to void
     * @param reason void reason
     * @return the voided CohortMembership
     * @since 2.1.0
     */
    @Authorized(PrivilegeConstants.EDIT_COHORTS)
    fun voidCohortMembership(cohortMembership: CohortMembership, reason: String): CohortMembership

    /**
     * Ends the specified CohortMembership i.e. sets its end date to the current date.
     *
     * @param cohortMembership the CohortMembership to end
     * @param onDate when to end the membership (optional, defaults to now)
     * @return the ended CohortMembership
     * @since 2.1.0
     */
    @Authorized(PrivilegeConstants.EDIT_COHORTS)
    fun endCohortMembership(cohortMembership: CohortMembership, onDate: Date?): CohortMembership

    /**
     * NOTE: CLIENT CODE SHOULD NEVER CALL THIS METHOD. TREAT THIS AS AN INTERNAL METHOD WHICH MAY CHANGE WITHOUT WARNING.
     *
     * Used to notify this service that a patient has been voided, and therefore we should void all cohort memberships
     * that refer to that patient.
     *
     * @since 2.1.0
     * @param patient patient that was voided
     */
    @Authorized(PrivilegeConstants.EDIT_COHORTS)
    fun notifyPatientVoided(patient: Patient)

    /**
     * NOTE: CLIENT CODE SHOULD NEVER CALL THIS METHOD. TREAT THIS AS AN INTERNAL METHOD WHICH MAY CHANGE WITHOUT WARNING.
     *
     * Used to notify this service that a patient has been unvoided, and therefore we should unvoid all cohort
     * memberships that were automatically voided with the patient.
     *
     * @since 2.1.0
     * @param patient patient that was unvoided
     * @param originallyVoidedBy the user who originally voided the patient
     * @param originalDateVoided the original date voided
     */
    @Authorized(PrivilegeConstants.EDIT_COHORTS)
    fun notifyPatientUnvoided(patient: Patient, originallyVoidedBy: User?, originalDateVoided: Date?)

    /**
     * Gets memberships for the given patient, optionally active on a specific date.
     *
     * @since 2.1.0
     * @param patientId the patient id
     * @param activeOnDate the date to check for active memberships
     * @param includeVoided whether to include voided memberships
     * @return matching memberships
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_COHORTS)
    fun getCohortMemberships(patientId: Int?, activeOnDate: Date?, includeVoided: Boolean): List<CohortMembership>
}
