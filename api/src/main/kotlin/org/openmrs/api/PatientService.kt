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

import org.openmrs.Allergies
import org.openmrs.Allergy
import org.openmrs.Concept
import org.openmrs.Location
import org.openmrs.Patient
import org.openmrs.PatientIdentifier
import org.openmrs.PatientIdentifierType
import org.openmrs.PatientProgram
import org.openmrs.annotation.Authorized
import org.openmrs.api.db.PatientDAO
import org.openmrs.patient.IdentifierValidator
import org.openmrs.serialization.SerializationException
import org.openmrs.util.PrivilegeConstants
import java.util.Date

/**
 * Contains methods pertaining to Patients in the system.
 *
 * Usage:
 * ```
 * val patients = Context.getPatientService().getAllPatients()
 * ```
 *
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.Patient
 */
interface PatientService : OpenmrsService {

    /**
     * Sets the DAO for this service.
     *
     * @param dao DAO for this service
     */
    fun setPatientDAO(dao: PatientDAO)

    /**
     * Saves the given patient to the database.
     *
     * @param patient patient to be created or updated
     * @return patient who was created or updated
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.ADD_PATIENTS, PrivilegeConstants.EDIT_PATIENTS)
    @Throws(APIException::class)
    fun savePatient(patient: Patient): Patient

    /**
     * Get patient by internal identifier.
     *
     * @param patientId internal patient identifier
     * @return patient with given internal identifier
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    @Throws(APIException::class)
    fun getPatient(patientId: Int?): Patient?

    /**
     * Get patient by internal identifier. If this id is for an existing person then instantiates a
     * new patient from that person.
     *
     * @param patientOrPersonId the patient or person id
     * @return a new unsaved patient or null if person or patient is not found
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    @Throws(APIException::class)
    fun getPatientOrPromotePerson(patientOrPersonId: Int?): Patient?

    /**
     * Get patient by universally unique identifier.
     *
     * @param uuid universally unique identifier
     * @return the patient that matches the uuid
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    @Throws(APIException::class)
    fun getPatientByUuid(uuid: String): Patient?

    /**
     * Get patient identifier by universally unique identifier.
     *
     * @param uuid universally unique identifier
     * @return the patient identifier that matches the uuid
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_IDENTIFIERS)
    @Throws(APIException::class)
    fun getPatientIdentifierByUuid(uuid: String): PatientIdentifier?

    /**
     * Returns all non voided patients in the system.
     *
     * @return non voided patients in the system
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    @Throws(APIException::class)
    fun getAllPatients(): List<Patient>

    /**
     * Returns patients in the system.
     *
     * @param includeVoided if false, will limit the search to non-voided patients
     * @return patients in the system
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    @Throws(APIException::class)
    fun getAllPatients(includeVoided: Boolean): List<Patient>

    /**
     * Get patients based on given criteria.
     *
     * @param name patients with a partial match on this name will be returned
     * @param identifier only patients with a matching identifier are returned
     * @param identifierTypes the PatientIdentifierTypes to restrict to
     * @param matchIdentifierExactly if true, then the given identifier must equal the id in the database
     * @return patients that matched the given criteria (and are not voided)
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    @Throws(APIException::class)
    fun getPatients(
        name: String?,
        identifier: String?,
        identifierTypes: @JvmSuppressWildcards List<PatientIdentifierType>?,
        matchIdentifierExactly: Boolean
    ): List<Patient>

    /**
     * Void patient record (functionally delete patient from system).
     *
     * @param patient patient to be voided
     * @param reason reason for voiding patient
     * @return the voided patient
     * @throws APIException if voiding fails
     */
    @Authorized(PrivilegeConstants.DELETE_PATIENTS)
    @Throws(APIException::class)
    fun voidPatient(patient: Patient, reason: String): Patient?

