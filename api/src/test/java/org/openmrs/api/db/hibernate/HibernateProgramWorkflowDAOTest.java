/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class HibernateProgramWorkflowDAOTest extends BaseContextSensitiveTest {
	@Autowired
	private HibernateProgramWorkflowDAO hibernateProgramWorkflowDAO;

	private static final String PATIENT_SET = "org/openmrs/api/db/hibernate/include/HibernateProgramWorkflowDAOTest-patients.xml";
	private static final String CONCEPT_SET = "org/openmrs/api/db/hibernate/include/HibernateProgramWorkflowDAOTest-concepts.xml";
	private static final String PROGRAM_SET = "org/openmrs/api/db/hibernate/include/HibernateProgramWorkflowDAOTest-programs.xml";
	private static final String PROGRAM_WORKFLOW_SET = "org/openmrs/api/db/hibernate/include/HibernateProgramWorkflowDAOTest-workflows.xml";

	@Before
	public void setUp() {
		executeDataSet(PATIENT_SET);
		executeDataSet(CONCEPT_SET);
		executeDataSet(PROGRAM_SET);
		executeDataSet(PROGRAM_WORKFLOW_SET);
	}

	/**
	 * @see HibernateProgramWorkflowDAO#saveProgram(Program)
	 */
    @Test
    public void saveProgram_shouldCreateANewProgramIfItDoesNotExist() {
		Program program = new Program();
		program.setProgramId(500);
		program.setDescription("program-description");
		program.setName("some-program");
		
		hibernateProgramWorkflowDAO.saveProgram(program);
		
		Program retrievedProgram = hibernateProgramWorkflowDAO.getProgram(500);
		assertEquals(program.getName(), retrievedProgram.getName());
    }

	/**
	 * @see HibernateProgramWorkflowDAO#saveProgram(Program)
	 */
    @Test
	public void saveProgram_shouldUpdateAnExistingProgram() {
		Program program = new Program();
		program.setProgramId(500);
		program.setDescription("program-description");
		program.setName("some-program");

		hibernateProgramWorkflowDAO.saveProgram(program);
		String updatedDescription = "description-changed";
		program.setName(updatedDescription);
		hibernateProgramWorkflowDAO.saveProgram(program);

		Program retrievedProgram = hibernateProgramWorkflowDAO.getProgram(500);
		assertEquals(updatedDescription, retrievedProgram.getName());
	}

	/**
	 * @see HibernateProgramWorkflowDAO#getProgram(Integer)
	 */
	@Test
    public void getProgram_shouldGetAProgramByIdIfExists() {
    	Program program = hibernateProgramWorkflowDAO.getProgram(1);
    	
    	assertNotNull(program);
    }

	/**
	 * @see HibernateProgramWorkflowDAO#getProgram(Integer)
	 */
    @Test
	public void getProgram_shouldReturnNullIfProgramWithIdDoesNotExist() {
		Program program = hibernateProgramWorkflowDAO.getProgram(500);

		assertNull(program);
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getAllPrograms(boolean)
	 */
	@Test
    public void getAllPrograms_shouldReturnAllProgramsIfIncludeRetiredFlagIsTrue() {
    	List<Program> programList = hibernateProgramWorkflowDAO.getAllPrograms(true);
    	
    	assertEquals(6, programList.size());
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getAllPrograms(boolean)
	 */
    @Test
	public void getAllPrograms_shouldReturnOnlyActiveProgramsIfRetiredFlagIsFalse() {
		List<Program> programList = hibernateProgramWorkflowDAO.getAllPrograms(false);

		assertEquals(3, programList.size());
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getProgramsByName(String, boolean)
	 */
	@Test
    public void getProgramsByName_shouldReturnAllProgramsMatchingTheNameIfIncludeRetiredFlagIsTrue() {
    	List<Program> programList = hibernateProgramWorkflowDAO.getProgramsByName("test-program-1", true);
    	
    	assertEquals(2, programList.size());
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getProgramsByName(String, boolean)
	 */
    @Test
	public void getProgramsByName_shouldReturnOnlyNonRetiredProgramsMatchingTheNameIfIncludeRetiredFlagIsFalse() {
		List<Program> programList = hibernateProgramWorkflowDAO.getProgramsByName("test-program-1", false);

		assertEquals(1, programList.size());
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getProgramsByName(String, boolean)
	 */
	@Test
	public void getProgramsByName_shouldReturnEmptyIfNameDoesNotMatchAnyOfThePrograms() {
		List<Program> programList = hibernateProgramWorkflowDAO.getProgramsByName("random-program-1", true);

		assertEquals(0, programList.size());
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#findPrograms(java.lang.String)
	 */
	@Test
    public void findPrograms_shouldReturnAllProgramsContainingTheMatchingText() {
		List<Program> programList = hibernateProgramWorkflowDAO.findPrograms("test");
		
		assertEquals(3, programList.size());
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#findPrograms(java.lang.String)
	 */
    @Test
	public void findPrograms_shouldReturnAnEmptyListIfNoneOfTheProgramsContainTheMatchingText() {
		List<Program> programList = hibernateProgramWorkflowDAO.findPrograms("xyz");

		assertEquals(0, programList.size());
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#deleteProgram(org.openmrs.Program)
	 */
	@Test
    public void deleteProgram_shouldDeleteTheProgramBeingPassedToIt() {
		Program program = hibernateProgramWorkflowDAO.getProgram(21);
		
		hibernateProgramWorkflowDAO.deleteProgram(program);
		
		assertNull(hibernateProgramWorkflowDAO.getProgram(21));
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#savePatientProgram(org.openmrs.PatientProgram)
	 */
	@Test
    public void savePatientProgram_shouldCreateAPatientProgramIfItDoesNotExist() {
		Person person = new Person();
		person.setPersonId(101);
		
		Patient patient = new Patient();
		patient.setPatientId(101);
		patient.setPersonId(101);
		
		Program program = new Program();
		program.setName("program");
		program.setProgramId(500);
		
		PatientProgram patientProgram = new PatientProgram();
		patientProgram.setPatientProgramId(1);
		patientProgram.setPatient(patient);
		patientProgram.setProgram(program);
		
		hibernateProgramWorkflowDAO.savePatientProgram(patientProgram);
		
		assertNotNull(hibernateProgramWorkflowDAO.getPatientProgram(1));
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getPatientProgram(java.lang.Integer)
	 */
	@Test
    public void getPatientProgram_shouldGetPatientProgramByIdIfExists() {
		PatientProgram patientProgram = hibernateProgramWorkflowDAO.getPatientProgram(1);
		
		assertNotNull(patientProgram);
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getPatientProgram(java.lang.Integer)
	 */
    @Test
	public void getPatientProgram_shouldReturnNullWhenThereIsNoPatientProgramWithSuppliedId() {
		PatientProgram patientProgram = hibernateProgramWorkflowDAO.getPatientProgram(500);

		assertNull(patientProgram);
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getPatientPrograms(Patient, Program, Date, Date,
	 *      Date, Date, boolean)
	 */
	@Test
    public void getPatientPrograms_shouldReturnAllPatientProgramsIfNoOtherFiltersApplied() {
		Patient patient = new Patient();
		patient.setPatientId(2001);
		
		Program program = new Program();
		program.setProgramId(21);
		
		List<PatientProgram> patientPrograms = hibernateProgramWorkflowDAO.getPatientPrograms(patient, program, null, null, null, null, true);
		
		assertEquals(3, patientPrograms.size());
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getPatientPrograms(Patient, Program, Date, Date,
	 *      Date, Date, boolean)
	 */
	@Test
	public void getPatientPrograms_shouldReturnNonVoidedPatientProgramsIfIncludeVoideFlagIsFalse() {
		Patient patient = new Patient();
		patient.setPatientId(2001);

		Program program = new Program();
		program.setProgramId(21);

		List<PatientProgram> patientPrograms = hibernateProgramWorkflowDAO.getPatientPrograms(patient, program, null, null, null, null, false);

		assertEquals(2, patientPrograms.size());
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getPatientPrograms(Patient, Program, Date, Date,
	 *      Date, Date, boolean)
	 */
	@Test
	public void getPatientPrograms_shouldReturnAllPatientProgramsBetweenTheRangeOfSpecifiedEnrollmentAndCompletionDate() throws ParseException {
		Patient patient = new Patient();
		patient.setPatientId(2001);

		Program program = new Program();
		program.setProgramId(21);

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date minEnrollmentDate = dateFormat.parse("2019-01-01");
		Date maxEnrollmentDate = dateFormat.parse("2019-12-31");
		Date minCompletionDate = dateFormat.parse("2020-01-01");
		Date maxCompletionDate = dateFormat.parse("2020-12-01");
		List<PatientProgram> patientPrograms = hibernateProgramWorkflowDAO.getPatientPrograms(patient, program, minEnrollmentDate, maxEnrollmentDate, minCompletionDate, maxCompletionDate, true);

		assertEquals(1, patientPrograms.size());
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getPatientPrograms(org.openmrs.Cohort,
	 *      java.util.Collection)
	 */
	@Test
    public void getPatientPrograms_shouldReturnAllThePatientProgramsForPatientsBelongingToCohort() {
		Cohort cohort = new Cohort("patients", "patients", new Integer[]{ 2001 });
		
		List<Program> programList = new ArrayList<>();
		Program program = new Program();
		program.setProgramId(21);
		programList.add(program);
		
		List<PatientProgram> patientPrograms = hibernateProgramWorkflowDAO.getPatientPrograms(cohort, programList);
		
		assertEquals(3, patientPrograms.size());
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getPatientPrograms(org.openmrs.Cohort,
	 *      java.util.Collection)
	 */
    @Test
	public void getPatientPrograms_shouldReturnAllThePatientProgramsForAGivenListOfProgramsIfCohortIsNull() {
		List<Program> programList = new ArrayList<>();
		Program program = new Program();
		program.setProgramId(21);
		programList.add(program);

		List<PatientProgram> patientPrograms = hibernateProgramWorkflowDAO.getPatientPrograms(null, programList);

		assertEquals(3, patientPrograms.size());
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getPatientPrograms(org.openmrs.Cohort,
	 *      java.util.Collection)
	 */
	@Test
	public void getPatientPrograms_shouldReturnAllThePatientProgramsIfBothCohortAndProgramsAreNull() {
		List<PatientProgram> patientPrograms = hibernateProgramWorkflowDAO.getPatientPrograms(null, null);
		
		assertEquals(5, patientPrograms.size());
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#deletePatientProgram(org.openmrs.PatientProgram)
	 */
	@Test
    public void deletePatientProgram_shouldDeleteThePatientProgramBeingPassed() throws ParseException {
		PatientProgram patientProgram = new PatientProgram();
		patientProgram.setPatientProgramId(1);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date creationDate = dateFormat.parse("2013-11-06");
		patientProgram.setDateCreated(creationDate);
		
		hibernateProgramWorkflowDAO.deletePatientProgram(patientProgram);
		
		assertNull(hibernateProgramWorkflowDAO.getPatientProgram(1));
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#saveConceptStateConversion(org.openmrs.ConceptStateConversion)
	 */
    @Test
    public void saveConceptStateConversion_shouldCreateOrUpdateTheGivenConceptStateConversion() {
		Concept concept = new Concept();
		concept.setConceptId(1);
		
		ProgramWorkflow programWorkflow = new ProgramWorkflow();
		programWorkflow.setProgramWorkflowId(1);
		
		ProgramWorkflowState programWorkflowState = new ProgramWorkflowState();
		programWorkflowState.setProgramWorkflowStateId(1);

		ConceptStateConversion conceptStateConversion = new ConceptStateConversion();
		conceptStateConversion.setConcept(concept);
		conceptStateConversion.setConceptStateConversionId(1);
		conceptStateConversion.setProgramWorkflow(programWorkflow);
		conceptStateConversion.setProgramWorkflowState(programWorkflowState);
		
		hibernateProgramWorkflowDAO.saveConceptStateConversion(conceptStateConversion);
		
		assertNotNull(hibernateProgramWorkflowDAO.getConceptStateConversion(1));
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getAllConceptStateConversions()
	 */
    @Test
    public void getAllConceptStateConversions_shouldReturnAllConceptStateConversions() {
    	List<ConceptStateConversion> conceptStateConversionList = hibernateProgramWorkflowDAO.getAllConceptStateConversions();
    	
    	assertEquals(1, conceptStateConversionList.size());
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getConceptStateConversion(java.lang.Integer)
	 */
    @Test
    public void getConceptStateConversion_shouldReturnASingleRowGivenTheId() {
    	ConceptStateConversion conceptStateConversion = hibernateProgramWorkflowDAO.getConceptStateConversion(1);
    	
    	assertNotNull(conceptStateConversion);
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getConceptStateConversion(java.lang.Integer)
	 */
    @Test
	public void getConceptStateConversion_shouldReturnNullWhenConceptStateConversionWithGivenIdDoesNotExist() {
		ConceptStateConversion conceptStateConversion = hibernateProgramWorkflowDAO.getConceptStateConversion(1000);

		assertNull(conceptStateConversion);
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#deleteConceptStateConversion(org.openmrs.ConceptStateConversion)
	 */
	@Test
    public void deleteConceptStateConversion_shouldDeleteTheConceptStateConversionPassed() {
		Concept concept = new Concept();
		concept.setConceptId(1);

		ProgramWorkflow programWorkflow = new ProgramWorkflow();
		programWorkflow.setProgramWorkflowId(1);

		ProgramWorkflowState programWorkflowState = new ProgramWorkflowState();
		programWorkflowState.setProgramWorkflowStateId(1);

		ConceptStateConversion conceptStateConversion = new ConceptStateConversion();
		conceptStateConversion.setConcept(concept);
		conceptStateConversion.setConceptStateConversionId(1);
		conceptStateConversion.setProgramWorkflow(programWorkflow);
		conceptStateConversion.setProgramWorkflowState(programWorkflowState);
    	
    	hibernateProgramWorkflowDAO.deleteConceptStateConversion(conceptStateConversion);
    	
    	assertNull(hibernateProgramWorkflowDAO.getConceptStateConversion(1));
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getConceptStateConversion(org.openmrs.ProgramWorkflow,
	 *      org.openmrs.Concept)
	 */
    @Test
    public void getConceptStateConversion_shouldReturnAConceptStateConversionGivenProgramWorkflowAndConcept() {
		ProgramWorkflow programWorkFlow = new ProgramWorkflow();
		programWorkFlow.setProgramWorkflowId(1);
		
		Concept concept = new Concept();
		concept.setConceptId(1);
		
		ConceptStateConversion conceptStateConversion = hibernateProgramWorkflowDAO.getConceptStateConversion(programWorkFlow, concept);
		
		assertNotNull(conceptStateConversion);
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getConceptStateConversion(org.openmrs.ProgramWorkflow,
	 *      org.openmrs.Concept)
	 */
    @Test
	public void getConceptStateConversion_shouldReturnNullIfProgramWorkFlowAndConceptAreNull() {
		ConceptStateConversion conceptStateConversion = hibernateProgramWorkflowDAO.getConceptStateConversion(null, null);

		assertNull(conceptStateConversion);
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getConceptStateConversionByUuid(java.lang.String)
	 */
	@Test
    public void getConceptStateConversionByUuid_shouldFetchCscByUuidIfExists() {
    	ConceptStateConversion conceptStateConversion = hibernateProgramWorkflowDAO.getConceptStateConversionByUuid("61ACD71F-989B-4B4A-B00E-0503B958199F");
    	
    	assertNotNull(conceptStateConversion);
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getConceptStateConversionByUuid(java.lang.String)
	 */
    @Test
	public void getConceptStateConversionByUuid_shouldReturnNullIfNoSuchCscExistsWithUuid() {
		ConceptStateConversion conceptStateConversion = hibernateProgramWorkflowDAO.getConceptStateConversionByUuid("uuid");

		assertNull(conceptStateConversion);
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getPatientProgramByUuid(java.lang.String)
	 */
	@Test
    public void getPatientProgramByUuid_shouldReturnPatientProgramByUuidIfExists() {
    	PatientProgram patientProgram = hibernateProgramWorkflowDAO.getPatientProgramByUuid("61ACD71F-989B-4B4A-B00E-0503B756199F");
    	
    	assertNotNull(patientProgram);
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getPatientProgramByUuid(java.lang.String)
	 */
    @Test
	public void getPatientProgramByUuid_shouldReturnNullIfNoSuchPatientProgramExistsWithUuid() {
		PatientProgram patientProgram = hibernateProgramWorkflowDAO.getPatientProgramByUuid("uuid");

		assertNull(patientProgram);
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getProgramByUuid(java.lang.String)
	 */
	@Test
    public void getProgramByUuid_shouldReturnProgramIfItExistsWithTheGivenUuid() {
    	Program program = hibernateProgramWorkflowDAO.getProgramByUuid("71ACD71F-989B-4B4A-B00E-0503B756199F");
    	
    	assertNotNull(program);
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getProgramByUuid(java.lang.String)
	 */
    @Test
	public void getProgramByUuid_shouldReturnNullIfItDoesNotExistWithTheGivenUuid() {
		Program program = hibernateProgramWorkflowDAO.getProgramByUuid("uuid");

		assertNull(program);
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getState(Integer)
	 */
	@Test
    public void getState_shouldReturnTheProgramWorkFlowStateIfItExistsWithTheParticularId() {
    	ProgramWorkflowState programWorkflowState = hibernateProgramWorkflowDAO.getState(1);
    	
    	assertNotNull(programWorkflowState);
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getState(Integer)
	 */
	@Test
	public void getState_shouldReturnNullIfNoProgramWorkflowExistsWithTheGivenId() {
		ProgramWorkflowState programWorkflowState = hibernateProgramWorkflowDAO.getState(100);

		assertNull(programWorkflowState);
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getStateByUuid(java.lang.String)
	 */
	@Test
    public void getStateByUuid_shouldReturnTheStateGivenUuidIfExists() {
		ProgramWorkflowState programWorkflowState = hibernateProgramWorkflowDAO.getStateByUuid("61ACD71F-989B-4B4A-B00E-0503B957199F");
		
		assertNotNull(programWorkflowState);
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getStateByUuid(java.lang.String)
	 */
    @Test
	public void getStateByUuid_shouldReturnNullIfStateWithUuidDoesNotExist() {
		ProgramWorkflowState programWorkflowState = hibernateProgramWorkflowDAO.getStateByUuid("uuid");

		assertNull(programWorkflowState);
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getPatientStateByUuid(String)
	 */
	@Test
    public void getPatientStateByUuid_shouldReturnPatientStateByUuidIfExists() {
    	PatientState patientState = hibernateProgramWorkflowDAO.getPatientStateByUuid("61ACD71F-989B-4B4A-B00E-0513B857199F");
    	
    	assertNotNull(patientState);
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getPatientStateByUuid(String)
	 */
    @Test
	public void getPatientStateByUuid_shouldReturnNullIfPatientStateWithUuidDoesNotExist() {
		PatientState patientState = hibernateProgramWorkflowDAO.getPatientStateByUuid("uuid");

		assertNull(patientState);
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getWorkflow(Integer)
	 */
	@Test
    public void getWorkflow_shouldReturnTheWorkFlowWithTheGivenIdIfItExists() {
    	ProgramWorkflow programWorkflow = hibernateProgramWorkflowDAO.getWorkflow(1);
    	
    	assertNotNull(programWorkflow);
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getWorkflow(Integer)
	 */
    @Test
	public void getWorkflow_shouldReturnNullIfWorkFlowWithTheGivenIdDoesNotExist() {
		ProgramWorkflow programWorkflow = hibernateProgramWorkflowDAO.getWorkflow(100);

		assertNull(programWorkflow);
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getWorkflowByUuid(java.lang.String)
	 */
	@Test
    public void getWorkflowByUuid_shouldReturnTheWorkflowWithTheGivenUuidIfExists() {
    	ProgramWorkflow programWorkflow = hibernateProgramWorkflowDAO.getWorkflowByUuid("91ACD71F-989B-4B4A-B00E-0503B957199F");
    	
    	assertNotNull(programWorkflow);
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getWorkflowByUuid(java.lang.String)
	 */
    @Test
	public void getWorkflowByUuid_shouldReturnNullIfTheWorkflowWithGivenUuidDoesNotExists() {
		ProgramWorkflow programWorkflow = hibernateProgramWorkflowDAO.getWorkflowByUuid("uuid");

		assertNull(programWorkflow);
	}

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getProgramsByConcept(org.openmrs.Concept)
	 */
	@Test
    public void getProgramsByConcept_shouldReturnProgramsWithTheGivenConcept() {
    	Concept concept = new Concept();
    	concept.setConceptId(1);
    	List<Program> programList = hibernateProgramWorkflowDAO.getProgramsByConcept(concept);
    	
    	assertEquals(3, programList.size());
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getProgramWorkflowsByConcept(org.openmrs.Concept)
	 */
    @Test
    public void getProgramWorkflowsByConcept_shouldReturnProgramWorkFlowsWithTheConceptIfExists() {
		Concept concept = new Concept();
		concept.setConceptId(1);
		List<ProgramWorkflow> programWorkflowList = hibernateProgramWorkflowDAO.getProgramWorkflowsByConcept(concept);
		
		assertEquals(1, programWorkflowList.size());
    }

	/**
	 * @see org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO#getProgramWorkflowStatesByConcept(org.openmrs.Concept)
	 */
    @Test
    public void getProgramWorkflowStatesByConcept_shouldReturnWorkFlowStatesIfExist() {
		Concept concept = new Concept();
		concept.setConceptId(1);
		List<ProgramWorkflowState> programWorkflowStateList = hibernateProgramWorkflowDAO.getProgramWorkflowStatesByConcept(concept);

		assertEquals(1, programWorkflowStateList.size());
    }

	/**
	 * @see HibernateProgramWorkflowDAO#getAllProgramAttributeTypes()
	 */
    @Test
    public void getAllProgramAttributeTypes_shouldFetchAllProgramAttributeTypes() {
    	List<ProgramAttributeType> programAttributeTypeList = hibernateProgramWorkflowDAO.getAllProgramAttributeTypes();
    	
    	assertEquals(2, programAttributeTypeList.size());
    }

	/**
	 * @see HibernateProgramWorkflowDAO#getProgramAttributeType(Integer)
	 */
    @Test
    public void getProgramAttributeType_shouldFetchTheProgramAttributeTypeWithTheGivenId() {
    	ProgramAttributeType programAttributeType = hibernateProgramWorkflowDAO.getProgramAttributeType(1);
    	
    	assertNotNull(programAttributeType);
    }

	/**
	 * @see HibernateProgramWorkflowDAO#getProgramAttributeType(Integer)
	 */
    @Test
	public void getProgramAttributeType_shouldReturnNullIfProgramAttributeTypeWithGivenIdDoesNotExist() {
		ProgramAttributeType programAttributeType = hibernateProgramWorkflowDAO.getProgramAttributeType(100);

		assertNull(programAttributeType);
	}

	/**
	 * @see HibernateProgramWorkflowDAO#getProgramAttributeTypeByUuid(String)
	 */
	@Test
    public void getProgramAttributeTypeByUuid_shouldReturnProgramAttributeTypeWithUuidIfExists() {
    	ProgramAttributeType programAttributeType = hibernateProgramWorkflowDAO.getProgramAttributeTypeByUuid("31ACD71F-989B-4B4A-B00E-0503B756199F");
    	
    	assertNotNull(programAttributeType);
    }

	/**
	 * @see HibernateProgramWorkflowDAO#getProgramAttributeTypeByUuid(String)
	 */
    @Test
	public void getProgramAttributeTypeByUuid_shouldReturnNullIfProgramAttributeTypeWithGivenUuidDoesNotExist() {
		ProgramAttributeType programAttributeType = hibernateProgramWorkflowDAO.getProgramAttributeTypeByUuid("uuid");

		assertNull(programAttributeType);
	}

	/**
	 * @see HibernateProgramWorkflowDAO#saveProgramAttributeType(ProgramAttributeType)
	 */
	@Test
    public void saveProgramAttributeType_shouldCreateOrUpdateTheProgramAttributeType() {
    	ProgramAttributeType programAttributeType = new ProgramAttributeType();
    	programAttributeType.setProgramAttributeTypeId(2);
    	programAttributeType.setDescription("attribute");
    	programAttributeType.setMinOccurs(1);
    	programAttributeType.setMaxOccurs(3);
    	
    	hibernateProgramWorkflowDAO.saveProgramAttributeType(programAttributeType);
    	
    	assertNotNull(hibernateProgramWorkflowDAO.getProgramAttributeType(2));
    }

	/**
	 * @see HibernateProgramWorkflowDAO#getPatientProgramAttributeByUuid(String)
	 */
    @Test
    public void getPatientProgramAttributeByUuid_shouldReturnThePatientProgramAttributeWithTheGivenUuidIfExists() {
		PatientProgramAttribute patientProgramAttribute = hibernateProgramWorkflowDAO.getPatientProgramAttributeByUuid("11ACD71F-989B-4B4A-B00E-0513B857199F");
		
		assertNotNull(patientProgramAttribute);
    }

	/**
	 * @see HibernateProgramWorkflowDAO#getPatientProgramAttributeByUuid(String)
	 */
    @Test
	public void getPatientProgramAttributeByUuid_shouldReturnNullWhenThePatientProgramAttributeWithTheGivenUuidDoesNotExist() {
		PatientProgramAttribute patientProgramAttribute = hibernateProgramWorkflowDAO.getPatientProgramAttributeByUuid("uuid");

		assertNull(patientProgramAttribute);
	}

	/**
	 * @see HibernateProgramWorkflowDAO#purgeProgramAttributeType(ProgramAttributeType)
	 */
	@Test
    public void purgeProgramAttributeType_shouldDeleteTheProgramAttributeTypeFromDB() throws ParseException {
    	ProgramAttributeType programAttributeType = new ProgramAttributeType();
    	programAttributeType.setProgramAttributeTypeId(1);
    	programAttributeType.setName("some-program");
    	User user  = new User();
    	user.setUserId(1);
    	programAttributeType.setCreator(user);
    	programAttributeType.setDateCreated(new SimpleDateFormat("yyyy-MM-dd").parse("2013-11-06"));
    	
    	hibernateProgramWorkflowDAO.purgeProgramAttributeType(programAttributeType);
    	
    	assertNull(hibernateProgramWorkflowDAO.getProgramAttributeType(1));
    }

	/**
	 * @see HibernateProgramWorkflowDAO#getPatientProgramByAttributeNameAndValue(String, String)
	 */
    @Test
    public void getPatientProgramByAttributeNameAndValue_shouldReturnAllUnVoidedPatientProgramsMatchingTheProgramNameAndAttributeValue() {
		List<PatientProgram> patientProgramList = hibernateProgramWorkflowDAO.getPatientProgramByAttributeNameAndValue("some-program", "some-value");
		
		assertEquals(1, patientProgramList.size());
    }

	/**
	 * @see HibernateProgramWorkflowDAO#getPatientProgramAttributeByAttributeName(List, String) 
	 */
	@Test
	public void getPatientProgramAttributeByAttributeName_shouldReturnPatientProgramAttributesAsAMapIfPatientIdsAndAttributeNameExistsAndAttributeTypeIfOfConceptDataType() {
		List<Integer> patientIds = new ArrayList<>();
		patientIds.add(2001);

		Map<Object, Object> attributeMap = hibernateProgramWorkflowDAO.getPatientProgramAttributeByAttributeName(patientIds, "some-program-2");

		assertNotNull(attributeMap.get(2001));
		assertEquals("{\"some-program-2\":\"1\"}", attributeMap.get(2001));
	}

	/**
	 * @see HibernateProgramWorkflowDAO#getPatientProgramAttributeByAttributeName(List, String)
	 */
    @Test
	public void getPatientProgramAttributeByAttributeName_shouldReturnEmptyMapIfPatientIdsAndAttributeNameDoNotExistInDB() {
		List<Integer> patientIds = new ArrayList<>();
		patientIds.add(2001);

		Map<Object, Object> attributeMap = hibernateProgramWorkflowDAO.getPatientProgramAttributeByAttributeName(patientIds,"some-program");

		assertNull(attributeMap.get(2001));
	}
}
