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

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.xstream.XStreamSerializer;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.LocaleUtility;

/**
 * TODO add methods for all context tests
 * 
 * @see Context
 */
public class ContextTest extends BaseContextSensitiveTest {
	
	/**
	 * Methods in this class might authenticate with a different user, so log that user out after
	 * this whole junit class is done.
	 */
	@AfterClass
	public static void logOutAfterThisTestClass() {
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
	
	/**
	 * @see {@link Context#getLocale()}
	 */
	@Test
	@Verifies(value = "should not fail if session hasnt been opened", method = "getLocale()")
	public void getLocale_shouldNotFailIfSessionHasntBeenOpened() throws Exception {
		Context.closeSession();
		Assert.assertEquals(LocaleUtility.getDefaultLocale(), Context.getLocale());
	}
	
	/**
	 * @see {@link Context#getUserContext()}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should fail if session hasnt been opened", method = "getUserContext()")
	public void getUserContext_shouldFailIfSessionHasntBeenOpened() throws Exception {
		Context.closeSession();
		Context.getUserContext(); // trigger the api exception
	}
	
	/**
	 * @see {@link Context#logout()}
	 */
	@Test
	@Verifies(value = "should not fail if session hasnt been opened yet", method = "logout()")
	public void logout_shouldNotFailIfSessionHasntBeenOpenedYet() throws Exception {
		Context.closeSession();
		Context.logout();
	}
	
	/**
	 * @see {@link Context#isSessionOpen()}
	 */
	@Test
	@Verifies(value = "should return true if session is closed", method = "isSessionOpen()")
	public void isSessionOpen_shouldReturnTrueIfSessionIsClosed() throws Exception {
		Assert.assertTrue(Context.isSessionOpen());
		Context.closeSession();
		Assert.assertFalse(Context.isSessionOpen());
	}
	
	/**
	 * @see {@link Context#refreshAuthenticatedUser()}
	 */
	@Test
	@Verifies(value = "should get fresh values from the database", method = "refreshAuthenticatedUser()")
	public void refreshAuthenticatedUser_shouldGetFreshValuesFromTheDatabase() throws Exception {
		User evictedUser = Context.getAuthenticatedUser();
		Context.evictFromSession(evictedUser);
		
		User fetchedUser = Context.getUserService().getUser(evictedUser.getUserId());
		fetchedUser.getPersonName().setGivenName("new username");
		
		Context.getUserService().saveUser(fetchedUser, null);
		
		// sanity check to make sure the cached object wasn't updated already
		Assert.assertNotSame(Context.getAuthenticatedUser().getGivenName(), fetchedUser.getGivenName());
		
		Context.refreshAuthenticatedUser();
		
		Assert.assertEquals("new username", Context.getAuthenticatedUser().getGivenName());
	}
	
	/**
	 * @see {@link Context#getRegisteredComponents(Class)}
	 */
	@Test
	@Verifies(value = "should return a list of all registered beans of the passed type", method = "getRegisteredComponents(Class)")
	public void getRegisteredComponents_shouldReturnAListOfAllRegisteredBeansOfThePassedType() throws Exception {
		List<OpenmrsSerializer> l = Context.getRegisteredComponents(OpenmrsSerializer.class);
		Assert.assertEquals(1, l.size());
		Assert.assertEquals(XStreamSerializer.class, l.iterator().next().getClass());
	}
	
	/**
	 * @see {@link Context#getRegisteredComponents(Class)}
	 */
	@Test
	@Verifies(value = "should return an empty list if no beans have been registered of the passed type", method = "getRegisteredComponents(Class)")
	public void getRegisteredComponents_shouldReturnAnEmptyListIfNoBeansHaveBeenRegisteredOfThePassedType() throws Exception {
		List<Location> l = Context.getRegisteredComponents(Location.class);
		Assert.assertNotNull(l);
		Assert.assertEquals(0, l.size());
	}
}
