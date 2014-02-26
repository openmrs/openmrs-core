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
package org.openmrs.web.patient;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.web.controller.patient.PatientDashboardGraphController;
import org.openmrs.web.controller.patient.PatientGraphData;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.ui.ModelMap;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Test for graphs on the patient dashboard
 */
public class PatientDashboardGraphControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * Test getting a concept by name and by partial name.
	 * 
	 * @see {@link PatientDashboardGraphController#showGraphData(Integer, Integer, ModelMap)}
	 */
	@Test
	@Verifies(value = "return json data with observation details and critical values for the concept", method = "showGraphData(Integer, Integer, ModelMap)")
	public void shouldReturnJSONWithPatientObservationDetails() throws Exception {
		executeDataSet("org/openmrs/api/include/ObsServiceTest-initial.xml");
		PatientDashboardGraphController controller = new PatientDashboardGraphController();
		
		long firstObsDate = new GregorianCalendar(2006, Calendar.FEBRUARY, 9).getTimeInMillis();
		long secondObsDate = new GregorianCalendar(2006, Calendar.FEBRUARY, 10).getTimeInMillis();
		
		ModelMap map = new ModelMap();
		controller.showGraphData(2, 1, map);
		PatientGraphData graph = (PatientGraphData) map.get("graph");
		String expectedData = String
		        .format(
		            "{\"absolute\":{\"high\":50.0,\"low\":2.0},\"critical\":{\"high\":null,\"low\":null},\"name\":\"Some concept name\",\"normal\":{\"high\":null,\"low\":null},\"data\":[[%d,2.0],[%d,1.0]],\"units\":null}",
		            secondObsDate, firstObsDate);
		Assert.assertEquals(expectedData, graph.toString());
	}
	
	/**
	 * Test the path of the form for rendering the json data
	 * 
	 * @see {@link PatientDashboardGraphController#showGraphData(Integer, Integer, ModelMap)}
	 */
	@Test
	@Verifies(value = "return form for rendering the json data", method = "showGraphData(Integer, Integer, ModelMap)")
	public void shouldDisplayPatientDashboardGraphForm() throws Exception {
		executeDataSet("org/openmrs/api/include/ObsServiceTest-initial.xml");
		Assert.assertEquals("patientGraphJsonForm", new PatientDashboardGraphController()
		        .showGraphData(2, 1, new ModelMap()));
	}
}
