/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.servlet;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests the {@link LoginServlet}
 */
public class LoginServletTest extends BaseWebContextSensitiveTest {
	
	/**
	 * The servlet should send the user back to the login box if the user enters the wrong username
	 * or password.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldRedirectBackToLoginScreenOnBadUsernameAndPassword() throws Exception {
		LoginServlet loginServlet = new LoginServlet();
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/loginServlet");
		request.setContextPath("/somecontextpath");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.setParameter("uname", "some wrong username");
		request.setParameter("pw", "some wrong password");
		
		loginServlet.service(request, response);
		
		Assert.assertEquals("/somecontextpath/login.htm", response.getRedirectedUrl());
	}
	
	/**
	 * If a user logs in correctly, they should never be redirected back to the login screen because
	 * this would cause confusion
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotRedirectBackToLoginScreenWithCorrectUsernameAndPassword() throws Exception {
		// this test depends on being able to log in as "admin:test".
		Context.logout();
		Context.authenticate("admin", "test");
		Assert.assertTrue(Context.isAuthenticated());
		
		// do the test now
		LoginServlet loginServlet = new LoginServlet();
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/loginServlet");
		request.setContextPath("/somecontextpath");
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.setParameter("uname", "admin");
		request.setParameter("pw", "test");
		
		loginServlet.service(request, response);
		
		Assert.assertNotSame("/somecontextpath/login.htm", response.getRedirectedUrl());
	}
	
	/**
	 * The lockout value is set to five
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldLockUserOutAfterFiveFailedLoginAttempts() throws Exception {
		// this test depends on being able to log in as "admin:test".
		Context.logout();
		Context.authenticate("admin", "test");
		Assert.assertTrue(Context.isAuthenticated());
		
		// do the test now
		LoginServlet loginServlet = new LoginServlet();
		
		for (int x = 1; x < 4; x++) {
			MockHttpServletRequest request = new MockHttpServletRequest("POST", "/loginServlet");
			request.setContextPath("/somecontextpath");
			MockHttpServletResponse response = new MockHttpServletResponse();
			
			// change the username everytime so that we're not  
			// accidentally testing against the API lockout
			request.setParameter("uname", "wrong username" + x);
			request.setParameter("pw", "wrong password");
			
			loginServlet.service(request, response);
		}
		
		// now attempting to log in the fifth time should fail 
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/loginServlet");
		request.setContextPath("/somecontextpath");
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setParameter("uname", "admin");
		request.setParameter("pw", "test");
		loginServlet.service(request, response);
		
		Assert.assertNotSame("/somecontextpath/login.htm", response.getRedirectedUrl());
	}
}
