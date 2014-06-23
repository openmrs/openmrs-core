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
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
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
