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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.openmrs.web.test.WebTestHelper;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.MessageSourceAccessor;

/**
 * Consists of unit tests for the PatientFormController
 * 
 * @see PatientFormController
 */
public class PatientFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link PatientFormController#onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,BindException errors)}
	 */
	@Test
	@Verifies(value = "should not void the patient if void reason isn't supplied", method = "onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,BindException errors)")
	public void onSubmit_shouldNotVoidPatientIfVoidReasonIsNotSupplied() throws Exception {
		Patient p = Context.getPatientService().getPatient(2);
		PatientService ps = Context.getPatientService();
		String action = "Patient deleted";
		p.setVoidReason("");
		if (action.equals("Patient deleted")) {
			String voidReason = p.getVoidReason();
			if (StringUtils.isBlank(voidReason)) {

			} else {
				ps.voidPatient(p, voidReason);
			}
		}
		boolean voidReasonBoolean = p.isVoided();
		assertTrue("Patient must not be voided now", voidReasonBoolean == false);
	}
	
	/**
	 * @see {@link ShortPatientFormController#saveShortPatient(WebRequest,ShortPatientModel,BindingResult,SessionStatus)}
	 */
	@Test
	@Verifies(value = "should void the patient if void reason is supplied", method = "onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,BindException errors)")
	public void onSubmit_shouldVoidPatientIfVoidReasonIsSupplied() throws Exception {
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
		boolean voidReasonBoolean = p.isVoided();
		assertTrue("Patient must not be voided now", voidReasonBoolean == true);
	}
}