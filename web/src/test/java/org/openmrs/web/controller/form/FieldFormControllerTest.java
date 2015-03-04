/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Ignore;
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
		request.setParameter("action", "save");
		
		controller.handleRequest(request, response);
	}
	
	@Test
	@Verifies(value = "should purge field", method = "onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)")
	public void onSubmit_shouldPurgeField() throws Exception {
		final String FIELD_ID = "1";
		
		HttpServletResponse response = new MockHttpServletResponse();
		FieldFormController controller = (FieldFormController) applicationContext.getBean("fieldForm");
		
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "");
		response = new MockHttpServletResponse();
		request.setParameter("fieldId", FIELD_ID);
		request.setParameter("name", "Some concept");
		request.setParameter("description", "This is a test field");
		request.setParameter("fieldTypeId", "1");
		request.setParameter("name", "Some concept");
		request.setParameter("conceptId", "3");
		request.setParameter("action", Context.getMessageSourceService().getMessage("general.delete"));
		
		controller.handleRequest(request, response);
		
		Assert.assertNull(Context.getFormService().getField(Integer.valueOf(FIELD_ID)));
	}
}
