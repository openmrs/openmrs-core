/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;

import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Facilitates testing controllers.
 * 
 * @since 1.10, 1.9.1, 1.8.4, 1.7.4
 */
@Component
public class WebTestHelper {
	
	@Autowired(required = false)
	List<HandlerAdapter> handlerAdapters;
	
	@Autowired(required = false)
	List<HandlerMapping> handlerMappings;
	
	/**
	 * Creates a GET request.
	 * 
	 * @param requestURI
	 * @return
	 */
	public MockHttpServletRequest newGET(final String requestURI) {
		return new MockHttpServletRequest("GET", requestURI);
	}
	
	/**
	 * Creates a chained GET request (within a single HttpSession).
	 * 
	 * @param requestURI
	 * @param session
	 * @return
	 */
	public MockHttpServletRequest newGET(final String requestURI, final Response previousResponse) {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", requestURI);
		request.setSession(previousResponse.session);
		return request;
	}
	
	/**
	 * Creates a POST request.
	 * 
	 * @param requestURI
	 * @return
	 */
	public MockHttpServletRequest newPOST(final String requestURI) {
		return new MockHttpServletRequest("POST", requestURI);
	}
	
	/**
	 * Creates a chained POST request (within a single HttpSession).
	 * 
	 * @param requestURI
	 * @param previousResponse
	 * @return
	 */
	public MockHttpServletRequest newPOST(final String requestURI, final Response previousResponse) {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", requestURI);
		request.setSession(previousResponse.session);
		return request;
	}
	
	/**
	 * Handles the request with a proper controller.
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Response handle(final HttpServletRequest request) throws Exception {
		if (handlerMappings == null || handlerAdapters == null) {
			throw new UnsupportedOperationException("The web context is not configured!");
		}
		
		//Simulate a request with a fresh Hibernate session
		Context.flushSession();
		Context.clearSession();
		
		final MockHttpServletResponse response = new MockHttpServletResponse();
		ModelAndView modelAndView = null;
		
		HandlerExecutionChain handlerChain = null;
		for (HandlerMapping handlerMapping : handlerMappings) {
			handlerChain = handlerMapping.getHandler(request);
			if (handlerChain != null) {
				break;
			}
		}
		assertNotNull(handlerChain, "The requested URI has no mapping: " + request.getRequestURI());
		
		boolean supported = false;
		for (HandlerAdapter handlerAdapter : handlerAdapters) {
			final Object handler = handlerChain.getHandler();
			if (handlerAdapter.supports(handler)) {
				assertFalse(supported, "The requested URI has more than one handler: " + request.getRequestURI());
				
				modelAndView = handlerAdapter.handle(request, response, handler);
				supported = true;
			}
		}
		
		assertTrue(supported, "The requested URI has no handlers: " + request.getRequestURI());
		
		return new Response(response, request.getSession(), modelAndView);
	}
	
	public static class Response {
		
		public final MockHttpServletResponse http;
		
		public final HttpSession session;
		
		public final ModelAndView modelAndView;
		
		public Response(MockHttpServletResponse http, HttpSession session, ModelAndView modelAndView) {
			this.http = http;
			this.session = session;
			this.modelAndView = modelAndView;
		}
		
		public Errors getErrors(String model) {
			return (Errors) modelAndView.getModel().get(BindException.MODEL_KEY_PREFIX + model);
		}
		
		public Errors getErrors() {
			return getErrors("command");
		}
	}
}
