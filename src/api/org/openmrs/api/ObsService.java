/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.ObsDAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.springframework.transaction.annotation.Transactional;

/**
 * The ObsService deals with saving and getting Obs to/from the
 * database
 * 
 * Usage:
 * <pre>
 *  ObsService obsService = Context.getObsService();
 *  // get the obs for patient with internal identifier of 1235
 *  List<Obs> someObsList = obsService.getObservationsByPerson(new Patient(1235));
 * </pre>
 * 
 * There are also a number of convenience methods for extracting obs
 * pertaining to certain Concepts, people, or encounters
 *
 * @see org.openmrs.Obs
 * @see org.openmrs.ComplexObs
 * @see org.openmrs.MimeType
 * @see org.openmrs.api.context.Context
 */
@Transactional
public interface ObsService extends OpenmrsService {

	/**
	 * @see org.openmrs.util.OpenmrsConstants
	 * @deprecated Use org.openmrs.util.OpenmrsConstants#PERSON_TYPE.PATIENT
	 */
	public static final Integer PERSON = 1;
	
	/**
	 * @see org.openmrs.util.OpenmrsConstants
	 * @deprecated Use OpenmrsConstants#PERSON_TYPE.PATIENT
	 */
	public static final Integer PATIENT = 2;
	
	/**
	 * @see org.openmrs.util.OpenmrsConstants
	 * @deprecated Use OpenmrsConstants.PERSON_TYPE.USER
	 */
	public static final Integer USER = 4;

	/**
	 * Set the given <code>dao</code> on this obs service. 
	 * The dao will act as the conduit through with all obs
	 * calls get to the database
	 * 
	 * @param dao specific ObsDAO to use for this service
	 */
	public void setObsDAO(ObsDAO dao);

	/**
	 * Create an observation
	 * 
	 * @param Obs
	 * @throws APIException
	 * @deprecated use {@link #saveObs(Obs, String)}
	 */
	@Authorized(OpenmrsConstants.PRIV_ADD_OBS)
	public void createObs(Obs obs) throws APIException;

	/**
	 * Create a grouping of observations (observations linked by
	 * {@link org.openmrs.Obs#getObsGroupId()}
	 * 
	 * The proper use is:
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
	 * @deprecated This method should no longer need to be called on the api. This
	 * 			  was meant as temporary until we created a true ObsGroup pojo.
	 * 			  Replaced by {@link #createObsGroup(Obs, List)}
	 * 
	 * @see #createObsGroup(Obs, List)
	 */
	@Authorized(OpenmrsConstants.PRIV_ADD_OBS)
	public void createObsGroup(Obs[] obs) throws APIException;

	/**
	 * Get an observation
	 * 
	 * @param integer obsId of observation desired
	 * @return matching Obs
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public Obs getObs(Integer obsId) throws APIException;

	/**
	 * Save changes to observation
	 * 
	 * @param Obs
	 * @throws APIException
	 * @deprecated use {@link #saveObs(Obs, String)}
	 */
	@Authorized(OpenmrsConstants.PRIV_EDIT_OBS)
	public void updateObs(Obs obs) throws APIException;

