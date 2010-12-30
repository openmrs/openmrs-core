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
package org.openmrs.web.controller.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Field;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tests the {@link FieldFormController} class.
 */
public class FieldFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link FieldFormController#formBackingObject(HttpServletRequest)}
	 */
	// @Transactional annotation needed because the parent class is @Transactional and so screws propagates to this readOnly test
	@Transactional(readOnly = true)
	@Test
	@Verifies(value = "should get field", method = "formBackingObject(HttpServletRequest)")
	public void formBackingObject_shouldGetField() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		request.setParameter("fieldId", "1");
		
		HttpServletResponse response = new MockHttpServletResponse();
		
		FieldFormController controller = (FieldFormController) applicationContext.getBean("fieldForm");
		
		ModelAndView modelAndView = controller.handleRequest(request, response);
		
		// make sure there is a "userId" filled in on the concept
		Field command = (Field) modelAndView.getModel().get("field");
		Assert.assertNotNull(command.getFieldId());
	}
	
	/**
	 * @see {@link FieldFormController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)}
	 */
	@Test
	@Verifies(value = "should not fail on field answers", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldNotFailOnFieldAnswers() throws Exception {
		final String FIELD_ID = "1";
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "");
		request.setParameter("fieldId", FIELD_ID);
		
		HttpServletResponse response = new MockHttpServletResponse();
		FieldFormController controller = (FieldFormController) applicationContext.getBean("fieldForm");
		controller.handleRequest(request, response);
		
		Context.closeSession();
		Context.openSession();
		authenticate();
		
		request = new MockHttpServletRequest("POST", "");
		response = new MockHttpServletResponse();
		request.setParameter("fieldId", FIELD_ID);
		request.setParameter("name", "Some concept");
		request.setParameter("description", "This is a test field");
		request.setParameter("fieldTypeId", "1");
		request.setParameter("name", "Some concept");
		request.setParameter("conceptId", "3");
		
		controller.handleRequest(request, response);
	}
	
}
