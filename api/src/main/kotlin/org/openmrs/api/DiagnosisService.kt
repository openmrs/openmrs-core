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

import org.openmrs.Diagnosis
import org.openmrs.DiagnosisAttribute
import org.openmrs.DiagnosisAttributeType
import org.openmrs.Encounter
import org.openmrs.Patient
import org.openmrs.Visit
import org.openmrs.annotation.Authorized
import org.openmrs.util.PrivilegeConstants
import java.util.Date

/**
 * API methods for managing diagnoses.
 *
 * @since 2.2
 */
interface DiagnosisService : OpenmrsService {

    /**
     * Saves a diagnosis.
     *
     * @param diagnosis the diagnosis to be saved
     * @return the diagnosis
     */
    @Authorized(PrivilegeConstants.EDIT_DIAGNOSES)
    fun save(diagnosis: Diagnosis): Diagnosis

    /**
     * Voids a diagnosis.
     *
     * @param diagnosis the diagnosis to be voided
     * @param voidReason the reason for voiding the diagnosis
     * @return the diagnosis that was voided
     */
    @Authorized(PrivilegeConstants.EDIT_DIAGNOSES)
    fun voidDiagnosis(diagnosis: Diagnosis, voidReason: String): Diagnosis

    /**
     * Gets a diagnosis based on the uuid.
     *
     * @param uuid uuid of the diagnosis to be returned
     * @return diagnosis matching the given uuid
     */
    @Authorized(PrivilegeConstants.GET_DIAGNOSES)
    fun getDiagnosisByUuid(uuid: String): Diagnosis?

    /**
     * Gets diagnoses since date, sorted in reverse chronological order.
     *
     * @param patient the patient whose diagnosis we are to get
     * @param fromDate the date used to filter diagnosis which happened from this date and later
     * @return the list of diagnoses for the given patient and starting from the given date
     */
    @Authorized(PrivilegeConstants.GET_DIAGNOSES)
    fun getDiagnoses(patient: Patient, fromDate: Date?): List<Diagnosis>

    /**
     * Gets diagnoses for an Encounter.
     *
     * @param encounter the encounter for which to fetch diagnoses
     * @param primaryOnly whether to return only primary diagnoses
     * @param confirmedOnly whether to return only confirmed diagnoses
     * @return the list of diagnoses for the given encounter
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.GET_DIAGNOSES)
    fun getDiagnosesByEncounter(encounter: Encounter, primaryOnly: Boolean, confirmedOnly: Boolean): List<Diagnosis>

    /**
     * Gets diagnoses for a Visit.
     *
     * @param visit the visit for which to fetch diagnoses
     * @param primaryOnly whether to return only primary diagnoses
     * @param confirmedOnly whether to return only confirmed diagnoses
     * @return the list of diagnoses for the given visit
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.GET_DIAGNOSES)
    fun getDiagnosesByVisit(visit: Visit, primaryOnly: Boolean, confirmedOnly: Boolean): List<Diagnosis>

    /**
     * Finds the primary diagnoses for a given encounter.
     *
     * @param encounter the encounter whose diagnoses we are to get
     * @return the list of diagnoses in the given encounter
     * @deprecated since 2.5.0, use [getDiagnosesByEncounter]
     */
    @Deprecated("Use getDiagnosesByEncounter instead", ReplaceWith("getDiagnosesByEncounter(encounter, true, false)"))
    fun getPrimaryDiagnoses(encounter: Encounter): List<Diagnosis>

    /**
     * Gets unique diagnoses since date, sorted in reverse chronological order.
     *
     * @param patient the patient whose diagnosis we are to get
     * @param fromDate the date used to filter diagnosis which happened from this date and later
     * @return the list of diagnoses
     */
    fun getUniqueDiagnoses(patient: Patient, fromDate: Date?): List<Diagnosis>

    /**
     * Gets a diagnosis by id.
     *
     * @param diagnosisId id of the diagnosis to be returned
     * @return diagnosis matching the given id
     */
    @Authorized(PrivilegeConstants.GET_DIAGNOSES)
    fun getDiagnosis(diagnosisId: Int?): Diagnosis?

