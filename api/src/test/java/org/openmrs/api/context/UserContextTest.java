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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

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
}
