package org.openmrs.api;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
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
		return daoContext.getEncounterDAO();
	}
	/**
	 * Creates a new encounter
	 * 
	 * @param encounter to be created
	 * @throws APIException
	 */
	public void createEncounter(Encounter encounter) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_ADD_ENCOUNTERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_ADD_ENCOUNTERS);
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_ENCOUNTERS);
		
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
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_ENCOUNTERS);
		
		return getEncounterDAO().getEncountersByPatientId(patientId, includeVoided);
	}
	
	/**
	 * Get all encounter types
	 * 
	 * @return encounter types list
	 * @throws APIException
	 */
	public List<EncounterType> getEncounterTypes() throws APIException {
		if (!context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
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
		if (!context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getEncounterDAO().getEncounterType(encounterTypeId);
	}
	
	/**
	 * Get encounterType by name
	 * 
	 * @param encounterType string
	 * @return EncounterType
	 * @throws APIException
	 */
	public EncounterType getEncounterType(String name) throws APIException {
		if (!context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getEncounterDAO().getEncounterType(name);
	}

	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws APIException
	 */
	public List<Location> getLocations() throws APIException {
		if (!context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
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
		if (!context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getEncounterDAO().getLocation(locationId);
	}

	/**
	 * Get location by name
	 * 
	 * @param name location's name
	 * @return location with given name
	 * @throws APIException
	 */
	public Location getLocationByName(String name) throws APIException {
		if (!context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getEncounterDAO().getLocationByName(name);
	}
	
	/**
	 * Search for locations by name.  Matches returned match the given string at 
	 * the beginning of the name
	 * 
	 * @param name location's name
	 * @return list of locations with similar name
	 * @throws APIException
	 */
	public List<Location> findLocations(String name) throws APIException {
		if (!context.isAuthenticated())
			throw new APIAuthenticationException("Authentication required");
		
		return getEncounterDAO().findLocations(name);
	}
	
	/**
	 * Save changes to encounter.  
	 * Automatically applys encounter.patient to all encounter.obs.patient
	 * 
	 * @param encounter
	 * @throws APIException
	 */
	public void updateEncounter(Encounter encounter) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_ENCOUNTERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_ENCOUNTERS);

		getEncounterDAO().updateEncounter(encounter);
		
		// Our data model duplicates the patient column to allow for observations to 
		//   not have to look up the parent Encounter to find the patient
		// Therefore, encounter.patient must always equal encounter.observations[0-n].patient
		Patient p = encounter.getPatient();
		for (Obs obs : daoContext.getObsDAO().getObservations(encounter)) {
			obs.setPatient(p);
			daoContext.getObsDAO().updateObs(obs);
		}
	}
	
	/**
	 * Delete encounter from database.
	 * 
	 * For super users only. If dereferencing encounters, use
	 * <code>voidEncounter(org.openmrs.Encounter)</code>
	 * 
	 * @param encounter encounter object to be deleted 
	 */
	public void deleteEncounter(Encounter encounter) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_DELETE_ENCOUNTERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_DELETE_ENCOUNTERS);
		getEncounterDAO().deleteEncounter(encounter);
	}
	
	/**
	 * Voiding a encounter essentially removes it from circulation
	 * 
	 * @param Encounter
	 *            encounter
	 * @param String
	 *            reason
	 */
	public void voidEncounter(Encounter encounter, String reason) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_ENCOUNTERS))
			throw new APIAuthenticationException("Privilege required: "
					+ OpenmrsConstants.PRIV_EDIT_ENCOUNTERS);

		if (reason == null)
			reason = "";
		
		ObsService os = context.getObsService();
		for (Obs o : encounter.getObs()) {
			if (!o.isVoided()) {
				os.voidObs(o, reason);
			}
		}
		
		encounter.setVoided(true);
		encounter.setVoidedBy(context.getAuthenticatedUser());
		encounter.setDateVoided(new Date());
		encounter.setVoidReason(reason);
		updateEncounter(encounter);
	}
	
	/**
	 * Unvoid encounter record 
	 * 
	 * @param encounter encounter to be revived
	 */
	public void unvoidEncounter(Encounter encounter) throws APIException {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_EDIT_ENCOUNTERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_EDIT_ENCOUNTERS);
		
		String voidReason = encounter.getVoidReason();
		if (voidReason == null)
			voidReason = "";
		
		ObsService os = context.getObsService();
		for (Obs o : encounter.getObs()) {
			if (voidReason.equals(o.getVoidReason()))
				os.unvoidObs(o);
		}
		
		encounter.setVoided(false);
		encounter.setVoidedBy(null);
		encounter.setDateVoided(null);
		encounter.setVoidReason(null);
		updateEncounter(encounter);
	}
	
	/**
	 * All unvoided encounters for a patient
	 * @param who
	 * @return
	 */
	public Set<Encounter> getEncounters(Patient who) {
		return getEncounters(who, false);
	}
	
	/**
	 * All encounters for a patient
	 * @param who
	 * @param includeVoided
	 * @return
	 */
	public Set<Encounter> getEncounters(Patient who, boolean includeVoided) {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_ENCOUNTERS);
		
		return getEncounterDAO().getEncounters(who, includeVoided);
	}

	/**
	 * Get all encounters for a patient that took place at a specific location
	 * @param who
	 * @param where
	 * @return
	 */
    public Set<Encounter> getEncounters(Patient who, Location where) {
    	if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_ENCOUNTERS);
		
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
    	if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_ENCOUNTERS);
		
		return getEncounterDAO().getEncounters(who, fromDate, toDate);
    }
    
    /**
     * Get all encounters that took place between fromDate and toDate (both nullable and inclusive)
     * @param fromDate
     * @param toDate
     * @return
     */
    public Collection<Encounter> getEncounters(Date fromDate, Date toDate) {
    	if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_ENCOUNTERS);
		
		return getEncounterDAO().getEncounters(fromDate, toDate);
    }
	
    /**
     * Get all encounters that took place between fromDate and toDate (both nullable and inclusive)
     * at the given location
     * @param loc Location
     * @param fromDate
     * @param toDate
     * @return
     */
    public Collection<Encounter> getEncounters(Location loc, Date fromDate, Date toDate) {
    	if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ENCOUNTERS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_ENCOUNTERS);
		
		return getEncounterDAO().getEncounters(loc, fromDate, toDate);
    }
}