    /**
     * Unvoid patient record.
     *
     * @param patient patient to be revived
     * @return the revived Patient
     * @throws APIException if unvoiding fails
     */
    @Authorized(PrivilegeConstants.DELETE_PATIENTS)
    @Throws(APIException::class)
    fun unvoidPatient(patient: Patient): Patient

    /**
     * Delete patient from database.
     *
     * @param patient patient to be deleted
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_PATIENTS)
    @Throws(APIException::class)
    fun purgePatient(patient: Patient)

    /**
     * Get all patientIdentifiers that match all of the given criteria.
     *
     * @param identifier the full identifier to match on
     * @param patientIdentifierTypes the type of identifiers to get
     * @param locations the locations of the identifiers to match
     * @param patients the patients containing these identifiers
     * @param isPreferred if true, limits to only preferred identifiers
     * @return PatientIdentifiers matching these criteria
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_IDENTIFIERS)
    @Throws(APIException::class)
    fun getPatientIdentifiers(
        identifier: String?,
        patientIdentifierTypes: @JvmSuppressWildcards List<PatientIdentifierType>?,
        locations: @JvmSuppressWildcards List<Location>?,
        patients: @JvmSuppressWildcards List<Patient>?,
        isPreferred: Boolean?
    ): List<PatientIdentifier>

    /**
     * Create or update a PatientIdentifierType.
     *
     * @param patientIdentifierType PatientIdentifierType to create or update
     * @return the saved type
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.MANAGE_IDENTIFIER_TYPES)
    @Throws(APIException::class)
    fun savePatientIdentifierType(patientIdentifierType: PatientIdentifierType): PatientIdentifierType

    /**
     * Get all patientIdentifier types.
     *
     * @return patientIdentifier types list
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_IDENTIFIER_TYPES)
    @Throws(APIException::class)
    fun getAllPatientIdentifierTypes(): List<PatientIdentifierType>

    /**
     * Get all patientIdentifier types.
     *
     * @param includeRetired true/false whether retired types should be included
     * @return patientIdentifier types list
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_IDENTIFIER_TYPES)
    @Throws(APIException::class)
    fun getAllPatientIdentifierTypes(includeRetired: Boolean): List<PatientIdentifierType>

    /**
     * Get all patientIdentifier types that match the given criteria.
     *
     * @param name name of the type to match on
     * @param format the string format to match on
     * @param required if true, limits to only identifiers marked as required
     * @param hasCheckDigit if true, limits to only check digit'd identifiers
     * @return patientIdentifier types list
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_IDENTIFIER_TYPES)
    @Throws(APIException::class)
    fun getPatientIdentifierTypes(
        name: String?,
        format: String?,
        required: Boolean?,
        hasCheckDigit: Boolean?
    ): List<PatientIdentifierType>

    /**
     * Get patientIdentifierType by internal identifier.
     *
     * @param patientIdentifierTypeId the id
     * @return patientIdentifierType with specified internal identifier
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_IDENTIFIER_TYPES)
    @Throws(APIException::class)
    fun getPatientIdentifierType(patientIdentifierTypeId: Int?): PatientIdentifierType?

    /**
     * Get patient identifierType by universally unique identifier.
     *
     * @param uuid the uuid
     * @return patientIdentifierType with specified internal identifier
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_IDENTIFIER_TYPES)
    @Throws(APIException::class)
    fun getPatientIdentifierTypeByUuid(uuid: String): PatientIdentifierType?

    /**
     * Get patientIdentifierType by exact name.
     *
     * @param name the name
     * @return patientIdentifierType with given name
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_IDENTIFIER_TYPES)
    @Throws(APIException::class)
    fun getPatientIdentifierTypeByName(name: String): PatientIdentifierType?

    /**
     * Retire a type of patient identifier.
     *
     * @param patientIdentifierType type of patient identifier to be retired
     * @param reason the reason to retire this identifier type
     * @return the retired type
     * @throws APIException if retiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_IDENTIFIER_TYPES)
    @Throws(APIException::class)
    fun retirePatientIdentifierType(patientIdentifierType: PatientIdentifierType, reason: String): PatientIdentifierType

    /**
     * Unretire a type of patient identifier.
     *
     * @param patientIdentifierType type of patient identifier to be unretired
     * @return the unretired type
     * @throws APIException if unretiring fails
     */
    @Authorized(PrivilegeConstants.MANAGE_IDENTIFIER_TYPES)
    @Throws(APIException::class)
    fun unretirePatientIdentifierType(patientIdentifierType: PatientIdentifierType): PatientIdentifierType

