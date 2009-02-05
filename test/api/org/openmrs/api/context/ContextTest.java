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

import org.junit.AfterClass;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

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
	 * @see {@link Context#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should not authenticate with null password", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateWithNullPassword() throws Exception {
		Context.authenticate("some username", null);
	}
	
	/**
	 * @see {@link Context#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should not authenticate with null password and proper system id", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateWithNullPasswordAndProperSystemId() throws Exception {
		Context.authenticate("1-8", null);
	}
	
	/**
	 * @see {@link Context#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should not authenticate with null password and proper username", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateWithNullPasswordAndProperUsername() throws Exception {
		Context.authenticate("admin", null);
	}
	
	/**
	 * @see {@link Context#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should not authenticate with null username", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateWithNullUsername() throws Exception {
		Context.authenticate(null, "some password");
	}
	
	/**
	 * @see {@link Context#authenticate(String,String)}
	 */
	@Test(expected = ContextAuthenticationException.class)
	@Verifies(value = "should not authenticate with null username and password", method = "authenticate(String,String)")
	public void authenticate_shouldNotAuthenticateWithNullUsernameAndPassword() throws Exception {
		Context.authenticate(null, null);
	}
	
}
