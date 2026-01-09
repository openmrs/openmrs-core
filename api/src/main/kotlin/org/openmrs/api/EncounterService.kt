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
import org.openmrs.Encounter
import org.openmrs.EncounterRole
import org.openmrs.EncounterType
import org.openmrs.Form
import org.openmrs.Location
import org.openmrs.Patient
import org.openmrs.Provider
import org.openmrs.User
import org.openmrs.Visit
import org.openmrs.VisitType
import org.openmrs.annotation.Authorized
import org.openmrs.api.db.EncounterDAO
import org.openmrs.api.handler.EncounterVisitHandler
import org.openmrs.parameter.EncounterSearchCriteria
import org.openmrs.util.PrivilegeConstants
import java.util.Date

/**
 * Services for Encounters and Encounter Types.
 *
 * @version 1.0
 */
interface EncounterService : OpenmrsService {

    /**
     * Set the given dao on this encounter service.
     *
     * @param dao the DAO to set
     */
    fun setEncounterDAO(dao: EncounterDAO)

    /**
     * Saves a new encounter or updates an existing encounter.
     *
     * @param encounter to be saved
     * @return the saved encounter
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.ADD_ENCOUNTERS, PrivilegeConstants.EDIT_ENCOUNTERS)
    @Throws(APIException::class)
    fun saveEncounter(encounter: Encounter): Encounter

    /**
     * Get encounter by internal identifier.
     *
     * @param encounterId encounter id
     * @return encounter with given internal identifier
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    @Throws(APIException::class)
    fun getEncounter(encounterId: Int?): Encounter?

    /**
     * Get Encounter by its UUID.
     *
     * @param uuid the uuid
     * @return encounter or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    @Throws(APIException::class)
    fun getEncounterByUuid(uuid: String): Encounter?

    /**
     * Get all encounters (not voided) for a patient, sorted by encounterDatetime ascending.
     *
     * @param patient the patient
     * @return List of encounters (not voided) for a patient
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    fun getEncountersByPatient(patient: Patient): List<Encounter>

    /**
     * Get encounters for a patientId.
     *
     * @param patientId the patient id
     * @return all encounters (not voided) for the given patient identifier
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    @Throws(APIException::class)
    fun getEncountersByPatientId(patientId: Int?): List<Encounter>

    /**
     * Get encounters (not voided) for a patient identifier.
     *
     * @param identifier the patient identifier
     * @return all encounters (not retired) for the given patient identifier
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    @Throws(APIException::class)
    fun getEncountersByPatientIdentifier(identifier: String): List<Encounter>

    /**
     * Get all encounters that match a variety of (nullable) criteria.
     *
     * @param who the patient the encounter is for
     * @param loc the location this encounter took place
     * @param fromDate the minimum date (inclusive) this encounter took place
     * @param toDate the maximum date (exclusive) this encounter took place
     * @param enteredViaForms the form that entered this encounter must be in this list
     * @param encounterTypes the type of encounter must be in this list
     * @param providers the provider of this encounter must be in this list
     * @param visitTypes the visit types of this encounter must be in this list
     * @param visits the visits of this encounter must be in this list
     * @param includeVoided true/false to include the voided encounters or not
     * @return a list of encounters ordered by increasing encounterDatetime
     * @since 1.9
     * @deprecated As of 2.0, replaced by [getEncounters(EncounterSearchCriteria)]
     */
    @Deprecated("As of 2.0, replaced by getEncounters(EncounterSearchCriteria)")
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    fun getEncounters(
        who: Patient?,
        loc: Location?,
        fromDate: Date?,
        toDate: Date?,
        enteredViaForms: @JvmSuppressWildcards Collection<Form>?,
        encounterTypes: @JvmSuppressWildcards Collection<EncounterType>?,
        providers: @JvmSuppressWildcards Collection<Provider>?,
        visitTypes: @JvmSuppressWildcards Collection<VisitType>?,
        visits: @JvmSuppressWildcards Collection<Visit>?,
        includeVoided: Boolean
    ): List<Encounter>

    /**
     * Get all encounters that match a variety of (nullable) criteria contained in the parameter object.
     *
     * @param encounterSearchCriteria the object containing search parameters
     * @return a list of encounters ordered by increasing encounterDatetime
     * @since 1.12
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    fun getEncounters(encounterSearchCriteria: EncounterSearchCriteria): List<Encounter>

    /**
     * Voiding an encounter essentially removes it from circulation.
     *
     * @param encounter Encounter object to void
     * @param reason String reason that it's being voided
     * @return the voided encounter
     */
    @Authorized(PrivilegeConstants.EDIT_ENCOUNTERS)
    fun voidEncounter(encounter: Encounter, reason: String): Encounter

