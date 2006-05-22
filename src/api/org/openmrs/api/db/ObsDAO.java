package org.openmrs.api.db;

import java.util.List;
import java.util.Set;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Patient;

/**
 * Observation-related database functions
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public interface ObsDAO {

	/**
	 * Create an observation 
	 * @param Obs
	 * @throws DAOException
	 */
	public void createObs(Obs obs) throws DAOException;

	/**
	 * Get an observation
	 * @param integer obsId of observation desired
	 * @return matching Obs
	 * @throws DAOException
	 */
	public Obs getObs(Integer obsId) throws DAOException;

	/**
	 * Save changes to observation
	 * @param Obs
	 * @throws DAOException
	 */
	public void updateObs(Obs obs) throws DAOException;

	/**
	 * Delete an observation.  SHOULD NOT BE CALLED unless caller is lower-level.
	 * @param Obs
	 * @throws DAOException
	 * @see voidObs(Obs)
	 */
	public void deleteObs(Obs obs) throws DAOException;
	
	/**
	 * Get all mime types
	 * 
	 * @return mime types list
	 * @throws DAOException
	 */
	public List<MimeType> getMimeTypes() throws DAOException;

	/**
	 * Get mimeType by internal identifier
	 * 
	 * @param mimeType id
	 * @return mimeType with given internal identifier
	 * @throws DAOException
	 */
	public MimeType getMimeType(Integer mimeTypeId) throws DAOException;
	
	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws DAOException
	 */
	public List<Location> getLocations() throws DAOException;

	/**
	 * Get location by internal identifier
	 * 
	 * @param location id
	 * @return location with given internal identifier
	 * @throws DAOException
	 */
	public Location getLocation(Integer locationId) throws DAOException;
	
	/**
	 * Get location by name
	 * 
	 * @param name location's name
	 * @return location with given name
	 * @throws DAOException
	 */
	public Location getLocationByName(String name) throws DAOException;
	
	/**
	 * Get all Observations for a patient
	 * @param who
	 * @return
	 */
	public Set<Obs> getObservations(Patient who) throws DAOException;
	
	/**
	 * Get all Observations for this concept/location
	 * Sort is optional
	 * @param concept
	 * @param location
	 * @param sort
	 * @return list of obs for a location
	 */
	public List<Obs> getObservations(Concept c, Location loc, String sort) throws DAOException;

	/**
	 * e.g. get all CD4 counts for a patient 
	 * @param who
	 * @param question
	 * @return
	 */
    public Set<Obs> getObservations(Patient who, Concept question) throws DAOException;

    /**
	 * e.g. get last 'n' number of observations for a patient for given concept
	 * @param n number of concepts to retrieve
	 * @param who
	 * @param question
	 * @return
	 */
    public List<Obs> getLastNObservations(Integer n, Patient who, Concept question);
	/**
	 * e.g. get all observations referring to RETURN VISIT DATE
	 * @param question (Concept: RETURN VISIT DATE)
	 * @param sort string (property name)
	 * @return
	 */
    public List<Obs> getObservations(Concept question, String sort) throws DAOException;
    
    /**
     * Get all observations from a specific encounter
     * @param whichEncounter
     * @return
     */
    public Set<Obs> getObservations(Encounter whichEncounter) throws DAOException;
    
    /**
     * Get all observations that have been voided
     * @return List of Obs
     */
    public List<Obs> getVoidedObservations() throws DAOException;
    
    /**
     * Find observations matching the search string
     * "matching" is defined as either the obsId or the patient identifier
     * 
     * @param search
     * @param includeVoided
     * @return list of matched observations
     */
    public List<Obs> findObservations(Integer id, boolean includeVoided) throws DAOException;
}
