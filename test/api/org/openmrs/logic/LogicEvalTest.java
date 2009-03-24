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
package org.openmrs.logic;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.logic.rule.ReferenceRule;

/**
 * TODO Add more tests here
 */
public class LogicEvalTest extends LogicBaseContextSensitiveTest {
	
	@Before
	public void runBeforeEachTest() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/logic/include/LogicTests-patients.xml");
		
		authenticate();
	}
	
	/**
	 * TODO: fix this to use asserts instead of just printing to stdout
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSimpleLogic() throws Exception {
		
		// add temperature as a concept in the database
		executeDataSet("org/openmrs/logic/include/LogicEvalTest.testSimpleLogic.xml");
		
		// RuleFactory needs to be cleaned up so that it picks up the new concepts
		
		// register temperature as a rule if needed
		Concept temperature = Context.getConceptService().getConcept(12232);
		String name = temperature.getBestName(Context.getLocale()).getName();
		try {
			Context.getLogicService().getRule(name);
		}
		catch (LogicException e) {
			// if not found, add it
			Rule rule = new ReferenceRule("obs." + name);
			Context.getLogicService().addRule(name, rule);
		}
		
		//Patient p = Context.getPatientService().getPatient(2);
		Cohort cohort = new Cohort();
		cohort.addMember(2);
		
		//System.out.print("Patients: ");
		//for (int id : cohort.getMemberIds())
		//	System.out.print(id + " ");
		//System.out.println();
		long l = System.currentTimeMillis();
		//System.out.println(new Date());
		Map<Integer, Result> m = Context.getLogicService().eval(cohort, "TEMPERATURE (C)");
		//System.out.println(m.toString());
		//System.out.println(System.currentTimeMillis() - l);
		
	}
}
