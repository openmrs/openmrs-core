package org.openmrs.api.db.ibatis;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ComplexObs;
import org.openmrs.EncounterType;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.api.db.APIException;
import org.openmrs.api.db.ObsService;
import org.openmrs.api.context.Context;

/**
 * Ibatis-specific implementation of org.openmrs.api.db.ObsService
 * 
 * @see org.openmrs.api.db.ObsService
 * 
 * @author Ben Wolfe
 * @version 1.0 
 */
public class IbatisObsService implements ObsService {

	private final Log log = LogFactory.getLog(getClass());

	private Context context;

	/**
	 * Service must be constructed within a <code>context</code>
	 * 
	 * @param context
	 * @see org.openmrs.api.context.Context
	 */
	public IbatisObsService(Context context) {
		this.context = context;
	}

	/**
	 * @see org.openmrs.api.db.ObsService#createObs(Obs)
	 */
	public void createObs(Obs obs) throws APIException {

		User authenticatedUser = context.getAuthenticatedUser();
		obs.setCreator(authenticatedUser);
		try {
			try {
				SqlMap.instance().startTransaction();
				SqlMap.instance().insert("createObs", obs);
				if (obs.isComplexObs())
					SqlMap.instance().insert("createComplexObs", obs);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getObs(Integer)
	 */
	public Obs getObs(Integer obsId) throws APIException {
		Obs obs;
		ComplexObs complexObs; 
		try {
			obs = (Obs) SqlMap.instance().queryForObject("getObs", obsId);
			complexObs = (ComplexObs) SqlMap.instance().queryForObject("getComplexObs", obsId);
			//if it is a complex observation, use it
			if (complexObs != null)
				obs = complexObs;

		} catch (SQLException e) {
			throw new APIException(e);
		}
		return obs;
	}

	/**
	 * @see org.openmrs.api.db.ObsService#updateObs(Obs)
	 */
	public void updateObs(Obs obs) throws APIException {
		try {
			try {
				if (obs.getCreator() == null) {
					this.createObs(obs);
					return;
				}
				SqlMap.instance().startTransaction();
				
					SqlMap.instance().update("updateObs", obs);
					if (obs.isComplexObs())
						SqlMap.instance().update("updateComplexObs", obs);
				
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}

	}

	/**
	 * @see org.openmrs.api.db.ObsService#voidObs(Obs)
	 */
	public void voidObs(Obs obs, String reason) throws APIException {
		try {
			try {
				obs.setVoidReason(reason);
				obs.setVoidedBy(context.getAuthenticatedUser());
				SqlMap.instance().startTransaction();
				SqlMap.instance().update("voidObs", obs);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.ObsService#unVoidObs(Obs)
	 */
	public void unvoidObs(Obs obs) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				SqlMap.instance().update("unvoidObs", obs);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.ObsService#deleteObs(Obs)
	 */
	public void deleteObs(Obs obs) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				if (obs.isComplexObs())
					SqlMap.instance().insert("deleteComplexObs", obs.getObsId());
				SqlMap.instance().delete("deleteObs", obs.getObsId());
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getMimeType(java.lang.Integer)
	 */
	public MimeType getMimeType(Integer mimeTypeId) throws APIException {

		MimeType mimeType;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				mimeType = (MimeType)SqlMap.instance().queryForObject("getMimeType", mimeTypeId);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return mimeType;
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getMimeTypes()
	 */
	public List<MimeType> getMimeTypes() throws APIException {
		
		List<MimeType> mimeTypes;
		
		try {
			try {
				SqlMap.instance().startTransaction();
				mimeTypes = SqlMap.instance().queryForList("getAllMimeTypes", null);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
		return mimeTypes;
	}

	
}
