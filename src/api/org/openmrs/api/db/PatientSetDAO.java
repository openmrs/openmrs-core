package org.openmrs.api.db;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.reporting.PatientSet;

public interface PatientSetDAO {

	public String exportXml(PatientSet ps) throws DAOException;
	
	public String exportXml(Integer patientId) throws DAOException;
	
	public PatientSet getAllPatients();
	
	public PatientSet getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate, Integer minAge, Integer maxAge, Boolean aliveOnly, Boolean deadOnly) throws DAOException;
	
	public PatientSet getPatientsHavingDateObs(Integer conceptId, Date startTime, Date endTime);
	
	public PatientSet getPatientsHavingNumericObs(Integer conceptId, TimeModifier timeModifier, PatientSetService.Modifier modifier, Number value, Date fromDate, Date toDate) throws DAOException;
	
	public PatientSet getPatientsHavingTextObs(Integer conceptId, String value) throws DAOException;
	
	public PatientSet getPatientsHavingLocation(Integer locationId) throws DAOException;
	
	public Map<Integer, String> getShortPatientDescriptions(Collection<Integer> patientIds) throws DAOException;
	
	public Map<Integer, List<Obs>> getObservations(PatientSet patients, Concept concept, Date fromDate, Date toDate) throws DAOException;
	
	public Map<Integer, List<Object>> getObservationsValues(PatientSet patients, Concept c, String attribute);
	
	public Map<Integer, Encounter> getEncountersByType(PatientSet patients, EncounterType encType);
	
	public Map<Integer, Encounter> getFirstEncountersByType(PatientSet patients, EncounterType encType);
	
	public Map<Integer, Object> getPatientAttributes(PatientSet patients, String className, String property, boolean returnAll);
	
	public Map<Integer, Map<String, Object>> getCharacteristics(PatientSet patients) throws DAOException;

	public PatientSet convertPatientIdentifier(List<String> identifiers) throws DAOException;
	
	public List<Patient> getPatients(Collection<Integer> patientIds) throws DAOException;

	public Map<Integer, Collection<Integer>> getActiveDrugIds(Collection<Integer> patientIds, Date onDate) throws DAOException;

	public Map<Integer, PatientState> getCurrentStates(PatientSet ps, ProgramWorkflow wf) throws DAOException;

	public Map<Integer, PatientProgram> getPatientPrograms(PatientSet ps, Program program, boolean includeVoided, boolean includePast) throws DAOException;

	public Map<Integer, List<DrugOrder>> getCurrentDrugOrders(PatientSet ps, List<Concept> drugConcepts) throws DAOException;
	
	public Map<Integer, List<DrugOrder>> getDrugOrders(PatientSet ps, List<Concept> drugConcepts) throws DAOException;
	
	public Map<Integer, List<Relationship>> getRelationships(PatientSet ps, RelationshipType relType) throws DAOException;
	
}
