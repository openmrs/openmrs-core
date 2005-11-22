package org.openmrs.api.db;

import java.util.List;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;

/**
 * Provides encounter-related services within the current context.
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public interface EncounterService {

	/**
	 * Creates a new encounter
	 * 
	 * @param encounter to be created
	 * @throws APIException
	 */
	public void createEncounter(Encounter encounter) throws APIException;

	/**
	 * Get encounter by internal identifier
	 * 
	 * @param encounterId encounter id
	 * @return encounter with given internal identifier
	 * @throws APIException
	 */
	public Encounter getEncounter(Integer encounterId) throws APIException;
	
	/**
	 * Get all encounter types
	 * 
	 * @return encounter types list
	 * @throws APIException
	 */
	public List<EncounterType> getEncounterTypes() throws APIException;

	/**
	 * Get encounterType by internal identifier
	 * 
	 * @param encounterType id
	 * @return encounterType with given internal identifier
	 * @throws APIException
	 */
	public EncounterType getEncounterType(Integer encounterTypeId) throws APIException;

	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws APIException
	 */
	public List<Location> getLocations() throws APIException;

	/**
	 * Get location by internal identifier
	 * 
	 * @param location id
	 * @return location with given internal identifier
	 * @throws APIException
	 */
	public Location getLocation(Integer locationId) throws APIException;

	/**
	 * Save changes to encounter
	 * @param encounter
	 * @throws APIException
	 */
	public void updateEncounter(Encounter encounter) throws APIException;
	
	/**
	 * Delete encounter from database.
	 * 
	 * @param encounter encounter object to be deleted 
	 */
	public void deleteEncounter(Encounter encounter) throws APIException;
	
	
	
}
