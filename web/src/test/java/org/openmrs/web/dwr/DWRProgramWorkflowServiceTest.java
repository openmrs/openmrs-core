/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
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
