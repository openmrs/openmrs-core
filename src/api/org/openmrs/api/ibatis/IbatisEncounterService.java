package org.openmrs.api.ibatis;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
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
		try {
			try {
				SqlMap.instance().startTransaction();
				SqlMap.instance().insert("createEncounter", encounter);
				SqlMap.instance().insert("createLocation", encounter.getLocation());
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
					SqlMap.instance().update("updateLocation", encounter.getLocation());
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
				SqlMap.instance().delete("deleteEncounter", encounter);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

}