	/**
	 * Save the given obs to the database. This will move the
	 * contents of the given <code>obs</code> to the database. This
	 * acts as both the initial save and an update kind of save.
	 * The returned obs will be the same as the obs passed in. It is
	 * included for chaining.
	 * 
	 * If this is an initial save, the obsId on the given
	 * <code>obs</code> object will be updated to reflect the auto
	 * numbering from the database.  The obsId on the returned obs
	 * will also have this number.
	 * 
	 * If there is already an obsId on the given <code>obs</code> object,
	 * the given obs will be voided and a new row in the database will be
	 * created that has a new obs id.
	 * 
	 * @param obs the Obs to save to the database
	 * @param changeMessage String explaining why <code>obs</code> is being changed. If 
	 * 		<code>obs</code> is a new obs, changeMessage is nullable, or if 
	 * 		it is being updated, it would be required
	 * @return Obs that was saved to the database
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_ADD_OBS, OpenmrsConstants.PRIV_EDIT_OBS})
	public Obs saveObs(Obs obs, String changeMessage) throws APIException;

	/**
	 * Equivalent to deleting an observation
	 * 
	 * @param Obs obs to void
	 * @param String reason
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_EDIT_OBS)
	public Obs voidObs(Obs obs, String reason) throws APIException;

	/**
	 * Revive an observation (pull a Lazarus)
	 * 
	 * @param Obs
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_EDIT_OBS)
	public Obs unvoidObs(Obs obs) throws APIException;

	/**
	 * This method shouldn't be used.  Use either {@link #purgeObs(Obs)} or
	 * {@link #voidObs(Obs)}
	 * 
	 * @param Obs
	 * @throws APIException
	 * @deprecated use #purgeObs(Obs)
	 */
	@Authorized(OpenmrsConstants.PRIV_DELETE_OBS)
	public void deleteObs(Obs obs) throws APIException;

	/**
	 * Completely remove an observation from the database. This should typically
	 * not be called because we don't want to ever lose data.  The data really
	 * <i>should</i> be voided and then it is not seen in interface any longer 
	 * (see #voidObs(Obs) for that one)
	 * 
	 * If other things link to this obs, an error will be thrown.
	 * 
	 * @param Obs
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_DELETE_OBS)
	public void purgeObs(Obs obs) throws APIException;
	
	/**
	 * Completely remove an observation from the database. This should typically
	 * not be called because we don't want to ever lose data.  The data really
	 * <i>should</i> be voided and then it is not seen in interface any longer 
	 * (see #voidObs(Obs) for that one)
	 * 
	 * @param Obs the observation to remove from the database
	 * @param cascade true/false whether or not to cascade down to other things
	 * 			that link to this observation (like Orders and ObsGroups)
	 * @throws APIException
	 * @see #purgeObs(Obs, boolean)
	 */
	@Authorized(OpenmrsConstants.PRIV_DELETE_OBS)
	public void purgeObs(Obs obs, boolean cascade) throws APIException;
	
	/**
	 * @deprecated use {@link #getAllMimeTypes()}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_MIME_TYPES)
	public List<MimeType> getMimeTypes() throws APIException;

	/**
	 * Gets all mime types (including retired ones)
	 * 
	 * @return list of MimeTypes in the system
	 * @see #getAllMimeTypes(boolean)
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_VIEW_MIME_TYPES)
	public List<MimeType> getAllMimeTypes() throws APIException;
	
	/**
	 * Gets all mime types and disregards the retired ones
	 * if <code>includeRetired</code> is true
	 * 
	 * @param includeRetired true/false of whether to also return the retired ones
	 * @return list of MimeTypes lll
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_VIEW_MIME_TYPES)
	public List<MimeType> getAllMimeTypes(boolean includeRetired) throws APIException;

	/**
	 * Get mimeType by internal identifier
	 * 
	 * @param mimeType id
	 * @return mimeType with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_MIME_TYPES)
	public MimeType getMimeType(Integer mimeTypeId) throws APIException;

	/**
	 * Save the given <code>mimeType</code> to the database.
	 * If mimeType is not null, the mimeType is updated in the database.
	 * If mimeType is null, a new mimeType is added to the database
	 * 
	 * @param mimeType mimeType
	 * @return mimeType that was saved/updated in the database
	 * @throws APIException
	 */
	@Authorized(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES)
	public MimeType saveMimeType(MimeType mimeType) throws APIException;
	
	/**
	 * This effectively removes the given mimeType from the system.  Voided
	 * mimeTypes are still linked to from complexObs, they just aren't shown 
	 * in the list of available mimeTypes 
	 * 
	 * @param mimeType the MimeType to remove
	 * @param reason the reason this mimeType is being voided
	 * @return
	 * @throws APIException
	 * @see {@link #createObs(Obs)}
	 */
	@Authorized(OpenmrsConstants.PRIV_MANAGE_MIME_TYPES)
	public MimeType voidMimeType(MimeType mimeType, String reason) throws APIException;
	
