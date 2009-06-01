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
package org.openmrs.web;

import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.openmrs.web.controller.ForgotPasswordFormController;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Test the different aspects of {@link org.openmrs.web.controller.ForgotPasswordFormController}
 */
public class ForgotPasswordFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * Log out before every test (authentication is done in the "@Before" in the parent class)
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		executeDataSet("org/openmrs/web/include/ForgotPasswordFormControllerTest.xml");
		Context.logout();
	}
	
	/**
	 * Log out the current user after all unit test cases just in case the password was reset
	 */
	@After
	public void cleanupAndLogoutUserAfterEachTest() {
		Context.logout();
	}
	
	/**
	 * Just check for no errors on normal page load
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldLoadPageNormallyWithNoInput() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/forgotPassword.form");
		new ForgotPasswordFormController().handleRequest(request, new MockHttpServletResponse());
	}
	
	/**
	 * Check to see if the admin's secret question comes back
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotNotFailOnNotFoundUsername() throws Exception {
		ForgotPasswordFormController controller = new ForgotPasswordFormController();
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		
		request.addParameter("uname", "validuser");
		
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		Assert.assertEquals("validuser", request.getAttribute("uname"));
		Assert.assertEquals("valid secret question", request.getAttribute("secretQuestion"));
	}
	
	/**
	 * Check to see if the admin's secret question comes back
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAuthenticateAsUserWithValidSecretQuestion() throws Exception {
		ForgotPasswordFormController controller = new ForgotPasswordFormController();
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		
		request.addParameter("uname", "validuser");
		request.addParameter("secretAnswer", "valid secret answer");
		
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		Assert.assertEquals(new User(2), Context.getAuthenticatedUser());
	}
	
	/**
	 * If a user enters the wrong secret answer, they should be kicked back to the form and not be
	 * authenticated
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotAuthenticateAsUserWithValidSecretQuestion() throws Exception {
		ForgotPasswordFormController controller = new ForgotPasswordFormController();
		
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("POST");
		
		request.addParameter("uname", "validuser");
		request.addParameter("secretAnswer", "invalid secret answer");
		
		HttpServletResponse response = new MockHttpServletResponse();
		controller.handleRequest(request, response);
		
		Assert.assertFalse(Context.isAuthenticated());
	}
	
	/**
	 * If a user enters 5 requests, the 6th should fail even if that one has a valid username in it
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldLockOutAfterFiveFailedInvalidUsernames() throws Exception {
		ForgotPasswordFormController controller = new ForgotPasswordFormController();
		
		for (int x = 1; x <= 5; x++) {
			MockHttpServletRequest request = new MockHttpServletRequest("POST", "/forgotPassword.form");
			request.addParameter("uname", "invaliduser");
			
			controller.handleRequest(request, new MockHttpServletResponse());
		}
		
		// those were the first five, now the sixth request (with a valid user) should fail
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/forgotPassword.form");
		request.addParameter("uname", "validuser");
		
		controller.handleRequest(request, new MockHttpServletResponse());
		Assert.assertNull(request.getAttribute("secretQuestion"));
	}
	
	/**
	 * If a user enters 5 requests, the 6th should fail even if that one has a valid username and a
	 * secret answer associated with it
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotAuthenticateAfterFiveFailedInvalidUsernames() throws Exception {
		ForgotPasswordFormController controller = new ForgotPasswordFormController();
		
		for (int x = 1; x <= 5; x++) {
			MockHttpServletRequest request = new MockHttpServletRequest("POST", "/forgotPassword.form");
			request.addParameter("uname", "invaliduser");
			
			controller.handleRequest(request, new MockHttpServletResponse());
		}
		
		// those were the first five, now the sixth request (with a valid user) should fail
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/forgotPassword.form");
		request.addParameter("uname", "validuser");
		request.addParameter("secretAnswer", "valid secret answer");
		controller.handleRequest(request, new MockHttpServletResponse());
		
		Assert.assertFalse(Context.isAuthenticated());
	}
	
	/**
	 * If a user enters 5 requests with username+secret answer, the 6th should fail even if that one
	 * has a valid answer in it
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldLockOutAfterFiveFailedInvalidSecretAnswers() throws Exception {
		ForgotPasswordFormController controller = new ForgotPasswordFormController();
		
		for (int x = 1; x <= 5; x++) {
			MockHttpServletRequest request = new MockHttpServletRequest("POST", "/forgotPassword.form");
			request.addParameter("uname", "validuser");
			request.addParameter("secretAnswer", "invalid secret answer");
			
			controller.handleRequest(request, new MockHttpServletResponse());
		}
		
		// those were the first five, now the sixth request (with a valid user) should fail
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/forgotPassword.form");
		request.addParameter("uname", "validuser");
		request.addParameter("secretAnswer", "valid secret answer");
		controller.handleRequest(request, new MockHttpServletResponse());
		
		Assert.assertFalse(Context.isAuthenticated());
	}
	
	/**
	 * If a user enters 4 username requests, the 5th one should reset the lockout and they should be
	 * allowed 5 attempts at the secret answer
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGiveUserFiveSecretAnswerAttemptsAfterLessThanFiveFailedUsernameAttempts() throws Exception {
		ForgotPasswordFormController controller = new ForgotPasswordFormController();
		
		for (int x = 1; x <= 4; x++) {
			MockHttpServletRequest request = new MockHttpServletRequest("POST", "/forgotPassword.form");
			request.addParameter("uname", "invaliduser");
			
			controller.handleRequest(request, new MockHttpServletResponse());
		}
		
		// those were the first four, now the fifth is a valid username 
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/forgotPassword.form");
		request.addParameter("uname", "validuser");
		
		controller.handleRequest(request, new MockHttpServletResponse());
		
		Assert.assertNotNull(request.getAttribute("secretQuestion"));
		
		// now the user has 5 chances at the secret answer
		
		// fifth request
		MockHttpServletRequest request5 = new MockHttpServletRequest("POST", "/forgotPassword.form");
		request5.addParameter("uname", "validuser");
		request5.addParameter("secretAnswer", "invalid answer");
		controller.handleRequest(request5, new MockHttpServletResponse());
		Assert.assertNotNull(request5.getAttribute("secretQuestion"));
		
		// sixth request (should not lock out because is after valid username)
		MockHttpServletRequest request6 = new MockHttpServletRequest("POST", "/forgotPassword.form");
		request6.addParameter("uname", "validuser");
		request6.addParameter("secretAnswer", "invalid answer");
		controller.handleRequest(request6, new MockHttpServletResponse());
		Assert.assertNotNull(request6.getAttribute("secretQuestion"));
		
		// seventh request (should authenticate with valid answer)
		MockHttpServletRequest request7 = new MockHttpServletRequest("POST", "/forgotPassword.form");
		request7.addParameter("uname", "validuser");
		request7.addParameter("secretAnswer", "valid secret answer");
		controller.handleRequest(request7, new MockHttpServletResponse());
		
		Assert.assertTrue(Context.isAuthenticated());
	}
	
}
