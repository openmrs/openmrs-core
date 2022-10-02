/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptStateConversion;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramAttributeType;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ProgramWorkflowDAO;
import org.openmrs.api.impl.ProgramWorkflowServiceImpl;
import org.openmrs.test.TestUtil;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class tests methods in the PatientService class TODO Add methods to test all methods in
 * PatientService class
 */
public class ProgramWorkflowServiceTest extends BaseContextSensitiveTest {
	
	protected static final String CREATE_PATIENT_PROGRAMS_XML = "org/openmrs/api/include/ProgramWorkflowServiceTest-createPatientProgram.xml";
	
	protected static final String PROGRAM_WITH_OUTCOMES_XML = "org/openmrs/api/include/ProgramWorkflowServiceTest-initialData.xml";
	
	protected static final String PROGRAM_ATTRIBUTES_XML = "org/openmrs/api/include/ProgramAttributesDataset.xml";

	protected static final String OTHER_PROGRAM_WORKFLOWS = "org/openmrs/api/include/ProgramWorkflowServiceTest-otherProgramWorkflows.xml";
        
	protected ProgramWorkflowService pws = null;
	
	@Autowired
	protected ProgramWorkflowDAO dao = null;
	
	private ProgramWorkflowServiceImpl pwsi = null;
	
	protected AdministrationService adminService = null;
	
	protected EncounterService encounterService = null;
	
	protected ConceptService cs = null;

	
	@BeforeEach
	public void runBeforeEachTest() {
		executeDataSet(CREATE_PATIENT_PROGRAMS_XML);
		executeDataSet(PROGRAM_ATTRIBUTES_XML);
		executeDataSet(OTHER_PROGRAM_WORKFLOWS);
                
		if (pws == null) {
			pws = Context.getProgramWorkflowService();
			adminService = Context.getAdministrationService();
			encounterService = Context.getEncounterService();
			cs = Context.getConceptService();
		}
	}

	@BeforeEach
	public void setup() {
		pwsi = new ProgramWorkflowServiceImpl();
		pwsi.setProgramWorkflowDAO(dao);
	}
	
	/**
	 * Tests fetching a PatientProgram, updating and saving it, and subsequently fetching the
	 * updated value. To use in MySQL database: Uncomment method useInMemoryDatabase() and comment
	 * out call to initializeInMemoryDatabase() and executeDataSet() within onSetupTransaction() .
	 * 
	 * @see ProgramWorkflowService#savePatientProgram(PatientProgram)
	 */
	@Test
	public void savePatientProgram_shouldUpdatePatientProgram() {
		
		Date today = new Date();
		
		PatientProgram patientProgram = pws.getPatientProgram(1);
		Date dateCompleted = patientProgram.getDateCompleted();
		Date dateChanged = patientProgram.getDateChanged();
		User changedBy = patientProgram.getChangedBy();
		if (null != dateCompleted) {
			//System.out.println("Date Completed: " + dateCompleted);
		}
		if (null != dateChanged) {
			//System.out.println("Date Changed: " + dateChanged);
		}
		if (null != changedBy) {
			//System.out.println("Changed By: " + changedBy.toString());
		}
		
		patientProgram.setDateCompleted(today);
		patientProgram.setChangedBy(Context.getAuthenticatedUser());
		patientProgram.setDateChanged(today);
		pws.savePatientProgram(patientProgram);
		
		// Uncomment to commit to database
		// setComplete( );
		
		PatientProgram ptProg = pws.getPatientProgram(1);
		Date dateCompleted2 = patientProgram.getDateCompleted();
		Date dateChanged2 = patientProgram.getDateChanged();
		User changedBy2 = patientProgram.getChangedBy();

		if (null != dateCompleted2) {
			//System.out.println("Date Completed: " + dateCompleted2);
		}
		if (null != dateChanged2) {
			//System.out.println("Date Changed: " + dateChanged2);
		}
		if (null != changedBy2) {
			//System.out.println("Changed By: " + changedBy2.toString());
		}
		
		assertNotNull(ptProg.getDateCompleted());
		assertEquals(today, ptProg.getDateCompleted());
		
	}

