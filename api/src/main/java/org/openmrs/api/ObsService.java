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
import java.util.Set;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.ObsDAO;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.OpenmrsConstants;
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
 * @see org.openmrs.ComplexObs
 * @see org.openmrs.MimeType
 * @see org.openmrs.api.context.Context
 */
public interface ObsService extends OpenmrsService {
	
	/**
	 * @see org.openmrs.util.OpenmrsConstants
	 * @deprecated Use org.openmrs.util.OpenmrsConstants#PERSON_TYPE.PATIENT
	 */
	@Deprecated
	public static final Integer PERSON = 1;
	
	/**
	 * @see org.openmrs.util.OpenmrsConstants
	 * @deprecated Use OpenmrsConstants#PERSON_TYPE.PATIENT
	 */
	@Deprecated
	public static final Integer PATIENT = 2;
	
	/**
	 * @see org.openmrs.util.OpenmrsConstants
	 * @deprecated Use OpenmrsConstants.PERSON_TYPE.USER
	 */
	@Deprecated
	public static final Integer USER = 4;
	
	/**
	 * Set the given <code>dao</code> on this obs service. The dao will act as the conduit through
	 * with all obs calls get to the database
	 * 
	 * @param dao specific ObsDAO to use for this service
	 */
	public void setObsDAO(ObsDAO dao);
	
	/**
	 * Create an observation
	 * 
	 * @param obs
	 * @throws APIException
	 * @deprecated use {@link #saveObs(Obs, String)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.ADD_OBS)
	public void createObs(Obs obs) throws APIException;
	
	/**
	 * Create a grouping of observations (observations linked by
	 * {@link org.openmrs.Obs#getObsGroupId()} The proper use is:
	 * 
	 * <pre>
	 * Obs obsGroup = new Obs();
	 * for (Obs member : obs) {
	 *   obsGroup.addGroupMember(obs);
	 * }
	 * pass obsGroup to {@link #createObs(Obs)}
	 * </pre>
	 * 
	 * @param obs - array of observations to be grouped
	 * @throws APIException
	 * @deprecated This method should no longer need to be called on the api. This was meant as
	 *             temporary until we created a true ObsGroup pojo. Replaced by
	 *             {@link #saveObs(Obs, String)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.ADD_OBS)
	public void createObsGroup(Obs[] obs) throws APIException;
	
	/**
	 * Get an observation
	 * 
	 * @param obsId integer obsId of observation desired
	 * @return matching Obs
	 * @throws APIException
	 * @should get obs matching given obsId
	 */
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public Obs getObs(Integer obsId) throws APIException;
	
	/**
	 * Get Obs by its UUID
	 * 
	 * @param uuid
	 * @return
	 * @should find object given valid uuid
	 * @should return null if no object found with given uuid
	 */
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public Obs getObsByUuid(String uuid) throws APIException;
	
	/**
	 * Save changes to observation
	 * 
	 * @param obs
	 * @throws APIException
	 * @deprecated use {@link #saveObs(Obs, String)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.EDIT_OBS)
	public void updateObs(Obs obs) throws APIException;
	
	/**
	 * Save the given obs to the database. This will move the contents of the given <code>obs</code>
	 * to the database. This acts as both the initial save and an update kind of save. The returned
	 * obs will be the same as the obs passed in. It is included for chaining. If this is an initial
	 * save, the obsId on the given <code>obs</code> object will be updated to reflect the auto
	 * numbering from the database. The obsId on the returned obs will also have this number. If
	 * there is already an obsId on the given <code>obs</code> object, the given obs will be voided
	 * and a new row in the database will be created that has a new obs id.
	 * 
	 * @param obs the Obs to save to the database
	 * @param changeMessage String explaining why <code>obs</code> is being changed. If
	 *            <code>obs</code> is a new obs, changeMessage is nullable, or if it is being
	 *            updated, it would be required
	 * @return Obs that was saved to the database
	 * @throws APIException
	 * @should create new file from complex data for new obs
	 * @should not overwrite file when updating a complex obs
	 * @should void the given obs in the database
	 * @should create very basic obs and add new obsId
	 * @should allow changing of every property on obs
	 * @should return a different object when updating an obs
	 * @should set creator and dateCreated on new obs
	 * @should cascade save to child obs groups
	 * @should cascade update to new child obs groups
	 * @should link original and updated obs
	 * @should set void reason message to changeMessage
	 */
	@Authorized( { PrivilegeConstants.ADD_OBS, PrivilegeConstants.EDIT_OBS })
	public Obs saveObs(Obs obs, String changeMessage) throws APIException;
	
