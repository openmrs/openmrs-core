package org.openmrs.api;

import java.util.Date;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientSetDAO;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.OpenmrsConstants;

public class PatientSetService {

	private Context context;
	private DAOContext daoContext;
	
	public PatientSetService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}

	private PatientSetDAO getPatientSetDAO() {
		if (!context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_PATIENT_SETS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_PATIENT_SETS);
		return daoContext.getPatientSetDAO();
	}

	public PatientSet getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate) throws DAOException {
		return getPatientSetDAO().getPatientsByCharacteristics(gender, minBirthdate, maxBirthdate);
	}
	
	public Map<Integer, String> getShortPatientDescriptions(PatientSet patients) {
		return getPatientSetDAO().getShortPatientDescriptions(patients);
	}
	
}