	/**
	 * This completely removes the given <code>MimeType</code> from the database.
	 * If data has been stored already that points at this mimeType an
	 * exception is thrown
	 * 
	 * @param mimeType the MimeType to remove
	 * @throws APIException
	 * @see {@link #purgeMimeType(MimeType)
	 */
	@Authorized(OpenmrsConstants.PRIV_PURGE_MIME_TYPES)
	public void purgeMimeType(MimeType mimeType) throws APIException;
	
	/**
	 * @see {@link org.openmrs.Person#getObservations()}
	 * @deprecated use {@link #getObservationsByPerson(Person)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public Set<Obs> getObservations(Person who, boolean includeVoided);

	/**
	 * Get all Observations for the given person.
	 * 
	 * Does not return voided observations
	 * 
	 * @param who the user to match on
	 * @return
	 * @see {@link #getObservations(List, List, List, List, List, List, String, Integer, Integer, Date, Date, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> getObservationsByPerson(Person who);
	
	/**
	 * This method fetches observations according to the criteria in
	 * the given arguments.  All arguments are optional and nullable.  If
	 * more than one argument is non-null, the result is equivalent to an
	 * "and"ing of the arguments. (e.g. if both a <code>location</code> and 
	 * a <code>fromDate</code> are given, only Obs that are <u>both</u> at 
	 * that Location and after the fromDate are returned).
	 * 
	 * Note: If <code>whom</code> has elements, <code>personType</code> is ignored 
	 * 
	 * @param whom List<Person> to restrict obs to (optional)
	 * @param encounters List<Encounter> to restrict obs to (optional)
	 * @param questions List<Concept> to restrict the obs to (optional)
	 * @param answers List<Concept> to restrict the valueCoded to (optional)
	 * @param personType PERSON_TYPE objects to restrict this to. Only used if <code>whom</code> not an
	 * 				empty list (optional)
	 * @param locations The org.openmrs.Location objects to restrict to (optional)
	 * @param sort list of column names to sort on (obsId, obsDatetime, etc) if null, defaults to obsDatetime (optional)
	 * @param mostRecentN restrict the number of obs returned to this size (optional)
	 * @param obsGroupId the Obs.getObsGroupId() to this integer (optional)
	 * @param fromDate the earliest Obs date to get (optional)
	 * @param toDate the latest Obs date to get (optional)
	 * @param includeVoidedObs true/false whether to also include the voided obs (required)
	 * @return list of Observations that match all of the criteria given in the arguments
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> getObservations(List<Person> whom, List<Encounter> encounters, 
	                                 List<Concept> questions, List<Concept> answers, 
	                                 List<PERSON_TYPE> personTypes, List<Location> locations, 
	                                 List<String> sort, Integer mostRecentN, Integer obsGroupId, 
	                                 Date fromDate, Date toDate, boolean includeVoidedObs) 
	                                 throws APIException;
	
	/**
	 * This method searches the obs table based on the given
	 * <code>searchString</code>.
	 * 
	 * @param searchString The string to search on
	 * @return observations matching the given string 
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> getObservations(String searchString) throws APIException;
	
	/**
	 * @deprecated use {@link #getObservations(List, Encounter, List, List, List, List, String, Integer, Integer, Date, Date, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> getObservations(Concept c, Location loc, String sort, Integer personType, boolean includeVoided);
	
	/**
	 * @deprecated use {@link #getObservationsByPersonAndConcept(Person, Concept)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public Set<Obs> getObservations(Person who, Concept question, boolean includeVoided);
	
	/**
	 * Get all nonvoided observations for the given patient with the given
	 * concept as the question concept (conceptId)
	 * 
	 * @param who person to match on
	 * @param question conceptId to match on
	 * @return list of all nonvoided observations matching these criteria
	 * @throws APIException
	 * @see {@link #getObservations(List, List, List, List, List, List, List, Integer, Integer, Date, Date, boolean)}
	 */
	public List<Obs> getObservationsByPersonAndConcept(Person who, Concept question) throws APIException;
	
