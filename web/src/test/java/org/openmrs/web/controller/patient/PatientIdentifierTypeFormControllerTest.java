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
package org.openmrs.web.controller.patient;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

public class PatientIdentifierTypeFormControllerTest extends BaseWebContextSensitiveTest {
	
	PatientService patientService;
	
	@Test
	public void shouldSavePAtientIdentifierTypeWhenPatientIdentifierTypesAreNotLocked() throws Exception {
		
		PatientIdentifierTypeFormController controller = (PatientIdentifierTypeFormController) applicationContext
		        .getBean("patientIdentifierTypeForm");
		controller.setApplicationContext(applicationContext);
		controller.setSuccessView("index.htm");
		controller.setFormView("PatientIdentifierType.form");
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET",
		        "/admin/patients/patientIdentifierType.form?patientIdentifierTypeId=2");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		request.setMethod("POST");
		
		request.addParameter("action", "Save PatientIdentifierType");
		
		ModelAndView mav = controller.handleRequest(request, response);
		
		patientService = Context.getPatientService();
		
		Assert.assertSame(controller.getFormView(), mav.getViewName());
		Assert.assertNotEquals("The save attempt should have succeeded!", "index.htm", mav.getViewName());
		Assert.assertNotNull(patientService.getPatientIdentifierType(2));
	}
	
	@Test
	public void shouldNotDeletePatientIdentifierTypeWhenPatientIdentifierTypesAreLocked() throws Exception {
		
		// dataset locks patient identifier types
		executeDataSet("org/openmrs/web/patient/include/PatientIdentifierTypeFormControllerTest.xml");
		
		PatientIdentifierTypeFormController controller = (PatientIdentifierTypeFormController) applicationContext
		        .getBean("patientIdentifierTypeForm");
		controller.setApplicationContext(applicationContext);
		controller.setSuccessView("index.htm");
		controller.setFormView("PatientIdentifierType.form");
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET",
		        "/admin/patients/patientIdentifierType.form?patientIdentifierTypeId=2");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		request.setMethod("POST");
		
		request.addParameter("action", "Delete PatientIdentifierType");
		
		// send the parameters to the controller
		ModelAndView mav = controller.handleRequest(request, response);
		
		patientService = Context.getPatientService();
		
		Assert.assertEquals("The purge attempt should have failed!", "PatientIdentifierType.form", mav.getViewName());
		Assert.assertSame(controller.getFormView(), mav.getViewName());
		Assert.assertNotNull(patientService.getPatientIdentifierType(2));
	}
}
