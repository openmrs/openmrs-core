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
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.FormEditor;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Tests the {@link FormFormController} class.
 */
public class FormFormControllerTest extends BaseWebContextSensitiveTest {
	
	private FormService formService;
	
	private FormFormController controller;
	
	@Before
	public void setup() throws Exception {
		if (formService == null) {
			formService = Context.getFormService();
		}
		// dataset to locks forms
		executeDataSet("org/openmrs/web/controller/include/FormFormControllerTest.xml");
		
		//setting the controller
		controller = (FormFormController) applicationContext.getBean("formEditForm");
		controller.setApplicationContext(applicationContext);
		controller.setFormView("index.htm");
		controller.setSuccessView("formEdit.form");
	}
	
	@Test
	public void shouldNotSaveAFormWhenFormsAreLocked() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/forms/formEdit.form?formId=1");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		request.addParameter("name", "TRUNK");
		request.addParameter("version", "1");
		request.addParameter("action", "Form.save");
		request.setContentType("application/x-www-form-urlencoded");
		
		ModelAndView mav = controller.handleRequest(request, response);
		Assert.assertEquals("The save attempt should have failed!", "index.htm", mav.getViewName());
		Assert.assertNotEquals("formEdit.form", mav.getViewName());
		Assert.assertSame(controller.getFormView(), mav.getViewName());
		Assert.assertNotNull(formService.getForm(1));
	}
	
	@Test
	public void shouldNotDuplicateAFormWhenFormsAreLocked() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST",
		        "/admin/forms/formEdit.form?duplicate=true&formId=1");
		request.setSession(new MockHttpSession(null));
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		request.addParameter("name", "TRUNK");
		request.addParameter("version", "1");
		request.addParameter("action", "Form.Duplicate");
		request.setContentType("application/x-www-form-urlencoded");
		
		ModelAndView mav = controller.handleRequest(request, response);
		Assert.assertEquals("The duplicate attempt should have failed!", "index.htm", mav.getViewName());
		Assert.assertNotEquals("formEdit.form", mav.getViewName());
		Assert.assertSame(controller.getFormView(), mav.getViewName());
		Assert.assertNotNull(formService.getForm(1));
	}
}