    /**
     * Purge PatientIdentifierType (cannot be undone).
     *
     * @param patientIdentifierType PatientIdentifierType to purge from the database
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_IDENTIFIER_TYPES)
    @Throws(APIException::class)
    fun purgePatientIdentifierType(patientIdentifierType: PatientIdentifierType)

    /**
     * Convenience method to validate all identifiers for a given patient.
     *
     * @param patient patient for which to validate identifiers
     * @throws PatientIdentifierException if one or more of the identifiers are invalid
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_IDENTIFIERS)
    @Throws(PatientIdentifierException::class)
    fun checkPatientIdentifiers(patient: Patient)

    /**
     * Generic search on patients based on the given string.
     *
     * @param query the string to search on
     * @return a list of matching Patients
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    @Throws(APIException::class)
    fun getPatients(query: String?): List<Patient>

    /**
     * Generic search on patients based on the given string.
     *
     * @param query the string to search on
     * @param start the starting index
     * @param length the number of patients to return
     * @return a list of matching Patients
     * @throws APIException if retrieval fails
     * @since 1.8
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    @Throws(APIException::class)
    fun getPatients(query: String?, start: Int?, length: Int?): List<Patient>

    /**
     * Generic search on patients based on the given string.
     *
     * @param query the string to search on
     * @param includeVoided true/false whether or not to included voided patients
     * @param start the starting index
     * @param length the number of patients to return
     * @return a list of matching Patients
     * @throws APIException if retrieval fails
     * @since 1.11
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    @Throws(APIException::class)
    fun getPatients(query: String?, includeVoided: Boolean, start: Int?, length: Int?): List<Patient>

    /**
     * This method tries to find a patient in the database given the attributes.
     *
     * @param patientToMatch the patient to match
     * @return null if no match found, a fresh patient object from the db if is found
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    @Throws(APIException::class)
    fun getPatientByExample(patientToMatch: Patient): Patient?

    /**
     * Search the database for patients that both share the given attributes.
     *
     * @param attributes attributes on a Person or Patient object
     * @return list of patients that match other patients
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    @Throws(APIException::class)
    fun getDuplicatePatientsByAttributes(attributes: @JvmSuppressWildcards List<String>): List<Patient>

    /**
     * Convenience method to join two patients' information into one record.
     *
     * @param preferred The Patient to merge to
     * @param notPreferred The Patient to merge from (and then void)
     * @throws APIException if merging fails
     * @throws SerializationException if serialization fails
     */
    @Authorized(PrivilegeConstants.EDIT_PATIENTS)
    @Throws(APIException::class, SerializationException::class)
    fun mergePatients(preferred: Patient, notPreferred: Patient)

    /**
     * Convenience method to join multiple patients' information into one record.
     *
     * @param preferred the preferred patient
     * @param notPreferred the list of patients to merge
     * @throws APIException if merging fails
     * @throws SerializationException if serialization fails
     */
    @Throws(APIException::class, SerializationException::class)
    fun mergePatients(preferred: Patient, notPreferred: @JvmSuppressWildcards List<Patient>)

    /**
     * Convenience method to establish that a patient has died.
     *
     * @param patient - the patient who has died
     * @param dateDied - the declared date/time of the patient's death
     * @param causeOfDeath - the concept that corresponds with the reason the patient died
     * @param otherReason - if the concept representing the reason is OTHER NON-CODED
     * @throws APIException if processing fails
     */
    @Authorized(PrivilegeConstants.EDIT_PATIENTS)
    @Throws(APIException::class)
    fun processDeath(patient: Patient, dateDied: Date, causeOfDeath: Concept, otherReason: String?)