	/**
	 * Equivalent to deleting an observation
	 * 
	 * @param obs Obs to void
	 * @param reason String reason it's being voided
	 * @throws APIException
	 * @should set voided bit on given obs
	 * @should fail if reason parameter is empty
	 */
	@Authorized(PrivilegeConstants.EDIT_OBS)
	public Obs voidObs(Obs obs, String reason) throws APIException;
	
	/**
	 * Revive an observation (pull a Lazarus)
	 * 
	 * @param obs Obs to unvoid
	 * @throws APIException
	 * @should unset voided bit on given obs
	 * @should cascade unvoid to child grouped obs
	 */
	@Authorized(PrivilegeConstants.EDIT_OBS)
	public Obs unvoidObs(Obs obs) throws APIException;
	
	/**
	 * This method shouldn't be used. Use either {@link #purgeObs(Obs)} or
	 * {@link #voidObs(Obs,String)}
	 * 
	 * @param obs
	 * @throws APIException
	 * @deprecated use #purgeObs(Obs)
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.DELETE_OBS)
	public void deleteObs(Obs obs) throws APIException;
	
	/**
	 * Completely remove an observation from the database. This should typically not be called
	 * because we don't want to ever lose data. The data really <i>should</i> be voided and then it
	 * is not seen in interface any longer (see #voidObs(Obs) for that one) If other things link to
	 * this obs, an error will be thrown.
	 * 
	 * @param obs
	 * @throws APIException
	 * @see #purgeObs(Obs, boolean)
	 * @should delete the given obs from the database
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
	 * @should throw APIException if given true cascade
	 */
	@Authorized(PrivilegeConstants.DELETE_OBS)
	public void purgeObs(Obs obs, boolean cascade) throws APIException;
	
	/**
	 * @deprecated use {@link #getAllMimeTypes()}
	 */
	@Deprecated
	@Authorized(OpenmrsConstants.PRIV_VIEW_MIME_TYPES)
	public List<MimeType> getMimeTypes() throws APIException;
	
	/**
	 * Gets all mime types (including retired ones)
	 * 
	 * @return list of MimeTypes in the system
	 * @see #getAllMimeTypes(boolean)
	 * @throws APIException
	 * @deprecated MimeTypes are no longer used. See ConceptComplex and its use of handlers
	 */
	@Deprecated
	@Authorized(OpenmrsConstants.PRIV_VIEW_MIME_TYPES)
	public List<MimeType> getAllMimeTypes() throws APIException;
	
	/**
	 * Gets all mime types and disregards the retired ones if <code>includeRetired</code> is true
	 * 
	 * @param includeRetired true/false of whether to also return the retired ones
	 * @return list of MimeTypes lll
	 * @throws APIException
	 * @deprecated MimeTypes are no longer used. See ConceptComplex and its use of handlers
	 */
	@Deprecated
	@Authorized(OpenmrsConstants.PRIV_VIEW_MIME_TYPES)
	public List<MimeType> getAllMimeTypes(boolean includeRetired) throws APIException;
	
	/**
	 * Get mimeType by internal identifier
	 * 
	 * @param mimeTypeId
	 * @return mimeType with given internal identifier
	 * @throws APIException
	 * @deprecated MimeTypes are no longer used. See ConceptComplex and its use of handlers
	 */
	@Deprecated
	@Authorized(OpenmrsConstants.PRIV_VIEW_MIME_TYPES)
	public MimeType getMimeType(Integer mimeTypeId) throws APIException;
	
	/**
	 * Save the given <code>mimeType</code> to the database. If mimeType is not null, the mimeType
	 * is updated in the database. If mimeType is null, a new mimeType is added to the database
	 * 
	 * @param mimeType mimeType
	 * @return mimeType that was saved/updated in the database
	 * @throws APIException
	 * @deprecated MimeTypes are no longer used. See ConceptComplex and its use of handlers
	 */
	@Deprecated
	@Authorized(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES)
	public MimeType saveMimeType(MimeType mimeType) throws APIException;
	
