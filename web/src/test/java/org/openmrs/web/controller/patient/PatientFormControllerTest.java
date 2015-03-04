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
