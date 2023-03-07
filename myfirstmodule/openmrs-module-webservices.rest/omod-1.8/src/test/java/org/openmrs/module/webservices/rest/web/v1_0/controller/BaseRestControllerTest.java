/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.GenericRestException;
import org.openmrs.module.webservices.rest.web.response.IllegalPropertyException;
import org.openmrs.module.webservices.validation.ValidationException;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import java.util.LinkedHashMap;

public class BaseRestControllerTest extends BaseModuleWebContextSensitiveTest {
	
	BaseRestController controller;
	
	MockHttpServletRequest request;
	
	MockHttpServletResponse response;
	
	Log spyOnLog;
	
	@Before
	public void before() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		controller = new BaseRestController();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		spyOnLog = spy(LogFactory.getLog(BaseRestController.class));
		// Need to get the logger using reflection
		Field log;
		log = controller.getClass().getDeclaredField("log");
		log.setAccessible(true);
		
		log.set(controller, spyOnLog);
		
	}
	
	/**
	 * @verifies return unauthorized if not logged in
	 * @see BaseRestController#apiAuthenticationExceptionHandler(Exception,
	 *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Test
	public void apiAuthenticationExceptionHandler_shouldReturnUnauthorizedIfNotLoggedIn() throws Exception {
		Context.logout();
		
		controller.apiAuthenticationExceptionHandler(new APIAuthenticationException(), request, response);
		
		assertThat(response.getStatus(), is(HttpServletResponse.SC_UNAUTHORIZED));
	}
	
	/**
	 * @verifies return forbidden if logged in
	 * @see BaseRestController#apiAuthenticationExceptionHandler(Exception,
	 *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Test
	public void apiAuthenticationExceptionHandler_shouldReturnForbiddenIfLoggedIn() throws Exception {
		controller.apiAuthenticationExceptionHandler(new APIAuthenticationException(), request, response);
		
		assertThat(response.getStatus(), is(HttpServletResponse.SC_FORBIDDEN));
	}
	
	@Test
	public void validationException_shouldReturnBadRequestResponse() throws Exception {
		Errors ex = new BindException(new Person(), "");
		ex.reject("error.message");
		
		SimpleObject responseSimpleObject = controller.validationExceptionHandler(new ValidationException(ex), request,
		    response);
		assertThat(response.getStatus(), is(HttpServletResponse.SC_BAD_REQUEST));
		
		SimpleObject errors = (SimpleObject) responseSimpleObject.get("error");
		Assert.assertEquals("webservices.rest.error.invalid.submission", errors.get("code"));
	}
	
	@Test
	public void handleException_shouldLogUnannotatedAsErrors() throws Exception {
		
		String message = "ErrorMessage";
		Exception ex = new Exception(message);
		controller.handleException(ex, request, response);
		
		verify(spyOnLog).error(message, ex);
		
	}
	
	@Test
	public void handleException_shouldLog500AndAboveAsErrors() throws Exception {
		
		String message = "ErrorMessage";
		Exception ex = new GenericRestException(message);
		
		controller.handleException(ex, request, response);
		
		verify(spyOnLog).error(message, ex);
		
	}
	
	@Test
	public void handleException_shouldLogBelow500AsInfo() throws Exception {
		
		String message = "ErrorMessage";
		Exception ex = new IllegalPropertyException(message);
		
		controller.handleException(ex, request, response);
		
		verify(spyOnLog).info(message, ex);
	}
	
	@Test
	public void handleConversionException_shouldLogConversionErrorAsInfo() throws Exception {
		
		String message = "conversion error";
		ConversionException ex = new ConversionException(message);
		SimpleObject responseSimpleObject = controller.conversionExceptionHandler(ex, request, response);
		assertThat(response.getStatus(), is(HttpServletResponse.SC_BAD_REQUEST));
		LinkedHashMap errors = (LinkedHashMap) responseSimpleObject.get("error");
		Assert.assertEquals("[" + message + "]", errors.get("message"));
	}
	
	@Test
	public void httpMessageNotReadableExceptionHandler_shouldReturnBadRequestIfEmptyBody() throws Exception {
		controller.httpMessageNotReadableExceptionHandler(new HttpMessageNotReadableException(""), request, response);
		assertThat(response.getStatus(), is(HttpServletResponse.SC_BAD_REQUEST));
	}
}
