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
import org.openmrs.api.AdministrationService;
import org.openmrs.context.Context;

/**
 * Ibatis-specific implementation of org.openmrs.api.AdministrationService
 * 
 * @see org.openmrs.api.AdministrationService
 * 
 * @author Ben Wolfe
 * @version 1.0 
 */
public class IbatisAdministrationService implements AdministrationService {

	private final Log log = LogFactory.getLog(getClass());

	private Context context;

	/**
	 * Service must be constructed within a <code>context</code>
	 * 
	 * @param context
	 * @see org.openmrs.context.Context
	 */
	public IbatisAdministrationService(Context context) {
		this.context = context;
	}

	/**
	 * @see org.openmrs.api.EncounterTypeService#createEncounterType(EncounterType)
	 */
	public EncounterType createEncounterType(EncounterType encounterType) throws APIException {

		User authenticatedUser = context.getAuthenticatedUser();
		encounterType.setCreator(authenticatedUser);
		try {
			try {
				SqlMap.instance().startTransaction();
				
				SqlMap.instance().insert("createEncounterType", encounterType);
				
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
	 * @see org.openmrs.api.EncounterTypeService#updateEncounterType(EncounterType)
	 */
	public void updateEncounterType(EncounterType encounterType) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();

				if (encounterType.getCreator() == null) {
					this.createEncounterType(encounterType);
				} else {
					SqlMap.instance().update("updateEncounterType", encounterType);
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
	 * @see org.openmrs.api.EncounterTypeService#deleteEncounterType(EncounterType)
	 */
	public void deleteEncounterType(EncounterType encounterType) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				SqlMap.instance().delete("deleteEncounterType", encounterType);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}
}
