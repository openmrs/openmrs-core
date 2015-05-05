/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.patient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.web.controller.person.PersonAttributeTypeFormController;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

/**
 * Tests the {@link PatientIdentifierTypeFormController}
 */
public class PatientIdentifierTypeFormControllerTest extends BaseWebContextSensitiveTest {
	
	private MockHttpServletRequest request;
	
	private HttpServletResponse response;
	
	private PatientIdentifierTypeFormController controller;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("org/openmrs/web/patient/include/PatientIdentifierTypeFormControllerTest.xml");
		controller = (PatientIdentifierTypeFormController) applicationContext.getBean("patientIdentifierTypeForm");
		controller.setFormView("index.htm");
		controller.setSuccessView("patientIdentifierType.form");
		
		request = new MockHttpServletRequest("POST", "/admin/patients/patientIdentifierType.form?patientIdentifierTypeId=1");
		request.setSession(new MockHttpSession(null));
		request.setContentType("application/x-www-form-urlencoded");
		request.addParameter("name", "TRUNK");
		response = new MockHttpServletResponse();
	}
	
	@Test
	public void shouldNotSavePatientIdentifierTypeWhenPatientIdentifierTypesAreLocked() throws Exception {
		request.addParameter("save", "Save Identifier Type");
		
		ModelAndView mav = controller.handleRequest(request, response);
		Assert.assertEquals("The save attempt should have failed!", "index.htm", mav.getViewName());
		Assert.assertNotEquals("patientIdentifierType.form", mav.getViewName());
		Assert.assertNotNull(Context.getPersonService().getPersonAttributeType(1));
	}
	
	@Test
	public void shouldNotRetirePatientIdentifierTypeWhenPatientIdentifierTypesAreLocked() throws Exception {
		request.addParameter("retire", "Retire Identifier Type");
		request.addParameter("retireReason", "Same reason");
		
		ModelAndView mav = controller.handleRequest(request, response);
		Assert.assertEquals("The retire attempt should have failed!", "index.htm", mav.getViewName());
		Assert.assertNotEquals("patientIdentifierType.form", mav.getViewName());
		Assert.assertNotNull(Context.getPersonService().getPersonAttributeType(1));
	}
	
	@Test
	public void shouldNotUnretirePatientIdentifierTypeWhenPatientIdentifierTypesAreLocked() throws Exception {
		request.addParameter("unretire", "Unretire Identifier Type");
		
		ModelAndView mav = controller.handleRequest(request, response);
		Assert.assertEquals("The unretire attempt should have failed!", "index.htm", mav.getViewName());
		Assert.assertNotEquals("patientIdentifierType.form", mav.getViewName());
		Assert.assertNotNull(Context.getPersonService().getPersonAttributeType(1));
	}
	
	@Test
	public void shouldNotDeletePatientIdentifierTypeWhenPatientIdentifierTypesAreLocked() throws Exception {
		request.addParameter("purge", "Delete Identifier Type");
		
		ModelAndView mav = controller.handleRequest(request, response);
		Assert.assertEquals("The delete attempt should have failed!", "index.htm", mav.getViewName());
		Assert.assertNotEquals("patientIdentifierType.form", mav.getViewName());
		Assert.assertNotNull(Context.getPersonService().getPersonAttributeType(1));
	}
}
