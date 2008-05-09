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
import org.springframework.transaction.annotation.Transactional;

/**
 * The ObsService provides methods for acting on Obs, ObsGroup, and 
 * ComplexObs objects.
 * 
 * Use:
 * <code>
 * 	Context.getObsService().getObs(123);
 * </code>
 * 
 * There are also a number of convenience methods for extracting obs
 * pertaining to certain Concepts, people, or encounters
 * 
 */
@Transactional
public interface ObsService {

	public static final Integer PERSON = 1;
	public static final Integer PATIENT = 2;
	public static final Integer USER = 4;

	public void setObsDAO(ObsDAO dao);

	/**
	 * Create an observation
	 * 
	 * @param Obs
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_ADD_OBS})
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
	@Authorized({OpenmrsConstants.PRIV_ADD_OBS})
	public void createObsGroup(Obs[] obs) throws APIException;

	/**
	 * Get an observation
	 * 
	 * @param integer obsId of observation desired
	 * @return matching Obs
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	@Authorized({OpenmrsConstants.PRIV_VIEW_OBS})
	public Obs getObs(Integer obsId) throws APIException;

	/**
	 * Save changes to observation
	 * 
	 * @param Obs
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_EDIT_OBS})
	public void updateObs(Obs obs) throws APIException;

	/**
	 * Equivalent to deleting an observation
	 * 
	 * @param Obs obs to void
	 * @param String reason
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_EDIT_OBS})
	public void voidObs(Obs obs, String reason) throws APIException;

	/**
	 * Revive an observation (pull a Lazarus)
	 * 
	 * @param Obs
	 * @throws APIException
	 */
	@Authorized({OpenmrsConstants.PRIV_EDIT_OBS})
	public void unvoidObs(Obs obs) throws APIException;

	/**
	 * Delete an observation. SHOULD NOT BE CALLED unless caller is lower-level.
	 * 
	 * @param Obs
	 * @throws APIException
	 * @see voidObs(Obs)
	 */
	@Authorized({OpenmrsConstants.PRIV_DELETE_OBS})
	public void deleteObs(Obs obs) throws APIException;

	/**
	 * Get all mime types
	 * 
	 * @return mime types list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<MimeType> getMimeTypes() throws APIException;

	/**
	 * Get mimeType by internal identifier
	 * 
	 * @param mimeType id
	 * @return mimeType with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public MimeType getMimeType(Integer mimeTypeId) throws APIException;

	/**
	 * Get all Observations for a person
	 * 
	 * @param who
	 * @param includeVoided
	 * @return
	 */
	@Transactional(readOnly = true)
	public Set<Obs> getObservations(Person who, boolean includeVoided);

	/**
	 * Get all Observations for this concept/location Sort is optional
	 * 
	 * @param concept
	 * @param location
	 * @param sort
	 * @param personType
	 * @param includeVoided
	 * @return list of obs for a location
	 */
	@Transactional(readOnly = true)
	public List<Obs> getObservations(Concept c, Location loc, String sort,
	        Integer personType, boolean includeVoided);

	/**
	 * e.g. get all CD4 counts for a person
	 * 
	 * @param who
	 * @param question
	 * @param includeVoided
	 * @return
	 */
	@Transactional(readOnly = true)
	public Set<Obs> getObservations(Person who, Concept question,
	        boolean includeVoided);

	/**
	 * e.g. get last 'n' number of observations for a person for given concept
	 * 
	 * @param n number of concepts to retrieve
	 * @param who
	 * @param question
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Obs> getLastNObservations(Integer n, Person who,
	        Concept question, boolean includeVoided);

	/**
	 * e.g. get all observations referring to RETURN VISIT DATE
	 * 
	 * @param question (Concept: RETURN VISIT DATE)
	 * @param sort (obsId, obsDatetime, etc) if null, defaults to obsId
	 * @param personType
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Obs> getObservations(Concept question, String sort,
	        Integer personType, boolean includeVoided);

	/**
	 * Return all observations that have the given concept as an answer (<code>answer.getConceptId()</code> ==
	 * value_coded)
	 * 
	 * @param concept
	 * @param personType
	 * @return list of obs
	 */
	@Transactional(readOnly = true)
	public List<Obs> getObservationsAnsweredByConcept(Concept answer,
	        Integer personType, boolean includeVoided);

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
	 * 
	 * @return List<Object[]> [0]=<code>obsId</code>, [1]=<code>obsDatetime</code>,
	 *         [2]=<code>valueNumeric</code>s
	 */
	@Transactional(readOnly = true)
	public List<Object[]> getNumericAnswersForConcept(Concept answer,
	        Boolean sortByValue, Integer personType, boolean includeVoided);

	/**
	 * Get all observations from a specific encounter
	 * 
	 * @param whichEncounter
	 * @return Set of Obs
	 */
	@Transactional(readOnly = true)
	public Set<Obs> getObservations(Encounter whichEncounter);

	/**
	 * Get all observations that have been voided Observations are ordered by
	 * descending voidedDate
	 * 
	 * @return List of Obs
	 */
	@Transactional(readOnly = true)
	public List<Obs> getVoidedObservations();

	/**
	 * Find observations matching the search string "matching" is defined as
	 * either the obsId or the person identifier
	 * 
	 * @param search
	 * @param includeVoided
	 * @param personType
	 * @return list of matched observations
	 */
	@Transactional(readOnly = true)
	public List<Obs> findObservations(String search, boolean includeVoided,
	        Integer personType);

	/**
	 * 
	 * @param question
	 * @param personType
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<String> getDistinctObservationValues(Concept question,
	        Integer personType);

	/**
	 * @param obsGroupId
	 * @return All obs that share obsGroupId
	 */
	@Transactional(readOnly = true)
	public List<Obs> findObsByGroupId(Integer obsGroupId);
	
	/**
	 * Get all Observations for these concepts between these dates.  Ideal for getting things like recent lab results regardless of what patient
	 * 
	 * @param concepts get observations for these concepts (leave as null to get all)
	 * @param fromDate
	 * @param toDate
	 * @param includeVoided
	 * @return list of obs for a location
	 */
	@Transactional(readOnly = true)
	public List<Obs> getObservations(List<Concept> concepts, Date fromDate, Date toDate, boolean includeVoided);

	/**
	 * Get all Observations for these concepts between these dates.  Ideal for getting things like recent lab results regardless of what patient
	 * 
	 * @param concepts get observations for these concepts (leave as null to get all)
	 * @param fromDate
	 * @param toDate
	 * @return list of obs for a location
	 */
	@Transactional(readOnly = true)
	public List<Obs> getObservations(List<Concept> concepts, Date fromDate, Date toDate);

	
	/**
	 * Get all Observations for this patient set, for these concepts, between these dates.  Ideal for getting things like recent lab results for a set of patients
	 * 
	 * @param patients the patientset for which to retrieve data for - null means all patients
	 * @param concepts list of the concepts for which to retrieve obs - null means all obs
	 * @param fromDate lower bound for date - null means no lower bound
	 * @param toDate upper bound for date - null means no upper bound
	 * @return observations, for this patient set, with concepts in list of concepts passed, between the two dates passed in
	 */
	@Transactional(readOnly=true)
	public List<Obs> getObservations(Cohort patients, List<Concept> concepts, Date fromDate, Date toDate);
}