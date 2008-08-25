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
package org.openmrs.test.api.context;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.test.testutil.BaseContextSensitiveTest;
import org.springframework.test.AssertThrows;

/**
 * TODO Should we separate into spring required and non-spring-required tests?
 * TODO add methods for all context tests
 */
public class ContextTest extends BaseContextSensitiveTest {
	
	/**
	 * Set up the database with the initial dataset before every test method
	 * in this class.
	 * 
	 * 
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		// creates the basic user and give it full rights
		initializeInMemoryDatabase();
	}
	
	/**
	 * Null parameters to the authenticate method should not cause errors to be
	 * thrown and should not ever show the Context to be authenticated  
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotAuthenticateWithNullParameters() throws Exception {
		
		Context.logout();
		assertFalse("This test needs to start with an unauthenticated context",
		            Context.isAuthenticated());
		            
		// check null username and null password
		new AssertThrows(ContextAuthenticationException.class) {
			public void test() throws Exception {
				Context.authenticate(null, null);
			}
		}.runTest();
		assertFalse("No one should ever be authenticated with null parameters",
		            Context.isAuthenticated());
		
		// check non-null username and null password
		new AssertThrows(ContextAuthenticationException.class) {
			public void test() throws Exception {
				Context.authenticate("some username", null);
			}
		}.runTest();
		assertFalse("No one should ever be authenticated with null parameters", Context.isAuthenticated());
		
		// check null username and non-null password
		new AssertThrows(ContextAuthenticationException.class) {
			public void test() throws Exception {
				Context.authenticate(null, "some password");
			}
		}.runTest();
		assertFalse("No one should ever be authenticated with null parameters", Context.isAuthenticated());
		
		// check proper username and null pw
		new AssertThrows(ContextAuthenticationException.class) {
			public void test() throws Exception {
				Context.authenticate("admin", null);
			}
		}.runTest();
		assertFalse("No one should ever be authenticated with null password and proper username", Context.isAuthenticated());
		
		// check proper system id and null pw
		new AssertThrows(ContextAuthenticationException.class) {
			public void test() throws Exception {
				Context.authenticate("1-8", null);
			}
		}.runTest();
		assertFalse("No one should ever be authenticated with null password and proper system id", Context.isAuthenticated());
		
	}
	
	/**
	 * TODO finish and complete
	 */
	@Test
	public void shouldGettingUser() throws Exception {
		authenticate();
		
		UserService us = Context.getUserService();
		String username = "admin";
		User user = us.getUserByUsername(username);
		assertNotNull("user " + username, user);
		System.out.println("Successfully found user: " + user.getPersonName() + " (" + username + ")");
		
	}
	
	/**
	 * TODO create method
	 */
	@Test
	public void shouldProxyPrivilege() throws Exception {
		authenticate();
		
		//create a bum user
		
		// make sure they can't do High Level Task X
		
		// give them privileges to do High Level Task X
		
		// now make sure they can do High Level Task X
		
		// delete the user
		
	}

}
