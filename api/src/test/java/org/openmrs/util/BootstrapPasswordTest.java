/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;

class BootstrapPasswordTest {

	private User user;

	@BeforeEach
	void setUp() {
		Properties props = new Properties();
		props.setProperty("openmrs.bootstrap.pepper", "test-pepper-123");
		props.setProperty("openmrs.bootstrap.iterations", "1000");
		Context.setRuntimeProperties(props);

		user = new User();
		user.setUuid("123e4567-e89b-12d3-a456-426614174000");
		user.setUsername("testuser");
	}

	@Test
	void testGenerateDeterministicHash_shouldReturnSameOutputForSameInput() {
		String input = "test-input";
		String salt = "test-salt";
		String hash1 = Security.generateDeterministicHash(input, salt, 1000);
		String hash2 = Security.generateDeterministicHash(input, salt, 1000);
		assertEquals(hash1, hash2, "Same input should produce same hash");
	}

	@Test
	void testGenerateDeterministicHash_shouldReturnDifferentOutputForDifferentInputs() {
		String salt = "test-salt";
		String hash1 = Security.generateDeterministicHash("input-1", salt, 1000);
		String hash2 = Security.generateDeterministicHash("input-2", salt, 1000);
		assertNotEquals(hash1, hash2, "Different inputs should produce different hashes");
	}

	@Test
	void testGenerateDeterministicHash_shouldNotContainVisuallyAmbiguousCharacters() {
		for (int i = 0; i < 100; i++) {
			String hash = Security.generateDeterministicHash("input-" + i, "salt-" + i, 1000);
			assertFalse(hash.chars().anyMatch(c -> "0OIl+/".indexOf(c) >= 0),
				"Hash must not contain 0, O, I, l, + or / but was: " + hash);
		}
	}

	@Test
	void testGenerateDeterministicHash_shouldAlwaysContainADigit() {
		for (int i = 0; i < 100; i++) {
			String hash = Security.generateDeterministicHash("input-" + i, "salt-" + i, 1000);
			assertTrue(hash.chars().anyMatch(c -> c >= '0' && c <= '9'),
				"Hash must contain at least one digit but was: " + hash);
		}
	}

	@Test
	void testGenerateDeterministicHash_shouldAddTheMissingLetterCase() {
		// before the guarantee, these derive to strings with no upper-case and no lower-case letter respectively
		assertEquals("o1oe4noay5kk1dhiyv9U", Security.generateDeterministicHash("rate-103847", "salt-103847", 1));
		assertEquals("9W8WDXAG52TZHJK2EPEm", Security.generateDeterministicHash("rate-22687", "salt-22687", 1));
	}

	@Test
	void testGenerateBootstrapPassword_shouldSatisfyTheDefaultPasswordPolicy() {
		for (int i = 0; i < 100; i++) {
			User u = new User();
			u.setUuid("00000000-0000-1000-8000-" + String.format("%012d", i));
			String password = Security.generateBootstrapPassword(u);
			assertTrue(password.chars().anyMatch(c -> c >= '0' && c <= '9'),
				"Password must contain at least one digit but was: " + password);
			assertTrue(password.chars().anyMatch(c -> c >= 'A' && c <= 'Z'),
				"Password must contain at least one upper-case letter but was: " + password);
			assertTrue(password.chars().anyMatch(c -> c >= 'a' && c <= 'z'),
				"Password must contain at least one lower-case letter but was: " + password);
			assertFalse(password.chars().anyMatch(c -> "0OIl+/".indexOf(c) >= 0),
				"Password must not contain 0, O, I, l, + or / but was: " + password);
		}
	}

	@Test
	void testGenerateBootstrapPassword_shouldReturnPassword() {
		String password = Security.generateBootstrapPassword(user);
		assertNotNull(password);
		assertFalse(password.isEmpty());
		assertTrue(password.length() <= 20, "Password should be truncated to 20 characters");
	}

	@Test
	void testGenerateBootstrapPassword_shouldBeDeterministic() {
		String password1 = Security.generateBootstrapPassword(user);
		String password2 = Security.generateBootstrapPassword(user);
		assertEquals(password1, password2, "Bootstrap password should be deterministic");
	}

	@Test
	void testGenerateBootstrapPassword_shouldMatchTheKnownAnswer() {
		assertEquals("ExxjUCbx8xbsRxQMW1xr", Security.generateBootstrapPassword(user));
	}

