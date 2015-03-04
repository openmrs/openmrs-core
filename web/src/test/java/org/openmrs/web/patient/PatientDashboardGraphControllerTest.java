/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
		            "{\"absolute\":{\"high\":50.0,\"low\":2.0},\"critical\":{\"high\":null,\"low\":null},\"name\":\"Some concept name\",\"normal\":{\"high\":null,\"low\":null},\"data\":[[%d,2.0],[%d,1.0]],\"units\":\"\"}",
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
