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

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.Duration;
import org.openmrs.logic.LogicCriteria;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.result.Result;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * TODO clean up and add tests for all methods in ObsService
 */
public class LogicBasicTest extends BaseContextSensitiveTest {

	@Override
	protected void onSetUpInTransaction() throws Exception {
		// initializeInMemoryDatabase();
		// executeDataSet("org/openmrs/test/include/LargeTestDatabase.xml");
		authenticate();
	}

	/**
	 * @see org.openmrs.test.BaseContextSensitiveTest#useInMemoryDatabase()
	 */
	@Override
	public Boolean useInMemoryDatabase() {
		// TODO Auto-generated method stub
		return false;
	}

	public void testCheckWhetherRecentResultsExist() throws Exception {
		// Result = NO CD4 COUNT IN LAST 6 MONTHS
		Patient patient = Context.getPatientService().getPatient(2);
		Result result = Context.getLogicService()
		                       .eval(patient,
		                             new LogicCriteria("CD4 COUNT").within(Duration.months(6))
		                                                           .exists());
	}

	public void testFilterByNumericResult() throws Exception {
		// Result = LAST CD4 COUNT < 350
		Patient patient = Context.getPatientService().getPatient(2);
		Result result = Context.getLogicService()
		                       .eval(patient,
		                             new LogicCriteria("CD4 COUNT").last()
		                                                           .lt(350));

	}

	public void testFetchActiveMedications() throws Exception {
		// Result = ACTIVE MEDICATIONS
		Patient patient = Context.getPatientService().getPatient(2);
		Result result = Context.getLogicService()
		                       .eval(patient,
		                             new LogicCriteria("CURRENT ANTIRETROVIRAL DRUGS USED FOR TREATMENT"));
	}

	public void testFilterUsingComposition() throws Exception {
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
	public void testSimpleLogic() throws Exception {

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
