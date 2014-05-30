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
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests the {@link PersonAttributeTypeFormController}
 */
public class PersonAttributeTypeFormControllerTest extends BaseWebContextSensitiveTest {
	
	private MockHttpServletRequest request;
	
	private HttpServletResponse response;
	
	private PersonAttributeTypeFormController controller;
	
	@Before
	public void setup() throws Exception {
		executeDataSet("org/openmrs/web/controller/include/PersonAttributeTypeFormControllerTest.xml");
		controller = (PersonAttributeTypeFormController) applicationContext.getBean("personAttributeTypeForm");
		controller.setFormView("index.htm");
		controller.setSuccessView("PersonAttributeType.form");
		
		request = new MockHttpServletRequest("POST", "/admin/person/personAttributeType.form?personAttributeTypeId=1");
		request.setSession(new MockHttpSession(null));
		request.setContentType("application/x-www-form-urlencoded");
		request.addParameter("name", "TRUNK");
		request.addParameter("format", "java.lang.String");
		response = new MockHttpServletResponse();
	}
	
	@Test
	public void shouldNotSavePersonAttributeTypeWhenPersonAttributeTypesAreLocked() throws Exception {
		request.addParameter("save", "Save Person Attribute Type");
		
		ModelAndView mav = controller.handleRequest(request, response);
		Assert.assertEquals("The save attempt should have failed!", "index.htm", mav.getViewName());
		Assert.assertNotEquals("PersonAttributeType.form", mav.getViewName());
		Assert.assertNotNull(Context.getPersonService().getPersonAttributeType(1));
	}
	
	@Test
	public void shouldNotDeletePersonAttributeTypeWhenPersonAttributeTypesAreLocked() throws Exception {
		request.addParameter("purge", "Delete Person Attribute Type");
		
		ModelAndView mav = controller.handleRequest(request, response);
		Assert.assertEquals("The delete attempt should have failed!", "index.htm", mav.getViewName());
		Assert.assertNotEquals("PersonAttributeType.form", mav.getViewName());
		Assert.assertNotNull(Context.getPersonService().getPersonAttributeType(1));
	}
	
	@Test
	public void shouldNotRetirePersonAttributeTypeWhenPersonAttributeTypesAreLocked() throws Exception {
		request.addParameter("retire", "Retire Person Attribute Type");
		request.addParameter("retireReason", "Same reason");
		
		ModelAndView mav = controller.handleRequest(request, response);
		Assert.assertEquals("The retire attempt should have failed!", "index.htm", mav.getViewName());
		Assert.assertNotEquals("PersonAttributeType.form", mav.getViewName());
		Assert.assertNotNull(Context.getPersonService().getPersonAttributeType(1));
	}
	
	@Test
	public void shouldNotUnretirePersonAttributeTypeWhenPersonAttributeTypesAreLocked() throws Exception {
		request.addParameter("unretire", "Unretire Person Attribute Type");
		
		ModelAndView mav = controller.handleRequest(request, response);
		Assert.assertEquals("The unretire attempt should have failed!", "index.htm", mav.getViewName());
		Assert.assertNotEquals("PersonAttributeType.form", mav.getViewName());
		Assert.assertNotNull(Context.getPersonService().getPersonAttributeType(1));
	}
}
