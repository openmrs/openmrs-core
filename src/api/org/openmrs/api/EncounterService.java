package org.openmrs.api;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

/**
 * Provides encounter-related services within the current context.
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class EncounterService {
	
	private Context context;

	public EncounterService(Context c) {
		this.context = c;
	}
	
	/**
	 * Creates a new encounter
	 * 
	 * @param encounter to be created
	 * @throws APIException
	 */
	public void createEncounter(Encounter encounter) throws APIException {
		context.getDAOContext().getEncounterDAO().createEncounter(encounter);
	}

	/**
	 * Get encounter by internal identifier
	 * 
	 * @param encounterId encounter id
	 * @return encounter with given internal identifier
	 * @throws APIException
	 */
	public Encounter getEncounter(Integer encounterId) throws APIException {
		return context.getDAOContext().getEncounterDAO().getEncounter(encounterId);
	}
	
	/**
	 * Get all encounter types
	 * 
	 * @return encounter types list
	 * @throws APIException
	 */
	public List<EncounterType> getEncounterTypes() throws APIException {
		return context.getDAOContext().getEncounterDAO().getEncounterTypes();
	}

	/**
	 * Get encounterType by internal identifier
	 * 
	 * @param encounterType id
	 * @return encounterType with given internal identifier
	 * @throws APIException
	 */
	public EncounterType getEncounterType(Integer encounterTypeId) throws APIException {
		return context.getDAOContext().getEncounterDAO().getEncounterType(encounterTypeId);
	}

	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws APIException
	 */
	public List<Location> getLocations() throws APIException {
		return context.getDAOContext().getEncounterDAO().getLocations();
	}

	/**
	 * Get location by internal identifier
	 * 
	 * @param location id
	 * @return location with given internal identifier
	 * @throws APIException
	 */
	public Location getLocation(Integer locationId) throws APIException {
		return context.getDAOContext().getEncounterDAO().getLocation(locationId);
	}

	/**
	 * Save changes to encounter
	 * @param encounter
	 * @throws APIException
	 */
	public void updateEncounter(Encounter encounter) throws APIException {
		context.getDAOContext().getEncounterDAO().updateEncounter(encounter);
	}
	
	/**
	 * Delete encounter from database.
	 * 
	 * @param encounter encounter object to be deleted 
	 */
	public void deleteEncounter(Encounter encounter) throws APIException {
		context.getDAOContext().getEncounterDAO().deleteEncounter(encounter);
	}
	
	/**
	 * all encounters for a patient
	 * @param who
	 * @return
	 */
	public Set<Encounter> getEncounters(Patient who) {
		return context.getDAOContext().getEncounterDAO().getEncounters(who);
	}

	/**
	 * Get all encounters for a patient that took place at a specific location
	 * @param who
	 * @param where
	 * @return
	 */
    public Set<Encounter> getEncounters(Patient who, Location where) {
    	return context.getDAOContext().getEncounterDAO().getEncounters(who, where);
    }

    /**
     * Get all encounters for a patient that took place between fromDate and toDate (both nullable and inclusive)
     * @param who
     * @param fromDate
     * @param toDate
     * @return
     */
    public Set<Encounter> getEncounters(Patient who, Date fromDate, Date toDate) {
    	return context.getDAOContext().getEncounterDAO().getEncounters(who, fromDate, toDate);
    }
	
	
}
