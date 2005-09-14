package org.openmrs.api.ibatis;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.MimeType;
import org.openmrs.OrderType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Tribe;
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
	 * @see org.openmrs.api.AdministrationService#createFieldType(org.openmrs.FieldType)
	 */
	public FieldType createFieldType(FieldType fieldType) throws APIException {
		fieldType.setCreator(context.getAuthenticatedUser());
		try {
			try {
				SqlMap.instance().startTransaction();
				
				SqlMap.instance().insert("createFieldType", fieldType);
				
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return fieldType;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createMimeType(org.openmrs.MimeType)
	 */
	public MimeType createMimeType(MimeType mimeType) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				
				SqlMap.instance().insert("createMimeType", mimeType);
				
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
	 * @see org.openmrs.api.AdministrationService#createOrderType(org.openmrs.OrderType)
	 */
	public OrderType createOrderType(OrderType orderType) throws APIException {
		orderType.setCreator(context.getAuthenticatedUser());
		try {
			try {
				SqlMap.instance().startTransaction();
				
				SqlMap.instance().insert("createOrderType", orderType);
				
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return orderType;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createPatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public PatientIdentifierType createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		patientIdentifierType.setCreator(context.getAuthenticatedUser());
		try {
			try {
				SqlMap.instance().startTransaction();
				
				SqlMap.instance().insert("createPatientIdentifierType", patientIdentifierType);
				
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return patientIdentifierType;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createTribe(org.openmrs.Tribe)
	 */
	public Tribe createTribe(Tribe tribe) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				
				SqlMap.instance().insert("createTribe", tribe);
				
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		return tribe;
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

	/**
	 * @see org.openmrs.api.AdministrationService#deleteFieldType(org.openmrs.FieldType)
	 */
	public void deleteFieldType(FieldType fieldType) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				SqlMap.instance().delete("deleteFieldType", fieldType);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteMimeType(org.openmrs.MimeType)
	 */
	public void deleteMimeType(MimeType mimeType) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				SqlMap.instance().delete("deleteMimeType", mimeType);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteOrderType(org.openmrs.OrderType)
	 */
	public void deleteOrderType(OrderType orderType) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				SqlMap.instance().delete("deleteOrderType", orderType);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}

		
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deletePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				SqlMap.instance().delete("deletePatientIdentifierType", patientIdentifierType);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}

		
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteTribe(org.openmrs.Tribe)
	 */
	public void deleteTribe(Tribe tribe) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();
				SqlMap.instance().delete("deleteTribe", tribe);
				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}

		
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
	 * @see org.openmrs.api.AdministrationService#updateFieldType(org.openmrs.FieldType)
	 */
	public void updateFieldType(FieldType fieldType) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();

				if (fieldType.getCreator() == null) {
					this.createFieldType(fieldType);
				} else {
					SqlMap.instance().update("updateFieldType", fieldType);
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
	 * @see org.openmrs.api.AdministrationService#updateMimeType(org.openmrs.MimeType)
	 */
	public void updateMimeType(MimeType mimeType) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();

				//TODO check if mimetype is in the table already
				if (false) {
					this.createMimeType(mimeType);
				} else {
					SqlMap.instance().update("updateMimeType", mimeType);
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
	 * @see org.openmrs.api.AdministrationService#updateOrderType(org.openmrs.OrderType)
	 */
	public void updateOrderType(OrderType orderType) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();

				if (orderType.getCreator() == null) {
					this.createOrderType(orderType);
				} else {
					SqlMap.instance().update("updateOrderType", orderType);
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
	 * @see org.openmrs.api.AdministrationService#updatePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();

				if (patientIdentifierType.getCreator() == null) {
					this.createPatientIdentifierType(patientIdentifierType);
				} else {
					SqlMap.instance().update("updatePatientIdentifierType", patientIdentifierType);
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
	 * @see org.openmrs.api.AdministrationService#updateTribe(org.openmrs.Tribe)
	 */
	public void updateTribe(Tribe tribe) throws APIException {
		try {
			try {
				SqlMap.instance().startTransaction();

				//TODO check if tribe is alread in db
				if (false) {
					this.createTribe(tribe);
				} else {
					SqlMap.instance().update("updateTribe", tribe);
				}

				SqlMap.instance().commitTransaction();
			} finally {
				SqlMap.instance().endTransaction();
			}
		} catch (SQLException e) {
			throw new APIException(e);
		}
		
	}
}
