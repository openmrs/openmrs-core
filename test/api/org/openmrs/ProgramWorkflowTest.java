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

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;

/**
 * Tests methods in the {@link ProgramWorkflow} class
 */
public class ProgramWorkflowTest {
	
	/**
	 * @see {@link ProgramWorkflow#getSortedStates()}
	 */
	@Test
	@Verifies(value = "should sort names containing numbers intelligently", method = "getSortedStates()")
	public void getSortedStates_shouldSortNamesContainingNumbersIntelligently() throws Exception {
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
			if (x == 1)
				Assert.assertEquals("Group 2", state.getConcept().getBestName(null).getName());
			else if (x == 2)
				Assert.assertEquals("Group 10", state.getConcept().getBestName(null).getName());
			else
				Assert.fail("Wha?!");
			x++;
		}
		
	}
}
