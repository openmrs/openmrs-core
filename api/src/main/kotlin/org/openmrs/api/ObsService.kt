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
import org.openmrs.ConceptName
import org.openmrs.Encounter
import org.openmrs.Location
import org.openmrs.Obs
import org.openmrs.Person
import org.openmrs.Visit
import org.openmrs.annotation.Authorized
import org.openmrs.api.db.ObsDAO
import org.openmrs.obs.ComplexObsHandler
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE
import org.openmrs.util.PrivilegeConstants
import java.util.Date

/**
 * The ObsService deals with saving and getting Obs to/from the database.
 *
 * Usage:
 * ```
 * val obsService = Context.getObsService()
 * // get the obs for patient with internal identifier of 1235
 * val someObsList = obsService.getObservationsByPerson(Patient(1235))
 * ```
 *
 * There are also a number of convenience methods for extracting obs pertaining to certain Concepts,
 * people, or encounters.
 *
 * @see org.openmrs.Obs
 * @see org.openmrs.api.context.Context
 */
interface ObsService : OpenmrsService {

    /**
     * Set the given dao on this obs service. The dao will act as the conduit through
     * with all obs calls get to the database.
     *
     * @param dao specific ObsDAO to use for this service
     */
    fun setObsDAO(dao: ObsDAO)

    /**
     * Get an observation.
     *
     * @param obsId integer obsId of observation desired
     * @return matching Obs
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    @Throws(APIException::class)
    fun getObs(obsId: Int?): Obs?

    /**
     * Get Obs by its UUID.
     *
     * @param uuid the obs uuid
     * @return obs or null
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    @Throws(APIException::class)
    fun getObsByUuid(uuid: String): Obs?

    /**
     * Get Revision Obs for initial Obs.
     *
     * @param initialObs the initial obs
     * @return obs or null
     * @since 2.1
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    fun getRevisionObs(initialObs: Obs): Obs?

    /**
     * Save the given obs to the database. The behavior differs for first-time save, and edit.
     *
     * When you save a new observation to the database:
     * - the obs you pass in is saved to the database, and its obsId field is filled in
     * - the obs you pass in is returned
     * - the changeMessage parameter is ignored
     *
     * When you edit an existing observation:
     * - the values of the obs you pass to this method are written to the database as *new* obs
     * - the newly-created obs is returned (i.e. not the one you passed in)
     * - the obs you passed is marked as voided, with changeMessage as the void reason
     * - the newly-created obs points back to the voided one via its previousVersion field
     *
     * @param obs the Obs to save to the database
     * @param changeMessage String explaining why obs is being changed. If obs is a new obs,
     *        changeMessage is nullable, or if it is being updated, it would be required
     * @return Obs that was saved to the database
     * @throws APIException if saving fails
     */
    @Authorized(PrivilegeConstants.ADD_OBS, PrivilegeConstants.EDIT_OBS)
    @Throws(APIException::class)
    fun saveObs(obs: Obs, changeMessage: String?): Obs

    /**
     * Equivalent to deleting an observation.
     *
     * @param obs Obs to void
     * @param reason String reason it's being voided
     * @return the voided obs
     * @throws APIException if voiding fails
     */
    @Authorized(PrivilegeConstants.EDIT_OBS)
    @Throws(APIException::class)
    fun voidObs(obs: Obs, reason: String): Obs

    /**
     * Revive an observation (pull a Lazarus).
     *
     * @param obs Obs to unvoid
     * @return the unvoided obs
     * @throws APIException if unvoiding fails
     */
    @Authorized(PrivilegeConstants.EDIT_OBS)
    @Throws(APIException::class)
    fun unvoidObs(obs: Obs): Obs