	/**
	 * This effectively removes the given mimeType from the system. Voided mimeTypes are still
	 * linked to from complexObs, they just aren't shown in the list of available mimeTypes
	 * 
	 * @param mimeType the MimeType to remove
	 * @param reason the reason this mimeType is being voided
	 * @return the voided MimeType
	 * @throws APIException
	 * @see #createObs(Obs)
	 * @deprecated MimeTypes are no longer used. See ConceptComplex and its use of handlers
	 */
	@Deprecated
	@Authorized(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES)
	public MimeType voidMimeType(MimeType mimeType, String reason) throws APIException;
	
	/**
	 * This completely removes the given <code>MimeType</code> from the database. If data has been
	 * stored already that points at this mimeType an exception is thrown
	 * 
	 * @param mimeType the MimeType to remove
	 * @throws APIException
	 * @see #purgeMimeType(MimeType)
	 * @deprecated MimeTypes are no longer used. See {@link org.openmrs.ConceptComplex} and its use
	 *             of handlers
	 */
	@Deprecated
	@Authorized(OpenmrsConstants.PRIV_PURGE_MIME_TYPES)
	public void purgeMimeType(MimeType mimeType) throws APIException;
	
	/**
	 * @deprecated use {@link #getObservationsByPerson(Person)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public Set<Obs> getObservations(Person who, boolean includeVoided);
	
	/**
	 * Get all Observations for the given person, sorted by obsDatetime ascending. Does not return
	 * voided observations.
	 * 
	 * @param who the user to match on
	 * @return a List<Obs> object containing all non-voided observations for the specified person
	 * @see #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date,
	 *      boolean)
	 * @should get all observations assigned to given person
	 */
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> getObservationsByPerson(Person who);
	
	/**
	 * This method fetches observations according to the criteria in the given arguments. All
	 * arguments are optional and nullable. If more than one argument is non-null, the result is
	 * equivalent to an "and"ing of the arguments. (e.g. if both a <code>location</code> and a
	 * <code>fromDate</code> are given, only Obs that are <u>both</u> at that Location and after the
	 * fromDate are returned). <br/>
	 * <br/>
	 * Note: If <code>whom</code> has elements, <code>personType</code> is ignored <br/>
	 * <br/>
	 * Note: to get all observations on a certain date, use:<br/>
	 * Date fromDate = "2009-08-15";<br/>
	 * Date toDate = OpenmrsUtil.lastSecondOfDate(fromDate); List<Obs> obs = getObservations(....,
	 * fromDate, toDate, ...);
	 * 
	 * @param whom List<Person> to restrict obs to (optional)
	 * @param encounters List<Encounter> to restrict obs to (optional)
	 * @param questions List<Concept> to restrict the obs to (optional)
	 * @param answers List<Concept> to restrict the valueCoded to (optional)
	 * @param personTypes List<PERSON_TYPE> objects to restrict this to. Only used if
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
	@Authorized(PrivilegeConstants.VIEW_OBS)
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
	 * @param whom List<Person> to restrict obs to (optional)
	 * @param encounters List<Encounter> to restrict obs to (optional)
	 * @param questions List<Concept> to restrict the obs to (optional)
	 * @param answers List<Concept> to restrict the valueCoded to (optional)
	 * @param personTypes List<PERSON_TYPE> objects to restrict this to. Only used if
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
	 * @should compare dates using lte and gte
	 * @should get all obs assigned to given encounters
	 * @should get all obs with question concept in given questions parameter
	 * @should get all obs with answer concept in given answers parameter
	 * @should return all obs whose person is a person only
	 * @should return obs whose person is a patient only
	 * @should return obs whose person is a user only
	 * @should return obs with location in given locations parameter
	 * @should sort returned obs by obsDatetime if sort is empty
	 * @should sort returned obs by conceptId if sort is concept
	 * @should limit number of obs returned to mostReturnN parameter
	 * @should return obs whose groupId is given obsGroupId
	 * @should not include voided obs
	 * @should include voided obs if includeVoidedObs is true
	 * @should only return observations with matching accession number
	 */
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> getObservations(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, List<String> sort,
	        Integer mostRecentN, Integer obsGroupId, Date fromDate, Date toDate, boolean includeVoidedObs,
	        String accessionNumber) throws APIException;
	
