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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Tests {@link OpenmrsDelegatingPasswordEncoder}. Most tests drive the encoder with mock
 * delegates so that the routing logic (which delegate is picked and what it is handed) can be
 * asserted directly, independent of any real hashing algorithm.
 */
public class OpenmrsDelegatingPasswordEncoderTest {

	private PasswordEncoder fallbackEncoder;

	private PasswordEncoder bcryptEncoder;

	private Map<String, PasswordEncoder> idToPasswordEncoder;

	@BeforeEach
	public void setUp() {
		fallbackEncoder = mock(PasswordEncoder.class);
		bcryptEncoder = mock(PasswordEncoder.class);

		// deliberately a mutable map with no null-key restrictions, matching what Spring builds for
		// the <map/> element in applicationContext-service.xml
		idToPasswordEncoder = new HashMap<>();
		idToPasswordEncoder.put("bcrypt", bcryptEncoder);
	}

	@Test
	public void constructor_shouldFallBackToTheLegacyEncoderForAnUnknownIdForEncode() {
		// An unknown security.passwordEncoder value (a typo, or a key that is not wired up)
		// must not stop the server from starting: it warns and keeps writing with the legacy
		// (fallback) encoder, never the named encoder.
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("scrypt",
			idToPasswordEncoder, fallbackEncoder);

		when(fallbackEncoder.encode("password")).thenReturn("legacyHash");
		String encoded = encoder.encode("password");
		// the delegate used is the fallback, and no bcrypt encoder was touched
		verify(fallbackEncoder).encode("password");
		verify(bcryptEncoder, never()).encode(any());
		assertEquals("legacyHash", encoded.substring(encoded.indexOf("}") + 1));
	}

	@Test
	public void encode_shouldPrefixTheEncodedPasswordWithTheIdForEncode() {
		when(bcryptEncoder.encode("password")).thenReturn("hashedPassword");
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("bcrypt",
			idToPasswordEncoder, fallbackEncoder);

		assertEquals("{bcrypt}hashedPassword", encoder.encode("password"));
		verify(fallbackEncoder, never()).encode(any());
	}

	@Test
	public void encode_shouldNotPrefixTheEncodedPasswordWhenTheIdForEncodeIsNull() {
		when(fallbackEncoder.encode("password")).thenReturn("hashedPassword");
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder(null, idToPasswordEncoder,
			fallbackEncoder);

		assertEquals("hashedPassword", encoder.encode("password"));
	}

	@Test
	public void encode_shouldNotPrefixTheEncodedPasswordWhenTheIdForEncodeIsEmpty() {
		when(fallbackEncoder.encode("password")).thenReturn("hashedPassword");
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("", idToPasswordEncoder,
			fallbackEncoder);

		assertEquals("hashedPassword", encoder.encode("password"));
	}

	@Test
	public void matches_shouldRouteAPrefixedPasswordToTheNamedEncoderWithoutThePrefix() {
		when(bcryptEncoder.matches("password", "hashedPassword")).thenReturn(true);
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("", idToPasswordEncoder,
			fallbackEncoder);

		assertTrue(encoder.matches("password", "{bcrypt}hashedPassword"));
		verify(fallbackEncoder, never()).matches(any(), anyString());
	}

	@Test
	public void matches_shouldUseTheDefaultEncoderForAnUnprefixedPassword() {
		when(fallbackEncoder.matches("password", "hashedPassword")).thenReturn(true);
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("", idToPasswordEncoder,
			fallbackEncoder);

		assertTrue(encoder.matches("password", "hashedPassword"));
		verify(bcryptEncoder, never()).matches(any(), anyString());
	}

	@Test
	public void matches_shouldRouteAnUnprefixedPasswordToTheFallbackEncoder() {
		when(fallbackEncoder.matches("password", "hashedPassword")).thenReturn(true);
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("bcrypt",
			idToPasswordEncoder, fallbackEncoder);

		// An unprefixed value predates the id-based encoder, so it must be verified by the
		// fallback even when new passwords are written with an id prefix. Sending it to
		// bcryptEncoder would reject every legacy row once the opt-in is enabled.
		assertTrue(encoder.matches("password", "hashedPassword"));
		verify(bcryptEncoder, never()).matches(any(), anyString());
	}

	@Test
	public void matches_shouldFallBackToTheDefaultEncoderForAnUnrecognisedPrefix() {
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("", idToPasswordEncoder,
			fallbackEncoder);

		assertFalse(encoder.matches("password", "{scrypt}hashedPassword"));
		// the prefix is still stripped before the default encoder sees the value
		verify(fallbackEncoder).matches("password", "hashedPassword");
	}

	@Test
	public void matches_shouldTreatABraceThatIsNotAtTheStartAsPartOfTheHash() {
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("", idToPasswordEncoder,
			fallbackEncoder);

		assertFalse(encoder.matches("password", "hashed{bcrypt}Password"));
		verify(fallbackEncoder).matches("password", "hashed{bcrypt}Password");
	}

	@Test
	public void matches_shouldTreatAnUnclosedBraceAsPartOfTheHash() {
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("", idToPasswordEncoder,
			fallbackEncoder);

		assertFalse(encoder.matches("password", "{bcrypt hashedPassword"));
		verify(fallbackEncoder).matches("password", "{bcrypt hashedPassword");
	}

	@Test
	public void matches_shouldReturnTrueWhenBothThePasswordAndTheHashAreNull() {
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("", idToPasswordEncoder,
			fallbackEncoder);

		assertTrue(encoder.matches(null, null));
		verify(fallbackEncoder, never()).matches(any(), any());
	}