    /**
     * Completely remove an observation from the database. This should typically not be called
     * because we don't want to ever lose data. The data really should be voided and then it
     * is not seen in interface any longer (see [voidObs] for that one). If other things link to
     * this obs, an error will be thrown.
     *
     * @param obs the obs to purge
     * @throws APIException if purging fails
     * @see purgeObs
     */
    @Authorized(PrivilegeConstants.DELETE_OBS)
    @Throws(APIException::class)
    fun purgeObs(obs: Obs)

    /**
     * Completely remove an observation from the database. This should typically not be called
     * because we don't want to ever lose data. The data really should be voided and then it
     * is not seen in interface any longer (see [voidObs] for that one).
     *
     * @param obs the observation to remove from the database
     * @param cascade true/false whether or not to cascade down to other things that link to this
     *        observation (like Orders and ObsGroups)
     * @throws APIException if purging fails
     * @see purgeObs
     */
    @Authorized(PrivilegeConstants.DELETE_OBS)
    @Throws(APIException::class)
    fun purgeObs(obs: Obs, cascade: Boolean)

    /**
     * Get all Observations for the given person, sorted by obsDatetime ascending. Does not return
     * voided observations.
     *
     * @param who the user to match on
     * @return a List of Obs containing all non-voided observations for the specified person
     * @see getObservations
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    fun getObservationsByPerson(who: Person): List<Obs>

    /**
     * This method fetches observations according to the criteria in the given arguments. All
     * arguments are optional and nullable. If more than one argument is non-null, the result is
     * equivalent to an "and"ing of the arguments.
     *
     * Note: If whom has elements, personType is ignored
     *
     * @param whom List of Person to restrict obs to (optional)
     * @param encounters List of Encounter to restrict obs to (optional)
     * @param questions List of Concept to restrict the obs to (optional)
     * @param answers List of Concept to restrict the valueCoded to (optional)
     * @param personTypes List of PERSON_TYPE objects to restrict this to. Only used if whom is an empty list (optional)
     * @param locations The Location objects to restrict to (optional)
     * @param sort list of column names to sort on (obsId, obsDatetime, etc) if null, defaults to obsDatetime (optional)
     * @param mostRecentN restrict the number of obs returned to this size (optional)
     * @param obsGroupId the Obs.getObsGroupId() to this integer (optional)
     * @param fromDate the earliest Obs date to get (optional)
     * @param toDate the latest Obs date to get (optional)
     * @param includeVoidedObs true/false whether to also include the voided obs (required)
     * @return list of Observations that match all of the criteria given in the arguments
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    @Throws(APIException::class)
    fun getObservations(
        whom: @JvmSuppressWildcards List<Person>?,
        encounters: @JvmSuppressWildcards List<Encounter>?,
        questions: @JvmSuppressWildcards List<Concept>?,
        answers: @JvmSuppressWildcards List<Concept>?,
        personTypes: @JvmSuppressWildcards List<PERSON_TYPE>?,
        locations: @JvmSuppressWildcards List<Location>?,
        sort: @JvmSuppressWildcards List<String>?,
        mostRecentN: Int?,
        obsGroupId: Int?,
        fromDate: Date?,
        toDate: Date?,
        includeVoidedObs: Boolean
    ): List<Obs>

    /**
     * This method fetches observations according to the criteria in the given arguments with accession number.
     *
     * @param whom List of Person to restrict obs to (optional)
     * @param encounters List of Encounter to restrict obs to (optional)
     * @param questions List of Concept to restrict the obs to (optional)
     * @param answers List of Concept to restrict the valueCoded to (optional)
     * @param personTypes List of PERSON_TYPE objects to restrict this to. Only used if whom is an empty list (optional)
     * @param locations The Location objects to restrict to (optional)
     * @param sort list of column names to sort on (obsId, obsDatetime, etc) if null, defaults to obsDatetime (optional)
     * @param mostRecentN restrict the number of obs returned to this size (optional)
     * @param obsGroupId the Obs.getObsGroupId() to this integer (optional)
     * @param fromDate the earliest Obs date to get (optional)
     * @param toDate the latest Obs date to get (optional)
     * @param includeVoidedObs true/false whether to also include the voided obs (required)
     * @param accessionNumber accession number (optional)
     * @return list of Observations that match all of the criteria given in the arguments
     * @since 1.12
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    @Throws(APIException::class)
    fun getObservations(
        whom: @JvmSuppressWildcards List<Person>?,
        encounters: @JvmSuppressWildcards List<Encounter>?,
        questions: @JvmSuppressWildcards List<Concept>?,
        answers: @JvmSuppressWildcards List<Concept>?,
        personTypes: @JvmSuppressWildcards List<PERSON_TYPE>?,
        locations: @JvmSuppressWildcards List<Location>?,
        sort: @JvmSuppressWildcards List<String>?,
        mostRecentN: Int?,
        obsGroupId: Int?,
        fromDate: Date?,
        toDate: Date?,
        includeVoidedObs: Boolean,
        accessionNumber: String?
    ): List<Obs>

    /**
     * This method fetches the count of observations according to the criteria in the given arguments.
     *
     * @param whom List of Person to restrict obs to (optional)
     * @param encounters List of Encounter to restrict obs to (optional)
     * @param questions List of Concept to restrict the obs to (optional)
     * @param answers List of Concept to restrict the valueCoded to (optional)
     * @param personTypes List of PERSON_TYPE objects to restrict this to. Only used if whom is an empty list (optional)
     * @param locations The Location objects to restrict to (optional)
     * @param obsGroupId the Obs.getObsGroupId() to this integer (optional)
     * @param fromDate the earliest Obs date to get (optional)
     * @param toDate the latest Obs date to get (optional)
     * @param includeVoidedObs true/false whether to also include the voided obs (required)
     * @return count of Observations that match all of the criteria given in the arguments
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    @Throws(APIException::class)
    fun getObservationCount(
        whom: @JvmSuppressWildcards List<Person>?,
        encounters: @JvmSuppressWildcards List<Encounter>?,
        questions: @JvmSuppressWildcards List<Concept>?,
        answers: @JvmSuppressWildcards List<Concept>?,
        personTypes: @JvmSuppressWildcards List<PERSON_TYPE>?,
        locations: @JvmSuppressWildcards List<Location>?,
        obsGroupId: Int?,
        fromDate: Date?,
        toDate: Date?,
        includeVoidedObs: Boolean
    ): Integer

    /**
     * This method fetches the count of observations with accession number.
     *
     * @param whom List of Person to restrict obs to (optional)
     * @param encounters List of Encounter to restrict obs to (optional)
     * @param questions List of Concept to restrict the obs to (optional)
     * @param answers List of Concept to restrict the valueCoded to (optional)
     * @param personTypes List of PERSON_TYPE objects to restrict this to. Only used if whom is an empty list (optional)
     * @param locations The Location objects to restrict to (optional)
     * @param obsGroupId the Obs.getObsGroupId() to this integer (optional)
     * @param fromDate the earliest Obs date to get (optional)
     * @param toDate the latest Obs date to get (optional)
     * @param includeVoidedObs true/false whether to also include the voided obs (required)
     * @param accessionNumber accession number (optional)
     * @return count of Observations that match all of the criteria given in the arguments
     * @since 1.12
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    @Throws(APIException::class)
    fun getObservationCount(
        whom: @JvmSuppressWildcards List<Person>?,
        encounters: @JvmSuppressWildcards List<Encounter>?,
        questions: @JvmSuppressWildcards List<Concept>?,
        answers: @JvmSuppressWildcards List<Concept>?,
        personTypes: @JvmSuppressWildcards List<PERSON_TYPE>?,
        locations: @JvmSuppressWildcards List<Location>?,
        obsGroupId: Int?,
        fromDate: Date?,
        toDate: Date?,
        includeVoidedObs: Boolean,
        accessionNumber: String?
    ): Integer

    /**
     * This method searches the obs table based on the given searchString.
     *
     * @param searchString The string to search on
     * @return observations matching the given string
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    @Throws(APIException::class)
    fun getObservations(searchString: String): List<Obs>

    /**
     * Get all nonvoided observations for the given patient with the given concept as the question
     * concept (conceptId).
     *
     * @param who person to match on
     * @param question conceptId to match on
     * @return list of all nonvoided observations matching these criteria
     * @throws APIException if retrieval fails
     * @see getObservations
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    @Throws(APIException::class)
    fun getObservationsByPersonAndConcept(who: Person?, question: Concept?): List<Obs>

    /**
     * Get a complex observation. If obs.isComplex() is true, then returns an Obs with its
     * ComplexData. Otherwise returns a simple Obs.
     *
     * @param obsId the obs id
     * @param view the view
     * @return Obs with a ComplexData
     * @since 1.5
     * @deprecated as of 2.1.0, use [getObs]
     */
    @Deprecated("Use getObs instead", ReplaceWith("getObs(obsId)"))
    @Authorized(PrivilegeConstants.GET_OBS)
    @Throws(APIException::class)
    fun getComplexObs(obsId: Int?, view: String?): Obs?

