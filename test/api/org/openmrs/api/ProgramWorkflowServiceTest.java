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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptStateConversion;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.TestUtil;
import org.openmrs.test.Verifies;

/**
 * This class tests methods in the PatientService class TODO Add methods to test all methods in
 * PatientService class
 */
public class ProgramWorkflowServiceTest extends BaseContextSensitiveTest {
	
	protected static final String CREATE_PATIENT_PROGRAMS_XML = "org/openmrs/api/include/ProgramWorkflowServiceTest-createPatientProgram.xml";
	
	protected ProgramWorkflowService pws = null;
	
	protected AdministrationService adminService = null;
	
	protected EncounterService encounterService = null;
	
	protected ConceptService cs = null;
	
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet(CREATE_PATIENT_PROGRAMS_XML);
		
		if (pws == null) {
			pws = Context.getProgramWorkflowService();
			adminService = Context.getAdministrationService();
			encounterService = Context.getEncounterService();
			cs = Context.getConceptService();
		}
	}
	
	/**
	 * @see org.openmrs.testutil.BaseContextSensitiveTest#useInMemoryDatabase()
	 * 
	 @Override public Boolean useInMemoryDatabase( ) { return false; }
	 */
	
	/**
	 * Tests fetching a PatientProgram, updating and saving it, and subsequently fetching the
	 * updated value. To use in MySQL database: Uncomment method useInMemoryDatabase() and comment
	 * out call to initializeInMemoryDatabase() and executeDataSet() within onSetupTransaction() .
	 * 
	 * @see {@link ProgramWorkflowService#savePatientProgram(PatientProgram)}
	 */
	@Test
	@Verifies(value = "should update patient program", method = "savePatientProgram(PatientProgram)")
	public void savePatientProgram_shouldUpdatePatientProgram() throws Exception {
		
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
	 * Tests creating a new program containing workflows and states
	 * 
	 * @see {@link ProgramWorkflowService#saveProgram(Program)}
	 */
	@Test
	@Verifies(value = "should create program workflows", method = "saveProgram(Program)")
	public void saveProgram_shouldCreateProgramWorkflows() throws Exception {
		
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
		
		assertEquals("Failed to create program", numBefore + 1, Context.getProgramWorkflowService().getAllPrograms().size());
		Program p = Context.getProgramWorkflowService().getProgramByName("COUGH SYRUP");
		//System.out.println("TEST Program = " + p);
		assertNotNull("Program is null", p);
		assertNotNull("Workflows is null", p.getWorkflows());
		assertEquals("Wrong number of workflows", 1, p.getWorkflows().size());
		
		ProgramWorkflow wf = p.getWorkflowByName("CIVIL STATUS");
		assertNotNull(wf);
		
		List<String> names = new ArrayList<String>();
		for (ProgramWorkflowState s : wf.getStates()) {
			names.add(s.getConcept().getName().getName());
		}
		TestUtil.assertCollectionContentsEquals(Arrays.asList(new String[] { "SINGLE", "MARRIED" }), names);
	}

	/**
	 * @see {@link ProgramWorkflowService#getConceptStateConversionByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getConceptStateConversionByUuid(String)")
	public void getConceptStateConversionByUuid_shouldFindObjectGivenValidUuid()
			throws Exception {
		String uuid = "6c72b064-506d-11de-80cb-001e378eb67e";
		ConceptStateConversion conceptStateConversion = Context.getProgramWorkflowService().getConceptStateConversionByUuid(uuid);
		Assert.assertEquals(1, (int)conceptStateConversion.getConceptStateConversionId());
	}

	/**
	 * @see {@link ProgramWorkflowService#getConceptStateConversionByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getConceptStateConversionByUuid(String)")
	public void getConceptStateConversionByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid()
			throws Exception {
		Assert.assertNull(Context.getProgramWorkflowService().getConceptStateConversionByUuid("some invalid uuid"));
	}

	/**
	 * @see {@link ProgramWorkflowService#getPatientProgramByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getPatientProgramByUuid(String)")
	public void getPatientProgramByUuid_shouldFindObjectGivenValidUuid()
			throws Exception {
		String uuid = "2edf272c-bf05-4208-9f93-2fa213ed0415";
		PatientProgram patientProgram = Context.getProgramWorkflowService().getPatientProgramByUuid(uuid);
		Assert.assertEquals(2, (int)patientProgram.getPatientProgramId());
	}

	/**
	 * @see {@link ProgramWorkflowService#getPatientProgramByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getPatientProgramByUuid(String)")
	public void getPatientProgramByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid()
			throws Exception {
		Assert.assertNull(Context.getProgramWorkflowService().getPatientProgramByUuid("some invalid uuid"));
	}

	/**
	 * @see {@link ProgramWorkflowService#getPatientStateByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getPatientStateByUuid(String)")
	public void getPatientStateByUuid_shouldFindObjectGivenValidUuid()
			throws Exception {
		String uuid = "ea89deaa-23cc-4840-92fe-63d199c37e4c";
		PatientState patientState = Context.getProgramWorkflowService().getPatientStateByUuid(uuid);
		Assert.assertEquals(1, (int) patientState.getPatientStateId());
	}

	/**
	 * @see {@link ProgramWorkflowService#getPatientStateByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getPatientStateByUuid(String)")
	public void getPatientStateByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid()
			throws Exception {
		Assert.assertNull(Context.getProgramWorkflowService().getPatientStateByUuid("some invalid uuid"));
	}

	/**
	 * @see {@link ProgramWorkflowService#getProgramByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getProgramByUuid(String)")
	public void getProgramByUuid_shouldFindObjectGivenValidUuid()
			throws Exception {
		String uuid = "eae98b4c-e195-403b-b34a-82d94103b2c0";
		Program program = Context.getProgramWorkflowService().getProgramByUuid(uuid);
		Assert.assertEquals(1, (int)program.getProgramId());
	}

	/**
	 * @see {@link ProgramWorkflowService#getProgramByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getProgramByUuid(String)")
	public void getProgramByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid()
			throws Exception {
		Assert.assertNull(Context.getProgramWorkflowService().getProgramByUuid("some invalid uuid"));
	}

	/**
	 * @see {@link ProgramWorkflowService#getStateByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getStateByUuid(String)")
	public void getStateByUuid_shouldFindObjectGivenValidUuid()
			throws Exception {
		String uuid = "92584cdc-6a20-4c84-a659-e035e45d36b0";
		ProgramWorkflowState state = Context.getProgramWorkflowService().getStateByUuid(uuid);
		Assert.assertEquals(1, (int)state.getProgramWorkflowStateId());
	}

	/**
	 * @see {@link ProgramWorkflowService#getStateByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getStateByUuid(String)")
	public void getStateByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid()
			throws Exception {
		Assert.assertNull(Context.getProgramWorkflowService().getStateByUuid("some invalid uuid"));
	}

	/**
	 * @see {@link ProgramWorkflowService#getWorkflowByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should find object given valid uuid", method = "getWorkflowByUuid(String)")
	public void getWorkflowByUuid_shouldFindObjectGivenValidUuid()
			throws Exception {
		String uuid = "84f0effa-dd73-46cb-b931-7cd6be6c5f81";
		ProgramWorkflow programWorkflow = Context.getProgramWorkflowService().getWorkflowByUuid(uuid);
		Assert.assertEquals(1, (int)programWorkflow.getProgramWorkflowId());
	}

	/**
	 * @see {@link ProgramWorkflowService#getWorkflowByUuid(String)}
	 * 
	 */
	@Test
	@Verifies(value = "should return null if no object found with given uuid", method = "getWorkflowByUuid(String)")
	public void getWorkflowByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid()
			throws Exception {
		Assert.assertNull(Context.getProgramWorkflowService().getWorkflowByUuid("some invalid uuid"));
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
