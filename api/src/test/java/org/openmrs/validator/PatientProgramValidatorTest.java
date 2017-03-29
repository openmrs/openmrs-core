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

import java.text.ParseException;
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
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains tests for the {@link PatientProgramValidator}
 */
public class PatientProgramValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfThePatientFieldIsBlank() {
		PatientProgram program = new PatientProgram();
		BindException errors = new BindException(program, "program");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("patient"));
	}
	
	/**
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test(expected = IllegalArgumentException.class)
	public void validate_shouldFailValidationIfObjIsNull() {
		new PatientProgramValidator().validate(null, new BindException(new Object(), ""));
	}
	
	/**
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfAnyPatientStateHasAnEndDateBeforeItsStartDate() {
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
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfThereIsMoreThanOnePatientStateWithTheSameStatesAndStartDates() {
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
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfAnyPatientStateHasANullWorkFlowState() {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		PatientState patientState = program.getStates().iterator().next();
		patientState.setState(null);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheProgramPropertyIsNull() {
		PatientProgram program = new PatientProgram();
		program.setPatient(new Patient());
	}
	
	/**
	 * @throws InterruptedException
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfAnyPatientStatesOverlapEachOtherInTheSameWorkFlow() throws InterruptedException {
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
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassForAValidProgram() {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		PatientState patientState = program.getStates().iterator().next();
		patientState.getPatientProgram().transitionToState(patientState.getState().getProgramWorkflow().getState(4),
		    new Date());
		ValidateUtil.validate(program);
	}
	
	/**
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfAPatientProgramHasDuplicateStatesInTheSameWorkFlow() {
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
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfAPatientStateHasAnInvalidWorkFlowState() {
		executeDataSet("org/openmrs/api/include/ProgramWorkflowServiceTest-otherProgramWorkflows.xml");
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		PatientState patientState = program.getStates().iterator().next();
		patientState.setState(Context.getProgramWorkflowService().getStateByUuid("31c82d66-245c-11e1-9cf0-00248140a5eb"));
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfAPatientIsInMultipleStatesInTheSameWorkFlow() {
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
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfAPatientIsInMultipleStatesInDifferentWorkFlows() {
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
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfTheStartDateOfTheFirstPatientStateInTheWorkFlowIsNull() {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		Assert.assertEquals(1, program.getStates().size());//sanity check
		PatientState patientState = program.getStates().iterator().next();
		patientState.setStartDate(null);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertEquals(false, errors.hasFieldErrors("states"));
	}
	
	/**
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassForPatientStatesThatHaveTheSameStartDatesInTheSameWorkFlow() {
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
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfThereIsMoreThanOneStateWithANullStartDateInTheSameWorkflow() {
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
	 * @see PatientProgramValidator#validate(Object,Errors)
	 * this test is to specifically validate fix for https://tickets.openmrs.org/browse/TRUNK-3670
	 */
	@Test
	public void validate_shouldNotFailIfPatientStateIsInRetiredWorkflow() {
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
	 * @throws ParseException
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfPatientProgramEndDateComesBeforeItsEnrolledDate() throws ParseException {
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
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfPatientProgramEnrolledDateIsInFuture() {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		Date date10DaysAfterSystemCurrentDate = new Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000);
		program.setDateEnrolled(date10DaysAfterSystemCurrentDate);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("dateEnrolled"));
	}
	
	/**
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfPatientProgramEndDateIsInFuture() {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		Date date10DaysAfterSystemCurrentDate = new Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000);
		program.setDateCompleted(date10DaysAfterSystemCurrentDate);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("dateCompleted"));
	}
	
	/**
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfPatientProgramEnrollDateIsEmpty() {
		PatientProgram program = Context.getProgramWorkflowService().getPatientProgram(1);
		program.setDateEnrolled(null);
		
		BindException errors = new BindException(program, "");
		new PatientProgramValidator().validate(program, errors);
		Assert.assertTrue(errors.hasFieldErrors("dateEnrolled"));
	}
	
	/**
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
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
	 * @see PatientProgramValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
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
