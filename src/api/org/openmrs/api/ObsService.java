package org.openmrs.api;

import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.db.ObsDAO;
import org.openmrs.logic.Aggregation;
import org.openmrs.logic.Constraint;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ObsService {

	public void setObsDAO(ObsDAO dao);

	/**
	 * Create an observation
	 * 
	 * @param Obs
	 * @throws APIException
	 */
	public void createObs(Obs obs) throws APIException;

	/**
	 * Create a grouping of observations (observations linked by
	 * obs.obs_group_id)
	 * 
	 * @param obs -
	 *            array of observations to be grouped
	 * @throws APIException
	 */
	public void createObsGroup(Obs[] obs) throws APIException;

	/**
	 * Get an observation
	 * 
	 * @param integer
	 *            obsId of observation desired
	 * @return matching Obs
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public Obs getObs(Integer obsId) throws APIException;

	/**
	 * Save changes to observation
	 * 
	 * @param Obs
	 * @throws APIException
	 */
	public void updateObs(Obs obs) throws APIException;

	/**
	 * Equivalent to deleting an observation
	 * 
	 * @param Obs
	 *            obs to void
	 * @param String
	 *            reason
	 * @throws APIException
	 */
	public void voidObs(Obs obs, String reason) throws APIException;

	/**
	 * Revive an observation (pull a Lazarus)
	 * 
	 * @param Obs
	 * @throws APIException
	 */
	public void unvoidObs(Obs obs) throws APIException;

	/**
	 * Delete an observation. SHOULD NOT BE CALLED unless caller is lower-level.
	 * 
	 * @param Obs
	 * @throws APIException
	 * @see voidObs(Obs)
	 */
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
	 * @param mimeType
	 *            id
	 * @return mimeType with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public MimeType getMimeType(Integer mimeTypeId) throws APIException;

	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<Location> getLocations() throws APIException;

	/**
	 * Get location by internal identifier
	 * 
	 * @param location
	 *            id
	 * @return location with given internal identifier
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public Location getLocation(Integer locationId) throws APIException;

	/**
	 * Get location by name
	 * 
	 * @param name
	 *            location's name
	 * @return location with given name
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public Location getLocationByName(String name) throws APIException;

	/**
	 * Get all Observations for a patient
	 * 
	 * @param who
	 * @return
	 */
	@Transactional(readOnly = true)
	public Set<Obs> getObservations(Patient who);

	/**
	 * Get all Observations for this concept/location Sort is optional
	 * 
	 * @param concept
	 * @param location
	 * @param sort
	 * @return list of obs for a location
	 */
	@Transactional(readOnly = true)
	public List<Obs> getObservations(Concept c, Location loc, String sort);

	/**
	 * e.g. get all CD4 counts for a patient
	 * 
	 * @param who
	 * @param question
	 * @return
	 */
	@Transactional(readOnly = true)
	public Set<Obs> getObservations(Patient who, Concept question);

	/**
	 * e.g. get last 'n' number of observations for a patient for given concept
	 * 
	 * @param n
	 *            number of concepts to retrieve
	 * @param who
	 * @param question
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Obs> getLastNObservations(Integer n, Patient who,
			Concept question);

	/**
	 * e.g. get all observations referring to RETURN VISIT DATE
	 * 
	 * @param question
	 *            (Concept: RETURN VISIT DATE)
	 * @return
	 */
	@Transactional(readOnly = true)
	public List<Obs> getObservations(Concept question, String sort);

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
	 * either the obsId or the patient identifier
	 * 
	 * @param search
	 * @param includeVoided
	 * @return list of matched observations
	 */
	@Transactional(readOnly = true)
	public List<Obs> findObservations(String search, boolean includeVoided);

	@Transactional(readOnly = true)
	public List<String> getDistinctObservationValues(Concept question);

	/**
	 * @param obsGroupId
	 * @return All obs that share obsGroupId
	 */
	@Transactional(readOnly = true)
	public List<Obs> findObsByGroupId(Integer obsGroupId);

	@Transactional(readOnly = true)
	@Authorized( { "View Patient" })
	public List<Obs> getObservations(Patient who, Aggregation aggregation,
			Concept question, Constraint constraint);
}