    /**
     * Unvoid encounter record.
     *
     * @param encounter Encounter to be revived
     * @return the unvoided encounter
     * @throws APIException if unvoiding fails
     */
    @Authorized(PrivilegeConstants.EDIT_ENCOUNTERS)
    @Throws(APIException::class)
    fun unvoidEncounter(encounter: Encounter): Encounter

    /**
     * Completely remove an encounter from database.
     *
     * @param encounter encounter object to be purged
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_ENCOUNTERS)
    @Throws(APIException::class)
    fun purgeEncounter(encounter: Encounter)

    /**
     * Completely remove an encounter from database.
     *
     * @param encounter encounter object to be purged
     * @param cascade Purge any related observations as well?
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_ENCOUNTERS)
    @Throws(APIException::class)
    fun purgeEncounter(encounter: Encounter, cascade: Boolean)

    /**
     * Save a new Encounter Type or update an existing Encounter Type.
     *
     * @param encounterType the encounter type to save
     * @return the saved encounter type
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_ENCOUNTER_TYPES)
    @Throws(APIException::class)
    fun saveEncounterType(encounterType: EncounterType): EncounterType

    /**
     * Get encounterType by internal identifier.
     *
     * @param encounterTypeId Integer
     * @return encounterType with given internal identifier
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTER_TYPES)
    @Throws(APIException::class)
    fun getEncounterType(encounterTypeId: Int?): EncounterType?

    /**
     * Get EncounterType by its UUID.
     *
     * @param uuid the uuid
     * @return encounter type or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTER_TYPES)
    @Throws(APIException::class)
    fun getEncounterTypeByUuid(uuid: String): EncounterType?

    /**
     * Get encounterType by exact name.
     *
     * @param name string to match to an Encounter.name
     * @return EncounterType that is not retired
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTER_TYPES)
    @Throws(APIException::class)
    fun getEncounterType(name: String): EncounterType?

    /**
     * Get all encounter types (including retired).
     *
     * @return encounter types list
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTER_TYPES)
    @Throws(APIException::class)
    fun getAllEncounterTypes(): List<EncounterType>

    /**
     * Get all encounter types. If includeRetired is true, also get retired encounter types.
     *
     * @param includeRetired whether to include retired encounter types
     * @return encounter types list
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTER_TYPES)
    @Throws(APIException::class)
    fun getAllEncounterTypes(includeRetired: Boolean): List<EncounterType>

    /**
     * Find Encounter Types with name matching the beginning of the search string.
     *
     * @param name of the encounter type to find
     * @return List of matching encounters
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTER_TYPES)
    @Throws(APIException::class)
    fun findEncounterTypes(name: String): List<EncounterType>

    /**
     * Retire an EncounterType.
     *
     * @param encounterType the encounter type to retire
     * @param reason required non-null purpose for retiring this encounter type
     * @return the retired encounter type
     * @throws APIException if retiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_ENCOUNTER_TYPES)
    @Throws(APIException::class)
    fun retireEncounterType(encounterType: EncounterType, reason: String): EncounterType

    /**
     * Unretire an EncounterType.
     *
     * @param encounterType the encounter type to unretire
     * @return the unretired encounter type
     * @throws APIException if unretiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_ENCOUNTER_TYPES)
    @Throws(APIException::class)
    fun unretireEncounterType(encounterType: EncounterType): EncounterType

    /**
     * Completely remove an encounter type from database.
     *
     * @param encounterType the encounter type to purge
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_ENCOUNTER_TYPES)
    @Throws(APIException::class)
    fun purgeEncounterType(encounterType: EncounterType)

    /**
     * Search for encounters by patient name or patient identifier.
     *
     * @param query patient name or identifier
     * @return list of encounters for the given patient
     * @throws APIException if retrieval fails
     * @see EncounterService.getEncountersByPatient
     * @since 1.7
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    @Throws(APIException::class)
    fun getEncountersByPatient(query: String): List<Encounter>

    /**
     * Search for encounters by patient name or patient identifier.
     *
     * @param query patient name or identifier
     * @param includeVoided Specifies whether voided encounters should be included
     * @return list of encounters for the given patient
     * @throws APIException if retrieval fails
     * @since 1.7
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    @Throws(APIException::class)
    fun getEncountersByPatient(query: String, includeVoided: Boolean): List<Encounter>

    /**
     * Search for encounters by patient name or patient identifier and returns a specific number of
     * them from the specified starting position.
     *
     * @param query patient name or identifier
     * @param start beginning index for the batch
     * @param length number of encounters to return in the batch
     * @param includeVoided Specifies whether voided encounters should be included
     * @return list of encounters for the given patient based on batch settings
     * @throws APIException if retrieval fails
     * @since 1.8
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    @Throws(APIException::class)
    fun getEncounters(query: String?, start: Int?, length: Int?, includeVoided: Boolean): List<Encounter>

    /**
     * Searches for encounters by patient id, provider identifier, location, encounter type,
     * provider, form or provider name.
     *
     * @param query provider identifier, location, encounter type, provider, form or provider name
     * @param patientId the patient id
     * @param start beginning index for the batch
     * @param length number of encounters to return in the batch
     * @param includeVoided Specifies whether voided encounters should be included
     * @return list of encounters for the given patient based on batch settings
     * @throws APIException if retrieval fails
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    @Throws(APIException::class)
    fun getEncounters(query: String?, patientId: Int?, start: Int?, length: Int?, includeVoided: Boolean): List<Encounter>

    /**
     * Get all encounters for a cohort of patients.
     *
     * @param patients Cohort of patients to search
     * @return Map of all encounters for specified patients
     * @since 1.8
     */
    fun getAllEncounters(patients: Cohort): Map<Int, List<Encounter>>