	@Test
	void testGenerateBootstrapPassword_shouldReturnDifferentForDifferentUsers() {
		User user2 = new User();
		user2.setUuid("9876f543-e21d-12a3-b456-426614174000");

		String password1 = Security.generateBootstrapPassword(user);
		String password2 = Security.generateBootstrapPassword(user2);
		assertNotEquals(password1, password2, "Different users should have different passwords");
	}

	@Test
	void testValidateBootstrapPassword_shouldReturnTrueForCorrectPassword() {
		String password = issueBootstrapPassword(user);
		assertTrue(Security.validateBootstrapPassword(user, password));
	}

	@Test
	void testValidateBootstrapPassword_shouldReturnFalseForIncorrectPassword() {
		String password = issueBootstrapPassword(user);
		assertFalse(Security.validateBootstrapPassword(user, password + "wrong"));
	}

	@Test
	void testValidateBootstrapPassword_shouldReturnFalseWhenNoBootstrapPasswordWasIssued() {
		String password = Security.generateBootstrapPassword(user);
		assertFalse(Security.validateBootstrapPassword(user, password),
			"An account that was never issued a bootstrap password must not validate one");
	}

	@Test
	void testValidateBootstrapPassword_shouldReturnFalseForNullUser() {
		assertFalse(Security.validateBootstrapPassword(null, "anyPassword"));
	}

	@Test
	void testValidateBootstrapPassword_shouldReturnFalseForNullPassword() {
		assertFalse(Security.validateBootstrapPassword(user, null));
	}

	@Test
	void testGenerateBootstrapPassword_shouldThrowExceptionForNullUser() {
		assertThrows(APIException.class, () -> Security.generateBootstrapPassword(null));
	}

	@Test
	void testGenerateBootstrapPassword_shouldThrowExceptionForUserWithoutUuid() {
		User userNoUuid = new User();
		userNoUuid.setUuid(null);
		assertThrows(APIException.class, () -> Security.generateBootstrapPassword(userNoUuid));
	}

	@Test
	void testGenerateBootstrapPassword_shouldThrowExceptionWhenPepperMissing() {
		Properties originalProps = Context.getRuntimeProperties();
		try {
			Properties emptyProps = new Properties();
			Context.setRuntimeProperties(emptyProps);

			assertThrows(APIException.class, () -> Security.generateBootstrapPassword(user));
		} finally {
			Context.setRuntimeProperties(originalProps);
		}
	}

	@Test
	void testGetBootstrapIterations_shouldReturnTheConfiguredValue() {
		assertEquals(1000, Security.getBootstrapIterations());
	}

	@Test
	void testGetBootstrapIterations_shouldFallBackToTheDefaultWhenNotConfigured() {
		Context.setRuntimeProperties(runtimePropertiesWithPepperOnly());
		assertEquals(600000, Security.getBootstrapIterations());
	}

	@Test
	void testGetBootstrapIterations_shouldFallBackToTheDefaultWhenNotANumber() {
		Properties props = runtimePropertiesWithPepperOnly();
		props.setProperty("openmrs.bootstrap.iterations", "not-a-number");
		Context.setRuntimeProperties(props);
		assertEquals(600000, Security.getBootstrapIterations());
	}

	@Test
	void testGetBootstrapIterations_shouldFallBackToTheDefaultWhenNotPositive() {
		Properties props = runtimePropertiesWithPepperOnly();
		props.setProperty("openmrs.bootstrap.iterations", "0");
		Context.setRuntimeProperties(props);
		assertEquals(600000, Security.getBootstrapIterations());
	}

	private Properties runtimePropertiesWithPepperOnly() {
		Properties props = new Properties();
		props.setProperty("openmrs.bootstrap.pepper", "test-pepper-123");
		return props;
	}

	@Test
	void testIsBootstrapPasswordIssued_shouldBeFalseWhenNothingHasBeenIssued() {
		assertFalse(Security.isBootstrapPasswordIssued(user));
	}

	@Test
	void testIsBootstrapPasswordIssued_shouldBeTrueAfterIssuance() {
		issueBootstrapPassword(user);
		assertTrue(Security.isBootstrapPasswordIssued(user));
	}

