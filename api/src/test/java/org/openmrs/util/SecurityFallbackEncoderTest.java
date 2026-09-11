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

import org.junit.jupiter.api.Test;
import org.openmrs.security.LegacyOpenmrsPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the fallback encoder (used when no Spring context is available, e.g. during the
 * database upgrade wizard) can read both bare hashes from older versions and values it wrote
 * itself, and separately that {@link Security#checkPassword(String, String)} round-trips a value
 * however {@link Security#getPasswordEncoder()} encoded it - fallback or real bean alike.
 * <p>
 * The first two tests instantiate the fallback directly, rather than obtaining it through
 * {@link Security#getPasswordEncoder()}, since that method's choice of fallback-vs-real-bean
 * depends on whether a Spring context has already been bootstrapped elsewhere in the JVM - true
 * early in the update wizard, but not reliably true here: Surefire reuses JVM forks across test
 * classes, so a fork that already ran a {@code BaseContextSensitiveTest}-based class would
 * otherwise make those two tests exercise the real, BCrypt-backed bean instead of the fallback they
 * mean to verify.
 */
class SecurityFallbackEncoderTest {

	private final PasswordEncoder fallback = new LegacyOpenmrsPasswordEncoder();

	@Test
	void fallbackEncoder_matchesBareHash() {
		String salt = Security.getRandomToken();
		String rawPassword = "password" + salt;
		String bareHash = Security.encodeString(rawPassword);

		assertTrue(fallback.matches(rawPassword, bareHash));
		assertFalse(fallback.matches("wrong" + salt, bareHash));
	}

	@Test
	void fallbackEncoder_matchesLegacyPrefixedValue() {
		String salt = Security.getRandomToken();
		String rawPassword = "password" + salt;
		String encoded = fallback.encode(rawPassword);

		assertFalse(encoded.startsWith("{legacy}"));
		assertTrue(fallback.matches(rawPassword, encoded));
	}

	@Test
	void fallbackEncoder_matchesLegacyPrefixedValueWrittenByBean() {
		String salt = Security.getRandomToken();
		String rawPassword = "password" + salt;
		String encoded = Security.encodePassword(rawPassword);

		assertTrue(Security.checkPassword(encoded, rawPassword));
		assertFalse(Security.checkPassword(encoded, "wrong" + salt));
	}
}
