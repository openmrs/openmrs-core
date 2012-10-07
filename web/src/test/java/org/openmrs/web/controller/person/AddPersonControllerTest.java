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
package org.openmrs.web.controller.person;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;

/**
 * Tests for the {@link AddPersonController} which handles the Add Person.form page.
 */
public class AddPersonControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link AddPersonController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "check BirthDate properly entered or not", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldAcceptBirthDateEnteredCorrectly() throws Exception {
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		HttpServletResponse response = new MockHttpServletResponse();
		
		request.setParameter("addName", "Gayan Perera");
		request.setParameter("addBirthdate", "03/07/1990");
		request.setParameter("addGender", "M");
		request.setParameter("personType", "patient");
		request.setParameter("viewType", "edit");
		
		int previousPersonCount = Context.getPersonService().getPeople("Gayan", false).size();
		AddPersonController controller = (AddPersonController) applicationContext.getBean("addPerson");
		controller.handleRequest(request, response);
		
		Assert.assertEquals(previousPersonCount, Context.getPersonService().getPeople("Gayan", false).size());
	}
}