	/**
	 * This method fetches the count of observations according to the criteria in the given
	 * arguments. All arguments are optional and nullable. If more than one argument is non-null,
	 * the result is equivalent to an "and"ing of the arguments. (e.g. if both a
	 * <code>location</code> and a <code>fromDate</code> are given, only Obs that are <u>both</u> at
	 * that Location and after the fromDate are returned). <br/>
	 * <br/>
	 * Note: If <code>whom</code> has elements, <code>personType</code> is ignored <br/>
	 * <br/>
	 * Note: to get all observations count on a certain date, use:<br/>
	 * Date fromDate = "2009-08-15";<br/>
	 * Date toDate = OpenmrsUtil.lastSecondOfDate(fromDate); List<Obs> obs = getObservations(....,
	 * fromDate, toDate, ...);
	 * 
	 * @param whom List<Person> to restrict obs to (optional)
	 * @param encounters List<Encounter> to restrict obs to (optional)
	 * @param questions List<Concept> to restrict the obs to (optional)
	 * @param answers List<Concept> to restrict the valueCoded to (optional)
	 * @param personTypes List<PERSON_TYPE> objects to restrict this to. Only used if
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
	@Authorized(PrivilegeConstants.VIEW_OBS)
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
	 * @param whom List<Person> to restrict obs to (optional)
	 * @param encounters List<Encounter> to restrict obs to (optional)
	 * @param questions List<Concept> to restrict the obs to (optional)
	 * @param answers List<Concept> to restrict the valueCoded to (optional)
	 * @param personTypes List<PERSON_TYPE> objects to restrict this to. Only used if
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
	 * @should compare dates using lte and gte
	 * @should get the count of all obs assigned to given encounters
	 * @should get the count of all obs with question concept in given questions parameter
	 * @should get the count of all obs with answer concept in given answers parameter
	 * @should return the count of all obs whose person is a person only
	 * @should return the count of all obs whose person is a patient only
	 * @should return the count of obs whose person is a user only
	 * @should return the count of obs with location in given locations parameter
	 * @should return the count of obs whose groupId is given obsGroupId
	 * @should not include count of voided obs
	 * @should include count of voided obs if includeVoidedObs is true
	 * @should return count of obs with matching accession number
	 */
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public Integer getObservationCount(List<Person> whom, List<Encounter> encounters, List<Concept> questions,
	        List<Concept> answers, List<PERSON_TYPE> personTypes, List<Location> locations, Integer obsGroupId,
	        Date fromDate, Date toDate, boolean includeVoidedObs, String accessionNumber) throws APIException;
	
	/**
	 * This method searches the obs table based on the given <code>searchString</code>.
	 * 
	 * @param searchString The string to search on
	 * @return observations matching the given string
	 * @throws APIException
	 * @should get obs matching patient identifier in searchString
	 * @should get obs matching encounterId in searchString
	 * @should get obs matching obsId in searchString
	 */
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> getObservations(String searchString) throws APIException;
	
	/**
	 * @deprecated use
	 *             {@link #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date, boolean)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> getObservations(Concept c, Location loc, String sort, Integer personType, boolean includeVoided);
	
	/**
	 * @deprecated use {@link #getObservationsByPersonAndConcept(Person, Concept)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public Set<Obs> getObservations(Person who, Concept question, boolean includeVoided);
	
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
	 * @should get observations matching person and question
	 * @should not fail with null person parameter
	 */
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> getObservationsByPersonAndConcept(Person who, Concept question) throws APIException;
	
	/**
	 * @deprecated use
	 *             {@link #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date, boolean)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> getLastNObservations(Integer n, Person who, Concept question, boolean includeVoided);
	
	/**
	 * @deprecated use
	 *             {@link #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date, boolean)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> getObservations(Concept question, String sort, Integer personType, boolean includeVoided);
	
	/**
	 * @deprecated use
	 *             {@link #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date, boolean)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> getObservationsAnsweredByConcept(Concept answer, Integer personType, boolean includeVoided);
	
	/**
	 * Return all numeric answer values for the given concept ordered by value numeric low to high
	 * personType should be one of PATIENT, PERSON, or USER;
	 * 
	 * @param answer
	 * @param sortByValue true/false if sorting by valueNumeric. If false, will sort by obsDatetime
	 * @param personType
	 * @deprecated use
	 *             {@link #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date, boolean)}
	 * @return List<Object[]> [0]=<code>obsId</code>, [1]=<code>obsDatetime</code>, [2]=
	 *         <code>valueNumeric</code>s
	 **/
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Object[]> getNumericAnswersForConcept(Concept answer, Boolean sortByValue, Integer personType,
	        boolean includeVoided);
	
