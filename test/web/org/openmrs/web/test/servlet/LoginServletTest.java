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
package org.openmrs.web.test.servlet;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.testutil.BaseContextSensitiveTest;
import org.openmrs.web.servlet.LoginServlet;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests the {@link LoginServlet}
 */
public class LoginServletTest extends BaseContextSensitiveTest {

	/**
	 * The servlet should send the user back to the login box
	 * if the user enters the wrong username or password.
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
	 * If a user logs in correctly, they should never be redirected back
	 * to the login screen because this would cause confusion
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
