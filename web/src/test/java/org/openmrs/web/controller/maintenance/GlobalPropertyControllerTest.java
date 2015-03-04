/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.maintenance;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;

public class GlobalPropertyControllerTest extends BaseWebContextSensitiveTest {
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private GlobalPropertyController controller;
	
	private AdministrationService administrationService;
	
	@Before
	public void before() {
		administrationService = Context.getAdministrationService();
	}
	
	/**
	 * @see GlobalPropertyController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)
	 * @verifies purge not included properties
	 */
	@Test
	public void onSubmit_shouldPurgeNotIncludedProperties() throws Exception {
		GlobalProperty gp = new GlobalProperty("test1", "test1_value");
		administrationService.saveGlobalProperty(gp);
		
		HttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		request.setParameter("action", messageSource.getMessage("general.save", new Object[0], Locale.getDefault()));
		String[] keys = new String[] { "test2", "test3" };
		String[] values = new String[] { "test2_value", "test3_value" };
		String[] descriptions = new String[] { "", "" };
		request.setParameter(GlobalPropertyController.PROP_NAME, keys);
		request.setParameter(GlobalPropertyController.PROP_VAL_NAME, values);
		request.setParameter(GlobalPropertyController.PROP_DESC_NAME, descriptions);
		
		controller.handleRequest(request, response);
		
		Assert.assertEquals(2, administrationService.getAllGlobalProperties().size());
		for (GlobalProperty globalProperty : administrationService.getAllGlobalProperties()) {
			if (globalProperty.getProperty().equals("test2")) {
				Assert.assertEquals("test2_value", globalProperty.getPropertyValue());
			} else if (globalProperty.getProperty().equals("test3")) {
				Assert.assertEquals("test3_value", globalProperty.getPropertyValue());
			} else {
				Assert.fail("Should be either test2 or test3");
			}
		}
	}
	
	/**
	 * @see GlobalPropertyController#onSubmit(HttpServletRequest,HttpServletResponse,Object,BindException)
	 * @verifies save or update included properties
	 */
	@Test
	public void onSubmit_shouldSaveOrUpdateIncludedProperties() throws Exception {
		GlobalProperty gp = new GlobalProperty("test1", "test1_value");
		administrationService.saveGlobalProperty(gp);
		
		HttpServletResponse response = new MockHttpServletResponse();
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		request.setParameter("action", messageSource.getMessage("general.save", new Object[0], Locale.getDefault()));
		String[] keys = new String[] { "test1", "test2" };
		String[] values = new String[] { "test1_new_value", "test2_value" };
		String[] descriptions = new String[] { "", "" };
		request.setParameter(GlobalPropertyController.PROP_NAME, keys);
		request.setParameter(GlobalPropertyController.PROP_VAL_NAME, values);
		request.setParameter(GlobalPropertyController.PROP_DESC_NAME, descriptions);
		
		controller.handleRequest(request, response);
		
		Assert.assertEquals(2, administrationService.getAllGlobalProperties().size());
		for (GlobalProperty globalProperty : administrationService.getAllGlobalProperties()) {
			if (globalProperty.getProperty().equals("test1")) {
				Assert.assertEquals(globalProperty.getPropertyValue(), "test1_new_value");
			} else if (globalProperty.getProperty().equals("test2")) {
				Assert.assertEquals("test2_value", globalProperty.getPropertyValue());
			} else {
				Assert.fail("Should be either test1 or test2");
			}
		}
	}
}
