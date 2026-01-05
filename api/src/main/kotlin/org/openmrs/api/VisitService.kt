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

import org.openmrs.Concept
import org.openmrs.Location
import org.openmrs.Patient
import org.openmrs.Visit
import org.openmrs.VisitAttribute
import org.openmrs.VisitAttributeType
import org.openmrs.VisitType
import org.openmrs.annotation.Authorized
import org.openmrs.parameter.VisitSearchCriteria
import org.openmrs.util.PrivilegeConstants
import java.util.Date

/**
 * This service contains methods relating to visits.
 *
 * @since 1.9
 */
interface VisitService : OpenmrsService {

    /**
     * Gets all visit types.
     *
     * @return a list of visit type objects
     */
    @Authorized(PrivilegeConstants.GET_VISIT_TYPES)
    fun getAllVisitTypes(): List<VisitType>

    /**
     * Get all visit types based on includeRetired flag.
     *
     * @param includeRetired whether to include retired visit types
     * @return list of all visit types
     * @since 1.9
     */
    @Authorized(PrivilegeConstants.MANAGE_VISIT_TYPES)
    fun getAllVisitTypes(includeRetired: Boolean): List<VisitType>

    /**
     * Gets a visit type by its visit type id.
     *
     * @param visitTypeId the visit type id
     * @return the visit type object found with the given id, else null
     */
    @Authorized(PrivilegeConstants.GET_VISIT_TYPES)
    fun getVisitType(visitTypeId: Int?): VisitType?

    /**
     * Gets a visit type by its UUID.
     *
     * @param uuid the visit type UUID
     * @return the visit type object found with the given uuid, else null
     */
    @Authorized(PrivilegeConstants.GET_VISIT_TYPES)
    fun getVisitTypeByUuid(uuid: String): VisitType?

    /**
     * Gets all visit types whose names are similar to or contain the given search phrase.
     *
     * @param fuzzySearchPhrase the search phrase to use
     * @return a list of all visit types with names similar to or containing the given phrase
     */
    @Authorized(PrivilegeConstants.GET_VISIT_TYPES)
    fun getVisitTypes(fuzzySearchPhrase: String): List<VisitType>

    /**
     * Creates or updates the given visit type in the database.
     *
     * @param visitType the visit type to create or update
     * @return the created or updated visit type
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.MANAGE_VISIT_TYPES)
    @Throws(APIException::class)
    fun saveVisitType(visitType: VisitType): VisitType

    /**
     * Retires a given visit type.
     *
     * @param visitType the visit type to retire
     * @param reason the reason why the visit type is retired
     * @return the visit type that has been retired
     */
    @Authorized(PrivilegeConstants.MANAGE_VISIT_TYPES)
    fun retireVisitType(visitType: VisitType, reason: String): VisitType

    /**
     * Unretires a visit type.
     *
     * @param visitType the visit type to unretire
     * @return the unretired visit type
     */
    @Authorized(PrivilegeConstants.MANAGE_VISIT_TYPES)
    fun unretireVisitType(visitType: VisitType): VisitType

    /**
     * Completely removes a visit type from the database. This is not reversible.
     *
     * @param visitType the visit type to delete from the database
     */
    @Authorized(PrivilegeConstants.MANAGE_VISIT_TYPES)
    fun purgeVisitType(visitType: VisitType)

    /**
     * Gets all unvoided visits in the database.
     *
     * @return a list of visit objects
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.GET_VISITS)
    @Throws(APIException::class)
    fun getAllVisits(): List<Visit>

    /**
     * Gets a visit by its visit id.
     *
     * @param visitId the visit id
     * @return the visit object found with the given id, else null
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.GET_VISITS)
    @Throws(APIException::class)
    fun getVisit(visitId: Int?): Visit?

    /**
     * Gets a visit by its UUID.
     *
     * @param uuid the visit UUID
     * @return the visit object found with the given uuid, else null
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.GET_VISITS)
    @Throws(APIException::class)
    fun getVisitByUuid(uuid: String): Visit?

    /**
     * Creates or updates the given visit in the database.
     *
     * @param visit the visit to create or update
     * @return the created or updated visit
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.ADD_VISITS, PrivilegeConstants.EDIT_VISITS)
    @Throws(APIException::class)
    fun saveVisit(visit: Visit): Visit

    /**
     * Sets the stopDate of a given visit.
     *
     * @param visit the visit whose stopDate is to be set
     * @param stopDate the date and time the visit is ending. if null, current date is used
     * @return the visit that was ended
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.EDIT_VISITS)
    @Throws(APIException::class)
    fun endVisit(visit: Visit, stopDate: Date?): Visit

    /**
     * Voids the given visit.
     *
     * @param visit the visit to void
     * @param reason the reason why the visit is voided
     * @return the visit that has been voided
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.DELETE_VISITS)
    @Throws(APIException::class)
    fun voidVisit(visit: Visit, reason: String): Visit

    /**
     * Unvoids the given visit.
     *
     * @param visit the visit to unvoid
     * @return the unvoided visit
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.DELETE_VISITS)
    @Throws(APIException::class)
    fun unvoidVisit(visit: Visit): Visit

    /**
     * Completely erases a visit from the database. This is not reversible.
     *
     * @param visit the visit to delete from the database
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.PURGE_VISITS)
    @Throws(APIException::class)
    fun purgeVisit(visit: Visit)

    /**
     * Gets the visits matching the specified arguments.
     *
     * @param visitTypes a list of visit types to match against
     * @param patients a list of patients to match against
     * @param locations a list of locations to match against
     * @param indications a list of indication concepts to match against
     * @param minStartDatetime the minimum visit start date to match against
     * @param maxStartDatetime the maximum visit start date to match against
     * @param minEndDatetime the minimum visit end date to match against
     * @param maxEndDatetime the maximum visit end date to match against
     * @param attributeValues attribute values to match against
     * @param includeInactive if false, the min/maxEndDatetime parameters are ignored and only open visits are returned
     * @param includeVoided specifies if voided visits should also be returned
     * @return a list of visits
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.GET_VISITS)
    @Throws(APIException::class)
    fun getVisits(
        visitTypes: @JvmSuppressWildcards Collection<VisitType>?,
        patients: @JvmSuppressWildcards Collection<Patient>?,
        locations: @JvmSuppressWildcards Collection<Location>?,
        indications: @JvmSuppressWildcards Collection<Concept>?,
        minStartDatetime: Date?,
        maxStartDatetime: Date?,
        minEndDatetime: Date?,
        maxEndDatetime: Date?,
        attributeValues: @JvmSuppressWildcards Map<VisitAttributeType, Any>?,
        includeInactive: Boolean,
        includeVoided: Boolean
    ): List<Visit>

    /**
     * Gets the visits matching the specified search criteria.
     *
     * @param visitSearchCriteria the search criteria
     * @return a list of visits
     * @throws APIException
     * @since 2.6.8
     * @since 2.7.0
     */
    @Authorized(PrivilegeConstants.GET_VISITS)
    @Throws(APIException::class)
    fun getVisits(visitSearchCriteria: VisitSearchCriteria): List<Visit>

