package org.openmrs.api;

import org.openmrs.EncounterType;

/**
 * Admin-related services
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public interface AdministrationService {
	
	/**
	 * Create a new EncounterType
	 * @param EncounterType to create
	 * @return newly created EncounterType
	 * @throws APIException
	 */
	public EncounterType createEncounterType(EncounterType encounterType) throws APIException;

	/**
	 * Update an encounter type
	 * @param EncounterType to update
	 * @throws APIException
	 */
	public void updateEncounterType(EncounterType encounterType) throws APIException;

	/**
	 * Delete an encounter type
	 * @param EncounterType to delete
	 * @throws APIException
	 */
	public void deleteEncounterType(EncounterType encounterType) throws APIException;

	
}
