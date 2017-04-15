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
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
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
	 * @see Context#authenticate(String,String)
	 */
	@Test(expected = ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateWithNullPassword() {
		Context.authenticate("some username", null);
	}
	
	/**
	 * @see Context#authenticate(String,String)
	 */
	@Test(expected = ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateWithNullPasswordAndProperSystemId() {
		Context.authenticate("1-8", null);
	}
	
	/**
	 * @see Context#authenticate(String,String)
	 */
	@Test(expected = ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateWithNullPasswordAndProperUsername() {
		Context.authenticate("admin", null);
	}
	
	/**
	 * @see Context#authenticate(String,String)
	 */
	@Test(expected = ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateWithNullUsername() {
		Context.authenticate(null, "some password");
	}
	
	/**
	 * @see Context#authenticate(String,String)
	 */
	@Test(expected = ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateWithNullUsernameAndPassword() {
		Context.authenticate(null, null);
	}
	
	/**
	 * @see Context#getLocale()
	 */
	@Test
	public void getLocale_shouldNotFailIfSessionHasntBeenOpened() {
		Context.closeSession();
		Assert.assertEquals(LocaleUtility.getDefaultLocale(), Context.getLocale());
	}
	
	/**
	 * @see Context#getUserContext()
	 */
	@Test(expected = APIException.class)
	public void getUserContext_shouldFailIfSessionHasntBeenOpened() {
		Context.closeSession();
		Context.getUserContext(); // trigger the api exception
	}
	
	/**
	 * @see Context#logout()
	 */
	@Test
	public void logout_shouldNotFailIfSessionHasntBeenOpenedYet() {
		Context.closeSession();
		Context.logout();
	}
	
	/**
	 * @see Context#isSessionOpen()
	 */
	@Test
	public void isSessionOpen_shouldReturnTrueIfSessionIsClosed() {
		Assert.assertTrue(Context.isSessionOpen());
		Context.closeSession();
		Assert.assertFalse(Context.isSessionOpen());
	}
	
	/**
	 * @see Context#refreshAuthenticatedUser()
	 */
	@Test
	public void refreshAuthenticatedUser_shouldGetFreshValuesFromTheDatabase() {
		User evictedUser = Context.getAuthenticatedUser();
		Context.evictFromSession(evictedUser);
		
		User fetchedUser = Context.getUserService().getUser(evictedUser.getUserId());
		fetchedUser.getPersonName().setGivenName("new username");
		
		Context.getUserService().saveUser(fetchedUser);
		
		// sanity check to make sure the cached object wasn't updated already
		Assert.assertNotSame(Context.getAuthenticatedUser().getGivenName(), fetchedUser.getGivenName());
		
		Context.refreshAuthenticatedUser();
		
		Assert.assertEquals("new username", Context.getAuthenticatedUser().getGivenName());
	}
	
	/**
	 * @see Context#getRegisteredComponents(Class)
	 */
	@Test
	public void getRegisteredComponents_shouldReturnAListOfAllRegisteredBeansOfThePassedType() {
		List<Validator> validators = Context.getRegisteredComponents(Validator.class);
		Assert.assertTrue(validators.size() > 0);
		Assert.assertTrue(Validator.class.isAssignableFrom(validators.iterator().next().getClass()));
	}
	
	/**
	 * @see Context#getRegisteredComponents(Class)
	 */
	@Test
	public void getRegisteredComponents_shouldReturnAnEmptyListIfNoBeansHaveBeenRegisteredOfThePassedType() {
		List<Location> l = Context.getRegisteredComponents(Location.class);
		Assert.assertNotNull(l);
		Assert.assertEquals(0, l.size());
	}
	
	/**
	 * @see Context#getRegisteredComponent(String,Class)
	 */
	@Test
	public void getRegisteredComponent_shouldReturnBeanHaveBeenRegisteredOfThePassedTypeAndName() {
		
		EncounterVisitHandler registeredComponent = Context.getRegisteredComponent("existingOrNewVisitAssignmentHandler",
		    EncounterVisitHandler.class);
		
		Assert.assertTrue(registeredComponent instanceof ExistingOrNewVisitAssignmentHandler);
	}
	
	/**
	 * @see Context#getRegisteredComponent(String, Class)
	 */
	@Test(expected = APIException.class)
	public void getRegisteredComponent_shouldFailIfBeanHaveBeenREgisteredOfThePassedTypeAndNameDoesntExist()
	{
		
		Context.getRegisteredComponent("invalidBeanName", EncounterVisitHandler.class);
		
		Assert.fail();
	}
	
	/**
	 * Prevents regression after patch from #2174:
	 * "Prevent duplicate proxies and AOP in context services"
	 * 
	 * @see Context#getService(Class)
	 */
	@Test
	public void getService_shouldReturnTheSameObjectWhenCalledMultipleTimesForTheSameClass() {
		PatientService ps1 = Context.getService(PatientService.class);
		PatientService ps2 = Context.getService(PatientService.class);
		Assert.assertTrue(ps1 == ps2);
	}
	
	/**
	 * @see Context#becomeUser(String)
	 */
	@Test
	public void becomeUser_shouldChangeLocaleWhenBecomeAnotherUser() {
		UserService userService = Context.getUserService();
		
		User user = new User(new Person());
		user.addName(new PersonName("givenName", "middleName", "familyName"));
		user.getPerson().setGender("M");
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, "pt_BR");
		userService.createUser(user, "TestPass123");
		
		Context.becomeUser(user.getSystemId());
		
		Locale locale = Context.getLocale();
		Assert.assertEquals("pt", locale.getLanguage());
		Assert.assertEquals("BR", locale.getCountry());
		
		Context.logout();
	}
}
