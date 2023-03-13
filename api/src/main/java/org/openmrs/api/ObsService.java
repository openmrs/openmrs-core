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
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.ObsDAO;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.openmrs.util.PrivilegeConstants;

/**
 * The ObsService deals with saving and getting Obs to/from the database Usage:
 * 
 * <pre>
 *  ObsService obsService = Context.getObsService();
 * 
 *  // get the obs for patient with internal identifier of 1235
 *  List&lt;Obs&gt; someObsList = obsService.getObservationsByPerson(new Patient(1235));
 * </pre>
 * 
 * There are also a number of convenience methods for extracting obs pertaining to certain Concepts,
 * people, or encounters
 * 
 * @see org.openmrs.Obs
 * @see org.openmrs.api.context.Context
 */
public interface ObsService extends OpenmrsService {
	
	/**
	 * Set the given <code>dao</code> on this obs service. The dao will act as the conduit through
	 * with all obs calls get to the database
	 * 
	 * @param dao specific ObsDAO to use for this service
	 */
	public void setObsDAO(ObsDAO dao);
	
	/**
	 * Get an observation
	 * 
	 * @param obsId integer obsId of observation desired
	 * @return matching Obs
	 * @throws APIException
	 * <strong>Should</strong> get obs matching given obsId
	 */
	@Authorized(PrivilegeConstants.GET_OBS)
	public Obs getObs(Integer obsId) throws APIException;
	
	/**
	 * Get Obs by its UUID
	 * 
	 * @param uuid
	 * @return obs or null
	 * <strong>Should</strong> find object given valid uuid
	 * <strong>Should</strong> return null if no object found with given uuid
	 */
	@Authorized(PrivilegeConstants.GET_OBS)
	public Obs getObsByUuid(String uuid) throws APIException;

	/**
	 * Get Revision Obs for initial Obs
	 *
	 * @param initialObs
	 * @return obs or null
	 * @since 2.1
	 * <strong>Should</strong> find revision obs for given valid obs
	 * <strong>Should</strong> return null if no revision obs found for given obs
	 */
	@Authorized(PrivilegeConstants.GET_OBS)
	public Obs getRevisionObs(Obs initialObs);

	/**
	 * <p>Save the given obs to the database. The behavior differs for first-time save, and edit.</p>
	 *
	 * <p>When you save a new observation to the database:
	 * <ul>
	 *     <li>the obs you pass in is saved to the database, and its obsId field is filled in</li>
	 *     <li>the obs you pass in is returned</li>
	 *     <li>the changeMesssage parameter is ignored</li>
	 * </ul>
	 * </p>
	 *
	 * <p>When you edit an existing observation:
	 * <ul>
	 *     <li>the values of the obs you pass to this method are written to the database as <em>new</em> obs</li>
	 *     <li>the newly-created obs is returned (i.e. not the one you passed in)</li>
	 *     <li>the obs you passed is marked as voided, with changeMessage as the void reason</li>
	 *     <li>the newly-created obs points back to the voided one via its previousVersion field</li>
	 * </ul>
	 * </p>
	 *
	 * @param obs the Obs to save to the database
	 * @param changeMessage String explaining why <code>obs</code> is being changed. If
	 *            <code>obs</code> is a new obs, changeMessage is nullable, or if it is being
	 *            updated, it would be required
	 * @return Obs that was saved to the database
	 * @throws APIException
	 * <strong>Should</strong> create new file from complex data for new obs
	 * <strong>Should</strong> not overwrite file when updating a complex obs
	 * <strong>Should</strong> void the given obs in the database
	 * <strong>Should</strong> create very basic obs and add new obsId
	 * <strong>Should</strong> allow setting properties on obs
	 * <strong>Should</strong> return a different object when updating an obs
	 * <strong>Should</strong> set creator and dateCreated on new obs
	 * <strong>Should</strong> cascade save to child obs groups
	 * <strong>Should</strong> cascade update to new child obs groups
	 * <strong>Should</strong> link original and updated obs
	 * <strong>Should</strong> set void reason message to changeMessage
     * <strong>Should</strong> not void an Obs with no changes
	 */
	@Authorized( { PrivilegeConstants.ADD_OBS, PrivilegeConstants.EDIT_OBS })
	public Obs saveObs(Obs obs, String changeMessage) throws APIException;
	
