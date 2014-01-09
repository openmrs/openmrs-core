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
package org.openmrs.web.controller.person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests for the {@link AddPersonController} which handles the Add Person.form page.
 */
public class AddPersonControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see AddPersonController#formBackingObject(HttpServletRequest)
	 * @verifies catch an invalid birthdate
	 */
	@Test
	public void formBackingObject_shouldCatchAnInvalidBirthdate() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		request.setParameter("addName", "Gayan Perera");
		request.setParameter("addBirthdate", "03/07/199s");
		request.setParameter("addGender", "M");
		request.setParameter("personType", "patient");
		request.setParameter("viewType", "edit");
		
		AddPersonController controller = (AddPersonController) applicationContext.getBean("addPerson");
		ModelAndView mav = controller.handleRequest(request, response);
		assertNotNull(mav);
		assertEquals("Person.birthdate.required", mav.getModel().get("errorMessage"));
	}
	
	/**
	 * @see AddPersonController#formBackingObject(HttpServletRequest)
	 * @verifies catch pass for a valid birthdate
	 */
	@Test
	public void formBackingObject_shouldCatchPassForAValidBirthdate() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		request.setParameter("addName", "Gayan Perera");
		request.setParameter("addBirthdate", "03/07/1990");
		request.setParameter("addGender", "M");
		request.setParameter("personType", "patient");
		request.setParameter("viewType", "edit");
		
		AddPersonController controller = (AddPersonController) applicationContext.getBean("addPerson");
		ModelAndView mav = controller.handleRequest(request, response);
		
		assertNotNull(mav);
		assertTrue(mav.getModel().isEmpty());
	}
}
