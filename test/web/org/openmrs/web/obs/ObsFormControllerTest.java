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
package org.openmrs.web.obs;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.openmrs.web.controller.observation.ObsFormController;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

/**
 * Test the methods on the {@link org.openmrs.web.controller.observation.ObsFormController}
 */
public class ObsFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * Tests that an "encounterId" parameter sets the obs.encounter attribute on an empty obs
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetObsFormWithEncounterFilledIn() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		request.setParameter("encounterId", "3");
		
		HttpServletResponse response = new MockHttpServletResponse();
		
		ObsFormController controller = new ObsFormController();
		
		ModelAndView modelAndView = controller.handleRequest(request, response);
		
		// make sure there is an "encounterId" element on the obs
		Obs commandObs = (Obs) modelAndView.getModel().get("command");
		Assert.assertNotNull(commandObs.getEncounter());
		
	}
	
	/**
	 * Test to make sure a new patient form can save a person relationship
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSaveObsFormNormally() throws Exception {
		ObsService os = Context.getObsService();
		
		// set up the controller
		ObsFormController controller = new ObsFormController();
		controller.setApplicationContext(applicationContext);
		controller.setSuccessView("encounter.form");
		controller.setFormView("obs.form");
		
		// set up the request and do an initial "get" as if the user loaded the
		// page for the first time
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/observations/obs.form");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		// set this to be a page submission
		request.setMethod("POST");
		
		// add all of the parameters that are expected
		// all but the relationship "3a" should match the stored data
		request.addParameter("person", "2");
		request.addParameter("encounter", "3");
		request.addParameter("location", "1");
		request.addParameter("obsDatetime", "05/05/2005");
		request.addParameter("concept", "4"); // CIVIL_STATUS (conceptid=4) concept
		request.addParameter("valueCoded", "5"); // conceptNameId=2458 for SINGLE concept
		request.addParameter("saveObs", "Save Obs"); // so that the form is processed
		
		// send the parameters to the controller
		controller.handleRequest(request, response);
		
		// make sure an obs was created
		List<Obs> obsForPatient = os.getObservationsByPerson(new Person(2));
		assertEquals(1, obsForPatient.size());
		assertEquals(new Encounter(3), obsForPatient.get(0).getEncounter());
		assertEquals(new Location(1), obsForPatient.get(0).getLocation());
	}
	
}
