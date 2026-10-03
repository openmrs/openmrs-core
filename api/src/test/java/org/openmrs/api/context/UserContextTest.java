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

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.PrivilegeListener;
import org.openmrs.User;
import org.openmrs.UserSessionListener;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class UserContextTest extends BaseContextSensitiveTest {

	@Autowired
	private UserService userService;

	@Autowired
	private PersonService personService;

	Person testPerson;

	User testUser;

	@BeforeEach
	void createUser() {
		testPerson = new Person();
		testPerson.addName(new PersonName("Carroll", "", "Deacon"));
		testPerson.setGender("U");
		testPerson = personService.savePerson(testPerson);

		testUser = new User();
		testUser.setUsername("testUser");
		testUser.setPerson(testPerson);
		testUser = userService.createUser(testUser, "Test1234");
	}

	@AfterEach
	void deleteUser() {
		userService.purgeUser(testUser);
		personService.purgePerson(testPerson);
	}

	@Test
	void getDefaultLocationId_shouldGetDefaultLocationById() {
		// arrange
		Context.getUserContext().setLocationId(null);
		testUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, "1");
		userService.saveUser(testUser);

		// act
		Integer locationId = Context.getUserContext().getDefaultLocationId(testUser);

		// assert
		assertThat(locationId, equalTo(1));
	}

	@Test
	void getDefaultLocationId_shouldGetDefaultLocationByUuid() {
		// arrange
		Context.getUserContext().setLocationId(null);
		testUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, "8d6c993e-c2cc-11de-8d13-0010c6dffd0f");
		userService.saveUser(testUser);

		// act
		Integer locationId = Context.getUserContext().getDefaultLocationId(testUser);

		// assert
		assertThat(locationId, equalTo(1));
	}

	@Test
	void getDefaultLocationId_shouldReturnNullForInvalidId() {
		// arrange
		Context.getUserContext().setLocationId(null);
		testUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, String.valueOf(Integer.MAX_VALUE));
		userService.saveUser(testUser);

		// act
		Integer locationId = Context.getUserContext().getDefaultLocationId(testUser);

		// assert
		assertThat(locationId, nullValue());
	}

	@Test
	void getDefaultLocationId_shouldReturnNullForInvalidUuid() {
		// arrange
		Context.getUserContext().setLocationId(null);
		testUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, "0e32f474-eca5-4cc2-a64d-53b086f27e52");
		userService.saveUser(testUser);

		// act
		Integer locationId = Context.getUserContext().getDefaultLocationId(testUser);

		// assert
		assertThat(locationId, nullValue());
	}

	@Test
	void addProxyPrivilege_shouldAddMultiplePrivileges() {
		// arrange
		UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());

		// act
		userContext.addProxyPrivilege("Privilege1", "Privilege2", "Privilege3");

		// assert
		assertThat(userContext.hasPrivilege("Privilege1"), is(true));
		assertThat(userContext.hasPrivilege("Privilege2"), is(true));
		assertThat(userContext.hasPrivilege("Privilege3"), is(true));
	}

	@Test
	void addProxyPrivilege_shouldThrowExceptionForNullArray() {
		// arrange
		UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());

		// act & assert
		assertThrows(IllegalArgumentException.class, () -> userContext.addProxyPrivilege((String[]) null));
	}

	@Test
	void addProxyPrivilege_shouldThrowExceptionForNullString() {
		// arrange
		UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());

		// act & assert
		assertThrows(IllegalArgumentException.class, () -> userContext.addProxyPrivilege((String) null));
	}

	@Test
	void removeProxyPrivilege_shouldRemoveMultiplePrivileges() {
		// arrange
		UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());
		userContext.addProxyPrivilege("Privilege1");
		userContext.addProxyPrivilege("Privilege2");
		userContext.addProxyPrivilege("Privilege3");

		// act
		userContext.removeProxyPrivilege("Privilege1", "Privilege3");

		// assert
		assertThat(userContext.hasPrivilege("Privilege1"), is(false));
		assertThat(userContext.hasPrivilege("Privilege2"), is(true));
		assertThat(userContext.hasPrivilege("Privilege3"), is(false));
	}

	@Test
	void removeProxyPrivilege_shouldHandleNullArrayGracefully() {
		// arrange
		UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());
		userContext.addProxyPrivilege("Privilege1");

		// act
		userContext.removeProxyPrivilege((String[]) null);

		// assert - should still have the privilege since null was passed
		assertThat(userContext.hasPrivilege("Privilege1"), is(true));
	}

	@Test
	void removeProxyPrivilege_shouldHandleNullStringGracefully() {
		// arrange
		UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());
		userContext.addProxyPrivilege("Privilege1");

		// act
		userContext.removeProxyPrivilege((String) null);

		// assert - should still have the privilege since null was passed
		assertThat(userContext.hasPrivilege("Privilege1"), is(true));
	}

	@Test
	void removeProxyPrivilege_shouldHandleNonExistentPrivilegeGracefully() {
		// arrange
		UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());

		// act & assert
		assertDoesNotThrow(() -> userContext.removeProxyPrivilege("Privilege 1"));
	}

	@Test
	void proxyPrivileges_shouldStackCorrectly() {
		// arrange
		UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());

		// act - deep nesting
		userContext.addProxyPrivilege("Privilege1");
		try {
			userContext.addProxyPrivilege("Privilege1");
			try {
				userContext.addProxyPrivilege("Privilege1");
				try {
					userContext.addProxyPrivilege("Privilege1");
				} finally {
					userContext.removeProxyPrivilege("Privilege1");
				}
			} finally {
				userContext.removeProxyPrivilege("Privilege1");
			}
		} finally {
			userContext.removeProxyPrivilege("Privilege1");
		}

		// assert
		assertThat(userContext.hasPrivilege("Privilege1"), is(true));
	}

	/**
	 * @see org.openmrs.api.context.UserContext#hasPrivilege(java.lang.String)
	 */
	@Test
	void hasPrivilege_shouldCachePrivilegeListeners() {
		try (MockedStatic<Context> contextMock = Mockito.mockStatic(Context.class)) {
			UserContext.clearCachedListeners();
			PrivilegeListener privilegeListener = mock(PrivilegeListener.class);

			contextMock.when(() -> Context.getRegisteredComponents(PrivilegeListener.class))
			        .thenReturn(Collections.singletonList(privilegeListener));

			UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());
			userContext.addProxyPrivilege("Some Privilege");

			userContext.hasPrivilege("Some Privilege");
			userContext.hasPrivilege("Some Privilege");

			// The listener lookup should happen only once because the result is cached.
			contextMock.verify(() -> Context.getRegisteredComponents(PrivilegeListener.class), times(1));
		} finally {
			UserContext.clearCachedListeners();
		}
	}

	/**
	 * @see org.openmrs.api.context.UserContext#hasPrivilege(java.lang.String)
	 */
	@Test
	void hasPrivilege_shouldCacheEmptyPrivilegeListenerList() {
		try (MockedStatic<Context> contextMock = Mockito.mockStatic(Context.class)) {
			UserContext.clearCachedListeners();
			contextMock.when(() -> Context.getRegisteredComponents(PrivilegeListener.class))
			        .thenReturn(Collections.emptyList());

			UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());
			userContext.addProxyPrivilege("Some Privilege");

			userContext.hasPrivilege("Some Privilege");
			userContext.hasPrivilege("Some Privilege");

			// An empty listener list should also be cached, avoiding another lookup.
			contextMock.verify(() -> Context.getRegisteredComponents(PrivilegeListener.class), times(1));
		} finally {
			UserContext.clearCachedListeners();
		}
	}

	/**
	 * @see org.openmrs.api.context.UserContext#clearCachedListeners()
	 */
	@Test
	void clearCachedListeners_shouldCausePrivilegeListenerLookupAgain() {
		try (MockedStatic<Context> contextMock = Mockito.mockStatic(Context.class)) {
			UserContext.clearCachedListeners();
			contextMock.when(() -> Context.getRegisteredComponents(PrivilegeListener.class))
			        .thenReturn(Collections.emptyList());

			UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());
			userContext.addProxyPrivilege("Some Privilege");

			// The first lookup populates the cache.
			userContext.hasPrivilege("Some Privilege");

			// Clearing the cache should force the next privilege check to look up listeners again.
			UserContext.clearCachedListeners();
			userContext.hasPrivilege("Some Privilege");

			contextMock.verify(() -> Context.getRegisteredComponents(PrivilegeListener.class), times(2));
		} finally {
			UserContext.clearCachedListeners();
		}
	}

	/**
	 * @see org.openmrs.api.context.UserContext#logout()
	 */
	@Test
	void logout_shouldCacheAndNotifyUserSessionListeners() {
		try (MockedStatic<Context> contextMock = Mockito.mockStatic(Context.class)) {
			UserContext.clearCachedListeners();

			UserSessionListener listener = mock(UserSessionListener.class);
			contextMock.when(() -> Context.getRegisteredComponents(UserSessionListener.class))
			        .thenReturn(Collections.singletonList(listener));

			UserContext userContext = new UserContext(new TestUsernameAuthenticationScheme());

			userContext.logout();
			userContext.logout();

			// Repeated session events should reuse the cached listener list.
			contextMock.verify(() -> Context.getRegisteredComponents(UserSessionListener.class), times(1));

			verify(listener, times(2)).loggedInOrOut(null, UserSessionListener.Event.LOGOUT,
			    UserSessionListener.Status.SUCCESS);
		} finally {
			UserContext.clearCachedListeners();
		}
	}
}
