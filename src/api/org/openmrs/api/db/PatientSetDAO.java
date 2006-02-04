package org.openmrs.api.db;

import java.util.Date;
import java.util.Map;

import org.openmrs.reporting.PatientSet;

public interface PatientSetDAO {

	public PatientSet getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate) throws DAOException;
	
	public Map<Integer, String> getShortPatientDescriptions(PatientSet patients) throws DAOException;
	
}