    /**
     * Get the ComplexObsHandler that has been registered with the given key.
     *
     * @param key that has been registered with a handler class
     * @return Object representing the handler for the given key
     * @since 1.5
     * @throws APIException if retrieval fails
     */
    @Throws(APIException::class)
    fun getHandler(key: String): ComplexObsHandler?

    /**
     * Get the ComplexObsHandler associated with a complex observation.
     * Returns the ComplexObsHandler.
     * Returns null if the Obs.isComplexObs() is false or there is an error
     * instantiating the handler class.
     *
     * @param obs A complex Obs.
     * @return ComplexObsHandler for the complex Obs. or null on error.
     * @since 1.12
     * @throws APIException if retrieval fails
     */
    @Throws(APIException::class)
    fun getHandler(obs: Obs): ComplexObsHandler?

    /**
     * Add the given map to this service's handlers. This method registers each
     * ComplexObsHandler to this service. If the given String key exists, that handler is
     * overwritten with the given handler. For most situations, this map is set via spring, see the
     * applicationContext-service.xml file to add more handlers.
     *
     * @param handlers Map of class to handler object
     * @throws APIException if setting fails
     * @since 1.5
     */
    @Throws(APIException::class)
    fun setHandlers(handlers: @JvmSuppressWildcards Map<String, ComplexObsHandler>)