	@Test
	public void matches_shouldDelegateWhenOnlyOneOfThePasswordAndTheHashIsNull() {
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("", idToPasswordEncoder,
			fallbackEncoder);

		assertFalse(encoder.matches("password", null));
		verify(fallbackEncoder).matches("password", null);

		assertFalse(encoder.matches(null, "hashedPassword"));
		verify(fallbackEncoder).matches(null, "hashedPassword");
	}

	@Test
	public void upgradeEncoding_shouldReturnTrueForAnUnprefixedPasswordWhenAnIdForEncodeIsConfigured() {
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("bcrypt",
			idToPasswordEncoder, fallbackEncoder);

		assertTrue(encoder.upgradeEncoding("hashedPassword"));
	}

	@Test
	public void upgradeEncoding_shouldReturnFalseForAPrefixedPassword() {
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("bcrypt",
			idToPasswordEncoder, fallbackEncoder);

		assertFalse(encoder.upgradeEncoding("{bcrypt}hashedPassword"));
		// even a prefix this instance cannot resolve counts as already-encoded
		assertFalse(encoder.upgradeEncoding("{scrypt}hashedPassword"));
	}

	@Test
	public void upgradeEncoding_shouldReturnFalseWhenNoIdForEncodeIsConfigured() {
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("", idToPasswordEncoder,
			fallbackEncoder);

		assertFalse(encoder.upgradeEncoding("hashedPassword"));
		assertFalse(encoder.upgradeEncoding("{bcrypt}hashedPassword"));
	}

	/**
	 * The shape wired up in applicationContext-service.xml: no id to encode with, an empty map of
	 * named encoders and the legacy encoder as the fallback. Existing rows must keep working and
	 * newly written values must stay unprefixed.
	 */
	@Test
	public void shouldRoundTripPasswordsWithTheConfigurationUsedByTheOpenmrsPasswordEncoderBean() {
		PasswordEncoder legacyEncoder = new LegacyOpenmrsPasswordEncoder();
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("", new HashMap<>(),
			legacyEncoder);

		String encoded = encoder.encode("password");

		assertFalse(encoded.startsWith("{"));
		assertTrue(encoder.matches("password", encoded));
		assertFalse(encoder.matches("wrongPassword", encoded));
		assertFalse(encoder.upgradeEncoding(encoded));
	}

	/**
	 * Models {@code security.passwordEncoder=argon2} and pins the exact behavior dkayiwa
	 * measured against a real context: every row shape that exists in a 2.8.x database has
	 * to keep authenticating the moment the opt-in is switched on. Nothing that was written
	 * before the opt-in may stop logging in.
	 */
	@Test
	public void shouldKeepVerifyingLegacyAndArgon2RowsWhenTheArgon2OptInIsEnabled() {
		Argon2PasswordEncoder argon2Encoder = new Argon2PasswordEncoder(16, 32, 1, 19456, 2);
		PasswordEncoder legacyEncoder = new LegacyOpenmrsPasswordEncoder();
		Map<String, PasswordEncoder> encoders = new HashMap<>();
		encoders.put("argon2", argon2Encoder);
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("argon2", encoders,
			legacyEncoder);

		// a new password is stored prefixed with the id, so the persisted value is
		// "{argon2}" + the PHC string
		String encoded = encoder.encode("password");
		assertTrue(encoded.startsWith("{argon2}"));
		assertTrue(encoder.matches("password", encoded));
		assertFalse(encoder.matches("wrongPassword", encoded));
		assertFalse(encoder.upgradeEncoding(encoded));

		// a bare SHA-512 value written before the opt-in must keep authenticating
		String legacyHash = legacyEncoder.encode("password");
		assertFalse(legacyHash.startsWith("{"));
		assertTrue(encoder.matches("password", legacyHash));
		// and must be a candidate for transparent re-encoding
		assertTrue(encoder.upgradeEncoding(legacyHash));

		// SHA-1 (40 char) rows from the pre-SHA-512 era must keep authenticating too
		assertTrue(encoder.matches("password", "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8"));
		assertTrue(encoder.upgradeEncoding("5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8"));

		// a bare Argon2 PHC row (however it got into the database) is also verified through
		// the fallback's hashMatches, which recognizes the $argon2id$ marker
		String bareArgon2 = argon2Encoder.encode("password");
		assertTrue(encoder.matches("password", bareArgon2));
		assertTrue(encoder.upgradeEncoding(bareArgon2));
	}

	/**
	 * Pins that {@code security.passwordEncoder=argon2id} (the algorithm name used in the
	 * ticket) opts in to the Argon2 encoder and writes "{argon2id}"-prefixed rows. The
	 * applicationContext-service.xml bean registers both the "argon2" and "argon2id" keys.
	 */
	@Test
	public void shouldOptInWithTheArgon2idEncoderName() {
		Argon2PasswordEncoder argon2Encoder = new Argon2PasswordEncoder(16, 32, 1, 19456, 2);
		PasswordEncoder legacyEncoder = new LegacyOpenmrsPasswordEncoder();
		Map<String, PasswordEncoder> encoders = new HashMap<>();
		encoders.put("argon2", argon2Encoder);
		encoders.put("argon2id", argon2Encoder);
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("argon2id", encoders,
			legacyEncoder);

		String encoded = encoder.encode("password");
		assertTrue(encoded.startsWith("{argon2id}"));
		assertTrue(encoder.matches("password", encoded));
		assertFalse(encoder.matches("wrongPassword", encoded));
	}
}