	/**
	 * Equivalent to deleting an observation
	 * 
	 * @param obs Obs to void
	 * @param reason String reason it's being voided
	 * @throws APIException
	 * <strong>Should</strong> set voided bit on given obs
	 * <strong>Should</strong> fail if reason parameter is empty
	 */
	@Authorized(PrivilegeConstants.EDIT_OBS)
	public Obs voidObs(Obs obs, String reason) throws APIException;
	
	/**
	 * Revive an observation (pull a Lazarus)
	 * 
	 * @param obs Obs to unvoid
	 * @throws APIException
	 * <strong>Should</strong> unset voided bit on given obs
	 * <strong>Should</strong> cascade unvoid to child grouped obs
	 */
	@Authorized(PrivilegeConstants.EDIT_OBS)
	public Obs unvoidObs(Obs obs) throws APIException;
	
	/**
	 * Completely remove an observation from the database. This should typically not be called
	 * because we don't want to ever lose data. The data really <i>should</i> be voided and then it
	 * is not seen in interface any longer (see #voidObs(Obs) for that one) If other things link to
	 * this obs, an error will be thrown.
	 * 
	 * @param obs
	 * @throws APIException
	 * @see #purgeObs(Obs, boolean)
	 * <strong>Should</strong> delete the given obs from the database
	 */
	@Authorized(PrivilegeConstants.DELETE_OBS)
	public void purgeObs(Obs obs) throws APIException;
	
	/**
	 * Completely remove an observation from the database. This should typically not be called
	 * because we don't want to ever lose data. The data really <i>should</i> be voided and then it
	 * is not seen in interface any longer (see #voidObs(Obs) for that one)
	 * 
	 * @param obs the observation to remove from the database
	 * @param cascade true/false whether or not to cascade down to other things that link to this
	 *            observation (like Orders and ObsGroups)
	 * @throws APIException
	 * @see #purgeObs(Obs, boolean)
	 *
	 * <strong>Should</strong> throw APIException if given true cascade
	 * <strong>Should</strong> delete any obsGroupMembers before deleting the obs
	 * <strong>Should</strong> not delete referenced orders when purging obs
	 */
	@Authorized(PrivilegeConstants.DELETE_OBS)
	public void purgeObs(Obs obs, boolean cascade) throws APIException;

	/**
	 * Get all Observations for the given person, sorted by obsDatetime ascending. Does not return
	 * voided observations.
	 * 
	 * @param who the user to match on
	 * @return a List&lt;Obs&gt; object containing all non-voided observations for the specified person
	 * @see #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date,
	 *      boolean)
	 * <strong>Should</strong> get all observations assigned to given person
	 */
	@Authorized(PrivilegeConstants.GET_OBS)
	public List<Obs> getObservationsByPerson(Person who);
	