    /**
     * Convenience method that saves the Obs that indicates when and why the patient died.
     *
     * @param patient - the patient who has died
     * @param dateDied - the declared date/time of the patient's death
     * @param causeOfDeath - the concept that corresponds with the reason the patient died
     * @param otherReason - if the concept representing the reason is OTHER NON-CODED
     * @throws APIException if saving fails
     */
    @Authorized(value = [PrivilegeConstants.GET_PATIENTS, PrivilegeConstants.EDIT_OBS], requireAll = true)
    @Throws(APIException::class)
    fun saveCauseOfDeathObs(patient: Patient, dateDied: Date, causeOfDeath: Concept, otherReason: String?)

    /**
     * Gets an identifier validator matching the given class.
     *
     * @param clazz identifierValidator which validator to get.
     * @return the identifier validator
     */
    fun getIdentifierValidator(clazz: Class<IdentifierValidator>): IdentifierValidator?

    /**
     * Gets an identifier validator by class name.
     *
     * @param pivClassName the class name
     * @return the identifier validator
     */
    fun getIdentifierValidator(pivClassName: String?): IdentifierValidator?

    /**
     * Gets the default IdentifierValidator.
     *
     * @return the default IdentifierValidator
     */
    fun getDefaultIdentifierValidator(): IdentifierValidator?

    /**
     * Gets all registered PatientIdentifierValidators.
     *
     * @return All registered PatientIdentifierValidators
     */
    fun getAllIdentifierValidators(): Collection<IdentifierValidator>

    /**
     * Checks whether the given patient identifier is already assigned to a patient other than
     * patientIdentifier.patient.
     *
     * @param patientIdentifier the patient identifier to look for in other patients
     * @return whether or not the identifier is in use by a patient
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    fun isIdentifierInUseByAnotherPatient(patientIdentifier: PatientIdentifier): Boolean

    /**
     * Returns a patient identifier that matches the given patientIdentifier id.
     *
     * @param patientIdentifierId the patientIdentifier id
     * @return the patientIdentifier matching the Id
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_IDENTIFIERS)
    @Throws(APIException::class)
    fun getPatientIdentifier(patientIdentifierId: Int?): PatientIdentifier?

    /**
     * Void patient identifier (functionally delete patient identifier from system).
     *
     * @param patientIdentifier patientIdentifier to be voided
     * @param reason reason for voiding patient identifier
     * @return the voided patient identifier
     * @throws APIException if voiding fails
     */
    @Authorized(PrivilegeConstants.DELETE_PATIENT_IDENTIFIERS)
    @Throws(APIException::class)
    fun voidPatientIdentifier(patientIdentifier: PatientIdentifier, reason: String): PatientIdentifier

    /**
     * Saves the given patientIdentifier to the database.
     *
     * @param patientIdentifier patientIdentifier to be created or updated
     * @return patientIdentifier that was created or updated
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.ADD_PATIENT_IDENTIFIERS, PrivilegeConstants.EDIT_PATIENT_IDENTIFIERS)
    @Throws(APIException::class)
    fun savePatientIdentifier(patientIdentifier: PatientIdentifier): PatientIdentifier

    /**
     * Purge PatientIdentifier (cannot be undone).
     *
     * @param patientIdentifier PatientIdentifier to purge from the database
     * @throws APIException if purging fails
     */
    @Authorized(PrivilegeConstants.PURGE_PATIENT_IDENTIFIERS)
    @Throws(APIException::class)
    fun purgePatientIdentifier(patientIdentifier: PatientIdentifier)

    /**
     * Gets allergies for a given patient.
     *
     * @param patient the patient
     * @return the allergies object
     */
    fun getAllergies(patient: Patient): Allergies

