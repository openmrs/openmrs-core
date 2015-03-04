/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;

import org.junit.Test;
import org.openmrs.Drug;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 *
 */
public class PatientFilterTest extends BaseContextSensitiveTest {
	
	DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
	
	@Test
	public void shouldDrugOrderFilter() throws Exception {
		initializeInMemoryDatabase();
		executeDataSet("org/openmrs/reporting/include/PatientFilterTest.xml");
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