	/**
	 * Tests if the savePatientProgram(PatientProgram) sets the EndDate of recent state of each workflow
	 * on calling the setDateCompleted(Date date) method.
	 *
	 * @see ProgramWorkflowService#savePatientProgram(PatientProgram)
	 */
	@Test
	public void savePatientProgram_shouldSetEndDateOfAllRecentStatesWhenCompletingTheProgram() throws Exception {
		Date day3 = new Date();
		Date day2_5 = new Date(day3.getTime() - 12*3600*1000);
		Date day2 = new Date(day3.getTime() - 24*3600*1000);
		Date day1 = new Date(day2.getTime() - 24*3600*1000);

		// Program Architecture
		Program program = new Program();
		program.setName("TEST PROGRAM");
		program.setDescription("TEST PROGRAM DESCRIPTION");
		program.setConcept(cs.getConcept(3));

		ProgramWorkflow workflow1 = new ProgramWorkflow();
		workflow1.setConcept(cs.getConcept(4));
		program.addWorkflow(workflow1);

		ProgramWorkflow workflow2 = new ProgramWorkflow();
		workflow2.setConcept(cs.getConcept(4));
		program.addWorkflow(workflow2);

		// workflow1
		ProgramWorkflowState state1_w1 = new ProgramWorkflowState();
		state1_w1.setConcept(cs.getConcept(5));
		state1_w1.setInitial(true);
		state1_w1.setTerminal(false);
		workflow1.addState(state1_w1);

		ProgramWorkflowState state2_w1 = new ProgramWorkflowState();
		state2_w1.setConcept(cs.getConcept(6));
		state2_w1.setInitial(false);
		state2_w1.setTerminal(true);
		workflow1.addState(state2_w1);

		// workflow2
		ProgramWorkflowState state1_w2 = new ProgramWorkflowState();
		state1_w2.setConcept(cs.getConcept(5));
		state1_w2.setInitial(true);
		state1_w2.setTerminal(false);
		workflow2.addState(state1_w2);

		ProgramWorkflowState state2_w2 = new ProgramWorkflowState();
		state2_w2.setConcept(cs.getConcept(6));
		state2_w2.setInitial(false);
		state2_w2.setTerminal(true);
		workflow2.addState(state2_w2);

		Context.getProgramWorkflowService().saveProgram(program);

		// Patient Program Architecture
		PatientProgram patientprogram = new PatientProgram();
		patientprogram.setProgram(program);
		patientprogram.setPatient(new Patient(2));
		patientprogram.setDateEnrolled(day1);
		patientprogram.setDateCompleted(null);

		PatientState patientstate1_w1 = new PatientState();
		patientstate1_w1.setStartDate(day1);
		patientstate1_w1.setEndDate(day2);
		patientstate1_w1.setState(state1_w1);

		PatientState patientstate2_w1 = new PatientState();
		patientstate2_w1.setStartDate(day2);
		// Forcefully setEndDate to simulate suspended state
		patientstate2_w1.setEndDate(day2_5);
		patientstate2_w1.setState(state2_w1);

		PatientState patientstate1_w2 = new PatientState();
		patientstate1_w2.setStartDate(day1);
		patientstate1_w2.setEndDate(day2);
		patientstate1_w2.setState(state1_w2);

		PatientState patientstate2_w2 = new PatientState();
		patientstate2_w2.setStartDate(day2);
		patientstate2_w2.setEndDate(null);
		patientstate2_w2.setState(state2_w2);

		patientprogram.getStates().add(patientstate1_w1);
		patientprogram.getStates().add(patientstate2_w1);
		patientprogram.getStates().add(patientstate1_w2);
		patientprogram.getStates().add(patientstate2_w2);

		patientstate1_w1.setPatientProgram(patientprogram);
		patientstate2_w1.setPatientProgram(patientprogram);
		patientstate1_w2.setPatientProgram(patientprogram);
		patientstate2_w2.setPatientProgram(patientprogram);

		// when
		Date terminal_date = day3;
		patientprogram.setDateCompleted(terminal_date);
		Context.getProgramWorkflowService().savePatientProgram(patientprogram);

		// then
		// End date of recent active states should be set
		assertTrue((patientstate2_w2.getEndDate().toString()).equals(terminal_date.toString()));
		assertTrue((patientprogram.getDateCompleted()).equals(patientstate2_w2.getEndDate()));
		// End Date of suspended state should not change
		assertTrue((patientstate2_w1.getEndDate().toString()).equals(day2_5.toString()));
		// End date of past states should not change
		assertTrue((patientstate1_w1.getEndDate().toString()).equals(day2.toString()));
		assertTrue((patientstate1_w2.getEndDate().toString()).equals(day2.toString()));

	}