    /**
     * Return the number of encounters matching a patient name or patient identifier.
     *
     * @param query patient name or identifier
     * @param includeVoided Specifies whether voided encounters should be included
     * @return the number of encounters matching the given search phrase
     * @since 1.8
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    fun getCountOfEncounters(query: String?, includeVoided: Boolean): Int?

    /**
     * Gets all encounters grouped within a given visit.
     *
     * @param visit the visit.
     * @param includeVoided whether voided encounters should be returned
     * @return list of encounters in the given visit.
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    fun getEncountersByVisit(visit: Visit, includeVoided: Boolean): List<Encounter>

    /**
     * Returns list of handlers for determining if an encounter should go into a visit.
     *
     * @return list of handlers
     * @see EncounterVisitHandler
     * @since 1.9
     */
    fun getEncounterVisitHandlers(): List<EncounterVisitHandler>

    /**
     * Gets the active handler for assigning visits to encounters.
     *
     * @see EncounterVisitHandler
     * @since 1.9
     * @return the active handler class.
     * @throws APIException thrown if something goes wrong during the retrieval of the handler.
     */
    @Throws(APIException::class)
    fun getActiveEncounterVisitHandler(): EncounterVisitHandler?

    /**
     * Saves a new encounter role or updates an existing encounter role.
     *
     * @param encounterRole to be saved
     * @return EncounterRole
     * @throws APIException if saving fails
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.MANAGE_ENCOUNTER_ROLES)
    @Throws(APIException::class)
    fun saveEncounterRole(encounterRole: EncounterRole): EncounterRole

    /**
     * Gets an encounter role when an internal encounter role id is provided.
     *
     * @param encounterRoleId to be retrieved
     * @return EncounterRole
     * @throws APIException if retrieval fails
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTER_ROLES)
    @Throws(APIException::class)
    fun getEncounterRole(encounterRoleId: Int?): EncounterRole?

    /**
     * Completely remove an encounter role from database.
     *
     * @param encounterRole encounter role object to be purged
     * @throws APIException if purging fails
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.PURGE_ENCOUNTER_ROLES)
    @Throws(APIException::class)
    fun purgeEncounterRole(encounterRole: EncounterRole)

    /**
     * Get all encounter roles based on includeRetired flag.
     *
     * @param includeRetired whether to include retired
     * @return List of all encounter roles
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTER_ROLES)
    fun getAllEncounterRoles(includeRetired: Boolean): List<EncounterRole>

    /**
     * Get EncounterRole by its UUID.
     *
     * @param uuid the uuid
     * @return EncounterRole
     * @throws APIException if retrieval fails
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTER_ROLES)
    @Throws(APIException::class)
    fun getEncounterRoleByUuid(uuid: String): EncounterRole?

    /**
     * Get EncounterRole by name.
     *
     * @param name the name
     * @return EncounterRole object by name
     * @since 1.10
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTER_ROLES)
    fun getEncounterRoleByName(name: String): EncounterRole?

    /**
     * Retire an EncounterRole.
     *
     * @param encounterRole the encounter role to retire
     * @param reason required non-null purpose for retiring this encounter role
     * @return the retired encounter role
     * @throws APIException if retiring fails
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.MANAGE_ENCOUNTER_ROLES)
    @Throws(APIException::class)
    fun retireEncounterRole(encounterRole: EncounterRole, reason: String): EncounterRole

    /**
     * Unretire an EncounterRole.
     *
     * @param encounterType the encounter role to unretire
     * @return the unretired encounter role
     * @throws APIException if unretiring fails
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.MANAGE_ENCOUNTER_ROLES)
    @Throws(APIException::class)
    fun unretireEncounterRole(encounterType: EncounterRole): EncounterRole

    /**
     * Gets the unvoided encounters for the specified patient that are not assigned to any visit.
     *
     * @param patient the patient to match against
     * @return a list of Encounters
     * @throws APIException if retrieval fails
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    @Throws(APIException::class)
    fun getEncountersNotAssignedToAnyVisit(patient: Patient): List<Encounter>

    /**
     * Gets encounters for the given patient. It populates results with empty encounters to include
     * visits that have no assigned encounters.
     *
     * @param patient the patient to match
     * @param includeVoided if voided encounters or visits should be included
     * @param query filters results
     * @param start index to start with
     * @param length number of results to return
     * @return encounters and empty encounters with only visit set
     * @throws APIException if retrieval fails
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_VISITS)
    @Throws(APIException::class)
    fun getEncountersByVisitsAndPatient(
        patient: Patient,
        includeVoided: Boolean,
        query: String?,
        start: Int?,
        length: Int?
    ): List<Encounter>

    /**
     * Returns result count for [getEncountersByVisitsAndPatient].
     *
     * @param patient the patient
     * @param includeVoided whether to include voided
     * @param query the query
     * @return number of results
     * @throws APIException if retrieval fails
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.GET_VISITS)
    @Throws(APIException::class)
    fun getEncountersByVisitsAndPatientCount(patient: Patient, includeVoided: Boolean, query: String?): Int?

    /**
     * Filters out all encounters to which given user does not have access.
     *
     * @param encounters the list of encounters to be filtered
     * @param user the user instance to filter "visible" encounters for
     * @return list that does not include encounters which can not be shown to given user
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTERS)
    fun filterEncountersByViewPermissions(encounters: @JvmSuppressWildcards List<Encounter>, user: User?): List<Encounter>

    /**
     * Determines whether given user is granted to view all encounter types or not.
     *
     * @param subject the user whose permission to view all encounter types will be checked
     * @return true if user has access to view all types of encounters
     */
    fun canViewAllEncounterTypes(subject: User): Boolean

