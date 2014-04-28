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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientIdentifierType.LocationBehavior;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.test.Verifies;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.openmrs.web.test.WebTestHelper;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests for the {@link PatientFormController} which handles the patient.form page.
 */
public class PatientFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link PatientFormController#onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,BindException errors)}
	 */
	@Test
	@Verifies(value = "should not void the patient if void reason isn't supplied", method = "onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,BindException errors)")
	public void onSubmit_shouldNotVoidPatientIfVoidReasonIsEmpty() throws Exception {
		
		Patient p = Context.getPatientService().getPatient(2);
		p.setVoidReason("");
		String voidReasonit = "";
		Object obj = new Object();
		obj = (Object) p;
		HttpServletResponse response = new MockHttpServletResponse();
		BindException errors = new BindException(p, "patient");
		
		PatientFormController controller = (PatientFormController) applicationContext.getBean("patientForm");
		controller.setApplicationContext(applicationContext);
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		request.setParameter("patientId", p.getPatientId().toString());
		request.setParameter("voidReason", voidReasonit);
		request.setParameter("isVoided", "true");
		ModelAndView modelAndview = controller.handleRequest(request, response);
		BeanPropertyBindingResult bindingResult = (BeanPropertyBindingResult) modelAndview.getModel().get(
		    "org.springframework.validation.BindingResult.patient");
		Assert.assertTrue(bindingResult.hasFieldErrors("voidReason"));
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)}
	 */
	/*@Test
	@Verifies(value = "should void the patient if void reason is supplied", method = "onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,BindException errors)")
	public void onSubmit_shouldVoidPatientIfVoidReasonIsNotEmpty() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		PatientService ps = Context.getPatientService();
		String action = "Patient deleted";
		p.setVoidReason("Patient must be voided");
		if (action.equals("Patient deleted")) {
			String voidReason = p.getVoidReason();
			if (StringUtils.isBlank(p.getVoidReason())) {} else {
				ps.voidPatient(p, voidReason);
			}
		}
		assertTrue("Patient must not be voided now", p.isVoided() == true);
	}*/
}
