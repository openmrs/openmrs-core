package org.openmrs.api.context;

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

	@Test
	void getDefaultLocationId_shouldGetDefaultLocationById() {
		// arrange
		Context.getUserContext().setLocationId(null);
		Person testPerson = new Person();
		testPerson.addName(new PersonName("Carroll", "", "Deacon"));
		testPerson.setGender("U");
		personService.savePerson(testPerson);
		
		User testUser = new User();
		testUser.setUsername("testUser");
		testUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, "1");
		testUser.setPerson(testPerson);
		userService.createUser(testUser, "Test1234");
		
		try {
			// act
			Integer locationId = Context.getUserContext().getDefaultLocationId(testUser);

			// assert
			assertThat(locationId, equalTo(1));
		} finally {
			try {
				userService.purgeUser(testUser);
			} catch (Exception ignored) {}
			
			try {
				personService.purgePerson(testPerson);
			} catch (Exception ignored) {}
		}
	}

	@Test
	void getDefaultLocationId_shouldGetDefaultLocationByUuid() {
		// arrange
		Context.getUserContext().setLocationId(null);
		Person testPerson = new Person();
		testPerson.addName(new PersonName("Carroll", "", "Deacon"));
		testPerson.setGender("U");
		personService.savePerson(testPerson);

		User testUser = new User();
		testUser.setUsername("testUser");
		testUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, "8d6c993e-c2cc-11de-8d13-0010c6dffd0f");
		testUser.setPerson(testPerson);
		userService.createUser(testUser, "Test1234");

		try {
			// act
			Integer locationId = Context.getUserContext().getDefaultLocationId(testUser);

			// assert
			assertThat(locationId, equalTo(1));
		} finally {
			try {
				userService.purgeUser(testUser);
			} catch (Exception ignored) {}

			try {
				personService.purgePerson(testPerson);
			} catch (Exception ignored) {}
		}
	}

	@Test
	void getDefaultLocationId_shouldReturnNullForInvalidId() {
		// arrange
		Context.getUserContext().setLocationId(null);
		Person testPerson = new Person();
		testPerson.addName(new PersonName("Carroll", "", "Deacon"));
		testPerson.setGender("U");
		personService.savePerson(testPerson);

		User testUser = new User();
		testUser.setUsername("testUser");
		testUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, String.valueOf(Integer.MAX_VALUE));
		testUser.setPerson(testPerson);
		userService.createUser(testUser, "Test1234");

		try {
			// act
			Integer locationId = Context.getUserContext().getDefaultLocationId(testUser);

			// assert
			assertThat(locationId, nullValue());
		} finally {
			try {
				userService.purgeUser(testUser);
			} catch (Exception ignored) {}

			try {
				personService.purgePerson(testPerson);
			} catch (Exception ignored) {}
		}
	}

	@Test
	void getDefaultLocationId_shouldReturnNullForInvalidUuid() {
		// arrange
		Context.getUserContext().setLocationId(null);
		Person testPerson = new Person();
		testPerson.addName(new PersonName("Carroll", "", "Deacon"));
		testPerson.setGender("U");
		personService.savePerson(testPerson);

		User testUser = new User();
		testUser.setUsername("testUser");
		testUser.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCATION, "0e32f474-eca5-4cc2-a64d-53b086f27e52");
		testUser.setPerson(testPerson);
		userService.createUser(testUser, "Test1234");

		try {
			// act
			Integer locationId = Context.getUserContext().getDefaultLocationId(testUser);

			// assert
			assertThat(locationId, nullValue());
		} finally {
			try {
				userService.purgeUser(testUser);
			} catch (Exception ignored) {}

			try {
				personService.purgePerson(testPerson);
			} catch (Exception ignored) {}
		}
	}
}