	/**
	 * @deprecated use {@link org.openmrs#Encounter.getObservations()}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public Set<Obs> getObservations(Encounter whichEncounter);
	
	/**
	 * @deprecated use
	 *             {@link #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date, boolean)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> getVoidedObservations();
	
	/**
	 * @deprecated use {@link #getObservations(String)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> findObservations(String search, boolean includeVoided, Integer personType);
	
	/**
	 * @deprecated should use {@link Obs#getGroupMembers()} or
	 *             {@link #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date, boolean)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> findObsByGroupId(Integer obsGroupId);
	
	/**
	 * @deprecated use
	 *             {@link #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date, boolean)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> getObservations(List<Concept> concepts, Date fromDate, Date toDate, boolean includeVoided);
	
	/**
	 * @deprecated use
	 *             {@link #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date, boolean)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> getObservations(List<Concept> concepts, Date fromDate, Date toDate);
	
	/**
	 * @deprecated use
	 *             {@link #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date, boolean)}
	 */
	@Deprecated
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public List<Obs> getObservations(Cohort patients, List<Concept> concepts, Date fromDate, Date toDate);
	
	/**
	 * Get a complex observation. If obs.isComplex() is true, then returns an Obs with its
	 * ComplexData. Otherwise returns a simple Obs. TODO: Possibly remove this method. It is
	 * confusing because you may have a complexObs, but not want the complexData attached at the
	 * time. Nevertheless, you may think that this method is necessary to use, when you could just
	 * call getObs(). This will attach the complexData onto the obs.
	 * 
	 * @param obsId
	 * @return Obs with a ComplexData
	 * @since 1.5
	 * @should fill in complex data object for complex obs
	 * @should return normal obs for non complex obs
	 * @should not fail with null view
	 */
	@Authorized( { PrivilegeConstants.VIEW_OBS })
	public Obs getComplexObs(Integer obsId, String view) throws APIException;
	
	/**
	 * Get the ComplexObsHandler that has been registered with the given key
	 * 
	 * @param key that has been registered with a handler class
	 * @return Object representing the handler for the given key
	 * @since 1.5
	 * @should get handler with matching key
	 * @should have default image and text handlers registered by spring
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
	 * @should get handler associated with complex observation
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
	 * @should override handlers with same key
	 * @should add new handlers with new keys
	 */
	public void setHandlers(Map<String, ComplexObsHandler> handlers) throws APIException;
	
	/**
	 * Gets the handlers map registered
	 *
	 * @return map of keys to handlers
	 * @since 1.5
	 * @throws APIException
	 * @should never return null
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
	 * @should register handler with the given key
	 */
	public void registerHandler(String key, ComplexObsHandler handler) throws APIException;
	
	/**
	 * Convenience method for {@link #registerHandler(String, ComplexObsHandler)}
	 * 
	 * @param key the key name to use for this handler
	 * @param handlerClass the class to register with this key
	 * @throws APIException
	 * @since 1.5
	 * @should load handler and register key
	 */
	public void registerHandler(String key, String handlerClass) throws APIException;
	
	/**
	 * Remove the handler associated with the key from list of available handlers
	 * 
	 * @param key the key of the handler to unregister
	 * @since 1.5
	 * @should remove handler with matching key
	 * @should not fail with invalid key
	 */
	public void removeHandler(String key) throws APIException;
	
	/**
	 * Gets the number of observations(including voided ones) that are using the specified
	 * conceptNames as valueCodedName answers
	 * 
	 * @param conceptNames the conceptNames to be searched against
	 * @param includeVoided whether voided observation should be included
	 * @return The number of observations using the specified conceptNames as valueCodedNames
	 * @should return the count of all observations using the specified conceptNames as answers
	 * @should include voided observations using the specified conceptNames as answers
	 * @should return zero if no observation is using any of the concepNames in the list
	 * @since Version 1.7
	 */
	@Authorized(PrivilegeConstants.VIEW_OBS)
	public Integer getObservationCount(List<ConceptName> conceptNames, boolean includeVoided);
	
}