    /**
     * Gets the handlers map registered.
     *
     * @return map of keys to handlers
     * @since 1.5
     * @throws APIException if retrieval fails
     */
    @Throws(APIException::class)
    fun getHandlers(): Map<String, ComplexObsHandler>

    /**
     * Registers the given handler with the given key. If the given String key exists, that handler
     * is overwritten with the given handler.
     *
     * @param key the key name to use for this handler
     * @param handler the class to register with this key
     * @throws APIException if registration fails
     * @since 1.5
     */
    @Throws(APIException::class)
    fun registerHandler(key: String, handler: ComplexObsHandler)

    /**
     * Convenience method for [registerHandler].
     *
     * @param key the key name to use for this handler
     * @param handlerClass the class to register with this key
     * @throws APIException if registration fails
     * @since 1.5
     */
    @Throws(APIException::class)
    fun registerHandler(key: String, handlerClass: String)

    /**
     * Remove the handler associated with the key from list of available handlers.
     *
     * @param key the key of the handler to unregister
     * @since 1.5
     * @throws APIException if removal fails
     */
    @Throws(APIException::class)
    fun removeHandler(key: String)

    /**
     * Gets the number of observations(including voided ones) that are using the specified
     * conceptNames as valueCodedName answers.
     *
     * @param conceptNames the conceptNames to be searched against
     * @param includeVoided whether voided observation should be included
     * @return The number of observations using the specified conceptNames as valueCodedNames
     * @since Version 1.7
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    fun getObservationCount(conceptNames: @JvmSuppressWildcards List<ConceptName>?, includeVoided: Boolean): Integer

    /**
     * This method fetches observations with visits.
     *
     * @param whom List of Person to restrict obs to (optional)
     * @param encounters List of Encounter to restrict obs to (optional)
     * @param questions List of Concept to restrict the obs to (optional)
     * @param answers List of Concept to restrict the valueCoded to (optional)
     * @param personTypes List of PERSON_TYPE objects to restrict this to. Only used if whom is an empty list (optional)
     * @param locations The Location objects to restrict to (optional)
     * @param sort list of column names to sort on (obsId, obsDatetime, etc) if null, defaults to obsDatetime (optional)
     * @param visits List of Visit to restrict obs to (optional)
     * @param mostRecentN restrict the number of obs returned to this size (optional)
     * @param obsGroupId the Obs.getObsGroupId() to this integer (optional)
     * @param fromDate the earliest Obs date to get (optional)
     * @param toDate the latest Obs date to get (optional)
     * @param includeVoidedObs true/false whether to also include the voided obs (required)
     * @param accessionNumber accession number (optional)
     * @return list of Observations that match all of the criteria given in the arguments
     * @since 2.7.0
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    @Throws(APIException::class)
    fun getObservations(
        whom: @JvmSuppressWildcards List<Person>?,
        encounters: @JvmSuppressWildcards List<Encounter>?,
        questions: @JvmSuppressWildcards List<Concept>?,
        answers: @JvmSuppressWildcards List<Concept>?,
        personTypes: @JvmSuppressWildcards List<PERSON_TYPE>?,
        locations: @JvmSuppressWildcards List<Location>?,
        sort: @JvmSuppressWildcards List<String>?,
        visits: @JvmSuppressWildcards List<Visit>?,
        mostRecentN: Int?,
        obsGroupId: Int?,
        fromDate: Date?,
        toDate: Date?,
        includeVoidedObs: Boolean,
        accessionNumber: String?
    ): List<Obs>

    /**
     * This method fetches the count of observations with visits.
     *
     * @param whom List of Person to restrict obs to (optional)
     * @param encounters List of Encounter to restrict obs to (optional)
     * @param questions List of Concept to restrict the obs to (optional)
     * @param answers List of Concept to restrict the valueCoded to (optional)
     * @param personTypes List of PERSON_TYPE objects to restrict this to. Only used if whom is an empty list (optional)
     * @param locations The Location objects to restrict to (optional)
     * @param visits List of Visit to restrict obs to (optional)
     * @param obsGroupId the Obs.getObsGroupId() to this integer (optional)
     * @param fromDate the earliest Obs date to get (optional)
     * @param toDate the latest Obs date to get (optional)
     * @param includeVoidedObs true/false whether to also include the voided obs (required)
     * @param accessionNumber accession number (optional)
     * @return count of Observations that match all of the criteria given in the arguments
     * @since 2.7.0
     * @throws APIException if retrieval fails
     */
    @Authorized(PrivilegeConstants.GET_OBS)
    @Throws(APIException::class)
    fun getObservationCount(
        whom: @JvmSuppressWildcards List<Person>?,
        encounters: @JvmSuppressWildcards List<Encounter>?,
        questions: @JvmSuppressWildcards List<Concept>?,
        answers: @JvmSuppressWildcards List<Concept>?,
        personTypes: @JvmSuppressWildcards List<PERSON_TYPE>?,
        locations: @JvmSuppressWildcards List<Location>?,
        visits: @JvmSuppressWildcards List<Visit>?,
        obsGroupId: Int?,
        fromDate: Date?,
        toDate: Date?,
        includeVoidedObs: Boolean,
        accessionNumber: String?
    ): Integer
}