	/**
	 * This method fetches observations according to the criteria in the given arguments. All
	 * arguments are optional and nullable. If more than one argument is non-null, the result is
	 * equivalent to an "and"ing of the arguments. (e.g. if both a <code>location</code> and a
	 * <code>fromDate</code> are given, only Obs that are <u>both</u> at that Location and after the
	 * fromDate are returned). <br>
	 * <br>
	 * Note: If <code>whom</code> has elements, <code>personType</code> is ignored <br>
	 * <br>
	 * Note: to get all observations on a certain date, use:<br>
	 * Date fromDate = "2009-08-15";<br>
	 * Date toDate = OpenmrsUtil.lastSecondOfDate(fromDate); List&lt;Obs&gt; obs = getObservations(....,
	 * fromDate, toDate, ...);
	 * 
	 * @param whom List&lt;Person&gt; to restrict obs to (optional)
	 * @param encounters List&lt;Encounter&gt; to restrict obs to (optional)
	 * @param questions List&lt;Concept&gt; to restrict the obs to (optional)
	 * @param answers List&lt;Concept&gt; to restrict the valueCoded to (optional)
	 * @param personTypes List&lt;PERSON_TYPE&gt; objects to restrict this to. Only used if
	 *            <code>whom</code> is an empty list (optional)
	 * @param locations The org.openmrs.Location objects to restrict to (optional)
	 * @param sort list of column names to sort on (obsId, obsDatetime, etc) if null, defaults to
	 *            obsDatetime (optional)
	 * @param mostRecentN restrict the number of obs returned to this size (optional)
	 * @param obsGroupId the Obs.getObsGroupId() to this integer (optional)
	 * @param fromDate the earliest Obs date to get (optional)
	 * @param toDate the latest Obs date to get (optional)
	 * @param includeVoidedObs true/false whether to also include the voided obs (required)
	 * @return list of Observations that match all of the criteria given in the arguments
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_OBS)
	public List<Obs> getObservations(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, List<String> sort,
	        Integer mostRecentN, Integer obsGroupId, Date fromDate, Date toDate, boolean includeVoidedObs)
	        throws APIException;
	
	/**
	 * @see org.openmrs.api.ObsService#getObservations(java.util.List, java.util.List,
	 *      java.util.List, java.util.List, java.util.List, java.util.List, java.util.List,
	 *      java.lang.Integer, java.lang.Integer, java.util.Date, java.util.Date, boolean)
	 *
	 * This method works exactly the same; it only adds accession number search criteria.
	 * It effectively surpasses the above method; the old one is however kept for backward
	 * compatibility reasons.
	 *
	 * @param whom List&lt;Person&gt; to restrict obs to (optional)
	 * @param encounters List&lt;Encounter&gt; to restrict obs to (optional)
	 * @param questions List&lt;Concept&gt; to restrict the obs to (optional)
	 * @param answers List&lt;Concept&gt; to restrict the valueCoded to (optional)
	 * @param personTypes List&lt;PERSON_TYPE&gt; objects to restrict this to. Only used if
	 *            <code>whom</code> is an empty list (optional)
	 * @param locations The org.openmrs.Location objects to restrict to (optional)
	 * @param sort list of column names to sort on (obsId, obsDatetime, etc) if null, defaults to
	 *            obsDatetime (optional)
	 * @param mostRecentN restrict the number of obs returned to this size (optional)
	 * @param obsGroupId the Obs.getObsGroupId() to this integer (optional)
	 * @param fromDate the earliest Obs date to get (optional)
	 * @param toDate the latest Obs date to get (optional)
	 * @param includeVoidedObs true/false whether to also include the voided obs (required)
	 * @param accessionNumber accession number (optional)
	 * @return list of Observations that match all of the criteria given in the arguments
	 * @since 1.12
	 * @throws APIException
	 * <strong>Should</strong> compare dates using lte and gte
	 * <strong>Should</strong> get all obs assigned to given encounters
	 * <strong>Should</strong> get all obs with question concept in given questions parameter
	 * <strong>Should</strong> get all obs with answer concept in given answers parameter
	 * <strong>Should</strong> return all obs whose person is a person only
	 * <strong>Should</strong> return obs whose person is a patient only
	 * <strong>Should</strong> return obs whose person is a user only
	 * <strong>Should</strong> return obs with location in given locations parameter
	 * <strong>Should</strong> sort returned obs by obsDatetime if sort is empty
	 * <strong>Should</strong> sort returned obs by conceptId if sort is concept
	 * <strong>Should</strong> limit number of obs returned to mostReturnN parameter
	 * <strong>Should</strong> return obs whose groupId is given obsGroupId
	 * <strong>Should</strong> not include voided obs
	 * <strong>Should</strong> include voided obs if includeVoidedObs is true
	 * <strong>Should</strong> only return observations with matching accession number
	 */
	@Authorized(PrivilegeConstants.GET_OBS)
	public List<Obs> getObservations(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, List<String> sort,
	        Integer mostRecentN, Integer obsGroupId, Date fromDate, Date toDate, boolean includeVoidedObs,
	        String accessionNumber) throws APIException;
	
	/**
	 * This method fetches the count of observations according to the criteria in the given
	 * arguments. All arguments are optional and nullable. If more than one argument is non-null,
	 * the result is equivalent to an "and"ing of the arguments. (e.g. if both a
	 * <code>location</code> and a <code>fromDate</code> are given, only Obs that are <u>both</u> at
	 * that Location and after the fromDate are returned). <br>
	 * <br>
	 * Note: If <code>whom</code> has elements, <code>personType</code> is ignored <br>
	 * <br>
	 * Note: to get all observations count on a certain date, use:<br>
	 * Date fromDate = "2009-08-15";<br>
	 * Date toDate = OpenmrsUtil.lastSecondOfDate(fromDate); List&lt;Obs&gt; obs = getObservations(....,
	 * fromDate, toDate, ...);
	 * 
	 * @param whom List&lt;Person&gt; to restrict obs to (optional)
	 * @param encounters List&lt;Encounter&gt; to restrict obs to (optional)
	 * @param questions List&lt;Concept&gt; to restrict the obs to (optional)
	 * @param answers List&lt;Concept&gt; to restrict the valueCoded to (optional)
	 * @param personTypes List&lt;PERSON_TYPE&gt; objects to restrict this to. Only used if
	 *            <code>whom</code> is an empty list (optional)
	 * @param locations The org.openmrs.Location objects to restrict to (optional) obsDatetime
	 *            (optional)
	 * @param obsGroupId the Obs.getObsGroupId() to this integer (optional)
	 * @param fromDate the earliest Obs date to get (optional)
	 * @param toDate the latest Obs date to get (optional)
	 * @param includeVoidedObs true/false whether to also include the voided obs (required)
	 * @return list of Observations that match all of the criteria given in the arguments
	 * @throws APIException
	 */
	@Authorized(PrivilegeConstants.GET_OBS)
	public Integer getObservationCount(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, Integer obsGroupId,
	        Date fromDate, Date toDate, boolean includeVoidedObs) throws APIException;
	
