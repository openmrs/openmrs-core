/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Pins the encoder set that {@link org.openmrs.util.Security} relies on before a Spring context
 * exists. These are separate tests from {@code SecurityFallbackEncoderTest} because they guard the
 * configuration itself rather than the wiring.
 */
class PasswordEncodersTest {

	/**
	 * The parameter segment of a PHC string, e.g. {@code m=19456,t=2,p=1}, which is the only part
	 * that varies with the work factors: the salt and hash are random per encode.
	 */
	private static String paramsOf(String phcHash) {
		int start = phcHash.indexOf("v=19$") + "v=19$".length();
		return phcHash.substring(start, phcHash.indexOf('$', start));
	}

	@Test
	void supportedEncoders_shouldExposeArgon2AndNothingElse() {
		Map<String, PasswordEncoder> encoders = PasswordEncoders.supportedEncoders();

		assertEquals(1, encoders.size());
		assertTrue(encoders.containsKey("argon2"));
	}

	/**
	 * The no-context encoder hard-codes the work factors, while the {@code openmrsPasswordEncoder}
	 * bean takes them from {@code security.argon2.*} in {@code openmrs-runtime.properties}. The two
	 * definitions have to agree, so this pins the values the bean falls back to when those
	 * properties are absent. Note that Spring's own
	 * {@code Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()} is deliberately not the
	 * reference: it uses m=16384, whereas OpenMRS ships the OWASP-recommended m=19456.
	 * {@code OpenmrsPasswordEncoderBeanTest} checks the two definitions against each other.
	 */
	@Test
	void noContextEncoder_shouldUseTheOwaspRecommendedArgon2WorkFactors() {
		PasswordEncoder argon2 = PasswordEncoders.supportedEncoders().get(PasswordEncoders.ARGON2_ID);

		assertEquals("m=19456,t=2,p=1", paramsOf(argon2.encode("password")));
	}

	@Test
	void noContextEncoder_shouldVerifyAnArgon2HashWrittenWithTheBean() {
		PasswordEncoder noContext = PasswordEncoders.noContextEncoder();
		String stored = "{" + PasswordEncoders.ARGON2_ID + "}"
			+ Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8().encode("password" + "salty");

		assertTrue(noContext.matches("password" + "salty", stored));
		assertFalse(noContext.matches("wrong" + "salty", stored));
	}

	/**
	 * Regression guard: a site that never opts in still has unprefixed legacy hashes, and those must
	 * keep authenticating on the no-context path.
	 */
	@Test
	void noContextEncoder_shouldStillVerifyAnUnprefixedLegacyHash() {
		PasswordEncoder noContext = PasswordEncoders.noContextEncoder();
		String stored = new LegacyOpenmrsPasswordEncoder().encode("password" + "salty");

		assertFalse(stored.startsWith("{"));
		assertTrue(noContext.matches("password" + "salty", stored));
		assertFalse(noContext.matches("wrong" + "salty", stored));
	}

	/**
	 * The work factors reach the encoder as constructor arguments, so an out-of-range value has to
	 * be refused while the bean is being built. The boundaries are the ones Argon2 itself imposes;
	 * BouncyCastle is what rejects the parallelism and iteration bounds, and it does so from the
	 * first encode rather than from bean creation.
	 */
	@Test
	void workFactor_shouldAcceptTheArgon2Boundaries() {
		assertEquals(1, PasswordEncoders.parallelismWorkFactor("1"));
		assertEquals(16777215, PasswordEncoders.parallelismWorkFactor("16777215"), "BouncyCastle's lane ceiling");
		assertEquals(1, PasswordEncoders.iterationsWorkFactor("1"));
		assertEquals(19456, PasswordEncoders.memoryWorkFactor("19456"));
		assertEquals(8, PasswordEncoders.memoryWorkFactor("8"), "Argon2's smallest usable memory cost");
	}

	@Test
	void parallelismWorkFactor_shouldRejectValuesArgon2CannotHashWith() {
		IllegalArgumentException tooMany = assertThrows(IllegalArgumentException.class,
			() -> PasswordEncoders.parallelismWorkFactor("16777216"));
		assertTrue(tooMany.getMessage().contains("security.argon2.parallelism"),
			"the message has to name the property, but was: " + tooMany.getMessage());

		assertThrows(IllegalArgumentException.class, () -> PasswordEncoders.parallelismWorkFactor("0"));
		assertThrows(IllegalArgumentException.class, () -> PasswordEncoders.parallelismWorkFactor("-1"));
	}

	/**
	 * A memory cost of 0 encodes successfully in BouncyCastle and produces a hash carrying
	 * {@code m=0}, so without this check a mistyped value reaches the database as a working-looking
	 * password hash that is not one. Nothing downstream rejects it either, which is why the
	 * failure has to happen before the bean is built.
	 */
	@Test
	void memoryWorkFactor_shouldRejectAMemoryCostArgon2CannotUse() {
		IllegalArgumentException zero = assertThrows(IllegalArgumentException.class,
			() -> PasswordEncoders.memoryWorkFactor("0"));
		assertTrue(zero.getMessage().contains("security.argon2.memory"),
			"the message has to name the property, but was: " + zero.getMessage());

		assertThrows(IllegalArgumentException.class, () -> PasswordEncoders.memoryWorkFactor("-1"));
	}

	@Test
	void iterationsWorkFactor_shouldRejectAValueArgon2CannotUse() {
		assertThrows(IllegalArgumentException.class, () -> PasswordEncoders.iterationsWorkFactor("0"));
		assertThrows(IllegalArgumentException.class, () -> PasswordEncoders.iterationsWorkFactor("-1"));
	}

	@Test
	void workFactor_shouldRejectAValueThatIsNotANumber() {
		IllegalArgumentException notANumber = assertThrows(IllegalArgumentException.class,
			() -> PasswordEncoders.parallelismWorkFactor("many"));
		assertTrue(notANumber.getMessage().contains("security.argon2.parallelism"),
			"the message has to name the property, but was: " + notANumber.getMessage());
	}
}
