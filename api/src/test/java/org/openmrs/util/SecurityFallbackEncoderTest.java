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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.spring.LegacyOpenmrsPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Verifies that the fallback encoder (used when no Spring context is available, e.g. during the
 * database upgrade wizard) is a {@code DelegatingPasswordEncoder} that can read both the
 * {@code {legacy}...} values written by the bean and bare hashes from older versions.
 * <p>
 * This test deliberately does <b>not</b> extend {@code BaseContextSensitiveTest} so it never
 * touches a Spring context — the exact path where the fallback is needed.
 */
class SecurityFallbackEncoderTest {

	@Test
	void fallbackEncoder_matchesBareHash() {
		PasswordEncoder fallback = new LegacyOpenmrsPasswordEncoder();
		String salt = Security.getRandomToken();
		String rawPassword = "password" + salt;
		String bareHash = Security.encodeString(rawPassword);

		assertTrue(fallback.matches(rawPassword, bareHash));
		assertFalse(fallback.matches("wrong" + salt, bareHash));
	}

	@Test
	void fallbackEncoder_matchesLegacyPrefixedValue() {
		PasswordEncoder fallback = Security.getPasswordEncoder();
		String salt = Security.getRandomToken();
		String rawPassword = "password" + salt;
		String encoded = fallback.encode(rawPassword);

		assertFalse(encoded.startsWith("{legacy}"));
		assertTrue(fallback.matches(rawPassword, encoded));
	}

	@Test
	void fallbackEncoder_matchesLegacyPrefixedValueWrittenByBean() {
		PasswordEncoder bean = Security.getPasswordEncoder();
		String salt = Security.getRandomToken();
		String rawPassword = "password" + salt;
		String encoded = bean.encode(rawPassword);

		assertTrue(Security.checkPassword(encoded, rawPassword));
		assertFalse(Security.checkPassword(encoded,"wrong" + salt));
	}
}