	/**
	 * @deprecated use {@link #getObservations(List, List, List, org.openmrs.api.ObsService.PERSON_TYPE, Location, String, Integer, Integer, Date, Date, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> getLastNObservations(Integer n, Person who,
	        Concept question, boolean includeVoided);

	/**
	 * @deprecated use {@link #getObservations(List, List, List, org.openmrs.api.ObsService.PERSON_TYPE, Location, String, Integer, Integer, Date, Date, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> getObservations(Concept question, String sort, Integer personType, boolean includeVoided);

	/**
	 * @deprecated use {@link #getObservations(List, List, List, org.openmrs.api.ObsService.PERSON_TYPE, Location, String, Integer, Integer, Date, Date, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> getObservationsAnsweredByConcept(Concept answer, Integer personType, boolean includeVoided);

	/**
	 * Return all numeric answer values for the given concept ordered by value
	 * numeric low to high
	 * 
	 * personType should be one of PATIENT, PERSON, or USER;
	 * 
	 * @param concept
	 * @param sortByValue true/false if sorting by valueNumeric. If false, will
	 *        sort by obsDatetime
	 * @param personType
	 * @deprecated use {@link #getObservations(List, Encounter, List, List, org.openmrs.api.ObsService.PERSON_TYPE, Location, String, Integer, Integer, Date, Date, boolean)}
	 * 
	 * @return List<Object[]> [0]=<code>obsId</code>, [1]=<code>obsDatetime</code>, [2]=<code>valueNumeric</code>s
	 **/
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Object[]> getNumericAnswersForConcept(Concept answer, Boolean sortByValue, Integer personType, boolean includeVoided);

	/**
	 * @deprecated use org.openmrs.Encounter.getObservations()
	 * @see {@link org.openmrs.Encounter.getObservations()}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public Set<Obs> getObservations(Encounter whichEncounter);

	/**
	 * @deprecated use {@link #getObservations(List, List, List, org.openmrs.api.ObsService.PERSON_TYPE, Location, String, Integer, Integer, Date, Date, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> getVoidedObservations();

	/**
	 * @deprecated use {@link #getObservations(List, List, List, org.openmrs.api.ObsService.PERSON_TYPE, Location, String, Integer, Integer, Date, Date, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> findObservations(String search, boolean includeVoided, Integer personType);

	/**
	 * @deprecated should use Obs.getGroupMembers() or {@link #getObservations(List, List, List, org.openmrs.api.ObsService.PERSON_TYPE, Location, String, Integer, Integer, Date, Date, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> findObsByGroupId(Integer obsGroupId);
	
	/**
	 * @deprecated use {@link #getObservations(List, List, List, org.openmrs.api.ObsService.PERSON_TYPE, Location, String, Integer, Integer, Date, Date, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> getObservations(List<Concept> concepts, Date fromDate, Date toDate, boolean includeVoided);

	/**
	 * @deprecated use {@link #getObservations(List, List, List, org.openmrs.api.ObsService.PERSON_TYPE, Location, String, Integer, Integer, Date, Date, boolean)}
	 */
	@Transactional(readOnly = true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> getObservations(List<Concept> concepts, Date fromDate, Date toDate);

	
	/**
	 * @deprecated use {@link #getObservations(List, List, List, org.openmrs.api.ObsService.PERSON_TYPE, Location, String, Integer, Integer, Date, Date, boolean)}
	 */
	@Transactional(readOnly=true)
	@Authorized(OpenmrsConstants.PRIV_VIEW_OBS)
	public List<Obs> getObservations(Cohort patients, List<Concept> concepts, Date fromDate, Date toDate);
	
}