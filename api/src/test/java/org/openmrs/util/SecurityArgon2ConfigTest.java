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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * Tests how the Argon2id work factors are resolved from the {@code security.argon2.*}
 * runtime properties and clamped to safe bounds, and that an encoder produced from the
 * resolved values yields a PHC string that always fits the {@value
 * Security#MAX_PASSWORD_COLUMN_LENGTH} character {@code users.password} column.
 * <p>
 * Deliberately a plain unit test (no Spring context): the {@code argon2PasswordEncoder}
 * bean is wired through {@link Security#createArgon2PasswordEncoder(String, String, String, String, String)}
 * from the same placeholders, so this exercises the exact parsing and clamping the Spring
 * context relies on without booting a server.
 */
class SecurityArgon2ConfigTest {

	@Test
	void shouldUseOWASPDefaultsWhenNoRuntimePropertiesAreSet() {
		Security.Argon2Config config = Security.resolveArgon2Config(new Properties());
		assertEquals(16, config.saltLength);
		assertEquals(32, config.hashLength);
		assertEquals(1, config.parallelism);
		assertEquals(19456, config.memory);
		assertEquals(2, config.iterations);
	}

	@Test
	void shouldUseTheValuesFromTheRuntimeProperties() {
		Properties props = new Properties();
		props.setProperty("security.argon2.memory", "32768");
		props.setProperty("security.argon2.iterations", "4");
		props.setProperty("security.argon2.parallelism", "2");
		props.setProperty("security.argon2.saltLength", "16");
		props.setProperty("security.argon2.hashLength", "32");
		Security.Argon2Config config = Security.resolveArgon2Config(props);
		assertEquals(32768, config.memory);
		assertEquals(4, config.iterations);
		assertEquals(2, config.parallelism);
		assertEquals(16, config.saltLength);
		assertEquals(32, config.hashLength);
	}

	@Test
	void shouldClampValuesThatAreBelowTheAllowedMinimum() {
		Properties props = new Properties();
		props.setProperty("security.argon2.memory", "1");
		props.setProperty("security.argon2.iterations", "0");
		props.setProperty("security.argon2.parallelism", "0");
		props.setProperty("security.argon2.saltLength", "4");
		props.setProperty("security.argon2.hashLength", "2");
		Security.Argon2Config config = Security.resolveArgon2Config(props);
		// Clamp to the OWASP security floor (m=19456, t=2, p=1, hash=32), not the encoder's
		// technical minimums: a site that sets everything low must not end up weaker than
		// SHA-512 (a 32 byte Argon2 hash is far below SHA-512's 64 bytes, and lowering the
		// technical Argon2 minimum would make it weaker still).
		assertEquals(19456, config.memory);
		assertEquals(2, config.iterations);
		assertEquals(1, config.parallelism);
		assertEquals(8, config.saltLength);
		assertEquals(32, config.hashLength);
	}

	@Test
	void shouldClampValuesThatExceedTheAllowedMaximum() {
		Properties props = new Properties();
		props.setProperty("security.argon2.memory", "2000000");
		props.setProperty("security.argon2.iterations", "100");
		props.setProperty("security.argon2.parallelism", "16");
		props.setProperty("security.argon2.saltLength", "64");
		props.setProperty("security.argon2.hashLength", "128");
		Security.Argon2Config config = Security.resolveArgon2Config(props);
		assertEquals(1048576, config.memory);
		assertEquals(10, config.iterations);
		assertEquals(8, config.parallelism);
		assertEquals(32, config.saltLength);
		// The 128 byte hash length does not fit together with the encoder id
		// prefix and the PHC header at these maximum work factors, so it is
		// capped to the 31 bytes that do fit.
		assertEquals(31, config.hashLength);
	}

	@Test
	void shouldUseDefaultsForNonNumericValues() {
		Properties props = new Properties();
		props.setProperty("security.argon2.memory", "large");
		props.setProperty("security.argon2.iterations", "many");
		props.setProperty("security.argon2.parallelism", "lots");
		props.setProperty("security.argon2.saltLength", "salty");
		props.setProperty("security.argon2.hashLength", "long");
		Security.Argon2Config config = Security.resolveArgon2Config(props);
		assertEquals(19456, config.memory);
		assertEquals(2, config.iterations);
		assertEquals(1, config.parallelism);
		assertEquals(16, config.saltLength);
		assertEquals(32, config.hashLength);
	}

	@Test
	void shouldFallBackToDefaultsWhenTheBeanFactoryReceivesNonNumericPlaceholderValues() {
		// The argon2PasswordEncoder bean is wired in applicationContext-service.xml from
		// ${security.argon2.*} placeholders via this factory method, so the raw (possibly
		// mistyped) value must be parsed here with the same fallback the resolver documents.
		// Regression test for the startup failure that an int-typed factory method produced:
		// Spring converted a mistyped value (e.g. security.argon2.memory=19456k) to int before
		// this method ran, bypassing the fallback and stopping the context from coming up.
		org.springframework.security.crypto.argon2.Argon2PasswordEncoder encoder = Security
			.createArgon2PasswordEncoder("salty", "long", "lots", "19456k", "many");

		String phc = encoder.encode("test");
		assertTrue(phc.startsWith("$argon2id$"));
		// Falling back to the OWASP defaults means a value 3 KiB bigger than the v5.8 default;
		// the memory value appears in the PHC header.
		assertTrue(phc.contains("m=19456,t=2,p=1"), "Expected OWASP default work factors in PHC, got: " + phc);
	}

	@Test
	void shouldClampHashLengthSoThePhcAlwaysFitsThePasswordColumn() {
		// With the default 16 byte salt the largest hash length whose "{argon2}"-prefixed
		// value still fits varchar(128) is 49; 50 or more overflows and would be silently
		// truncated on a non-strict DB.
		Properties props = new Properties();
		props.setProperty("security.argon2.saltLength", "16");
		props.setProperty("security.argon2.hashLength", "56");
		Security.Argon2Config config = Security.resolveArgon2Config(props);
		assertEquals(49, config.hashLength);
	}

	@Test
	void shouldClampHashLengthWithMaximumSaltLength() {
		// At salt 32 (the maximum the resolver allows) the largest fitting hash length is 33.
		Properties props = new Properties();
		props.setProperty("security.argon2.saltLength", "32");
		props.setProperty("security.argon2.hashLength", "40");
		Security.Argon2Config config = Security.resolveArgon2Config(props);
		assertEquals(33, config.hashLength);
	}

	@Test
	void shouldProducePhcThatFitsThePasswordColumnForDefaultWorkFactors() {
		org.springframework.security.crypto.argon2.Argon2PasswordEncoder encoder = Security.createArgon2PasswordEncoder("16", "32", "1", "19456", "2");
		String phc = encoder.encode("test");
		assertTrue(phc.length() <= 128, "PHC length " + phc.length() + " exceeds the 128 char password column: " + phc);
		assertTrue(Security.hashMatches(phc, "test"), "Encoded value must authenticate its raw password");
		// The openmrsPasswordEncoder bean persists "{argon2}" + phc, so the prefixed value
		// must fit the column as well as the bare PHC string.
		String prefixed = "{argon2}" + phc;
		assertTrue(prefixed.length() <= 128, "Stored length " + prefixed.length() + " exceeds the 128 char password column");
		assertTrue(Security.hashMatches(prefixed, "test"),
			"Prefixed value must authenticate through hashMatches as a stored row would");
	}
}
