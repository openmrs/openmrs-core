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
package org.openmrs.test.reporting;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;

import org.openmrs.Drug;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.reporting.DrugOrderFilter;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 *
 */
public class PatientFilterTest extends BaseContextSensitiveTest {

	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	
	public void testDrugOrderFilter() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/test/reporting/include/PatientFilterTest.xml");
		authenticate();
		
		EvaluationContext ec = new EvaluationContext();
		Drug inh = Context.getConceptService().getDrug("INH 300mg");
		DrugOrderFilter filter = new DrugOrderFilter();
		filter.setAnyOrAll(PatientSetService.GroupMethod.ANY);
		filter.setDrugList(Collections.singletonList(inh));

		assertEquals("No dates should get 1", 1, filter.filter(null, ec).size());
		
		filter.setUntilDate(ymd.parse("2004-06-01"));
		assertEquals("Until before should get 0", 0, filter.filter(null, ec).size());
		filter.setUntilDate(ymd.parse("2005-06-01"));
		assertEquals("Until during should get 1", 1, filter.filter(null, ec).size());
		filter.setUntilDate(ymd.parse("2006-06-01"));
		assertEquals("Until after should get 1", 1, filter.filter(null, ec).size());
		filter.setUntilDate(null);
		
		filter.setSinceDate(ymd.parse("2004-06-01"));
		assertEquals("since before should get 1", 1, filter.filter(null, ec).size());
		filter.setSinceDate(ymd.parse("2005-06-01"));
		assertEquals("since during should get 1", 1, filter.filter(null, ec).size());
		filter.setSinceDate(ymd.parse("2006-06-01"));
		assertEquals("since after should get 0", 0, filter.filter(null, ec).size());
		filter.setSinceDate(null);
	}
	
}