	/**
	 * @see org.openmrs.api.ObsService#getObservationCount(java.util.List, java.util.List,
	 *      java.util.List, java.util.List, java.util.List, java.util.List, java.lang.Integer,
	 *      java.util.Date, java.util.Date, boolean)
	 *
	 * This method works exactly the same; it only adds accession number search criteria.
	 * It effectively surpasses the above method; the old one is however kept for backward
	 * compatibility reasons.
	 *
	 * @param whom List&lt;Person&gt; to restrict obs to (optional)
	 * @param encounters List&lt;Encounter&gt; to restrict obs to (optional)
	 * @param questions List&lt;Concept&gt; to restrict the obs to (optional)
	 * @param answers List&lt;Concept&gt; to restrict the valueCoded to (optional)
	 * @param personTypes List&lt;PERSON_TYPE&gt; objects to restrict this to. Only used if
	 *            <code>whom</code> is an empty list (optional)
	 * @param locations The org.openmrs.Location objects to restrict to (optional) obsDatetime
	 *            (optional)
	 * @param obsGroupId the Obs.getObsGroupId() to this integer (optional)
	 * @param fromDate the earliest Obs date to get (optional)
	 * @param toDate the latest Obs date to get (optional)
	 * @param includeVoidedObs true/false whether to also include the voided obs (required)
	 * @param accessionNumber accession number (optional)
	 * @return list of Observations that match all of the criteria given in the arguments
	 * @since 1.12
	 * @throws APIException
	 * <strong>Should</strong> compare dates using lte and gte
	 * <strong>Should</strong> get the count of all obs assigned to given encounters
	 * <strong>Should</strong> get the count of all obs with question concept in given questions parameter
	 * <strong>Should</strong> get the count of all obs with answer concept in given answers parameter
	 * <strong>Should</strong> return the count of all obs whose person is a person only
	 * <strong>Should</strong> return the count of all obs whose person is a patient only
	 * <strong>Should</strong> return the count of obs whose person is a user only
	 * <strong>Should</strong> return the count of obs with location in given locations parameter
	 * <strong>Should</strong> return the count of obs whose groupId is given obsGroupId
	 * <strong>Should</strong> not include count of voided obs
	 * <strong>Should</strong> include count of voided obs if includeVoidedObs is true
	 * <strong>Should</strong> return count of obs with matching accession number
	 */
	@Authorized(PrivilegeConstants.GET_OBS)
	public Integer getObservationCount(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, Integer obsGroupId,
	        Date fromDate, Date toDate, boolean includeVoidedObs, String accessionNumber) throws APIException;
	
	/**
	 * This method searches the obs table based on the given <code>searchString</code>.
	 * 
	 * @param searchString The string to search on
	 * @return observations matching the given string
	 * @throws APIException
	 * <strong>Should</strong> get obs matching patient identifier in searchString
	 * <strong>Should</strong> get obs matching encounterId in searchString
	 * <strong>Should</strong> get obs matching obsId in searchString
	 */
	@Authorized(PrivilegeConstants.GET_OBS)
	public List<Obs> getObservations(String searchString) throws APIException;
	
	/**
	 * Get all nonvoided observations for the given patient with the given concept as the question
	 * concept (conceptId)
	 * 
	 * @param who person to match on
	 * @param question conceptId to match on
	 * @return list of all nonvoided observations matching these criteria
	 * @throws APIException
	 * @see #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date,
	 *      boolean)
	 * <strong>Should</strong> get observations matching person and question
	 * <strong>Should</strong> not fail with null person parameter
	 */
	@Authorized(PrivilegeConstants.GET_OBS)
	public List<Obs> getObservationsByPersonAndConcept(Person who, Concept question) throws APIException;
	
