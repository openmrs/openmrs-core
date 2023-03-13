/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class PatientProgramTest {
	
	private Date date = new Date();
	
	private Date earlierDate = new Date(date.getTime() - 10000);
	
	private Date laterDate = new Date(date.getTime() + 10000);
	
	/**
	 * @see PatientProgram#voidLastState(ProgramWorkflow,User,Date,String)
	 */
	@Test
	public void voidLastState_shouldVoidStateWithEndDateNullIfStartDatesEqual() throws Exception {
		//given
		PatientProgram program = new PatientProgram();
		ProgramWorkflow workflow = new ProgramWorkflow();
		ProgramWorkflowState workflowState = new ProgramWorkflowState();
		workflowState.setProgramWorkflow(workflow);
		
		Date startDate = new Date();
		
		PatientState state1 = new PatientState();
		state1.setStartDate(startDate);
		state1.setEndDate(null);
		state1.setState(workflowState);
		
		PatientState state2 = new PatientState();
		state2.setStartDate(startDate);
		state2.setEndDate(new Date());
		state2.setState(workflowState);
		
		program.getStates().add(state1);
		program.getStates().add(state2);
		state1.setPatientProgram(program);
		state2.setPatientProgram(program);
		
		//when
		program.voidLastState(workflow, new User(), new Date(), "");
		
		//then
		assertTrue(state1.isVoided());
		assertFalse(state2.isVoided());
		assertNull(state2.getEndDate());
	}
	
	/**
	 * @see PatientProgram#voidLastState(ProgramWorkflow,User,Date,String)
	 * <strong>Verifies</strong> void state with endDate null if startDates equal
	 */
	@Test
	public void voidLastState_shouldVoidStateWithEndDateEqualToProgramCompletionDate() throws Exception {
		//given
		PatientProgram program = new PatientProgram();
		program.setDateEnrolled(new Date());
		program.setDateCompleted(new Date());
		ProgramWorkflow workflow = new ProgramWorkflow();
		ProgramWorkflowState workflowState = new ProgramWorkflowState();
		workflowState.setProgramWorkflow(workflow);
		
		Date startDate = new Date();
		
		PatientState state1 = new PatientState();
		state1.setStartDate(startDate);
		state1.setEndDate(null);
		state1.setState(workflowState);
		
		PatientState state2 = new PatientState();
		state2.setStartDate(startDate);
		state2.setEndDate(new Date());
		state2.setState(workflowState);
		
		program.getStates().add(state1);
		program.getStates().add(state2);
		state1.setPatientProgram(program);
		state2.setPatientProgram(program);
		
		//when
		program.voidLastState(workflow, new User(), new Date(), "");
		
		//then
		assertTrue(state1.isVoided());
		assertFalse(state2.isVoided());
		
		assertTrue(program.getDateCompleted().equals(state2.getEndDate()));
	}
	
	@Test
	public void transitionToDate_shouldSetEndDateOfNewStateToProgramCompletionDateIfProgramCompleted() throws Exception {
		
		//given
		PatientProgram program = new PatientProgram();
		program.setDateEnrolled(new Date());
		program.setDateCompleted(new Date());
		ProgramWorkflow workflow = new ProgramWorkflow();
		ProgramWorkflowState workflowState = new ProgramWorkflowState();
		workflowState.setTerminal(false);
		workflowState.setProgramWorkflow(workflow);
		
		//when
		program.transitionToState(workflowState, new Date());
		
		//then
		assertThat(program.getStates(), hasSize(1));
		assertTrue(program.getStates().iterator().next().getEndDate().equals(program.getDateCompleted()));
		
	}
	
	@Test
	public void transitionToDate_shouldSetEndDateOfNewStateToNullIfProgramNotCompleted() throws Exception {
		
		//given
		PatientProgram program = new PatientProgram();
		program.setDateEnrolled(new Date());
		ProgramWorkflow workflow = new ProgramWorkflow();
		ProgramWorkflowState workflowState = new ProgramWorkflowState();
		workflowState.setTerminal(false);
		workflowState.setProgramWorkflow(workflow);
		
		//when
		program.transitionToState(workflowState, new Date());
		
		//then
		assertThat(program.getStates(), hasSize(1));
		assertNull(program.getStates().iterator().next().getEndDate());
	}
	
	@Test
	public void getSortedStates_shouldReturnAllStatesEvenIfTwoHaveIdenticalStartAndEndDates() throws Exception {
		
		// this test written specifically to verify fix for https://tickets.openmrs.org/browse/TRUNK-3645
		Method getSortedStates = PatientProgram.class.getDeclaredMethod("getSortedStates");
		getSortedStates.setAccessible(true);
		assertNotNull(getSortedStates);
		
		Set<PatientState> patientStates = new HashSet<>();
		PatientState patientState = new PatientState();
		patientState.setStartDate(date);
		patientState.setEndDate(laterDate);
		patientState.setVoided(false);
		patientStates.add(patientState);
		
		PatientState patientState2 = new PatientState();
		patientState2.setStartDate(date);
		patientState2.setEndDate(laterDate);
		patientState2.setVoided(false);
		patientStates.add(patientState2);
		
		PatientState patientState3 = new PatientState();
		patientState3.setStartDate(earlierDate);
		patientState3.setEndDate(date);
		patientState3.setVoided(false);
		patientStates.add(patientState3);
		
		PatientProgram program = new PatientProgram();
		program.setStates(patientStates);
		
		// when
		List<PatientState> sortedStates = (List<PatientState>) getSortedStates.invoke(program);
		
		// then
		assertEquals(3, sortedStates.size());
		
	}
}
