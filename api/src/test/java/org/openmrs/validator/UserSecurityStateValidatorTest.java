/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import org.junit.jupiter.api.Test;
import org.openmrs.UserSecurityState;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link UserSecurityStateValidator}.
 */
public class UserSecurityStateValidatorTest extends BaseContextSensitiveTest {

	/**
	 * @see UserSecurityStateValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfUserIdIsNull() {
		UserSecurityState entry = entry();
		entry.setUserId(null);

		assertTrue(validate(entry).hasFieldErrors("userId"));
	}

	/**
	 * @see UserSecurityStateValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPropertyIsNull() {
		UserSecurityState entry = entry();
		entry.setProperty(null);

		assertTrue(validate(entry).hasFieldErrors("property"));
	}

	/**
	 * @see UserSecurityStateValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPropertyIsEmptyOrWhitespace() {
		UserSecurityState entry = entry();
		entry.setProperty(" ");

		assertTrue(validate(entry).hasFieldErrors("property"));
	}

	/**
	 * @see UserSecurityStateValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPropertyIsLongerThan255Characters() {
		UserSecurityState entry = entry();
		entry.setProperty("x".repeat(256));

		assertTrue(validate(entry).hasFieldErrors("property"));
	}

	/**
	 * @see UserSecurityStateValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfValueIsNull() {
		UserSecurityState entry = entry();
		entry.setValue(null);

		assertTrue(validate(entry).hasFieldErrors("value"));
	}

	/**
	 * @see UserSecurityStateValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfValueIsEmptyOrWhitespace() {
		UserSecurityState entry = entry();
		entry.setValue(" ");

		assertTrue(validate(entry).hasFieldErrors("value"));
	}

	/**
	 * @see UserSecurityStateValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredFieldsHaveProperValues() {
		assertFalse(validate(entry()).hasErrors());
	}

	private static UserSecurityState entry() {
		UserSecurityState entry = new UserSecurityState();
		entry.setUserId(1);
		entry.setProperty("x".repeat(255));
		entry.setValue("x".repeat(5000));
		return entry;
	}

	private static Errors validate(UserSecurityState entry) {
		Errors errors = new BindException(entry, "entry");
		new UserSecurityStateValidator().validate(entry, errors);
		return errors;
	}
}
