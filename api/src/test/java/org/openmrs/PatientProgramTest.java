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
