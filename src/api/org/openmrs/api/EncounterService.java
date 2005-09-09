package org.openmrs.api;

import org.openmrs.Encounter;

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
	 * @return newly created encounter
	 * @throws APIException
	 */
	public Encounter createEncounter(Encounter encounter) throws APIException;

	/**
	 * Get encounter by internal identifier
	 * 
	 * @param encounter encounter object
	 * @return encounter with given internal identifier
	 * @throws APIException
	 */
	public Encounter getEncounter(Integer encounterId) throws APIException;

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
