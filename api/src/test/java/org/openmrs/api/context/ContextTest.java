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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.handler.EncounterVisitHandler;
import org.openmrs.api.handler.ExistingOrNewVisitAssignmentHandler;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
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
	@AfterAll
	public static void logOutAfterThisTestClass() {
		Context.logout();
	}
	
	/**
	 * @see Context#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateWithNullPassword() {
		assertThrows(ContextAuthenticationException.class, () -> Context.authenticate("some username", null));
	}
	
	/**
	 * @see Context#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateWithNullPasswordAndProperSystemId() {
		assertThrows(ContextAuthenticationException.class, () -> Context.authenticate("1-8", null));
	}
	
	/**
	 * @see Context#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateWithNullPasswordAndProperUsername() {
		assertThrows(ContextAuthenticationException.class, () -> Context.authenticate("admin", null));
	}
	
	/**
	 * @see Context#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateWithNullUsername() {
		assertThrows(ContextAuthenticationException.class, () -> Context.authenticate(null, "some password"));
	}
	
	/**
	 * @see Context#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldNotAuthenticateWithNullUsernameAndPassword() {
		assertThrows(ContextAuthenticationException.class, () -> Context.authenticate((String) null, (String) null));
	}
	
	/**
	 * @see Context#authenticate(String,String)
	 */
	@Test
	public void authenticate_shouldAuthenticateUserWithUsernameAndPassword() {
		// replay
		Context.logout();
		Context.authenticate("admin", "test");
		
		// verif
		assertEquals("admin", Context.getAuthenticatedUser().getUsername());
	}
	
	/**
	 * @see Context#getLocale()
	 */
	@Test
	public void getLocale_shouldNotFailIfSessionHasntBeenOpened() {
		Context.closeSession();
		assertEquals(LocaleUtility.getDefaultLocale(), Context.getLocale());
	}
	
	/**
	 * @see Context#getUserContext()
	 */
	@Test
	public void getUserContext_shouldFailIfSessionHasntBeenOpened() {
		Context.closeSession();
		assertThrows(APIException.class, () -> Context.getUserContext()); // trigger the api exception
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
		assertTrue(Context.isSessionOpen());
		Context.closeSession();
		assertFalse(Context.isSessionOpen());
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
		assertNotSame(Context.getAuthenticatedUser().getGivenName(), fetchedUser.getGivenName());
		
		Context.refreshAuthenticatedUser();
		
		assertEquals("new username", Context.getAuthenticatedUser().getGivenName());
	}
	
	/**
	 * @see Context#refreshAuthenticatedUser()
	 */
	@Test
	public void refreshAuthenticatedUser_shouldNotUnsetUserLocation() {
		Location userLocation = Context.getLocationService().getLocation(2);
		Context.getUserContext().setLocation(userLocation);
		User evictedUser = Context.getAuthenticatedUser();
		Context.evictFromSession(evictedUser);
		
		Context.refreshAuthenticatedUser();
		
		assertEquals(userLocation, Context.getUserContext().getLocation());
	}
	
	/**
	 * @see Context#refreshAuthenticatedUser()
	 */
	@Test
	public void refreshAuthenticatedUser_shouldSetDefaultLocationIfLocationNull() {
		User evictedUser = Context.getAuthenticatedUser();
		Map<String, String> properties = evictedUser.getUserProperties();
		properties.put(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, "2");
		evictedUser.setUserProperties(properties);
		Context.getUserService().saveUser(evictedUser);
		Context.flushSession();
		Context.evictFromSession(evictedUser);
		
		Context.logout();
		authenticate();
		
		assertEquals(Context.getLocationService().getLocation(2), Context.getUserContext().getLocation());
	}
	
	/**
	 * @see Context#getRegisteredComponents(Class)
	 */
	@Test
	public void getRegisteredComponents_shouldReturnAListOfAllRegisteredBeansOfThePassedType() {
		List<Validator> validators = Context.getRegisteredComponents(Validator.class);
		assertTrue(validators.size() > 0);
		assertTrue(Validator.class.isAssignableFrom(validators.iterator().next().getClass()));
	}
	
	/**
	 * @see Context#getRegisteredComponents(Class)
	 */
	@Test
	public void getRegisteredComponents_shouldReturnAnEmptyListIfNoBeansHaveBeenRegisteredOfThePassedType() {
		List<Location> l = Context.getRegisteredComponents(Location.class);
		assertNotNull(l);
		assertEquals(0, l.size());
	}
	
	/**
	 * @see Context#getRegisteredComponent(String,Class)
	 */
	@Test
	public void getRegisteredComponent_shouldReturnBeanHaveBeenRegisteredOfThePassedTypeAndName() {
		
		EncounterVisitHandler registeredComponent = Context.getRegisteredComponent("existingOrNewVisitAssignmentHandler",
		    EncounterVisitHandler.class);
		
		assertTrue(registeredComponent instanceof ExistingOrNewVisitAssignmentHandler);
	}
	
	/**
	 * @see Context#getRegisteredComponent(String, Class)
	 */
	@Test
	public void getRegisteredComponent_shouldFailIfBeanHaveBeenREgisteredOfThePassedTypeAndNameDoesntExist()
	{
		assertThrows(APIException.class, () -> Context.getRegisteredComponent("invalidBeanName", EncounterVisitHandler.class));
		
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
		assertEquals(ps2, ps1);
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
		assertEquals("pt", locale.getLanguage());
		assertEquals("BR", locale.getCountry());
		
		Context.logout();
	}
}
