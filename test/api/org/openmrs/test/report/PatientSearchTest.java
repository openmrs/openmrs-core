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
package org.openmrs.test.report;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.Parameter;
import org.openmrs.reporting.ObsPatientFilter;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.SearchArgument;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;

/**
 *
 */
public class PatientSearchTest extends BaseContextSensitiveTest {

	/**
	 * Set up the database with the initial dataset before every test method
	 * in this class.
	 * 
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpBeforeTransaction()
	 */
	@Override
	protected void onSetUpInTransaction() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/test/report/include/PatientSearchTest.xml");
		authenticate();
	}
	
	/**
	 * TODO: Make this use asserts instead of printing to stdout
	 * 
	 * @throws Exception
	 */
	public void testParameters() throws Exception {
		ObsService obsService = Context.getObsService();
		
		// set the date of the first obs to be within the range of the param
		// (This date is outside of the "default" value, but inside the set value)
		Calendar inRange = new GregorianCalendar();
		inRange.add(Calendar.DATE, -45);
		Obs firstObs = obsService.getObs(1);
		firstObs.setObsDatetime(inRange.getTime());
		obsService.updateObs(firstObs);
		
		// set the date of the second obs to be OUT of the range of the param
		Calendar outOfRange = new GregorianCalendar();
		outOfRange.add(Calendar.DATE, -95);
		Obs secondObs = obsService.getObs(2);
		secondObs.setObsDatetime(outOfRange.getTime());
		obsService.updateObs(secondObs);
		
		// flush (commit) these changes to the database
		transactionManager.commit(transactionStatus);
		transactionManager.getTransaction(transactionDefinition);
		
		Map<Parameter, Object> globalParamValues = new HashMap<Parameter, Object>();
		{
			Parameter p = new Parameter("howManyDays", "How many days?", Integer.class, 30);
			globalParamValues.put(p, 60);
		}
		
		PatientSearch search = new PatientSearch();
		{
			search.setFilterClass(ObsPatientFilter.class);
			List<SearchArgument> args = new ArrayList<SearchArgument>();
			args.add(new SearchArgument("timeModifier", "ANY", PatientSetService.TimeModifier.class));
			args.add(new SearchArgument("question",
			                            Context.getConceptService().getConceptByName("CD4 COUNT").getConceptId().toString(),
			                            Concept.class));
			args.add(new SearchArgument("withinLastDays", "${howManyDays}", Integer.class));
			search.setArguments(args);
		}
		
		EvaluationContext ec = new EvaluationContext();
		for (Map.Entry<Parameter, Object> e : globalParamValues.entrySet())
			ec.addParameterValue(e.getKey(), e.getValue());
		
		for (Parameter p : search.getParameters()) {
			ec.addParameterValue(search, p, ec.evaluateExpression(p.getDefaultValue().toString()));
		}
		
		PatientFilter filterToRun = OpenmrsUtil.toPatientFilter(search, null, ec);
		Cohort result = filterToRun.filter(Context.getPatientSetService().getAllPatients(), ec);
		
		System.out.println("results is " + result.size());
		
		assertEquals(1, result.size());
		
		// make sure it was the patient#2 that was selected (the patient for the first obs)
		assertTrue(result.getMemberIds().contains(2));
	}
	
}
