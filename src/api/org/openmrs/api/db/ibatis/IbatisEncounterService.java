package org.openmrs.api.ibatis;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.context.Context;

/**
 * Ibatis-specific implementation of org.openmrs.api.EncounterService
 * 
 * @see org.openmrs.api.EncounterService
 * 
 * @author Ben Wolfe
 * @version 1.0 
 */
public class IbatisEncounterService implements EncounterService {

	private final Log log = LogFactory.getLog(getClass());

	private Context context;

	/**
	 * Service must be constructed within a <code>context</code>
	 * 
	 * @param context
	 * @see org.openmrs.context.Context
	 */
	public IbatisEncounterService(Context context) {
		this.context = context;
	}

	/**
	 * @see org.openmrs.api.EncounterService#createEncounter(Encounter)
	 */
	public Encounter createEncounter(Encounter encounter) throws APIException {

		User authenticatedUser = context.getAuthenticatedUser();
		encounter.setCreator(authenticatedUser);
		if (encounter.getPatient() == null || encounter.getPatient().getPatientId() == null)
			throw new APIException("Patient cannot be null");
		if (encounter.getProvider() == null || encounter.getProvider().getUserId() == null)
			throw new APIException("Provider cannot be null");
		if (encounter.getLocation() == null || encounter.getLocation().getLocationId() == null)
			throw new APIException("Location cannot be null");
		try {
			try {
				SqlMap.instance().startTransaction();
				
				Location loc = encounter.getLocation();
				if (loc != null && loc.getLocationId() == null) {
					loc.setCreator(authenticatedUser);
					SqlMap.instance().insert("createLocation", loc);
				}
				SqlMap.instance().insert("createEncounter", encounter);
				
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return encounter;
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounter(Integer)
	 */
	public Encounter getEncounter(Integer encounterId) throws APIException {
		Encounter encounter;
		try {
			encounter = (Encounter) SqlMap.instance().queryForObject(
					"getEncounter", encounterId);
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return encounter;
	}

	/**
	 * @see org.openmrs.api.EncounterService#updateEncounter(Encounter)
	 */
	public void updateEncounter(Encounter encounter) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();

				if (encounter.getCreator() == null) {
					this.createEncounter(encounter);
				} else {
					SqlMap.instance().update("updateEncounter", encounter);
				}

				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}

	}

	/**
	 * @see org.openmrs.api.EncounterService#deleteEncounter(Encounter)
	 */
	public void deleteEncounter(Encounter encounter) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				if (encounter.getEncounterId() == null)
					throw new APIException("encounterId cannot be null");
				SqlMap.instance().delete("deleteEncounter", encounter.getEncounterId());
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}
	
	public List<EncounterType> getEncounterTypes() throws APIException {
		
		List<EncounterType> encounterTypes;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				encounterTypes = SqlMap.instance().queryForList("getAllEncounterTypes", null);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return encounterTypes;
	}

	public EncounterType getEncounterType(Integer encounterTypeId) throws APIException {

		EncounterType encounterType;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				encounterType = (EncounterType)SqlMap.instance().queryForObject("getEncounterType", encounterTypeId);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return encounterType;
	}

	/**
	 * @see org.openmrs.api.EncounterService#getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws APIException {
		Location location;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				location = (Location)SqlMap.instance().queryForObject("getLocation", locationId);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return location;
	}

	/**
	 * @see org.openmrs.api.EncounterService#getLocations()
	 */
	public List<Location> getLocations() throws APIException {
		List<Location> locations;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				locations = SqlMap.instance().queryForList("getAllLocations", null);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return locations;
	}
	
}
