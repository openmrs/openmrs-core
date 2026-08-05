/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.initialization;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the normalizeEnvVariableKey method to ensure env vars are properly normalized.
 * This tests the fix for env-var key normalization that was dropping create_database_* and
 * create_user_* credentials in scripted setup.
 */
public class InitializationFilterNormalizationTest {

	private InitializationFilter filter = new InitializationFilter();

	/**
	 * Tests that CREATE_DATABASE_USERNAME is preserved with underscores, not converted to dots
	 */
	@Test
	public void shouldPreserveCreateDatabaseUsernameUnderscores() throws Exception {
		String normalized = normalizeKey("CREATE_DATABASE_USERNAME");
		assertEquals("create_database_username", normalized,
		    "CREATE_DATABASE_USERNAME should be lowercased but keep underscores");
	}

	/**
	 * Tests that CREATE_DATABASE_PASSWORD is preserved with underscores, not converted to dots
	 */
	@Test
	public void shouldPreserveCreateDatabasePasswordUnderscores() throws Exception {
		String normalized = normalizeKey("CREATE_DATABASE_PASSWORD");
		assertEquals("create_database_password", normalized,
		    "CREATE_DATABASE_PASSWORD should be lowercased but keep underscores");
	}

	/**
	 * Tests that CREATE_USER_USERNAME is preserved with underscores, not converted to dots
	 */
	@Test
	public void shouldPreserveCreateUserUsernameUnderscores() throws Exception {
		String normalized = normalizeKey("CREATE_USER_USERNAME");
		assertEquals("create_user_username", normalized, "CREATE_USER_USERNAME should be lowercased but keep underscores");
	}

	/**
	 * Tests that CREATE_USER_PASSWORD is preserved with underscores, not converted to dots
	 */
	@Test
	public void shouldPreserveCreateUserPasswordUnderscores() throws Exception {
		String normalized = normalizeKey("CREATE_USER_PASSWORD");
		assertEquals("create_user_password", normalized, "CREATE_USER_PASSWORD should be lowercased but keep underscores");
	}

	/**
	 * Tests that non-whitelisted keys still get underscore-to-dot conversion
	 */
	@Test
	public void shouldConvertNonWhitelistedKeysUnderscoresToDots() throws Exception {
		String normalized = normalizeKey("CONNECTION_URL");
		assertEquals("connection.url", normalized,
		    "CONNECTION_URL (not in whitelist) should have underscores converted to dots");
	}

	/**
	 * Tests that other whitelisted keys are preserved with underscores
	 */
	@Test
	public void shouldPreserveOtherWhitelistedKeysUnderscores() throws Exception {
		String normalized = normalizeKey("DATABASE_NAME");
		assertEquals("database_name", normalized, "DATABASE_NAME (in whitelist) should be lowercased but keep underscores");
	}

	/**
	 * Invoke the private normalizeEnvVariableKey method using reflection
	 */
	private String normalizeKey(String key) throws Exception {
		Method method = InitializationFilter.class.getDeclaredMethod("normalizeEnvVariableKey", String.class);
		method.setAccessible(true);
		return (String) method.invoke(filter, key);
	}
}
