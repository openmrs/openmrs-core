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
package org.openmrs.web.controller.form;

import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

public class FormFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * testcase confirming that the user can't save a form when forms are locked
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotSaveFormWhenFormsAreLocked() throws Exception {
		// dataset to lock forms
		executeDataSet("org/openmrs/web/include/FormFormControllerTest.xml");
		
		FormService fs = Context.getFormService();
		
		FormFormController controller = (FormFormController) applicationContext.getBean("formEditForm");
		controller.setApplicationContext(applicationContext);
		controller.setSuccessView("index.htm");
		controller.setFormView("Form.form");
		
		// setting up the request and doing an initial "get" equivalent to the user loading the page for the first time
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/forms/formEdit.form?formId=1");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		// set this to be a page submission
		request.setMethod("POST");
		
		request.addParameter("action", "Save Form");
		
		// send the parameters to the controller
		ModelAndView mav = controller.handleRequest(request, response);
		
		Assert.assertSame(controller.getFormView(), mav.getViewName());
		Assert.assertNotSame("The save attempt should have failed!", "index.htm", mav.getViewName());
		Assert.assertNotNull(fs.getForm(1));
	}
	
	@Test
	public void shouldDeleteFormWhenFormsAreNotLocked() throws Exception {
		FormService formService = Context.getFormService();
		
		FormFormController controller = (FormFormController) applicationContext.getBean("formEditForm");
		controller.setApplicationContext(applicationContext);
		controller.setSuccessView("index.htm");
		controller.setFormView("form.form");
		
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/forms/formEdit.form?formId=1");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		request.setMethod("POST");
		
		request.addParameter("action", "Delete form");
		
		ModelAndView mav = controller.handleRequest(request, response);
		
		Assert.assertSame(controller.getFormView(), mav.getViewName());
		Assert.assertNotEquals("The delete attempt should have passed!", "index.htm", mav.getViewName());
		Assert.assertNotNull(formService.getForm(1));
	}
}
