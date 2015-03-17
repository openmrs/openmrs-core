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

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.junit.Assert;
import org.junit.Test;

public class PatientProgramTest {
	
	private Date date = new Date();
	
	private Date earlierDate = new Date(date.getTime() - 10000);
	
	private Date laterDate = new Date(date.getTime() + 10000);
	
	/**
	 * @see PatientProgram#voidLastState(ProgramWorkflow,User,Date,String)
	 * @verifies void state with endDate null if startDates equal
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
		
		//when
		program.voidLastState(workflow, new User(), new Date(), "");
		
		//then
		Assert.assertTrue(state1.isVoided());
		Assert.assertFalse(state2.isVoided());
	}
	
	@Test
	public void getSortedStates_shouldReturnAllStatesEvenIfTwoHaveIdenticalStartAndEndDates() throws Exception {
		
		// this test written specifically to verify fix for https://tickets.openmrs.org/browse/TRUNK-3645
		Method getSortedStates = PatientProgram.class.getDeclaredMethod("getSortedStates");
		getSortedStates.setAccessible(true);
		Assert.assertNotNull(getSortedStates);
		
		Set<PatientState> patientStates = new HashSet<PatientState>();
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
		Assert.assertEquals(3, sortedStates.size());
		
	}
}
