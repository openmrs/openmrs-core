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
package org.openmrs;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class PatientProgramTest {
	
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
}