	/**
	 * Tests if the savePatientProgram(PatientProgram) sets the EndDate of recent state of each workflow
	 * when a patient transitions to a terminal state.
	 *
	 * @see ProgramWorkflowService#savePatientProgram(PatientProgram)
	 */
	@Test
	public void savePatientProgram_shouldSetEndDateOfAllRecentStatesOnTransitionToTerminalState() throws Exception {
		Date day3 = new Date();
		Date day2_5 = new Date(day3.getTime() - 12*3600*1000);
		Date day2 = new Date(day3.getTime() - 24*3600*1000);
		Date day1 = new Date(day2.getTime() - 24*3600*1000);

		// Program Architecture
		Program program = new Program();
		program.setName("TEST PROGRAM");
		program.setDescription("TEST PROGRAM DESCRIPTION");
		program.setConcept(cs.getConcept(3));

		ProgramWorkflow workflow1 = new ProgramWorkflow();
		workflow1.setConcept(cs.getConcept(4));
		program.addWorkflow(workflow1);

		ProgramWorkflow workflow2 = new ProgramWorkflow();
		workflow2.setConcept(cs.getConcept(4));
		program.addWorkflow(workflow2);

		ProgramWorkflow workflow3 = new ProgramWorkflow();
		workflow3.setConcept(cs.getConcept(4));
		program.addWorkflow(workflow3);

		// workflow1
		ProgramWorkflowState state1_w1 = new ProgramWorkflowState();
		state1_w1.setConcept(cs.getConcept(5));
		state1_w1.setInitial(true);
		state1_w1.setTerminal(false);
		workflow1.addState(state1_w1);

		ProgramWorkflowState state2_w1 = new ProgramWorkflowState();
		state2_w1.setConcept(cs.getConcept(6));
		state2_w1.setInitial(false);
		state2_w1.setTerminal(true);
		workflow1.addState(state2_w1);

		// workflow2
		ProgramWorkflowState state1_w2 = new ProgramWorkflowState();
		state1_w2.setConcept(cs.getConcept(5));
		state1_w2.setInitial(true);
		state1_w2.setTerminal(false);
		workflow2.addState(state1_w2);

		ProgramWorkflowState state2_w2 = new ProgramWorkflowState();
		state2_w2.setConcept(cs.getConcept(6));
		state2_w2.setInitial(false);
		state2_w2.setTerminal(true);
		workflow2.addState(state2_w2);

		//workflow3
		ProgramWorkflowState state1_w3 = new ProgramWorkflowState();
		state1_w3.setConcept(cs.getConcept(5));
		state1_w3.setInitial(true);
		state1_w3.setTerminal(false);
		workflow3.addState(state1_w3);

		ProgramWorkflowState state2_w3 = new ProgramWorkflowState();
		state2_w3.setConcept(cs.getConcept(6));
		state2_w3.setInitial(false);
		state2_w3.setTerminal(true);
		workflow3.addState(state2_w3);

		Context.getProgramWorkflowService().saveProgram(program);

		// Patient Program Architecture
		PatientProgram patientprogram = new PatientProgram();
		patientprogram.setProgram(program);
		patientprogram.setPatient(new Patient(2));
		patientprogram.setDateEnrolled(day1);
		patientprogram.setDateCompleted(null);

		PatientState patientstate1_w1 = new PatientState();
		patientstate1_w1.setStartDate(day1);
		patientstate1_w1.setState(state1_w1);

		PatientState patientstate1_w2 = new PatientState();
		patientstate1_w2.setStartDate(day1);
		patientstate1_w2.setState(state1_w2);

		PatientState patientstate1_w3 = new PatientState();
		patientstate1_w3.setStartDate(day1);
		patientstate1_w3.setState(state1_w3);
		// Forcefully setEndDate to simulate suspended state
		patientstate1_w3.setEndDate(day2_5);

		patientprogram.getStates().add(patientstate1_w1);
		patientprogram.getStates().add(patientstate1_w2);
		patientprogram.getStates().add(patientstate1_w3);

		patientstate1_w1.setPatientProgram(patientprogram);
		patientstate1_w2.setPatientProgram(patientprogram);
		patientstate1_w3.setPatientProgram(patientprogram);

		// when
		patientprogram.transitionToState(state2_w1, day3);
		pws.savePatientProgram(patientprogram);

		// then
		for (PatientState state : patientprogram.getMostRecentStateInEachWorkflow()) {
			if (!(state.equals(patientstate1_w3))) {
				assertTrue(state.getEndDate().toString().equals(patientprogram.getDateCompleted().toString()));
			}
		}

		// End date of recent states should be set
		assertTrue((patientstate1_w2.getEndDate().toString()).equals(patientprogram.getDateCompleted().toString()));
		assertTrue((patientprogram.getDateCompleted().toString()).equals(day3.toString()));
		// End date of suspended state should not change
		assertTrue(patientstate1_w3.getEndDate().toString().equals(day2_5.toString()));
		// End date of past states should not change
		assertTrue((patientstate1_w1.getEndDate().toString()).equals(day3.toString()));
	}
	
	/**
	 * Tests creating a new program containing workflows and states
	 * 
	 * @see ProgramWorkflowService#saveProgram(Program)
	 */
	@Test
	public void saveProgram_shouldCreateProgramWorkflows() {
		
		int numBefore = Context.getProgramWorkflowService().getAllPrograms().size();
		
		Program program = new Program();
		program.setName("TEST PROGRAM");
		program.setDescription("TEST PROGRAM DESCRIPTION");
		program.setConcept(cs.getConcept(3));
		
		ProgramWorkflow workflow = new ProgramWorkflow();
		workflow.setConcept(cs.getConcept(4));
		program.addWorkflow(workflow);
		
		ProgramWorkflowState state1 = new ProgramWorkflowState();
		state1.setConcept(cs.getConcept(5));
		state1.setInitial(true);
		state1.setTerminal(false);
		workflow.addState(state1);
		
		ProgramWorkflowState state2 = new ProgramWorkflowState();
		state2.setConcept(cs.getConcept(6));
		state2.setInitial(false);
		state2.setTerminal(true);
		workflow.addState(state2);
		
		Context.getProgramWorkflowService().saveProgram(program);
		
		assertEquals(numBefore + 1, Context.getProgramWorkflowService().getAllPrograms().size(), "Failed to create program");
		Program p = Context.getProgramWorkflowService().getProgramByName("TEST PROGRAM");
		assertNotNull(p, "Program is null");
		assertNotNull(p.getWorkflows(), "Workflows is null");
		assertEquals(1, p.getWorkflows().size(), "Wrong number of workflows");
		
		ProgramWorkflow wf = p.getWorkflowByName("CIVIL STATUS");
		assertNotNull(wf);
		
		List<String> names = new ArrayList<>();
		for (ProgramWorkflowState s : wf.getStates()) {
			names.add(s.getConcept().getName().getName());
		}
		TestUtil.assertCollectionContentsEquals(Arrays.asList("SINGLE", "MARRIED"), names);
	}
	
