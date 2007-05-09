package org.openmrs.api.db;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.PatientLocationMethod;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.reporting.PatientSet;

public interface PatientSetDAO {

	public String exportXml(PatientSet ps) throws DAOException;
	
	public String exportXml(Integer patientId) throws DAOException;
	
	public PatientSet getAllPatients();
	
	public PatientSet getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate, Integer minAge, Integer maxAge, Boolean aliveOnly, Boolean deadOnly) throws DAOException;
	
	public PatientSet getPatientsHavingDateObs(Integer conceptId, Date startTime, Date endTime);
	
	public PatientSet getPatientsHavingNumericObs(Integer conceptId, TimeModifier timeModifier, PatientSetService.Modifier modifier, Number value, Date fromDate, Date toDate) throws DAOException;
	
	public PatientSet getPatientsHavingObs(Integer conceptId, TimeModifier timeModifier, PatientSetService.Modifier modifier, Object value, Date fromDate, Date toDate) throws DAOException;
	
	public PatientSet getPatientsHavingEncounters(EncounterType encounterType, Location location, Date fromDate, Date toDate, Integer minCount, Integer maxCount) throws DAOException;
	
	public PatientSet getPatientsByProgramAndState(Program program, ProgramWorkflowState state, Date fromDate, Date toDate) throws DAOException;
	
	public PatientSet getPatientsInProgram(Integer programId, Date fromDate, Date toDate) throws DAOException;
	
	public PatientSet getPatientsHavingTextObs(Integer conceptId, String value, TimeModifier timeModifier) throws DAOException;
	
	public PatientSet getPatientsHavingLocation(Integer locationId, PatientLocationMethod method) throws DAOException;
	
	public Map<Integer, String> getShortPatientDescriptions(Collection<Integer> patientIds) throws DAOException;
	
	public Map<Integer, List<Obs>> getObservations(PatientSet patients, Concept concept, Date fromDate, Date toDate) throws DAOException;
	
	public Map<Integer, List<List<Object>>> getObservationsValues(PatientSet patients, Concept c, List<String> attributes);
	
	public Map<Integer, Encounter> getEncountersByType(PatientSet patients, List<EncounterType> encType);
	
	public Map<Integer, Object> getEncounterAttrsByType(PatientSet patients, List<EncounterType> encTypes, String attr, Boolean earliestFirst);
		
	public Map<Integer, Encounter> getEncounters(PatientSet patients);
	
	public Map<Integer, Encounter> getFirstEncountersByType(PatientSet patients, List<EncounterType> encType);
	
	public Map<Integer, Object> getPatientAttributes(PatientSet patients, String className, String property, boolean returnAll);
	
	public Map<Integer, Map<String, Object>> getCharacteristics(PatientSet patients) throws DAOException;

	public PatientSet convertPatientIdentifier(List<String> identifiers) throws DAOException;
	
	public List<Patient> getPatients(Collection<Integer> patientIds) throws DAOException;

	public Map<Integer, Collection<Integer>> getActiveDrugIds(Collection<Integer> patientIds, Date fromDate, Date toDate) throws DAOException;

	public Map<Integer, PatientState> getCurrentStates(PatientSet ps, ProgramWorkflow wf) throws DAOException;

	public Map<Integer, PatientProgram> getPatientPrograms(PatientSet ps, Program program, boolean includeVoided, boolean includePast) throws DAOException;

	public Map<Integer, List<DrugOrder>> getCurrentDrugOrders(PatientSet ps, List<Concept> drugConcepts) throws DAOException;
	
	public Map<Integer, List<DrugOrder>> getDrugOrders(PatientSet ps, List<Concept> drugConcepts) throws DAOException;
	
	public Map<Integer, List<Relationship>> getRelationships(PatientSet ps, RelationshipType relType) throws DAOException;
	
	public Map<Integer, Object> getPersonAttributes(PatientSet patients, String attributeName, String joinClass, String joinProperty, String outputColumn, boolean returnAll);
	
	public PatientSet getPatientsHavingPersonAttribute(PersonAttributeType attribute, String value);
	
	public PatientSet getPatientsHavingDrugOrder(List<Drug> drugList, List<Concept> drugConceptList, Date startDateFrom, Date startDateTo, Date stopDateFrom, Date stopDateTo, Boolean discontinued, List<Concept> discontinuedReason);
	
}
