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
package org.openmrs.cohort;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.PatientCharacteristicFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.reporting.ProgramStatePatientFilter;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods in the {@link CohortUtil} class.
 */
public class CohortUtilTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link CohortUtil#parse(String)}
	 */
	@Test
	@Verifies(value = "should parse specification with and in it", method = "parse(String)")
	public void parse_shouldParseSpecificationWithAndInIt() throws Exception {
		// sets up the database
		{
			// Create a search called "Male" 
			PatientSearch ps = PatientSearch.createFilterSearch(PatientCharacteristicFilter.class);
			ps.addArgument("gender", "m", String.class);
			Context.getReportObjectService().saveReportObject(new PatientSearchReportObject("Male", ps));
		}
		{
			// Create a search called "EnrolledOnDate" with one parameter called untilDate
			PatientSearch ps = PatientSearch.createFilterSearch(ProgramStatePatientFilter.class);
			//ps.addArgument("program", Context.getProgramWorkflowService().getProgram("HIV PROGRAM").getProgramId().toString(), Integer.class);
			ps.addArgument("untilDate", "${date}", Date.class);
			Context.getReportObjectService().saveReportObject(new PatientSearchReportObject("EnrolledOnDate", ps));
		}
		
		// TODO this is the actual test.  Move the above logic into a dbunit xml file or use the standard xml run by BaseContextSensitiveTest
		PatientSearch ps = (PatientSearch) CohortUtil.parse("[Male] and [EnrolledOnDate|untilDate=${report.startDate}]");
		List<Object> list = ps.getParsedComposition();
		{
			PatientSearch test = (PatientSearch) list.get(0);
			assertEquals(test.getFilterClass(), PatientCharacteristicFilter.class);
			assertEquals(test.getArguments().iterator().next().getValue(), "m");
			assertEquals(test.getArguments().size(), 1);
		}
		assertEquals(list.get(1), PatientSetService.BooleanOperator.AND);
		{
			PatientSearch test = (PatientSearch) list.get(2);
			assertEquals(test.getFilterClass(), ProgramStatePatientFilter.class);
			assertEquals(test.getArguments().iterator().next().getValue(), "${date}");
			assertEquals(test.getArgumentValue("untilDate"), "${report.startDate}");
			assertEquals(test.getArguments().size(), 1);
		}
	}
	
}