    /**
     * Revive a diagnosis (pull a Lazarus).
     *
     * @param diagnosis diagnosis to unvoid
     * @return the unvoided diagnosis
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.EDIT_DIAGNOSES)
    @Throws(APIException::class)
    fun unvoidDiagnosis(diagnosis: Diagnosis): Diagnosis

    /**
     * Completely remove a diagnosis from the database. This should typically not be called
     * because we don't want to ever lose data. The data really should be voided and then it
     * is not seen in interface any longer (see [voidDiagnosis] for that one). If other things link to
     * this diagnosis, an error will be thrown.
     *
     * @param diagnosis diagnosis to remove from the database
     * @throws APIException
     * @see purgeDiagnosis
     */
    @Authorized(PrivilegeConstants.DELETE_DIAGNOSES)
    @Throws(APIException::class)
    fun purgeDiagnosis(diagnosis: Diagnosis)

    /**
     * Fetches all diagnosis attribute types including retired ones.
     *
     * @return all [DiagnosisAttributeType]s
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.GET_DIAGNOSES_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun getAllDiagnosisAttributeTypes(): List<DiagnosisAttributeType>

    /**
     * Fetches a given diagnosis attribute type using the provided id.
     *
     * @param id the id of the diagnosis attribute type to fetch
     * @return the [DiagnosisAttributeType] with the given id
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.GET_DIAGNOSES_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun getDiagnosisAttributeTypeById(id: Int?): DiagnosisAttributeType?

    /**
     * Fetches a given diagnosis attribute type using the provided uuid.
     *
     * @param uuid the uuid of the diagnosis attribute type to fetch
     * @return the [DiagnosisAttributeType] with the given uuid
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.GET_DIAGNOSES_ATTRIBUTE_TYPES)
    @Throws(APIException::class)
    fun getDiagnosisAttributeTypeByUuid(uuid: String): DiagnosisAttributeType?

    /**
     * Creates or updates the given diagnosis attribute type in the database.
     *
     * @param diagnosisAttributeType the diagnosis attribute type to save or update
     * @return the DiagnosisAttributeType created/saved
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.EDIT_DIAGNOSES)
    @Throws(APIException::class)
    fun saveDiagnosisAttributeType(diagnosisAttributeType: DiagnosisAttributeType): DiagnosisAttributeType

    /**
     * Retires the given diagnosis attribute type in the database.
     *
     * @param diagnosisAttributeType the diagnosis attribute type to retire
     * @param reason the reason why the diagnosis attribute type is being retired
     * @return the diagnosisAttributeType retired
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.EDIT_DIAGNOSES)
    @Throws(APIException::class)
    fun retireDiagnosisAttributeType(diagnosisAttributeType: DiagnosisAttributeType, reason: String): DiagnosisAttributeType

    /**
     * Restores a diagnosis attribute type that was previously retired.
     *
     * @param diagnosisAttributeType the diagnosis attribute type to unretire
     * @return the DiagnosisAttributeType unretired
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.EDIT_DIAGNOSES)
    @Throws(APIException::class)
    fun unretireDiagnosisAttributeType(diagnosisAttributeType: DiagnosisAttributeType): DiagnosisAttributeType

    /**
     * Completely removes a diagnosis attribute type from the database.
     *
     * @param diagnosisAttributeType the diagnosis attribute type to purge
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.DELETE_DIAGNOSES)
    @Throws(APIException::class)
    fun purgeDiagnosisAttributeType(diagnosisAttributeType: DiagnosisAttributeType)

    /**
     * Fetches a given diagnosis attribute using the provided uuid.
     *
     * @param uuid the uuid of the diagnosis attribute to fetch
     * @return the [DiagnosisAttribute] with the given uuid
     * @since 2.5.0
     */
    @Authorized(PrivilegeConstants.GET_DIAGNOSES)
    @Throws(APIException::class)
    fun getDiagnosisAttributeByUuid(uuid: String): DiagnosisAttribute?
}
