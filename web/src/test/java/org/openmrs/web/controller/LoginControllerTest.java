/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller;

import org.junit.Test;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class LoginControllerTest extends BaseWebContextSensitiveTest {
	
	@Autowired
	private LoginController controller;
	
	@Test
	public void shouldReplaceHashtagInRedirectUrl() {
		String redirectUrl = "www.openmrs.org/_HASHTAG_";
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setParameter("redirect_url", redirectUrl);
		
		WebRequest webRequest = new ServletWebRequest(mockRequest);
		webRequest.setAttribute(WebConstants.INSUFFICIENT_PRIVILEGES, true, WebRequest.SCOPE_SESSION);
		
		ModelMap model = new ModelMap();
		
		controller.handleRequest(webRequest, model);
		assertEquals(webRequest.getAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, 1), "www.openmrs.org/#");
	}
	
	@Test
	public void shouldSetTheRedirectAttribute() {
		String redirectUrl = "www.openmrs.org/index";
		
		MockHttpServletRequest mockRequest = new MockHttpServletRequest();
		mockRequest.setParameter("redirect_url", redirectUrl);
		
		WebRequest webRequest = new ServletWebRequest(mockRequest);
		webRequest.setAttribute(WebConstants.INSUFFICIENT_PRIVILEGES, true, WebRequest.SCOPE_SESSION);
		
		ModelMap model = new ModelMap();
		
		controller.handleRequest(webRequest, model);
		assertEquals(webRequest.getAttribute(WebConstants.OPENMRS_LOGIN_REDIRECT_HTTPSESSION_ATTR, 1),
		    "www.openmrs.org/index");
	}
}
