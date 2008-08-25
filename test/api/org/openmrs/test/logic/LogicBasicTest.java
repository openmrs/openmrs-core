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
package org.openmrs.test.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.Duration;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.result.Result;
import org.openmrs.test.testutil.BaseContextSensitiveTest;
import org.openmrs.test.testutil.SkipBaseSetup;

/**
 * TODO clean up and add tests for all methods in ObsService
 */
@SkipBaseSetup
public class LogicBasicTest extends BaseContextSensitiveTest {

	@Before
	public void runBeforeEachTest() throws Exception {
		initializeInMemoryDatabase();
		//executeDataSet("org/openmrs/test/logic/include/LargeTestDatabase.xml");
		executeDataSet("org/openmrs/test/logic/include/LogicTests-patients.xml");
		
		authenticate();
	}

	@Test
	public void shouldCheckWhetherRecentResultsExist() throws Exception {
		executeDataSet("org/openmrs/test/logic/include/LogicBasicTest.concepts.xml");
				
		// Result = NO CD4 COUNT IN LAST 6 MONTHS
		Patient patient = Context.getPatientService().getPatient(2);
		Result result = Context.getLogicService()
		                       .eval(patient,
		                             new LogicCriteria("CD4 COUNT").within(Duration.months(6))
		                                                           .exists());
		
		assertFalse(result.exists());
	}
	
	/**
	 * This test looks for "LAST CD4 COUNT < 350".
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFilterByNumericResult() throws Exception {
		executeDataSet("org/openmrs/test/logic/include/LogicBasicTest.concepts.xml");
		// Result = LAST CD4 COUNT < 350
		Patient patient = Context.getPatientService().getPatient(3);
		Result result = Context.getLogicService()
		                       .eval(patient,
		                             new LogicCriteria("CD4 COUNT").last()
		                                                           .lt(350));
		assertTrue(result.exists());
		assertEquals(125.0, result.toNumber(), 0);
	}
	
	/**
	 * This test looks for "LAST CD4 COUNT < 350".  The catch is that
	 * the last cd4 count for patient #2 is voided
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldFilterByNumericResultWithVoidedObs() throws Exception {
		executeDataSet("org/openmrs/test/logic/include/LogicBasicTest.concepts.xml");
		// Result = LAST CD4 COUNT < 350
		Patient patient = Context.getPatientService().getPatient(2);
		Result result = Context.getLogicService()
		                       .eval(patient,
		                             new LogicCriteria("CD4 COUNT").last()
		                                                           .lt(350));
		assertTrue("A result should exist", result.exists());
		assertEquals(100.0, result.toNumber().doubleValue());
	}

	@Test
	public void shouldFetchActiveMedications() throws Exception {
		executeDataSet("org/openmrs/test/logic/include/LogicBasicTest.concepts.xml");
		// Result = ACTIVE MEDICATIONS
		Patient patient = Context.getPatientService().getPatient(2);
		Result result = Context.getLogicService()
		                       .eval(patient,
		                             new LogicCriteria("CURRENT ANTIRETROVIRAL DRUGS USED FOR TREATMENT"));
	}

	@Test
	public void shouldFilterUsingComposition() throws Exception {
		executeDataSet("org/openmrs/test/logic/include/LogicBasicTest.concepts.xml");
		// LAST CD4 COUNT < 350 AND NO ACTIVE MEDICATIONS
		Patient patient = Context.getPatientService().getPatient(2);
		Result result = Context.getLogicService()
		                       .eval(patient,
		                             new LogicCriteria("CD4 COUNT").last()
		                                                           .lt(350)
		                                                           .and(new LogicCriteria("%%orders.ACTIVE MEDS").notExists()));
		
	}

	/**
	 * Creates then updates an obs
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void shouldSimpleLogic() throws Exception {

		// Patient p = Context.getPatientService().getPatient(2);
		Cohort cohort = new Cohort();
		ArrayList<Integer> ids = new java.util.ArrayList(Context.getPatientSetService()
		                                                        .getAllPatients()
		                                                        .getMemberIds());
		for (int i = 1; i < ids.size(); i++) {
			cohort.addMember(ids.get(i));
		}
		cohort.addMember(2);
		long l = System.currentTimeMillis();
		System.out.println(new Date());
		LogicService ls = Context.getLogicService();
		Map<Integer, Result> m = ls.eval(cohort, "WEIGHT (KG)");
		System.out.println(m.toString());
		System.out.println(String.valueOf(System.currentTimeMillis() - l)
		        + " milliseconds");

	}
}
