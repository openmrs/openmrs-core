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
		String password = Security.generateBootstrapPassword(user);
		assertTrue(Security.validateBootstrapPassword(user, password));
	}

	@Test
	void testValidateBootstrapPassword_shouldReturnFalseForIncorrectPassword() {
		String password = Security.generateBootstrapPassword(user);
		assertFalse(Security.validateBootstrapPassword(user, password + "wrong"));
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
	void testIsBootstrapPasswordExpired_shouldBeFalseWhenNothingHasConsumedTheBootstrapPassword() {
		assertFalse(Security.isBootstrapPasswordExpired(user));
	}

	@Test
	void testIsBootstrapPasswordExpired_shouldBeTrueWhenBootstrapPasswordWasConsumed() {
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_BOOTSTRAP_PASSWORD_EXPIRED, "true");
		assertTrue(Security.isBootstrapPasswordExpired(user));
	}

	@Test
	void testIsBootstrapPasswordExpired_shouldNotBeSetByTheForcePasswordFlagAlone() {
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, "true");
		assertFalse(Security.isBootstrapPasswordExpired(user));
	}

	@Test
	void testIsBootstrapPasswordExpired_shouldNotBeClearedWhenTheForcePasswordFlagIsCleared() {
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_BOOTSTRAP_PASSWORD_EXPIRED, "true");
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, "false");
		assertTrue(Security.isBootstrapPasswordExpired(user));
	}

	@Test
	void testIsBootstrapPasswordExpired_shouldReturnFalseForNullUser() {
		assertFalse(Security.isBootstrapPasswordExpired(null));
	}

	@Test
	void testForcePasswordChange_shouldAlsoExpireTheBootstrapPassword() {
		Security.forcePasswordChange(user);
		assertEquals("true", user.getUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD));
		assertTrue(Security.isBootstrapPasswordExpired(user));
	}

	@Test
	void testValidateBootstrapPassword_shouldNotBeBlockedByTheForcePasswordFlagAlone() {
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, "true");
		String password = Security.generateBootstrapPassword(user);
		assertTrue(Security.validateBootstrapPassword(user, password));
	}

	@Test
	void testValidateBootstrapPassword_shouldReturnFalseForAnExpiredBootstrapPassword() {
		String password = Security.generateBootstrapPassword(user);
		assertTrue(Security.validateBootstrapPassword(user, password));

		Security.forcePasswordChange(user);

		assertFalse(Security.validateBootstrapPassword(user, password));
	}

	@Test
	void testValidateBootstrapPassword_shouldReturnFalseForAnExpiredBootstrapPasswordEvenAfterThePasswordIsCleared() {
		String password = Security.generateBootstrapPassword(user);
		Security.forcePasswordChange(user);
		user.setUserProperty(OpenmrsConstants.USER_PROPERTY_CHANGE_PASSWORD, "false");

		assertFalse(Security.validateBootstrapPassword(user, password));
	}
}
