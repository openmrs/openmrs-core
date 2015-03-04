/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.util.List;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.handler.EncounterVisitHandler;
import org.openmrs.api.handler.ExistingOrNewVisitAssignmentHandler;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.validation.Validator;

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
		List<Validator> validators = Context.getRegisteredComponents(Validator.class);
		Assert.assertTrue(validators.size() > 0);
		Assert.assertTrue(Validator.class.isAssignableFrom(validators.iterator().next().getClass()));
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
	
	/**
	 * @see {@link Context#getRegisteredComponent(String,Class)}
	 */
	@Test
	@Verifies(value = "return bean of the correct type", method = "getRegisteredComponent(String, Class)")
	public void getRegisteredComponent_shouldReturnBeanHaveBeenRegisteredOfThePassedTypeAndName() throws Exception {
		
		EncounterVisitHandler registeredComponent = Context.getRegisteredComponent("existingOrNewVisitAssignmentHandler",
		    EncounterVisitHandler.class);
		
		Assert.assertTrue(registeredComponent instanceof ExistingOrNewVisitAssignmentHandler);
	}
	
	/**
	 * @see {@link Context#getRegisteredComponent(String, Class)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "fail for bean with the given type but different name", method = "getRegisteredComponent(String, Class)")
	public void getRegisteredComponent_shouldFailIfBeanHaveBeenREgisteredOfThePassedTypeAndNameDoesntExist()
	        throws Exception {
		
		Context.getRegisteredComponent("invalidBeanName", EncounterVisitHandler.class);
		
		Assert.fail();
	}
	
	/**
	 * Prevents regression after patch from #2174:
	 * "Prevent duplicate proxies and AOP in context services"
	 * 
	 * @see {@link Context#getService(Class)}
	 */
	@Test
	@Verifies(value = "should return the same object when called multiple times for the same class", method = "getService(Class)")
	public void getService_shouldReturnTheSameObjectWhenCalledMultipleTimesForTheSameClass() throws Exception {
		PatientService ps1 = Context.getService(PatientService.class);
		PatientService ps2 = Context.getService(PatientService.class);
		Assert.assertTrue(ps1 == ps2);
	}
	
	/**
	 * @see {@link Context#becomeUser(String)}
	 */
	@Test
	@Verifies(value = "change locale when become another user", method = "becomeUser(String)")
	public void becomeUser_shouldChangeLocaleWhenBecomeAnotherUser() throws Exception {
		UserService userService = Context.getUserService();
		
		User user = new User(new Person());
		user.addName(new PersonName("givenName", "middleName", "familyName"));
		user.getPerson().setGender("M");
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, "pt_BR");
		userService.saveUser(user, "TestPass123");
		
		Context.becomeUser(user.getSystemId());
		
		Locale locale = Context.getLocale();
		Assert.assertEquals("pt", locale.getLanguage());
		Assert.assertEquals("BR", locale.getCountry());
		
		Context.logout();
	}
}