	/**
	 * @see ProgramWorkflowService#getConceptStateConversionByUuid(String)
	 */
	@Test
	public void getConceptStateConversionByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "6c72b064-506d-11de-80cb-001e378eb67e";
		ConceptStateConversion conceptStateConversion = Context.getProgramWorkflowService().getConceptStateConversionByUuid(
		    uuid);
		assertEquals(1, (int) conceptStateConversion.getConceptStateConversionId());
	}
	
	/**
	 * @see ProgramWorkflowService#getConceptStateConversionByUuid(String)
	 */
	@Test
	public void getConceptStateConversionByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getProgramWorkflowService().getConceptStateConversionByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ProgramWorkflowService#getPatientProgramByUuid(String)
	 */
	@Test
	public void getPatientProgramByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "2edf272c-bf05-4208-9f93-2fa213ed0415";
		PatientProgram patientProgram = Context.getProgramWorkflowService().getPatientProgramByUuid(uuid);
		assertEquals(2, (int) patientProgram.getPatientProgramId());
	}
	
	/**
	 * @see ProgramWorkflowService#getPatientProgramByUuid(String)
	 */
	@Test
	public void getPatientProgramByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getProgramWorkflowService().getPatientProgramByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ProgramWorkflowService#getPatientStateByUuid(String)
	 */
	@Test
	public void getPatientStateByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "ea89deaa-23cc-4840-92fe-63d199c37e4c";
		PatientState patientState = Context.getProgramWorkflowService().getPatientStateByUuid(uuid);
		assertEquals(1, (int) patientState.getPatientStateId());
	}
	
	/**
	 * @see ProgramWorkflowService#getPatientStateByUuid(String)
	 */
	@Test
	public void getPatientStateByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getProgramWorkflowService().getPatientStateByUuid("some invalid uuid"));
	}
	
	/**
	 * @see ProgramWorkflowService#getProgramByUuid(String)
	 */
	@Test
	public void getProgramByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "eae98b4c-e195-403b-b34a-82d94103b2c0";
		Program program = Context.getProgramWorkflowService().getProgramByUuid(uuid);
		assertEquals(1, (int) program.getProgramId());
	}
	
	/**
	 * @see ProgramWorkflowService#getProgramByUuid(String)
	 */
	@Test
	public void getProgramByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getProgramWorkflowService().getProgramByUuid("some invalid uuid"));
	}
	
	@Test
	public void getState_shouldGetStateAssociatedWithGivenIdIfWorkflowStateIdExists() {
		
		final Integer EXISTING_WORKFLOW_STATE_ID = 1;
		
		ProgramWorkflowState state = pws.getState(EXISTING_WORKFLOW_STATE_ID);
		
		assertNotNull(state, "ProgramWorkflowState not found");
		assertThat(state.getId(), is(EXISTING_WORKFLOW_STATE_ID));
	}
	
	@Test
	public void getState_shouldReturnNullIfGivenWorkflowStateIdDoesNotExists() {
		
		assertNull(pws.getState(99999));
	}
	
	/**
	 * @see ProgramWorkflowService#getStateByUuid(String)
	 */
	@Test
	public void getStateByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "92584cdc-6a20-4c84-a659-e035e45d36b0";
		ProgramWorkflowState state = Context.getProgramWorkflowService().getStateByUuid(uuid);
		assertEquals(1, (int) state.getProgramWorkflowStateId());
	}
	
	/**
	 * @see ProgramWorkflowService#getStateByUuid(String)
	 */
	@Test
	public void getStateByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getProgramWorkflowService().getStateByUuid("some invalid uuid"));
	}
	
	@Test
	public void getWorkflow_shouldGetWorkflowAssociatedWithGivenIdIfWorkflowIdExists() {
		
		final Integer EXISTING_WORKFLOW_ID = 1;
		
		ProgramWorkflow workflow = pws.getWorkflow(EXISTING_WORKFLOW_ID);
		
		assertNotNull(workflow, "ProgramWorkflow not found");
		assertThat(workflow.getId(), is(EXISTING_WORKFLOW_ID));
	}
	
	@Test
	public void getWorkflow_shouldReturnNullIfGivenWorkflowIdDoesNotExists() {
		
		assertNull(pws.getWorkflow(99999));
	}
	
	/**
	 * @see ProgramWorkflowService#getWorkflowByUuid(String)
	 */
	@Test
	public void getWorkflowByUuid_shouldFindObjectGivenValidUuid() {
		String uuid = "84f0effa-dd73-46cb-b931-7cd6be6c5f81";
		ProgramWorkflow programWorkflow = Context.getProgramWorkflowService().getWorkflowByUuid(uuid);
		assertEquals(1, (int) programWorkflow.getProgramWorkflowId());
	}
	
	/**
	 * @see ProgramWorkflowService#getWorkflowByUuid(String)
	 */
	@Test
	public void getWorkflowByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getProgramWorkflowService().getWorkflowByUuid("some invalid uuid"));
	}
	
	/**
	 * THIS TEST SHOULD BE IN THE CLASS 'PROGRAMWORKFLOWTEST.JAVA' BUT IT REQUIRES ACCESS TO THE DAO
	 * LAYER
	 * 
	 * @see ProgramWorkflow#getSortedStates()
	 */
	
	@Test
	public void getSortedStates_shouldSortNamesContainingNumbersIntelligently() {
		
		ProgramWorkflow program = new ProgramWorkflow();
		
		ConceptName state1ConceptName = new ConceptName("Group 10", Context.getLocale());
		Concept state1Concept = new Concept();
		state1Concept.addName(state1ConceptName);
		ProgramWorkflowState state1 = new ProgramWorkflowState();
		state1.setConcept(state1Concept);
		program.addState(state1);
		
		ConceptName state2ConceptName = new ConceptName("Group 2", Context.getLocale());
		Concept state2Concept = new Concept();
		state2Concept.addName(state2ConceptName);
		ProgramWorkflowState state2 = new ProgramWorkflowState();
		state2.setConcept(state2Concept);
		program.addState(state2);
		
		Set<ProgramWorkflowState> sortedStates = program.getSortedStates();
		int x = 1;
		for (ProgramWorkflowState state : sortedStates) {
			if (x == 1) {
				assertEquals("Group 2", state.getConcept().getName(Context.getLocale()).getName());
			} else if (x == 2) {
				assertEquals("Group 10", state.getConcept().getName(Context.getLocale()).getName());
			} else {
				fail("Wha?!");
			}
			x++;
		}
	}
	
	@Test
	public void getPossibleOutcomes_shouldGetOutcomesForASet() {
		executeDataSet(PROGRAM_WITH_OUTCOMES_XML);
		
		List<Concept> possibleOutcomes = Context.getProgramWorkflowService().getPossibleOutcomes(4);
		assertEquals(4, possibleOutcomes.size());
	}
	
	@Test
	public void getPossibleOutcomes_shouldGetOutcomesForAQuestion() {
		executeDataSet(PROGRAM_WITH_OUTCOMES_XML);
		
		List<Concept> possibleOutcomes = Context.getProgramWorkflowService().getPossibleOutcomes(5);
		assertEquals(2, possibleOutcomes.size());
	}
	
	@Test
	public void getPossibleOutcomes_shouldReturnEmptyListWhenNoProgramExists() {
		executeDataSet(PROGRAM_WITH_OUTCOMES_XML);
		
		List<Concept> possibleOutcomes = Context.getProgramWorkflowService().getPossibleOutcomes(999);
		assertThat(possibleOutcomes, is(empty()));
	}
	
	@Test
	public void getPossibleOutcomes_shouldReturnEmptyListWhenProgramHasNoOutcome() {
		executeDataSet(PROGRAM_WITH_OUTCOMES_XML);
		
		List<Concept> possibleOutcomes = Context.getProgramWorkflowService().getPossibleOutcomes(1);
		assertThat(possibleOutcomes, is(empty()));
	}
	
	/**
	 * @see ProgramWorkflowService#saveProgram(Program)
	 */
	@Test
	public void saveProgram_shouldUpdateDetachedProgram() {
		Program program = Context.getProgramWorkflowService().getProgramByUuid("eae98b4c-e195-403b-b34a-82d94103b2c0");
		program.setDescription("new description");
		Context.evictFromSession(program);
		
		program = Context.getProgramWorkflowService().saveProgram(program);
		assertEquals("new description", program.getDescription());
	}
	
	/**
	 * @throws InterruptedException
	 * @see ProgramWorkflowService#triggerStateConversion(Patient,Concept,Date)
	 */
	@Test
	public void triggerStateConversion_shouldSkipPastPatientProgramsThatAreAlreadyCompleted() throws InterruptedException {
		Integer patientProgramId = 1;
		PatientProgram pp = pws.getPatientProgram(patientProgramId);
		Date originalDateCompleted = new Date();
		pp.setDateCompleted(originalDateCompleted);
		pp = pws.savePatientProgram(pp);
		
		Concept diedConcept = cs.getConcept(16);
		//sanity check to ensure the patient died is a possible state in one of the work flows
		assertNotNull(pp.getProgram().getWorkflow(1).getState(diedConcept));
		
		Thread.sleep(10);//delay so that we have a time difference
		
		pp = pws.getPatientProgram(patientProgramId);
		assertEquals(originalDateCompleted, pp.getDateCompleted());
	}
	
	@Test
	public void getProgramByName_shouldReturnProgramWhenNameMatches() {
		Program p = pws.getProgramByName("program name");
		assertNotNull(p);
	}
	
	@Test
	public void getProgramByName_shouldReturnNullWhenNoProgramForGivenName() {
		Program p = pws.getProgramByName("unexisting program");
		assertNull(p);
	}
	
	@Test
	public void retireProgram_shouldSaveTheRetiredProgramWithReason() throws APIException {
		String reason = "Feeling well.";
		
		String uuid = "eae98b4c-e195-403b-b34a-82d94103b2c0";
		Program program = Context.getProgramWorkflowService().getProgramByUuid(uuid);
		
		Program retireProgram = pws.retireProgram(program, reason);
		
		assertTrue(retireProgram.getRetired());
		assertEquals(reason, retireProgram.getRetireReason());
		for (ProgramWorkflow programWorkflow : program.getAllWorkflows()) {
			assertTrue(programWorkflow.getRetired());
			for (ProgramWorkflowState programWorkflowState : programWorkflow.getStates()) {
				assertTrue(programWorkflowState.getRetired());
			}
		}
		
	}
	
	@Test
	public void purgeProgram_shouldPurgeProgramWithPatientsEnrolled() {
		Program program = Context.getProgramWorkflowService().getProgram(2);
		
		// program has at least one patient enrolled
		List<PatientProgram> patientPrograms = Context.getProgramWorkflowService().getPatientPrograms(null, program, null,
		    null, null, null, true);
		assertTrue(patientPrograms.size() > 0);
		
		Context.getProgramWorkflowService().purgeProgram(program);
		
		// should cascade to patient programs
		for (PatientProgram patientProgram : patientPrograms) {
			assertNull(Context.getProgramWorkflowService().getPatientProgram(patientProgram.getId()));
		}
		// make sure that the program was deleted properly
		assertNull(Context.getProgramWorkflowService().getProgram(2));
	}
	@Test
	public void shouldTestGetAllProgramAttributeTypes() throws Exception {
                assertEquals(1, pws.getAllProgramAttributeTypes().size());
	}

	@Test
	public void shouldTestGetProgramAttributeType() throws Exception {

		ProgramAttributeType programAttributeType  = pws.getProgramAttributeType(1);
		assertEquals("d7477c21-bfc3-4922-9591-e89d8b9c8efb",programAttributeType.getUuid());
	}

	@Test
	public void shouldTestGetProgramAttributeTypeByUuid() throws Exception {
		ProgramAttributeType p = pws.getProgramAttributeTypeByUuid("d7477c21-bfc3-4922-9591-e89d8b9c8efb");
		assertEquals("ProgramId",p.getName());
	}

	@Test
	public void shouldTestSaveProgramAttributeType() throws Exception {
		assertEquals(1,pws.getAllProgramAttributeTypes().size());
		ProgramAttributeType programAttributeType = new ProgramAttributeType();
		programAttributeType.setName("test");
		pws.saveProgramAttributeType(programAttributeType);
		assertEquals(2,pws.getAllProgramAttributeTypes().size());
	}

	@Test
	public void shouldTestPurgeProgramAttributeType() throws Exception {
		ProgramAttributeType programAttributeType = pws.getProgramAttributeType(1);
                int totalAttributeTypes = pws.getAllProgramAttributeTypes().size();
		pws.purgeProgramAttributeType(programAttributeType);
		assertEquals((totalAttributeTypes - 1), pws.getAllProgramAttributeTypes().size());
	}
	
	
	@Test
	public void getPrograms_shouldTestGetPrograms() {
		List<Program> malPrograms = pws.getPrograms("MAL");
		List<Program> prPrograms = pws.getPrograms("PR");
		assertEquals(malPrograms.size(), 1);
		assertEquals(prPrograms.size(), 3);
	}
	
	@Test
	public void retireProgram_shouldSetRetiredStateToFalseAndSetAReason() {
		Concept concept = Context.getConceptService().getConcept(12);
		ProgramWorkflow programWorkflow = dao.getProgramWorkflowsByConcept(concept).get(1);
		ArrayList<ProgramWorkflow> programWorkflows = new ArrayList<>();
		programWorkflows.add(programWorkflow);
		Set<ProgramWorkflow> allWorkflows = new HashSet<>(programWorkflows);
		Program programTest = pws.getProgram(2);
		programTest.setAllWorkflows(allWorkflows);
		Program program = pws.retireProgram(programTest, "expired");
		for (ProgramWorkflow workflow : program.getWorkflows()) {
			assertTrue(workflow.getRetired());
			assertNotNull(workflow.getStates());
			for (ProgramWorkflowState state : workflow.getStates()) {
				assertTrue(state.getRetired());
			}
		}
		assertEquals(program.getRetireReason(), "expired");
	}
	
	@Test
	public void unretireProgram_shouldSetRetireFalseForWorkflowsAndWorkflowStates() {
		Program program = pws.getAllPrograms().get(0);
		Date lastModifiedDate = program.getDateChanged();
		assertEquals(program.getRetired(), false);
		for (ProgramWorkflow workflow : program.getAllWorkflows()) {
			if (lastModifiedDate != null && lastModifiedDate.equals(workflow.getDateChanged())) {
				assertEquals(workflow.getRetired(), false);
				for (ProgramWorkflowState state : workflow.getStates()) {
					if (lastModifiedDate.equals(state.getDateChanged())) {
						assertEquals(state.getRetired(), false);
					}
				}
			}
		}
	}
	
	@Test
	public void saveUnretireProgram_shouldTestSaveUnretireProgram() {
		Program program = new Program();
		Concept concept = Context.getConceptService().getAllConcepts().get(0);
		User testUser = Context.getUserService().getAllUsers().get(0);
		program.setConcept(concept);
		program.setDescription("test");
		program.setName("programTest");
		program.setCreator(testUser);
		pws.getAllPrograms();
		pws.retireProgram(program, "abc");
		pws.unretireProgram(program);
		Program programToBeAsserted = dao.getProgramByUuid(program.getUuid());
		assertEquals(program, programToBeAsserted);
	}
	
	@Test
	public void savePatientProgram_shouldTestThrowPatientStateRequiresException() {
		PatientProgram patientProgram = pws.getPatientProgram(1);
		for (PatientState state : patientProgram.getStates()) {
			state.setState(null);
		}
		APIException exception = assertThrows(APIException.class, () -> pws.savePatientProgram(patientProgram));
		assertThat(exception.getMessage(), is("'PatientProgram(id=1, patient=Patient#2, program=Program(id=1, concept=Concept #1738, " +
			"workflows=[ProgramWorkflow(id=1), ProgramWorkflow(id=2)]))' failed to validate with reason: states: State is required for a patient state"));
	}
	
	@Test
	public void savePatientProgram_shouldTestSetPatientProgram() {
		PatientProgram patientProgram = pws.getPatientProgram(1);
		for (PatientState state : patientProgram.getStates()) {
			state.setPatientProgram(null);
		}
		pws.savePatientProgram(patientProgram);
		for (PatientState state : patientProgram.getStates()) {
			assertEquals(state.getPatientProgram(), patientProgram);
		}
	}
	
	@Test
	public void savePatientProgram_shouldThrowPatientProgramAlreadyAssignedException() {
		PatientProgram patientProgram = pws.getPatientProgram(1);
		PatientProgram patientProgram1 = pws.getPatientProgram(2);
		for (PatientState state : patientProgram.getStates()) {
			state.setPatientProgram(patientProgram1);
		}
		APIException exception = assertThrows(APIException.class, () -> pws.savePatientProgram(patientProgram));
		assertThat(exception.getMessage(), is("This PatientProgram contains a ProgramWorkflowState whose parent is " +
			"already assigned to PatientProgram(id=2, patient=Patient#2, program=Program(id=2, concept=Concept #10, " +
			"workflows=[ProgramWorkflow(id=3)]))"));
	}
	
	@Test
	public void savePatientProgram_shouldTestSetState() {
		PatientProgram patientProgram = pws.getPatientProgram(1);
		pws.voidPatientProgram(patientProgram, "test");
		for (PatientState state : patientProgram.getStates()) {
			state.setVoided(false);
			state.setVoidReason(null);
		}
		pws.savePatientProgram(patientProgram);
		for (PatientState state : patientProgram.getStates()) {
			assertEquals(state.getVoided(), true);
			assertEquals(state.getVoidReason(), "test");
		}
	}

	@Test
	public void savePatientProgram_shouldTestPatientStateFormNamespaceAndPath() {
		final String NAMESPACE = "namespace";
		final String FORMFIELD_PATH = "formFieldPath";
		
		PatientProgram patientProgram = pws.getPatientProgram(1);
		for (PatientState state : patientProgram.getStates()) {
			state.setFormField(NAMESPACE, FORMFIELD_PATH);
		}
		PatientProgram updatePatientProgram = pws.savePatientProgram(patientProgram);
		for (PatientState state : updatePatientProgram.getStates()) {
			assertEquals(NAMESPACE + "^" + FORMFIELD_PATH, state.getFormNamespaceAndPath());
		}
	}

	@Test
	public void saveEncounter_shouldTestPatientStateEncounter() {
		PatientProgram patientProgram = pws.getPatientProgram(1);
		
		Encounter enc = new Encounter();
		enc.setEncounterType(Context.getEncounterService().getEncounterType(1));
		enc.setEncounterDatetime(patientProgram.getDateEnrolled());
		enc.setPatient(patientProgram.getPatient());
		
		Encounter savedEncounter = Context.getEncounterService().saveEncounter(enc);

		for (PatientState state : patientProgram.getStates()) {
			state.setEncounter(savedEncounter);
		}
		PatientProgram updatePatientProgram = pws.savePatientProgram(patientProgram);
		for (PatientState state : updatePatientProgram.getStates()) {
			assertEquals(savedEncounter.getEncounterId(), state.getEncounter().getEncounterId());
		}
	}
	@Test
	public void getPrograms_shouldTestGetProgramsIfCohortIsEmpty() {
		Cohort cohort = new Cohort();
		Collection<Program> programs = pws.getAllPrograms();
		List<PatientProgram> patientPrograms;
		cohort.getMemberIds().clear();
		patientPrograms = dao.getPatientPrograms(null, programs);
		assertEquals(patientPrograms.size(), 4);
	}
	
	@Test
	public void getPrograms_shouldTestGetProgramsIfCohortIsNotEmpty() {
		Cohort cohort = new Cohort();
		Collection<Program> programs = pws.getAllPrograms();
		List<PatientProgram> patientPrograms;
		cohort.addMember(1);
		cohort.addMember(2);
		patientPrograms = dao.getPatientPrograms(cohort, programs);
		assertEquals(patientPrograms.size(), 2);
	}
	
	
	@Test
	public void voidPatientProgram_shouldTestVoidPatientProgram() {
		PatientProgram patientProgram = pws.getPatientProgram(1);
		PatientProgram patientProgram1 = pws.voidPatientProgram(patientProgram, "abc");
		assertEquals(patientProgram1.getVoided(), true);
		assertEquals(patientProgram1.getVoidReason(), "abc");
	}
	
	@Test
	public void unvoidPatientProgram_shouldTestUnvoidPatientProgram() {
		PatientProgram existingPatientProgram = pws.getPatientProgram(1);
		PatientProgram existingPatientProgramVoided = pws.voidPatientProgram(existingPatientProgram, "expired");
		Date patientProgramDateVoided = existingPatientProgramVoided.getDateVoided();
		existingPatientProgramVoided = pws.unvoidPatientProgram(existingPatientProgram);
		assertEquals(existingPatientProgramVoided.getVoided(), false);
		for (PatientState state : existingPatientProgram.getStates()) {
			assertEquals(state.getVoided(), false);
			if (patientProgramDateVoided != null && patientProgramDateVoided.equals(state.getDateVoided())) {
				assertNull(state.getVoidedBy());
				assertNull(state.getDateVoided());
				assertNull(state.getVoidReason());
			}
		}
	}
	
	@Test
	public void saveConceptStateConversion_shouldThrowConceptStateConversionRequire() {
		ConceptStateConversion conceptStateConversion = new ConceptStateConversion();
		conceptStateConversion.setConcept(null);
		APIException exception = assertThrows(APIException.class, () -> pws.saveConceptStateConversion(conceptStateConversion));
		assertThat(exception.getMessage(), is("'ConceptStateConversion: Concept[null] results in State [null] for workflow [null]' failed to validate with reason: " +
			"concept: Invalid concept, programWorkflow: Invalid Programme Workflow, programWorkflowState: Invalid Programme Workflow State"));
	}
	
	@Test
	public void saveConceptStateConversion_shouldTestSaveConceptStateConversion() {
		ConceptStateConversion newConceptStateConversion = new ConceptStateConversion();
		ConceptStateConversion existingConceptStateConversion = pws.getAllConceptStateConversions().get(0);
		newConceptStateConversion.setConcept(new Concept(3));
		newConceptStateConversion.setProgramWorkflow(existingConceptStateConversion.getProgramWorkflow());
		newConceptStateConversion.setProgramWorkflowState(existingConceptStateConversion.getProgramWorkflowState());
		String conceptStateConversionUuid = newConceptStateConversion.getUuid();
		pws.saveConceptStateConversion(newConceptStateConversion);
		ConceptStateConversion conceptStateConversion2 = dao.getConceptStateConversionByUuid(conceptStateConversionUuid);
		assertEquals(conceptStateConversionUuid, conceptStateConversion2.getUuid());
	}
	
	@Test
	public void getConceptStateConversion_shouldTestGetConceptStateConversion() {
		ConceptStateConversion conceptStateConversion1 = pws.getAllConceptStateConversions().get(0);
		int conceptStateConversion1Id = conceptStateConversion1.getId();
		ConceptStateConversion conceptStateConversion2 = pws.getConceptStateConversion(conceptStateConversion1Id);
		assertEquals(conceptStateConversion1, conceptStateConversion2);
	}
	
	@Test
	public void getAllConceptStateConversion_shouldTestGetAllConceptStateConversion() {
		List<ConceptStateConversion> conceptStateConversions = pws.getAllConceptStateConversions();
		assertEquals(conceptStateConversions.size(), 1);
		
	}
	
	@Test
	public void purgeConceptStateConversion_shouldTestPurgeConceptStateConversion() {
		ConceptStateConversion conceptStateConversion = pws.getAllConceptStateConversions().get(0);
		Context.getProgramWorkflowService().purgeConceptStateConversion(conceptStateConversion, false);
		List<ConceptStateConversion> list = pws.getAllConceptStateConversions();
		assertEquals(list.size(), 0);
	}
	
	@Test
	public void getProgramsByConcept_shouldTestGetProgramsByConcept() {
		Concept concept = Context.getConceptService().getAllConcepts().get(0);
		List<Program> programs = pws.getProgramsByConcept(concept);
		assertEquals(programs.size(), 0);
	}
	
	@Test
	public void programWorkflowsByConcept_shouldTestGetProgramWorkflowsByConcept() {
		Concept concept = Context.getConceptService().getAllConcepts().get(0);
		List<ProgramWorkflow> programWorkflows = pws.getProgramWorkflowsByConcept(concept);
		assertEquals(programWorkflows.size(), 0);
	}
	
	@Test
	public void programWorkflowStatesByConcept_shouldTestGetProgramWorkflowStatesByConcept() {
		Concept concept = Context.getConceptService().getAllConcepts().get(0);
		List<ProgramWorkflowState> programWorkflowStates = pws.getProgramWorkflowStatesByConcept(concept);
		assertEquals(programWorkflowStates.size(), 0);
	}
	
	@Test
	public void getAllPrograms_shouldTestGetAllPrograms() {
		List<Program> programs = pws.getAllPrograms();
		assertEquals(programs.size(), 3);
	}
	
	@Test
	public void getConceptStateConversion_shouldGetConceptStateConversion(){
		ProgramWorkflow programWorkflow = pws.getProgram(1).getWorkflow(2);
		Concept concept = dao.getAllConceptStateConversions().get(0).getConcept();
		ConceptStateConversion conceptStateConversion = dao.getConceptStateConversion(programWorkflow, concept);
		assertEquals(conceptStateConversion, dao.getAllConceptStateConversions().get(0));
	}
	
	@Test 
	public void getProgram_shouldGetProgramByName(){
		Program program = pws.getAllPrograms().get(0);
		String programName = program.getName();
		assertEquals(Context.getProgramWorkflowService().getProgramByName(programName), program);
	}
	
	@Test
	public void triggerStateConversion_shouldThrowConvertStateInvalidPatient(){
		Concept trigger = Context.getConceptService().getAllConcepts().get(0);
		Date dateConverted = new Date();
		APIException exception = assertThrows(APIException.class, () -> pwsi.triggerStateConversion(null, trigger, dateConverted));
		assertThat(exception.getMessage(), is("Attempting to convert state of an invalid patient"));
	}

	@Test
	public void triggerStateConversion_shouldThrowConvertStatePatientWithoutValidTrigger(){
		Patient patient = Context.getPatientService().getAllPatients().get(0);
		Date dateConverted = new Date();
		APIException exception = assertThrows(APIException.class, () -> pwsi.triggerStateConversion(patient, null, dateConverted));
		assertThat(exception.getMessage(), is("Attempting to convert state for a patient without a valid trigger concept"));
	}

	@Test
	public void triggerStateConversion_shouldThrowConvertStateInvalidDate(){
		Patient patient = Context.getPatientService().getAllPatients().get(0);
		Concept trigger = Context.getConceptService().getAllConcepts().get(0);
		APIException exception = assertThrows(APIException.class, () -> pwsi.triggerStateConversion(patient, trigger, null));
		assertThat(exception.getMessage(), is("Invalid date for converting patient state"));
	}
	
	@Test
	public void triggerStateConversion_shouldTestTransitionToState(){
		Patient patient = Context.getPatientService().getPatientByUuid("6013a8cd-c6a0-4140-bfac-0af565704420");
		PatientProgram patientProgram = pws.getPatientProgram(1);
		Concept trigger = Context.getConceptService().getConcept(14);
		Date dateConverted = new Date();
		patientProgram.setDateCompleted(null);
		int patientStatesSize = patientProgram.getStates().size();
		pwsi.triggerStateConversion(patient, trigger, dateConverted);
		assertEquals(patientProgram.getStates().size(), (patientStatesSize + 1));
	}
	
	//	/**
	//	 * This method should be uncommented when you want to examine the actual hibernate
	//	 * sql calls being made.  The calls that should be limiting the number of returned
	//	 * patients should show a "top" or "limit" in the sql -- this proves hibernate's
	//	 * use of a native sql limit as opposed to a java-only limit.
	//	 *
	//	 * Note: if enabled, this test will be considerably slower
	//     *
	//     * @see org.openmrs.test.BaseContextSensitiveTest#getRuntimeProperties()
	//     */
	//    @Override
	//    public Properties getRuntimeProperties() {
	//	    Properties props = super.getRuntimeProperties();
	//	    props.setProperty("hibernate.show_sql", "true");
	//
	//    	return props;
	//    }
	
}
