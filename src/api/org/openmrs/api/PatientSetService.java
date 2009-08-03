/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Person;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.PatientSetDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface PatientSetService {
	
	public void setPatientSetDAO(PatientSetDAO dao);
	
	/**
	 * Export a set of patients to an XML
	 * 
	 * @param ps The set you want to export as XML
	 * @return an XML representation of this patient-set, including patient characteristics, and
	 *         observations
	 */
	@Transactional(readOnly = true)
	public String exportXml(Cohort ps);
	
	@Transactional(readOnly = true)
	public String exportXml(Integer patientId);
	
	@Transactional(readOnly = true)
	public Cohort getAllPatients() throws DAOException;
	
	@Transactional(readOnly = true)
	public Cohort getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate) throws DAOException;
	
	/**
	 * Get patients by specified gender, birthdate range, age range, and alive status (all optional)
	 * 
	 * @param gender
	 * @param minBirthdate
	 * @param maxBirthdate
	 * @param minAge
	 * @param maxAge
	 * @param aliveOnly
	 * @param deadOnly
	 * @return Cohort with all matching patients
	 * @throws DAOException
	 * @should get all patients when no parameters given
	 * @should get patients of given gender
	 * @should get patients born before date
	 * @should get patients born after date
	 * @should get patients born between dates
	 * @should get patients who are alive
	 * @should get patients who are dead
	 */
	@Transactional(readOnly = true)
	public Cohort getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate, Integer minAge,
	                                           Integer maxAge, Boolean aliveOnly, Boolean deadOnly) throws DAOException;
	
	/**
	 * Get patients by specified gender, birthdate range, age range, and alive status (all optional)
	 * 
	 * @param gender
	 * @param minBirthdate
	 * @param maxBirthdate
	 * @param minAge
	 * @param maxAge
	 * @param aliveOnly
	 * @param deadOnly
	 * @param effectiveDate
	 * @return Cohort with all matching patients
	 * @throws DAOException
	 * @should get all patients when no parameters given
	 * @should get patients of given gender
	 * @should get patients born before date
	 * @should get patients born after date
	 * @should get patients born between dates
	 * @should get patients who are alive
	 * @should get patients who are dead
	 * @should not get patients born after effectiveDate
	 */
	@Transactional(readOnly = true)
	public Cohort getPatientsByCharacteristics(String gender, Date minBirthdate, Date maxBirthdate, Integer minAge,
	                                           Integer maxAge, Boolean aliveOnly, Boolean deadOnly, Date effectiveDate)
	                                                                                                                   throws DAOException;
	
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingNumericObs(Integer conceptId, TimeModifier timeModifier,
	                                          PatientSetService.Modifier modifier, Number value, Date fromDate, Date toDate);
	
	/**
	 * Searches for patients who have observations as described by the arguments to this method
	 * 
	 * @param conceptId
	 * @param timeModifier
	 * @param modifier
	 * @param value
	 * @param fromDate
	 * @param toDate
	 * @return all patients with observations matching the arguments to this method
	 * @should get patients by concept and true boolean value
	 * @should get patients by concept and false boolean value
	 */
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingObs(Integer conceptId, TimeModifier timeModifier, Modifier modifier, Object value,
	                                   Date fromDate, Date toDate);
	
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingEncounters(EncounterType encounterType, Location location, Form form, Date fromDate,
	                                          Date toDate, Integer minCount, Integer maxCount);
	
	/**
	 * Gets patients who have encounters as described by the parameters specified (all optional)
	 * 
	 * @param encounterTypeList
	 * @param location
	 * @param form
	 * @param fromDate
	 * @param toDate
	 * @param minCount
	 * @param maxCount
	 * @return Cohort with matching Patients
	 * @should get all patients with encounters when no parameters specified
	 * @should get patients with encounters of type
	 * @should get patients with encounters of multiple types
	 * @should get patients with encounters at location
	 * @should get patients with encounters from form
	 * @should get patients with encounters before date
	 * @should get patients with encounters after date
	 * @should get patients with encounters between dates
	 * @should get patients with at least n encounters
	 * @should get patients with at most n encounters
	 */
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingEncounters(List<EncounterType> encounterTypeList, Location location, Form form,
	                                          Date fromDate, Date toDate, Integer minCount, Integer maxCount);
	
	/**
	 * Gets patients who are enrolled in the given program or in the given state(s) at the specified
	 * time
	 * 
	 * @param program
	 * @param stateList
	 * @param fromDate
	 * @param toDate
	 * @return Cohort with matching Patients
	 * @should get all patients in any program given null parameters
	 * @should get patients in program
	 * @should get patients in program from date
	 * @should get patients in program to date
	 * @should get patients in program between dates
	 * @should get patients in state
	 * @should get patients in states
	 * @should get patients in state from date
	 * @should get patients in state to date
	 * @should get patients in state between dates
	 */
	@Transactional(readOnly = true)
	public Cohort getPatientsByProgramAndState(Program program, List<ProgramWorkflowState> stateList, Date fromDate,
	                                           Date toDate);
	
	@Transactional(readOnly = true)
	public Cohort getPatientsInProgram(Program program, Date fromDate, Date toDate);
	
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingDateObs(Integer conceptId, Date startTime, Date endTime);
	
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingTextObs(Concept concept, String value, TimeModifier timeModifier);
	
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingTextObs(Integer conceptId, String value, TimeModifier timeModifier);
	
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingLocation(Location loc);
	
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingLocation(Location loc, PatientLocationMethod method);
	
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingLocation(Integer locationId);
	
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingLocation(Integer locationId, PatientLocationMethod method);
	
	/**
	 * Returns a Cohort of patient who had drug orders for a set of drugs active on a certain date.
	 * Can also be used to find patient with no drug orders on that date.
	 * 
	 * @param patientIds Collection of patientIds you're interested in. NULL means all patients.
	 * @param takingIds Collection of drugIds the patient is taking. (Or the empty set to mean
	 *            "any drug" or NULL to mean "no drugs")
	 * @param onDate Which date to look at the patients' drug orders. (NULL defaults to now().)
	 */
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingDrugOrder(Collection<Integer> patientIds, Collection<Integer> takingIds, Date onDate);
	
	/**
	 * Returns a Cohort of patient who had drug orders for a set of drugs active between a pair of
	 * dates. Can also be used to find patient with no drug orders on that date.
	 * 
	 * @param patientIds Collection of patientIds you're interested in. NULL means all patients.
	 * @param drugIds Collection of drugIds the patient is taking. (Or the empty set to mean
	 *            "any drug" or NULL to mean "no drugs")
	 * @param groupMethod whether to do NONE, ALL, or ANY of the list of specified ids.
	 * @param fromDate Beginning of date range to look at (NULL defaults to toDate if that isn't
	 *            null, or now() if it is.)
	 * @param toDate End of date range to look at (NULL defaults to fromDate if that isn't null, or
	 *            now() if it is.)
	 * @return Cohort with matching Patients
	 * @should get all patients with drug orders given null parameters
	 * @should get patients with no drug orders
	 * @should get patients with drug orders for drugs
	 * @should get patients with no drug orders for drugs
	 * @should get patients with drug orders from date
	 * @should get patients with drug orders to date
	 * @should get patients with drug order for drug between dates
	 */
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingDrugOrder(Collection<Integer> patientIds, Collection<Integer> drugIds,
	                                         GroupMethod groupMethod, Date fromDate, Date toDate);
	
	/**
	 * @return A Cohort of patients who had drug order for particular drugs or generics, with start
	 *         dates within a range, with end dates within a range, and a reason for
	 *         discontinuation.
	 */
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingDrugOrder(List<Drug> drug, List<Concept> drugConcept, Date startDateFrom,
	                                         Date startDateTo, Date stopDateFrom, Date stopDateTo, Boolean discontinued,
	                                         List<Concept> discontinuedReason);
	
	/**
	 * At least one of attribute and value must be non-null
	 * 
	 * @param attribute if not null, look for this attribute
	 * @param value if not null, look for this value
	 * @return Cohort of patients who have a person attribute (optionally) with attributeType of
	 *         attribute and (optionally) value of value.
	 */
	@Transactional(readOnly = true)
	public Cohort getPatientsHavingPersonAttribute(PersonAttributeType attribute, String value);
	
	@Transactional(readOnly = true)
	public Map<Integer, String> getShortPatientDescriptions(Collection<Integer> patientIds);
	
	@Transactional(readOnly = true)
	public Map<Integer, List<Obs>> getObservations(Cohort patients, Concept concept);
	
	/**
	 * Date range is inclusive of both endpoints
	 */
	@Transactional(readOnly = true)
	public Map<Integer, List<Obs>> getObservations(Cohort patients, Concept concept, Date fromDate, Date toDate);
	
	/**
	 * TODO write something here
	 * 
	 * @param patients
	 * @param c
	 * @return Map<patientId, List<Obs values>>
	 */
	@Transactional(readOnly = true)
	public Map<Integer, List<List<Object>>> getObservationsValues(Cohort patients, Concept c);
	
	/**
	 * Returns a mapping from patient id to obs for concept <code>c</code>
	 * <p>
	 * The returned List< attribute value > is [obs value, attr value, attr value, attr value...]
	 * The returned List<List< attribute value >> represents the obs rows
	 * 
	 * @param patients
	 * @param c
	 * @param attributes list of attributes
	 * @return <code>Map<patientId, List<List< attribute value >>></code>
	 */
	@Transactional(readOnly = true)
	public Map<Integer, List<List<Object>>> getObservationsValues(Cohort patients, Concept c, List<String> attributes);
	
	/**
	 * TODO write something here
	 * 
	 * @param patients Cohort of patients to search
	 * @param encType the type of the encounter
	 * @return Map<Integer, Encounter> of patientId to encounters matching a specific type
	 */
	@Transactional(readOnly = true)
	public Map<Integer, Encounter> getEncountersByType(Cohort patients, EncounterType encType);
	
	/**
	 * TODO write something here
	 * 
	 * @param patients Cohort of patients to search
	 * @param encTypes List<EncounterType> to include in the search
	 * @param attr <code>String</code> of attributes to get
	 * @return Map<Integer, Object> of encounter attributes
	 */
	public Map<Integer, Object> getEncounterAttrsByType(Cohort patients, List<EncounterType> encTypes, String attr);
	
	/**
	 * TODO write something here
	 * 
	 * @param patients Cohort of patients to search
	 * @param encType List<EncounterType> to include in the search
	 * @return Map<Integer, Encounter> of patientId to encounters matching a specific type
	 */
	@Transactional(readOnly = true)
	public Map<Integer, Encounter> getEncountersByType(Cohort patients, List<EncounterType> encType);
	
	/**
	 * TODO write something here
	 * 
	 * @param patients Cohort of patients to search
	 * @return Map<Integer, Encounter> of all encounters for specified patients.
	 */
	@Transactional(readOnly = true)
	public Map<Integer, Encounter> getEncounters(Cohort patients);
	
	/**
	 * TODO write something here
	 * 
	 * @param patients Cohort of patients to search
	 * @param encType
	 * @return Map<Integer, Encounter> of patientId to first encounters of specified patients, from
	 *         a specific type
	 */
	@Transactional(readOnly = true)
	public Map<Integer, Encounter> getFirstEncountersByType(Cohort patients, EncounterType encType);
	
	/**
	 * TODO write something here
	 * 
	 * @param patients Cohort of patients to search
	 * @param types List<EncounterType> to include in the search
	 * @return Map<Integer, Encounter> of patientId to first encounters of specified patients, from
	 *         a specific list of types
	 */
	@Transactional(readOnly = true)
	public Map<Integer, Encounter> getFirstEncountersByType(Cohort patients, List<EncounterType> types);
	
	/**
	 * TODO write something here
	 * 
	 * @param patients Cohort of patients to search
	 * @param encTypes List<EncounterType> to include in the search
	 * @param attr
	 * @return Map<Integer, Object> of patientId to first encounters properties
	 */
	@Transactional(readOnly = true)
	public Map<Integer, Object> getFirstEncounterAttrsByType(Cohort patients, List<EncounterType> encTypes, String attr);
	
	/**
	 * TODO write something here
	 * 
	 * @param patients Cohort of patients to search
	 * @param className
	 * @param property
	 * @param returnAll
	 * @return Map<Integer, Object> of patientId to patient properties
	 */
	@Transactional(readOnly = true)
	public Map<Integer, Object> getPatientAttributes(Cohort patients, String className, String property, boolean returnAll);
	
	/**
	 * TODO write something here
	 * 
	 * @param patients
	 * @param classNameDotProperty
	 * @param returnAll
	 * @return Map<Integer, Object> of patientId to patient properties
	 */
	@Transactional(readOnly = true)
	public Map<Integer, Object> getPatientAttributes(Cohort patients, String classNameDotProperty, boolean returnAll);
	
	/**
	 * TODO write something here
	 * 
	 * @param patients
	 * @param attributeName
	 * @param joinClass
	 * @param joinProperty
	 * @param outputColumn
	 * @param returnAll
	 * @return Map<Integer, Object> of patientId to person properties
	 */
	@Transactional(readOnly = true)
	public Map<Integer, Object> getPersonAttributes(Cohort patients, String attributeName, String joinClass,
	                                                String joinProperty, String outputColumn, boolean returnAll);
	
	/**
	 * TODO write something here
	 * 
	 * @param patients Cohort of patients to look up
	 * @return Map<Integer,Map<String,Object>> with characteristics of specified patients
	 */
	@Transactional(readOnly = true)
	public Map<Integer, Map<String, Object>> getCharacteristics(Cohort patients);
	
	/**
	 * Gets a map of patient identifiers by identifier type, indexed by patient primary key.
	 * 
	 * @param patients Cohort of patients to look up
	 * @param type PatientIdentifierType to retrieve
	 * @return Map of patient identifiers and PatientIdentifierTypes, for all patients in the
	 *         specified cohort
	 */
	@Transactional(readOnly = true)
	public Map<Integer, PatientIdentifier> getPatientIdentifiersByType(Cohort patients, PatientIdentifierType type);
	
	/**
	 * TODO write something here
	 * 
	 * @param identifiers List of String patient identifiers
	 * @return Cohort of patients matching specified identifiers
	 */
	@Transactional(readOnly = true)
	public Cohort convertPatientIdentifier(List<String> identifiers);
	
	/**
	 * TODO write something here
	 * 
	 * @param patientIds
	 * @return List of matching patients
	 */
	@Transactional(readOnly = true)
	public List<Patient> getPatients(Collection<Integer> patientIds);
	
	@Transactional(readOnly = true)
	public Map<Integer, PatientState> getCurrentStates(Cohort ps, ProgramWorkflow wf);
	
	@Transactional(readOnly = true)
	public Map<Integer, PatientProgram> getCurrentPatientPrograms(Cohort ps, Program program);
	
	@Transactional(readOnly = true)
	public Map<Integer, PatientProgram> getPatientPrograms(Cohort ps, Program program);
	
	@Transactional(readOnly = true)
	public Map<Integer, List<Relationship>> getRelationships(Cohort ps, RelationshipType relType);
	
	@Transactional(readOnly = true)
	public Map<Integer, List<Person>> getRelatives(Cohort ps, RelationshipType relType, boolean forwards);
	
	/**
	 * @return all active drug orders whose drug concept is in the given set (or all drugs if that's
	 *         null)
	 */
	@Transactional(readOnly = true)
	public Map<Integer, List<DrugOrder>> getCurrentDrugOrders(Cohort ps, Concept drugSet);
	
	/**
	 * @return all active or finished drug orders whose drug concept is in the given set (or all
	 *         drugs if that's null)
	 * @should return an empty list if cohort is empty
	 */
	@Transactional(readOnly = true)
	public Map<Integer, List<DrugOrder>> getDrugOrders(Cohort ps, Concept drugSet);
	
	/**
	 * Gets a list of encounters associated with the given form, filtered by the given patient set.
	 * 
	 * @param patients Cohort of the patients to filter by (null will return all encounters for all
	 *            patients)
	 * @param form List<Form> of the forms to filter by
	 */
	@Transactional(readOnly = true)
	public List<Encounter> getEncountersByForm(Cohort patients, List<Form> form);
	
	public enum Modifier {
		LESS_THAN("<"), LESS_EQUAL("<="), EQUAL("="), GREATER_EQUAL(">="), GREATER_THAN(">");
		
		public final String sqlRep;
		
		Modifier(String sqlRep) {
			this.sqlRep = sqlRep;
		}
		
		public String getSqlRepresentation() {
			return sqlRep;
		}
	}
	
	public enum TimeModifier {
		ANY, NO, FIRST, LAST, MIN, MAX, AVG;
	}
	
	public enum BooleanOperator {
		AND, OR, NOT;
	}
	
	// probably should combine this with TimeModifier
	public enum GroupMethod {
		ANY, ALL, NONE;
	}
	
	public enum PatientLocationMethod {
		EARLIEST_ENCOUNTER, LATEST_ENCOUNTER, ANY_ENCOUNTER, PATIENT_HEALTH_CENTER
	}
	
}
