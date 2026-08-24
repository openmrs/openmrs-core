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
	public void constructor_shouldRejectAnIdForEncodeThatIsNotConfigured() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> new OpenmrsDelegatingPasswordEncoder("scrypt", idToPasswordEncoder, fallbackEncoder));

		assertTrue(exception.getMessage().contains("scrypt"));
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
	public void matches_shouldUseTheEncoderNamedByTheIdForEncodeForAnUnprefixedPassword() {
		when(bcryptEncoder.matches("password", "hashedPassword")).thenReturn(true);
		OpenmrsDelegatingPasswordEncoder encoder = new OpenmrsDelegatingPasswordEncoder("bcrypt",
			idToPasswordEncoder, fallbackEncoder);

		assertTrue(encoder.matches("password", "hashedPassword"));
		verify(fallbackEncoder, never()).matches(any(), anyString());
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
}