    /**
     * Gets all unvoided visits for the specified patient.
     *
     * @param patient the patient whose visits to get
     * @return a list of visits
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.GET_VISITS)
    @Throws(APIException::class)
    fun getVisitsByPatient(patient: Patient): List<Visit>

    /**
     * Convenience method that delegates to getVisitsByPatient(patient, false, false).
     *
     * @param patient the patient whose visits to get
     * @return a list of visits
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.GET_VISITS)
    @Throws(APIException::class)
    fun getActiveVisitsByPatient(patient: Patient): List<Visit>

    /**
     * Gets all visits for the specified patient.
     *
     * @param patient the patient whose visits to get
     * @param includeInactive whether to include inactive visits
     * @param includeVoided whether to include voided visits
     * @return a list of visits
     * @throws APIException
     */
    @Authorized(PrivilegeConstants.GET_VISITS)
    @Throws(APIException::class)
    fun getVisitsByPatient(patient: Patient, includeInactive: Boolean, includeVoided: Boolean): List<Visit>

    /**
     * Gets all visit attribute types.
     *
     * @return all visit attribute types including retired ones
     */
    @Authorized(PrivilegeConstants.GET_VISIT_ATTRIBUTE_TYPES)
    fun getAllVisitAttributeTypes(): List<VisitAttributeType>

    /**
     * Gets a visit attribute type by id.
     *
     * @param id the visit attribute type id
     * @return the visit attribute type with the given internal id
     */
    @Authorized(PrivilegeConstants.GET_VISIT_ATTRIBUTE_TYPES)
    fun getVisitAttributeType(id: Int?): VisitAttributeType?

    /**
     * Gets a visit attribute type by uuid.
     *
     * @param uuid the visit attribute type uuid
     * @return the visit attribute type with the given uuid
     */
    @Authorized(PrivilegeConstants.GET_VISIT_ATTRIBUTE_TYPES)
    fun getVisitAttributeTypeByUuid(uuid: String): VisitAttributeType?

    /**
     * Creates or updates the given visit attribute type in the database.
     *
     * @param visitAttributeType the visit attribute type to save
     * @return the VisitAttributeType created/saved
     */
    @Authorized(PrivilegeConstants.MANAGE_VISIT_ATTRIBUTE_TYPES)
    fun saveVisitAttributeType(visitAttributeType: VisitAttributeType): VisitAttributeType

    /**
     * Retires the given visit attribute type in the database.
     *
     * @param visitAttributeType the visit attribute type to retire
     * @param reason the reason for retiring
     * @return the visitAttribute retired
     */
    @Authorized(PrivilegeConstants.MANAGE_VISIT_ATTRIBUTE_TYPES)
    fun retireVisitAttributeType(visitAttributeType: VisitAttributeType, reason: String): VisitAttributeType

    /**
     * Restores a visit attribute type that was previously retired in the database.
     *
     * @param visitAttributeType the visit attribute type to unretire
     * @return the VisitAttributeType unretired
     */
    @Authorized(PrivilegeConstants.MANAGE_VISIT_ATTRIBUTE_TYPES)
    fun unretireVisitAttributeType(visitAttributeType: VisitAttributeType): VisitAttributeType

    /**
     * Completely removes a visit attribute type from the database.
     *
     * @param visitAttributeType the visit attribute type to purge
     */
    @Authorized(PrivilegeConstants.PURGE_VISIT_ATTRIBUTE_TYPES)
    fun purgeVisitAttributeType(visitAttributeType: VisitAttributeType)

    /**
     * Gets a visit attribute by uuid.
     *
     * @param uuid the visit attribute uuid
     * @return the visit attribute with the given uuid
     */
    @Authorized(PrivilegeConstants.GET_VISITS)
    fun getVisitAttributeByUuid(uuid: String): VisitAttribute?

    /**
     * Stops all active visits started before or on the specified date which match any of the visit
     * types specified by the GP_VISIT_TYPES_TO_AUTO_CLOSE global property.
     * If startDatetime is null, the default will be end of the current day.
     *
     * @param maximumStartDate Visits started on or before this date time value will get stopped
     */
    @Authorized(PrivilegeConstants.EDIT_VISITS)
    fun stopVisits(maximumStartDate: Date?)
}