    /**
     * Determines whether given user is granted to edit all encounter types or not.
     *
     * @param subject the user whose permission to edit all encounter types will be checked
     * @return true if user has access to edit all types of encounters
     */
    fun canEditAllEncounterTypes(subject: User): Boolean

    /**
     * Checks if passed in user can edit given encounter.
     *
     * @param encounter the encounter instance to be checked
     * @param subject the user who requests edit access
     * @return true if user has privilege denoted by editPrivilege given on encounter type
     */
    fun canEditEncounter(encounter: Encounter, subject: User?): Boolean

    /**
     * Checks if passed in user can view given encounter.
     *
     * @param encounter the encounter instance to be checked
     * @param subject the user who requests view access
     * @return true if user has privilege denoted by viewPrivilege given on encounter type
     */
    fun canViewEncounter(encounter: Encounter, subject: User?): Boolean

    /**
     * Check if the encounter types are locked, and if so, throw exception during manipulation of
     * encounter type.
     *
     * @throws EncounterTypeLockedException if types are locked
     */
    @Throws(EncounterTypeLockedException::class)
    fun checkIfEncounterTypesAreLocked()

    /**
     * Get EncounterRoles by name.
     *
     * @param name the name
     * @return List of EncounterRole objects
     * @since 1.11
     */
    @Authorized(PrivilegeConstants.GET_ENCOUNTER_ROLES)
    fun getEncounterRolesByName(name: String): List<EncounterRole>

    /**
     * Transfer encounter to another patient.
     *
     * @param encounter the encounter to transfer
     * @param patient the destination patient
     * @return transferred encounter
     * @since 1.12
     */
    @Authorized(PrivilegeConstants.EDIT_ENCOUNTERS)
    fun transferEncounter(encounter: Encounter, patient: Patient): Encounter
}