	/**
	 * Get a complex observation. If obs.isComplex() is true, then returns an Obs with its
	 * ComplexData. Otherwise returns a simple Obs. 
	 * @param obsId
	 * @return Obs with a ComplexData
	 * @since 1.5
	 * <strong>Should</strong> fill in complex data object for complex obs
	 * <strong>Should</strong> return normal obs for non complex obs
	 * <strong>Should</strong> not fail with null view
	 * @deprecated as of 2.1.0, use {@link #getObs(Integer)} 
	 */
	@Deprecated
	@Authorized( { PrivilegeConstants.GET_OBS })
	public Obs getComplexObs(Integer obsId, String view) throws APIException;
	
	/**
	 * Get the ComplexObsHandler that has been registered with the given key
	 * 
	 * @param key that has been registered with a handler class
	 * @return Object representing the handler for the given key
	 * @since 1.5
	 * <strong>Should</strong> get handler with matching key
	 * <strong>Should</strong> have default image and text handlers registered by spring
	 */
	public ComplexObsHandler getHandler(String key) throws APIException;
	
	/**
	 * Get the ComplexObsHandler associated with a complex observation
	 * Returns the ComplexObsHandler.
	 * Returns null if the Obs.isComplexObs() is false or there is an error
	 * instantiating the handler class.
	 *
	 * @param obs A complex Obs.
	 * @return ComplexObsHandler for the complex Obs. or null on error.
	 * @since 1.12
	 * <strong>Should</strong> get handler associated with complex observation
	 */
	public ComplexObsHandler getHandler(Obs obs) throws APIException;
	
	/**
	 * <u>Add</u> the given map to this service's handlers. This method registers each
	 * ComplexObsHandler to this service. If the given String key exists, that handler is
	 * overwritten with the given handler For most situations, this map is set via spring, see the
	 * applicationContext-service.xml file to add more handlers.
	 *
	 * @param handlers Map of class to handler object
	 * @throws APIException
	 * @since 1.5
	 * <strong>Should</strong> override handlers with same key
	 * <strong>Should</strong> add new handlers with new keys
	 */
	public void setHandlers(Map<String, ComplexObsHandler> handlers) throws APIException;
	
	/**
	 * Gets the handlers map registered
	 *
	 * @return map of keys to handlers
	 * @since 1.5
	 * @throws APIException
	 * <strong>Should</strong> never return null
	 */
	public Map<String, ComplexObsHandler> getHandlers() throws APIException;
	
	/**
	 * Registers the given handler with the given key If the given String key exists, that handler
	 * is overwritten with the given handler
	 *
	 * @param key the key name to use for this handler
	 * @param handler the class to register with this key
	 * @throws APIException
	 * @since 1.5
	 * <strong>Should</strong> register handler with the given key
	 */
	public void registerHandler(String key, ComplexObsHandler handler) throws APIException;
	
	/**
	 * Convenience method for {@link #registerHandler(String, ComplexObsHandler)}
	 * 
	 * @param key the key name to use for this handler
	 * @param handlerClass the class to register with this key
	 * @throws APIException
	 * @since 1.5
	 * <strong>Should</strong> load handler and register key
	 */
	public void registerHandler(String key, String handlerClass) throws APIException;
	
	/**
	 * Remove the handler associated with the key from list of available handlers
	 * 
	 * @param key the key of the handler to unregister
	 * @since 1.5
	 * <strong>Should</strong> remove handler with matching key
	 * <strong>Should</strong> not fail with invalid key
	 */
	public void removeHandler(String key) throws APIException;
	
	/**
	 * Gets the number of observations(including voided ones) that are using the specified
	 * conceptNames as valueCodedName answers
	 * 
	 * @param conceptNames the conceptNames to be searched against
	 * @param includeVoided whether voided observation should be included
	 * @return The number of observations using the specified conceptNames as valueCodedNames
	 * <strong>Should</strong> return the count of all observations using the specified conceptNames as answers
	 * <strong>Should</strong> include voided observations using the specified conceptNames as answers
	 * <strong>Should</strong> return zero if no observation is using any of the concepNames in the list
	 * @since Version 1.7
	 */
	@Authorized(PrivilegeConstants.GET_OBS)
	public Integer getObservationCount(List<ConceptName> conceptNames, boolean includeVoided);
	
}
