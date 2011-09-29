package org.openmrs.web.dwr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

public class DWRProgramWorkflowServiceTest extends BaseWebContextSensitiveTest {
	
	private DWRProgramWorkflowService dwrProgramWorkflowService;
	
	protected static final String PROGRAM_WITH_OUTCOMES_XML = "org/openmrs/web/dwr/include/DWRProgramWorkflowServiceTest-initialData.xml";
	
	@Before
	public void setUp() throws Exception {
		dwrProgramWorkflowService = new DWRProgramWorkflowService();
	}
	
	@Test
	@Verifies(value = "should get possible outcomes for a program", method = "getPossibleOutcomes()")
	public void getPossibleOutcomes_shouldReturnOutcomeConceptsFromProgram() throws Exception {
		executeDataSet(PROGRAM_WITH_OUTCOMES_XML);
		Vector<ListItem> possibleOutcomes = dwrProgramWorkflowService.getPossibleOutcomes(4);
		assertFalse(possibleOutcomes.isEmpty());
		assertEquals(2, possibleOutcomes.size());
	}
}
