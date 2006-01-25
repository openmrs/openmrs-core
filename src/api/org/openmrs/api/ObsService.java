package org.openmrs.api;

import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.ObsDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * Observation-related services
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class ObsService {
	
	private Context context;
	private DAOContext daoContext;
	
	public ObsService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
	
	private ObsDAO getObsDAO() {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_MANAGE_OBS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_MANAGE_OBS);
		
		return daoContext.getObsDAO();
	}

	/**
	 * Create an observation 
	 * @param Obs
	 * @throws APIException
	 */
	public void createObs(Obs obs) throws APIException {
		getObsDAO().createObs(obs);
	}

	/**
	 * Get an observation
	 * @param integer obsId of observation desired
	 * @return matching Obs
	 * @throws APIException
	 */
	public Obs getObs(Integer obsId) throws APIException {
		return getObsDAO().getObs(obsId);
	}

	/**
	 * Save changes to observation
	 * @param Obs
	 * @throws APIException
	 */
	public void updateObs(Obs obs) throws APIException {
		getObsDAO().updateObs(obs);
	}

	/**
	 * Equivalent to deleting an observation
	 * @param Obs obs to void
	 * @param String reason
	 * @throws APIException
	 */
	public void voidObs(Obs obs, String reason) throws APIException {
		getObsDAO().voidObs(obs, reason);
	}
	
	/**
	 * Revive an observation (pull a Lazarus)
	 * @param Obs
	 * @throws APIException
	 */
	public void unvoidObs(Obs obs) throws APIException {
		getObsDAO().unvoidObs(obs);
	}

	/**
	 * Delete an observation.  SHOULD NOT BE CALLED unless caller is lower-level.
	 * @param Obs
	 * @throws APIException
	 * @see voidObs(Obs)
	 */
	public void deleteObs(Obs obs) throws APIException {
		getObsDAO().deleteObs(obs);
	}
	
	/**
	 * Get all mime types
	 * 
	 * @return mime types list
	 * @throws APIException
	 */
	public List<MimeType> getMimeTypes() throws APIException {
		return getObsDAO().getMimeTypes();
	}

	/**
	 * Get mimeType by internal identifier
	 * 
	 * @param mimeType id
	 * @return mimeType with given internal identifier
	 * @throws APIException
	 */
	public MimeType getMimeType(Integer mimeTypeId) throws APIException {
		return getObsDAO().getMimeType(mimeTypeId);
	}
	
	/**
	 * Get all locations
	 * 
	 * @return location list
	 * @throws APIException
	 */
	public List<Location> getLocations() throws APIException {
		return getObsDAO().getLocations();
	}

	/**
	 * Get location by internal identifier
	 * 
	 * @param location id
	 * @return location with given internal identifier
	 * @throws APIException
	 */
	public Location getLocation(Integer locationId) throws APIException {
		return getObsDAO().getLocation(locationId);
	}
	
	/**
	 * Get all Observations for a patient
	 * @param who
	 * @return
	 */
	public Set<Obs> getObservations(Patient who) {
		return getObsDAO().getObservations(who);
	}

	/**
	 * e.g. get all CD4 counts for a patient 
	 * @param who
	 * @param question
	 * @return
	 */
    public Set<Obs> getObservations(Patient who, Concept question) {
    	return getObsDAO().getObservations(who, question);
    }

    /**
     * Get all observations from a specific encounter
     * @param whichEncounter
     * @return
     */
    public Set<Obs> getObservations(Encounter whichEncounter) {
    	return getObsDAO().getObservations(whichEncounter);
    }
    
    public List<Obs> findObservations(String search, boolean includeVoided) {
    	List<Obs> obs = new Vector<Obs>();
    	for (Patient p : context.getPatientService().getPatientsByIdentifier(search, includeVoided)) {
    		obs.addAll(getObsDAO().findObservations(p.getPatientId(), includeVoided));
    	}
    	try {
    		Integer i = Integer.valueOf(search);
    		if (i != null)
    			obs.addAll(getObsDAO().findObservations(i, includeVoided));
    	}
    	catch (Exception e) {}
    	
    	return obs;
    }
}
