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
package org.openmrs.api.context;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.test.AssertThrows;

/**
 * TODO add methods for all context tests
 * 
 * @See {@link Context}
 */
public class ContextTest extends BaseContextSensitiveTest {
	
	/**
	 * Methods in this class might authenticate with a different user, so log that user out after
	 * this whole junit class is done.
	 */
	@AfterClass
	public static void logOutAfterThisTest() {
		Context.logout();
	}
	
	/**
	 * Null parameters to the authenticate method should not cause errors to be thrown and should
	 * not ever show the Context to be authenticated
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNotAuthenticateWithNullParameters() throws Exception {
		
		Context.logout();
		assertFalse("This test needs to start with an unauthenticated context", Context.isAuthenticated());
		
		// check null username and null password
		new AssertThrows(
		                 ContextAuthenticationException.class) {
			
			public void test() throws Exception {
				Context.authenticate(null, null);
			}
		}.runTest();
		assertFalse("No one should ever be authenticated with null parameters", Context.isAuthenticated());
		
		// check non-null username and null password
		new AssertThrows(
		                 ContextAuthenticationException.class) {
			
			public void test() throws Exception {
				Context.authenticate("some username", null);
			}
		}.runTest();
		assertFalse("No one should ever be authenticated with null parameters", Context.isAuthenticated());
		
		// check null username and non-null password
		new AssertThrows(
		                 ContextAuthenticationException.class) {
			
			public void test() throws Exception {
				Context.authenticate(null, "some password");
			}
		}.runTest();
		assertFalse("No one should ever be authenticated with null parameters", Context.isAuthenticated());
		
		// check proper username and null pw
		new AssertThrows(
		                 ContextAuthenticationException.class) {
			
			public void test() throws Exception {
				Context.authenticate("admin", null);
			}
		}.runTest();
		assertFalse("No one should ever be authenticated with null password and proper username", Context.isAuthenticated());
		
		// check proper system id and null pw
		new AssertThrows(
		                 ContextAuthenticationException.class) {
			
			public void test() throws Exception {
				Context.authenticate("1-8", null);
			}
		}.runTest();
		assertFalse("No one should ever be authenticated with null password and proper system id", Context.isAuthenticated());
		
	}
	
	/**
	 * 
	 */
	@Test
	public void shouldGetUserByUsername() throws Exception {
		UserService us = Context.getUserService();
		String username = "admin";
		User user = us.getUserByUsername(username);
		assertNotNull("user " + username, user);
	}
	
	/**
	 * TODO create method
	 */
	@Test
	public void shouldProxyPrivilege() throws Exception {
		
		// create a non-admin user using dbunit xml 
		
		// make sure they can't do High Level Task X
		
		// give them proxy privileges to do High Level Task X
		
		// now make sure they can do High Level Task X
		
	}
	
}
