/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.security;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the encoder set that {@link org.openmrs.util.Security} relies on before a Spring context
 * exists. These are separate tests from {@code SecurityFallbackEncoderTest} because they guard the
 * configuration itself rather than the wiring.
 */
class NoContextPasswordEncoderFactoryTest {

	/**
	 * The parameter segment of a PHC string, e.g. {@code m=19456,t=2,p=1}, which is the only part that
	 * varies with the work factors: the salt and hash are random per encode.
	 */
	private static String paramsOf(String phcHash) {
		int start = phcHash.indexOf("v=19$") + "v=19$".length();
		return phcHash.substring(start, phcHash.indexOf('$', start));
	}

	@Test
	void supportedEncoders_shouldExposeArgon2AndNothingElse() {
		Map<String, PasswordEncoder> encoders = NoContextPasswordEncoderFactory.supportedEncoders();

		assertEquals(1, encoders.size());
		assertTrue(encoders.containsKey("argon2"));
	}

	/**
	 * The no-context encoder hard-codes the work factors, while the {@code openmrsPasswordEncoder} bean
	 * takes them from {@code security.argon2.*} in {@code openmrs-runtime.properties}. The two
	 * definitions have to agree, so this pins the values the bean falls back to when those properties
	 * are absent. {@code OpenmrsPasswordEncoderBeanTest} checks the two definitions against each other.
	 */
	@Test
	void supportedEncoders_shouldUseTheConfiguredDefaultArgon2WorkFactors() {
		PasswordEncoder argon2 = NoContextPasswordEncoderFactory.supportedEncoders()
		        .get(NoContextPasswordEncoderFactory.ARGON2_ID);

		assertEquals("m=19456,t=2,p=1", paramsOf(argon2.encode("password")));
	}

	@Test
	void create_shouldVerifyAnArgon2HashWrittenWithTheBean() {
		PasswordEncoder noContext = NoContextPasswordEncoderFactory.create();
		String stored = "{" + NoContextPasswordEncoderFactory.ARGON2_ID + "}"
		        + Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode("password" + "salty");

		assertTrue(noContext.matches("password" + "salty", stored));
		assertFalse(noContext.matches("wrong" + "salty", stored));
	}

	/**
	 * Regression guard: a site that never opts in still has unprefixed legacy hashes, and those must
	 * keep authenticating on the no-context path.
	 */
	@Test
	void create_shouldStillVerifyAnUnprefixedLegacyHash() {
		PasswordEncoder noContext = NoContextPasswordEncoderFactory.create();
		String stored = new LegacyOpenmrsPasswordEncoder().encode("password" + "salty");

		assertFalse(stored.startsWith("{"));
		assertTrue(noContext.matches("password" + "salty", stored));
		assertFalse(noContext.matches("wrong" + "salty", stored));
	}
}
