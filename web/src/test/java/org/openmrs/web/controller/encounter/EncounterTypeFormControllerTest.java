/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.encounter;

import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.FormView;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

public class EncounterTypeFormControllerTest extends BaseWebContextSensitiveTest {
	
	@Test
	public void shouldNotDeleteEncounterTypeWhenEncounterTypesAreLocked() throws Exception {
		// dataset to lock encounter types
		executeDataSet("org/openmrs/web/encounter/include/EncounterTypeFormControllerTest.xml");
		
		EncounterService es = Context.getEncounterService();
		
		EncounterTypeFormController controller = (EncounterTypeFormController) applicationContext
		        .getBean("encounterTypeForm");
		controller.setApplicationContext(applicationContext);
		controller.setSuccessView("index.htm");
		controller.setFormView("EncounterType.form");
		
		// setting up the request and doing an initial "get" equivalent to the user loading the page for the first time
		MockHttpServletRequest request = new MockHttpServletRequest("GET",
		        "/admin/encounters/encounterType.form?encounterTypeId=1");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		// set this to be a page submission
		request.setMethod("POST");
		
		request.addParameter("action", "Delete EncounterType"); // so that the form is processed
		
		// send the parameters to the controller
		ModelAndView mav = controller.handleRequest(request, response);
		
		Assert.assertEquals("The purge attempt should have failed!", "EncounterType.form", mav.getViewName());
		Assert.assertSame(controller.getFormView(), mav.getViewName());
		Assert.assertNotNull(es.getEncounterType(1));
	}
	
	@Test
	public void shouldSaveEncounterTypeWhenEncounterTypesAreNotLocked() throws Exception {
		EncounterService es = Context.getEncounterService();
		
		EncounterTypeFormController controller = (EncounterTypeFormController) applicationContext
		        .getBean("encounterTypeForm");
		controller.setApplicationContext(applicationContext);
		controller.setSuccessView("index.htm");
		controller.setFormView("EncounterType.form");
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET",
		        "/admin/encounters/encounterType.form?encounterTypeId=1");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		request.setMethod("POST");
		
		request.addParameter("action", "Save EncounterType");
		
		ModelAndView mav = controller.handleRequest(request, response);
		
		Assert.assertSame(controller.getFormView(), mav.getViewName());
		Assert.assertNotEquals("The save attempt should have passed!", "index.htm", mav.getViewName());
		Assert.assertNotNull(es.getEncounterType(1));
	}
}
