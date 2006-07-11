package org.openmrs.api.db;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.api.PatientSetService;
import org.openmrs.reporting.PatientSet;

public interface PatientSetDAO {

	public String exportXml(PatientSet ps) throws DAOException;
	
	public String exportXml(Integer patientId) throws DAOException;
	
	public PatientSet getAllPatients();
	
	public PatientSet getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate) throws DAOException;
	
	public PatientSet getPatientsHavingNumericObs(Integer conceptId, PatientSetService.Modifier modifier, Number value) throws DAOException;
	
	public PatientSet getPatientsHavingTextObs(Integer conceptId, String value) throws DAOException;
	
	public PatientSet getPatientsHavingLocation(Integer locationId) throws DAOException;
	
	public Map<Integer, String> getShortPatientDescriptions(PatientSet patients) throws DAOException;
	
	public Map<Integer, List<Obs>> getObservations(PatientSet patients, Concept concept) throws DAOException;
	
	public Map<Integer, Encounter> getEncountersByType(PatientSet patients, EncounterType encType);
	
	public Map<Integer, Object> getPatientAttributes(PatientSet patients, String className, String property, boolean returnAll);
	
	public Map<Integer, Map<String, Object>> getCharacteristics(PatientSet patients) throws DAOException;
	
}