	@Test
	void testIsBootstrapPasswordIssued_shouldNotBeSetByTheForcePasswordFlagAlone() {
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, "true");
		assertFalse(Security.isBootstrapPasswordIssued(user));
	}

	@Test
	void testIsBootstrapPasswordIssued_shouldBeFalseAfterTheBootstrapPasswordWasConsumed() {
		issueBootstrapPassword(user);
		Security.consumeBootstrapPassword(user);
		assertFalse(Security.isBootstrapPasswordIssued(user));
	}

	@Test
	void testIsBootstrapPasswordIssued_shouldReturnFalseForNullUser() {
		assertFalse(Security.isBootstrapPasswordIssued(null));
	}

	@Test
	void testForcePasswordChange_shouldAlsoConsumeTheBootstrapPassword() {
		issueBootstrapPassword(user);
		assertTrue(Security.isBootstrapPasswordIssued(user));

		Security.forcePasswordChange(user);

		assertEquals("true", user.getUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD));
		assertFalse(Security.isBootstrapPasswordIssued(user));
	}

	@Test
	void testValidateBootstrapPassword_shouldNotBeBlockedByTheForcePasswordFlagAlone() {
		String password = issueBootstrapPassword(user);
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, "true");
		assertTrue(Security.validateBootstrapPassword(user, password));
	}

	@Test
	void testValidateBootstrapPassword_shouldReturnFalseForAConsumedBootstrapPassword() {
		String password = issueBootstrapPassword(user);
		assertTrue(Security.validateBootstrapPassword(user, password));

		Security.forcePasswordChange(user);

		assertFalse(Security.validateBootstrapPassword(user, password));
	}

	@Test
	void testValidateBootstrapPassword_shouldReturnFalseForAConsumedBootstrapPasswordEvenAfterThePasswordIsCleared() {
		String password = issueBootstrapPassword(user);
		Security.forcePasswordChange(user);
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, "false");

		assertFalse(Security.validateBootstrapPassword(user, password));
	}

	@Test
	void testValidateBootstrapPassword_shouldStayFalseWhenTheUserPropertiesAreReplaced() {
		String password = issueBootstrapPassword(user);
		Security.forcePasswordChange(user);
		assertFalse(Security.validateBootstrapPassword(user, password));

		// saving preferences replaces the whole property map, which used to drop the
		// consumed marker and bring the bootstrap password back to life
		user.getUserProperties().clear();
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, "en");

		assertFalse(Security.validateBootstrapPassword(user, password),
			"A consumed bootstrap password must not come back when the user properties are replaced");
	}

	@Test
	void testGenerateBootstrapPassword_shouldRefuseTheAdminUsernameWhileThePasswordIsLocked() {
		user.setUsername("admin");
		Properties props = Context.getRuntimeProperties();
		props.setProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY, "true");
		Context.setRuntimeProperties(props);

		assertThrows(APIException.class, () -> Security.generateBootstrapPassword(user));
	}

	@Test
	void testGenerateBootstrapPassword_shouldRefuseTheAdminSystemIdWhileThePasswordIsLocked() {
		user.setSystemId("admin");
		Properties props = Context.getRuntimeProperties();
		props.setProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY, "true");
		Context.setRuntimeProperties(props);

		assertThrows(APIException.class, () -> Security.generateBootstrapPassword(user));
	}

	@Test
	void testGenerateBootstrapPassword_shouldAllowOtherAccountsWhileTheAdminPasswordIsLocked() {
		Properties props = Context.getRuntimeProperties();
		props.setProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY, "true");
		Context.setRuntimeProperties(props);

		assertNotNull(Security.generateBootstrapPassword(user));
	}

	@Test
	void testValidateBootstrapPassword_shouldReturnFalseForAdminIssuedBeforeThePasswordWasLocked() {
		user.setUsername("admin");
		String password = issueBootstrapPassword(user);
		assertTrue(Security.validateBootstrapPassword(user, password));

		Properties props = Context.getRuntimeProperties();
		props.setProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY, "true");
		Context.setRuntimeProperties(props);

		assertFalse(Security.validateBootstrapPassword(user, password),
			"An admin bootstrap password issued before the lock must not validate while the lock is set");
	}

	/**
	 * Issues a bootstrap password in the test by marking the user directly, since the
	 * production issuance path is the service.
	 *
	 * @param user the user to issue a bootstrap password for
	 * @return the issued bootstrap password
	 */
	private String issueBootstrapPassword(User user) {
		String password = Security.generateBootstrapPassword(user);
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_BOOTSTRAP_PASSWORD_ISSUED, "true");
		return password;
	}
}
