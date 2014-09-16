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
import org.junit.Test;
import org.openmrs.Patient;

import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

/**
 * Consists of unit tests for the PatientFormController
 *
 * @see PatientFormController
 */
public class PatientFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see PatientFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	@Verifies(value = "void patient when void reason is not empty", method = "onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)")
	public void onSubmit_shouldVoidPatientWhenVoidReasonIsNotEmpty() throws Exception {
		
		Patient p = Context.getPatientService().getPatient(2);
		
		HttpServletResponse response = new MockHttpServletResponse();
		
		PatientFormController controller = (PatientFormController) applicationContext.getBean("patientForm");
		controller.setApplicationContext(applicationContext);
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("action", "Patient.void");
		request.setParameter("voidReason", "some reason");
		BindException errors = new BindException(p, "patient");
		ModelAndView modelAndview = controller.onSubmit(request, response, p, errors);
		
		Assert.assertTrue(p.isVoided());
	}
	
	/**
	 * @see PatientFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindException)
	 */
	@Test
	@Verifies(value = "not void patient when void reason is empty", method = "onSubmit(HttpServletRequest, HttpServletResponse, Object, BindException)")
	public void onSubmit_shouldNotVoidPatientWhenVoidReasonIsEmpty() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		
		HttpServletResponse response = new MockHttpServletResponse();
		
		PatientFormController controller = (PatientFormController) applicationContext.getBean("patientForm");
		controller.setApplicationContext(applicationContext);
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("action", "Patient.void");
		request.setParameter("voidReason", "");
		BindException errors = new BindException(p, "patient");
		ModelAndView modelAndview = controller.onSubmit(request, response, p, errors);
		
		Assert.assertTrue(!p.isVoided());
		String tmp = request.getSession().getAttribute(WebConstants.OPENMRS_ERROR_ATTR).toString();
		Assert.assertEquals(tmp, "Patient.error.void.reasonEmpty");
	}
}