    /**
     * Updates the patient's allergies.
     *
     * @param patient the patient
     * @param allergies the allergies
     * @return the saved allergies
     */
    fun setAllergies(patient: Patient, allergies: Allergies): Allergies

    /**
     * Returns the Allergy identified by internal Integer Id.
     *
     * @param allergyListId identifies allergy by internal Integer Id
     * @return the allergy
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_ALLERGIES)
    @Throws(APIException::class)
    fun getAllergy(allergyListId: Int?): Allergy?

    /**
     * Returns the Allergy identified by uuid.
     *
     * @param uuid identifies allergy
     * @return the allergy matching the given uuid
     * @throws APIException if retrieval fails
     * @since 2.0
     */
    @Authorized(PrivilegeConstants.GET_ALLERGIES)
    @Throws(APIException::class)
    fun getAllergyByUuid(uuid: String): Allergy?

    /**
     * Creates an Allergy to the Patient's Allergy Active List.
     *
     * @param allergy the Allergy
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.ADD_ALLERGIES, PrivilegeConstants.EDIT_ALLERGIES)
    @Throws(APIException::class)
    fun saveAllergy(allergy: Allergy)

    /**
     * Removes the Allergy from the Patient's Active List.
     *
     * @param allergy the Allergy
     * @param reason the reason of remove
     * @throws APIException if removal fails
     */
    @Authorized(PrivilegeConstants.EDIT_ALLERGIES)
    @Throws(APIException::class)
    fun removeAllergy(allergy: Allergy, reason: String)

    /**
     * Used only in cases where the Allergy was entered by error.
     *
     * @param allergy the allergy to void
     * @param reason the reason
     * @throws APIException if voiding fails
     */
    @Authorized(PrivilegeConstants.DELETE_ALLERGIES)
    @Throws(APIException::class)
    fun voidAllergy(allergy: Allergy, reason: String)

    /**
     * Return the number of unvoided patients with names or patient identifiers starting with or
     * equal to the specified text.
     *
     * @param query the string to search on
     * @return the number of patients matching the given search phrase
     * @since 1.8
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    fun getCountOfPatients(query: String?): Int?

    /**
     * Return the number of patients matching the given search phrase.
     *
     * @param query the string to search on
     * @param includeVoided true/false whether or not to included voided patients
     * @return the number of patients matching the given search phrase
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    fun getCountOfPatients(query: String?, includeVoided: Boolean): Int?

    /**
     * Get a limited size of patients from a given start index based on given criteria.
     *
     * @param name patients with a partial match on this name will be returned
     * @param identifier only patients with a matching identifier are returned
     * @param identifierTypes the PatientIdentifierTypes to restrict to
     * @param matchIdentifierExactly if true, then the given identifier must equal the id in the database
     * @param start the starting index
     * @param length the number of patients to return
     * @return patients that matched the given criteria (and are not voided)
     * @throws APIException if retrieval fails
     * @since 1.8
     */
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    @Throws(APIException::class)
    fun getPatients(
        name: String?,
        identifier: String?,
        identifierTypes: @JvmSuppressWildcards List<PatientIdentifierType>?,
        matchIdentifierExactly: Boolean,
        start: Int?,
        length: Int?
    ): List<Patient>

    /**
     * Check if patient identifier types are locked.
     *
     * @throws PatientIdentifierTypeLockedException if types are locked
     */
    @Throws(PatientIdentifierTypeLockedException::class)
    fun checkIfPatientIdentifierTypesAreLocked()

    /**
     * Get all patientIdentifiers that are associated to the patient program.
     *
     * @param patientProgram the patientProgram to be used to fetch the associated identifiers
     * @return PatientIdentifiers matching the patient program
     * @since 2.6.0
     */
    @Authorized(PrivilegeConstants.GET_PATIENT_IDENTIFIERS)
    fun getPatientIdentifiersByPatientProgram(patientProgram: PatientProgram): List<PatientIdentifier>
}
