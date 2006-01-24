package org.openmrs.api;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.EncounterDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * Encounter-related services
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class EncounterService {
	
	private Context context;
	private DAOContext daoContext;

	public EncounterService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
	
	private EncounterDAO getEncounterDAO() {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_ENC))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_ENC);
		
		return daoContext.getEncounterDAO();
	}
	/**
	 * Creates a new encounter
	 * 
	 * @param encounter to be created
	 * @throws APIException
	 */
	public void createEncounter(Encounter encounter) throws APIException {
		getEncounterDAO().createEncounter(encounter);
	}

	/**
	 * Get encounter by internal identifier
	 * 
	 * @param encounterId encounter id
	 * @return encounter with given internal identifier
	 * @throws APIException
	 */
	public Encounter getEncounter(Integer encounterId) throws APIException {
		return getEncounterDAO().getEncounter(encounterId);
	}
	
	/**
	 * 
	 * @param identifier
	 * @param includeVoided
	 * @return all encounters for the given patient identifer
	 * @throws APIException
	 */
	public List<Encounter> getEncountersByPatientIdentifier(String identifier, boolean includeVoided) throws APIException {
		List<Encounter> encs = new Vector<Encounter>();
		for(Patient p : daoContext.getPatientDAO().getPatientsByIdentifier(identifier, includeVoided)) {
			encs.addAll(getEncountersByPatientId(p.getPatientId(), includeVoided));
		}
		return encs;
	}
	
	/**
	 * 
	 * @param patientId
	 * @param includeVoided
	 * @return all encounters for the given patient identifer
	 * @throws APIException
	 */
	public List<Encounter> getEncountersByPatientId(Integer patientId, boolean includeVoided) throws APIException {
		return getEncounterDAO().getEncountersByPatientId(patientId, includeVoided);
	}
	
	/**
	 * Get all encounter types
	 * 
	 * @return encounter types list
	 * @throws APIException
	 */
	public List<EncounterType> getEncounterTypes() throws APIException {
		return getEncounterDAO().getEncounterTypes();
	}

	/**
	 * Get encounterType by internal identifier
	 * 
	 * @param encounterType id
	 * @return encounterType with given internal identifier
	 * @throws APIException
	 */
	public EncounterType getEncounterType(Integer encounterTypeId) throws APIException {
		return getEncounterDAO().getEncounterType(encounterTypeId);
	}

	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws APIException
	 */
	public List<Location> getLocations() throws APIException {
		return getEncounterDAO().getLocations();
	}

	/**
	 * Get location by internal identifier
	 * 
	 * @param location id
	 * @return location with given internal identifier
	 * @throws APIException
	 */
	public Location getLocation(Integer locationId) throws APIException {
		return getEncounterDAO().getLocation(locationId);
	}

	/**
	 * Save changes to encounter
	 * @param encounter
	 * @throws APIException
	 */
	public void updateEncounter(Encounter encounter) throws APIException {
		getEncounterDAO().updateEncounter(encounter);
	}
	
	/**
	 * Delete encounter from database.
	 * 
	 * @param encounter encounter object to be deleted 
	 */
	public void deleteEncounter(Encounter encounter) throws APIException {
		getEncounterDAO().deleteEncounter(encounter);
	}
	
	/**
	 * all encounters for a patient
	 * @param who
	 * @return
	 */
	public Set<Encounter> getEncounters(Patient who) {
		return getEncounterDAO().getEncounters(who);
	}

	/**
	 * Get all encounters for a patient that took place at a specific location
	 * @param who
	 * @param where
	 * @return
	 */
    public Set<Encounter> getEncounters(Patient who, Location where) {
    	return getEncounterDAO().getEncounters(who, where);
    }

    /**
     * Get all encounters for a patient that took place between fromDate and toDate (both nullable and inclusive)
     * @param who
     * @param fromDate
     * @param toDate
     * @return
     */
    public Set<Encounter> getEncounters(Patient who, Date fromDate, Date toDate) {
    	return getEncounterDAO().getEncounters(who, fromDate, toDate);
    }
	
	
}
