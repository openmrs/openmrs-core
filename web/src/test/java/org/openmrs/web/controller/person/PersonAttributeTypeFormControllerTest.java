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

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

public class PersonAttributeTypeFormControllerTest extends BaseWebContextSensitiveTest {
	
	@Test
	public void shouldNotSavePersonAttributeTypeWhenPersonAttributeTypesAreLocked() throws Exception {
		// dataset to lock person attribute types
		executeDataSet("org/openmrs/web/controller/include/PersonAttributeTypeFormControllerTest.xml");
		
		PersonService personService = Context.getPersonService();
		
		PersonAttributeTypeFormController controller = (PersonAttributeTypeFormController) applicationContext
		        .getBean("personAttributeTypeForm");
		controller.setApplicationContext(applicationContext);
		controller.setFormView("PersonAttributeType.form");
		controller.setSuccessView("index.htm");
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET",
		        "/admin/persons/personAttributeType.form?personAttributeTypeId=1");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		request.setMethod("POST");
		
		request.addParameter("action", "Save personAttributeType");
		
		ModelAndView mav = controller.handleRequest(request, response);
		
		Assert.assertEquals("The save attempt should have failed!", "PersonAttributeType.form", mav.getViewName());
		Assert.assertSame(controller.getFormView(), mav.getViewName());
		Assert.assertNotNull(personService.getPersonAttributeType(1));
	}
	
	@Test
	public void shouldDeletePersonAttributeTypeWhenPersonAttributeTypesAreNotLocked() throws Exception {
		PersonService personService = Context.getPersonService();
		
		PersonAttributeTypeFormController controller = (PersonAttributeTypeFormController) applicationContext
		        .getBean("personAttributeTypeForm");
		controller.setApplicationContext(applicationContext);
		controller.setSuccessView("index.htm");
		controller.setFormView("PersonAttributeType.form");
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET",
		        "/admin/persons/personAttributeType.form?personAttributeTypeId=1");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		request.setMethod("POST");
		
		request.addParameter("action", "Delete personAttributeType");
		
		ModelAndView mav = controller.handleRequest(request, response);
		
		Assert.assertSame(controller.getFormView(), mav.getViewName());
		Assert.assertNotEquals("The save attempt should have passed!", "index.htm", mav.getViewName());
		Assert.assertNotNull(personService.getPersonAttributeType(1));
	}
}
