/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains tests for the {@link PatientProgramValidator}
 */
public class PatientProgramValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the patient field is blank", method = "validate(Object,Errors)")
	public void validate_shouldFailIfThePatientFieldIsBlank() throws Exception {
		PatientProgram program = new PatientProgram();
		BindException errors = new BindException(program, "program");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("patient"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail validation if obj is null", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfObjIsNull() throws Exception {
		new PatientProgramValidator().validate(null, new BindException(new Object(), ""));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if any patient state has an end date before its start date", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAnyPatientStateHasAnEndDateBeforeItsStartDate() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		PatientState patientState = program.getStates().iterator().next();
		Calendar c = Calendar.getInstance();
		patientState.setStartDate(c.getTime());
		c.set(1970, 2, 1);//set to an old date
		patientState.setEndDate(c.getTime());
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if there is more than one patientState with the same states and startDates", method = "validate(Object,Errors)")
	public void validate_shouldFailIfThereIsMoreThanOnePatientStateWithTheSameStatesAndStartDates() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		Set<PatientState> states = program.getStates();
		Assert.assertNotNull(states);
		PatientState patientState = states.iterator().next();
		PatientState duplicate = patientState.copy();
		states.add(duplicate);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if any patient state has a null work flow state", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAnyPatientStateHasANullWorkFlowState() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		PatientState patientState = program.getStates().iterator().next();
		patientState.setState(null);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the program property is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheProgramPropertyIsNull() throws Exception {
		PatientProgram program = new PatientProgram();
		program.setPatient(new Patient());
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if any patient states overlap each other in the same work flow", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAnyPatientStatesOverlapEachOtherInTheSameWorkFlow() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		//Addition of new states to this program in the test data can make this test useless, so catch it her
		Assert.assertEquals(1, program.getStates().size());
		PatientState patientState1 = program.getStates().iterator().next();
		
		//Add a state that comes after patientState1
		PatientState patientState2 = new PatientState();
		patientState2.setStartDate(new Date());
		patientState2.setState(Context.getProgramWorkflowService().getWorkflowByUuid("84f0effa-dd73-46cb-b931-7cd6be6c5f81")
		        .getState(1));
		//guarantees that startDate of patientState2 is atleast 10ms earlier
		Thread.sleep(10);
		patientState1.setEndDate(new Date());
		
		program.getStates().add(patientState2);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass for a valid program", method = "validate(Object,Errors)")
	public void validate_shouldPassForAValidProgram() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		PatientState patientState = program.getStates().iterator().next();
		patientState.getPatientProgram().transitionToState(patientState.getState().getProgramWorkflow().getState(4),
		    new Date());
		ValidateUtil.validate(program);
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if a patient program has duplicate states in the same work flow", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAPatientProgramHasDuplicateStatesInTheSameWorkFlow() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		Set<PatientState> states = program.getStates();
		Assert.assertNotNull(states);
		PatientState patientState = states.iterator().next();
		PatientState duplicate = patientState.copy();
		states.add(duplicate);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if a patientState has an invalid work flow state", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAPatientStateHasAnInvalidWorkFlowState() throws Exception {
		executeDataSet("org/openmrs/api/include/ProgramWorkflowServiceTest-otherProgramWorkflows.xml");
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		PatientState patientState = program.getStates().iterator().next();
		patientState.setState(Context.getProgramWorkflowService().getStateByUuid("31c82d66-245c-11e1-9cf0-00248140a5eb"));
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if a patient is in multiple states in the same work flow", method = "validate(Object,Errors)")
	public void validate_shouldFailIfAPatientIsInMultipleStatesInTheSameWorkFlow() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		PatientState patientState = program.getStates().iterator().next();
		patientState.getPatientProgram().transitionToState(patientState.getState().getProgramWorkflow().getState(4),
		    new Date());
		//make the closed state active
		patientState.setEndDate(null);
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if a patient is in multiple states in different work flows", method = "validate(Object,Errors)")
	public void validate_shouldPassIfAPatientIsInMultipleStatesInDifferentWorkFlows() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		
		//Add another state to another work flow
		PatientState patientState2 = new PatientState();
		patientState2.setStartDate(new Date());
		patientState2.setState(Context.getProgramWorkflowService().getWorkflowByUuid("c66c8713-7df4-40de-96f6-dc4cce3432da")
		        .getState(5));
		program.getStates().add(patientState2);
		
		ValidateUtil.validate(program);
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass if the start date of the first patient state in the work flow is null", method = "validate(Object,Errors)")
	public void validate_shouldPassIfTheStartDateOfTheFirstPatientStateInTheWorkFlowIsNull() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		Assert.assertEquals(1, program.getStates().size());//sanity check
		PatientState patientState = program.getStates().iterator().next();
		patientState.setStartDate(null);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertEquals(false, errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass for patient states that have the same start dates in the same work flow", method = "validate(Object,Errors)")
	public void validate_shouldPassForPatientStatesThatHaveTheSameStartDatesInTheSameWorkFlow() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		PatientState patientState = program.getStates().iterator().next();
		//add a new state by moving the patient to a another one
		ProgramWorkflowState nextState = patientState.getState().getProgramWorkflow().getState(4);
		patientState.getPatientProgram().transitionToState(nextState, patientState.getStartDate());
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertEquals(false, errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if there is more than one state with a null start date in the same workflow", method = "validate(Object,Errors)")
	public void validate_shouldFailIfThereIsMoreThanOneStateWithANullStartDateInTheSameWorkflow() throws Exception {
		ProgramWorkflowService pws = Context.getProgramWorkflowService();
		Patient patient = Context.getPatientService().getPatient(6);
		PatientProgram pp = new PatientProgram();
		pp.setPatient(patient);
		pp.setProgram(pws.getProgram(1));
		ProgramWorkflow testWorkflow = pp.getProgram().getWorkflow(1);
		
		//Add 2 other patient states with null start date		
		PatientState newPatientState1 = new PatientState();
		newPatientState1.setState(testWorkflow.getState(1));
		pp.getStates().add(newPatientState1);
		
		PatientState newPatientState2 = new PatientState();
		newPatientState2.setState(testWorkflow.getState(2));
		pp.getStates().add(newPatientState2);
		
		BindException errors = new BindException(pp, "");
		new PatientProgramValidator().validate(pp, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 * this test is to specifically validate fix for https://tickets.openmrs.org/browse/TRUNK-3670
	 */
	@Test
	@Verifies(value = "should not fail if a non-voided patient state is associated with a retired workflow", method = "validate(Object,Errors)")
	public void validate_shouldNotFailIfPatientStateIsInRetiredWorkflow() throws Exception {
		ProgramWorkflowService pws = Context.getProgramWorkflowService();
		Patient patient = Context.getPatientService().getPatient(6);
		
		// create a patient program
		PatientProgram pp = new PatientProgram();
		pp.setPatient(patient);
		pp.setProgram(pws.getProgram(1));
		
		// add a test workflow, and put the patient in a state in that workflow
		ProgramWorkflow testWorkflow = pp.getProgram().getWorkflow(1);
		PatientState newPatientState = new PatientState();
		newPatientState.setState(testWorkflow.getState(1));
		pp.getStates().add(newPatientState);
		
		// now retire the workflow
		testWorkflow.setRetired(true);
		testWorkflow.setRetiredBy(Context.getAuthenticatedUser());
		testWorkflow.setRetireReason("test");
		
		BindException errors = new BindException(pp, "");
		new PatientProgramValidator().validate(pp, errors);
		Assert.assertEquals(false, errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if patient program end date comes before its enrolled date", method = "validate(Object,Errors)")
	public void validate_shouldFailIfPatientProgramEndDateComesBeforeItsEnrolledDate() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date dateEnrolled = sdf.parse("12/04/2014");
		Date dateCompleted = sdf.parse("21/03/2014");
		program.setDateEnrolled(dateEnrolled);
		program.setDateCompleted(dateCompleted);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("dateCompleted"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if patient program enrolled date is in future", method = "validate(Object,Errors)")
	public void validate_shouldFailIfPatientProgramEnrolledDateIsInFuture() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		Date date10DaysAfterSystemCurrentDate = new Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000);
		program.setDateEnrolled(date10DaysAfterSystemCurrentDate);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("dateEnrolled"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if patient program end date is in future", method = "validate(Object,Errors)")
	public void validate_shouldFailIfPatientProgramEndDateIsInFuture() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		Date date10DaysAfterSystemCurrentDate = new Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000);
		program.setDateCompleted(date10DaysAfterSystemCurrentDate);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("dateCompleted"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if patient program enroll date is empty", method = "validate(Object,Errors)")
	public void validate_shouldFailIfPatientProgramEnrollDateIsEmpty() throws Exception {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		program.setDateEnrolled(null);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("dateEnrolled"));
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Object,Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		ProgramWorkflowService pws = Context.getProgramWorkflowService();
		Patient patient = Context.getPatientService().getPatient(6);
		
		PatientProgram pp = new PatientProgram();
		pp.setPatient(patient);
		pp.setProgram(pws.getProgram(1));
		pp.setDateEnrolled(new Date());
		
		pp.setVoidReason("voidReason");
		
		BindException errors = new BindException(pp, "program");
		new PatientProgramValidator().validate(pp, errors);
		Assert.assertEquals(false, errors.hasErrors());
	}
	
	/**
	 * @see {@link PatientProgramValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Object,Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		ProgramWorkflowService pws = Context.getProgramWorkflowService();
		Patient patient = Context.getPatientService().getPatient(6);
		
		PatientProgram pp = new PatientProgram();
		pp.setPatient(patient);
		pp.setProgram(pws.getProgram(1));
		pp.setDateEnrolled(new Date());
		
		pp
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		BindException errors = new BindException(pp, "program");
		new PatientProgramValidator().validate(pp, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("voidReason"));
	}
}
