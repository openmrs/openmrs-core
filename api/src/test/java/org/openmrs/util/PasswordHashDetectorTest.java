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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.util.PasswordHashDetector.PasswordHashAlgorithm;

/**
 * Tests for the {@link PasswordHashDetector} class.
 */
public class PasswordHashDetectorTest {

	// Valid Argon2 hash formats (standard reference implementation encoded format)

	private static final String ARGON2ID_HASH = "$argon2id$v=19$m=65536,t=3,p=1$c29tZXNhbHQ$somehashvalue";

	private static final String ARGON2I_HASH = "$argon2i$v=19$m=65536,t=3,p=1$c29tZXNhbHQ$somehashvalue";

	private static final String ARGON2D_HASH = "$argon2d$v=19$m=65536,t=3,p=1$c29tZXNhbHQ$somehashvalue";

	// SHA-512 hash (128 hex characters - the current OpenMRS default)

	private static final String SHA512_HASH = "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce"
		+ "47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e";

	// Legacy SHA-1 hash (40 hex characters)

	private static final String SHA1_HASH = "4a1750c8607d0fa237de36c6305715c223415189";

	// isArgon2Hash tests

	@Test
	public void isArgon2Hash_shouldReturnTrueForArgon2idHash() {
		assertTrue(PasswordHashDetector.isArgon2Hash(ARGON2ID_HASH));
	}

	@Test
	public void isArgon2Hash_shouldReturnTrueForArgon2iHash() {
		assertTrue(PasswordHashDetector.isArgon2Hash(ARGON2I_HASH));
	}

	@Test
	public void isArgon2Hash_shouldReturnTrueForArgon2dHash() {
		assertTrue(PasswordHashDetector.isArgon2Hash(ARGON2D_HASH));
	}

	@Test
	public void isArgon2Hash_shouldReturnFalseForNullInput() {
		assertFalse(PasswordHashDetector.isArgon2Hash(null));
	}

	@Test
	public void isArgon2Hash_shouldReturnFalseForEmptyString() {
		assertFalse(PasswordHashDetector.isArgon2Hash(""));
	}

	@Test
	public void isArgon2Hash_shouldReturnFalseForSha512Hash() {
		assertFalse(PasswordHashDetector.isArgon2Hash(SHA512_HASH));
	}

	@Test
	public void isArgon2Hash_shouldReturnFalseForSha1Hash() {
		assertFalse(PasswordHashDetector.isArgon2Hash(SHA1_HASH));
	}

	@Test
	public void isArgon2Hash_shouldReturnFalseForPlainText() {
		assertFalse(PasswordHashDetector.isArgon2Hash("plaintextpassword"));
	}

	@Test
	public void isArgon2Hash_shouldReturnTrueForPartialArgon2Prefix() {
		assertTrue(PasswordHashDetector.isArgon2Hash("$argon2"));
	}

	@Test
	public void isArgon2Hash_shouldReturnFalseForMalformedArgon2Hash() {
		assertFalse(PasswordHashDetector.isArgon2Hash("argon2id$v=19$m=65536,t=3,p=1"));
	}

	// detectAlgorithm tests

	@Test
	public void detectAlgorithm_shouldReturnArgon2ForArgon2idHash() {
		assertEquals(PasswordHashAlgorithm.ARGON2, PasswordHashDetector.detectAlgorithm(ARGON2ID_HASH));
	}

	@Test
	public void detectAlgorithm_shouldReturnArgon2ForArgon2iHash() {
		assertEquals(PasswordHashAlgorithm.ARGON2, PasswordHashDetector.detectAlgorithm(ARGON2I_HASH));
	}

	@Test
	public void detectAlgorithm_shouldReturnArgon2ForArgon2dHash() {
		assertEquals(PasswordHashAlgorithm.ARGON2, PasswordHashDetector.detectAlgorithm(ARGON2D_HASH));
	}

	@Test
	public void detectAlgorithm_shouldReturnUnknownForNullInput() {
		assertEquals(PasswordHashAlgorithm.UNKNOWN, PasswordHashDetector.detectAlgorithm(null));
	}

	@Test
	public void detectAlgorithm_shouldReturnUnknownForEmptyString() {
		assertEquals(PasswordHashAlgorithm.UNKNOWN, PasswordHashDetector.detectAlgorithm(""));
	}

	@Test
	public void detectAlgorithm_shouldReturnUnknownForSha512Hash() {
		assertEquals(PasswordHashAlgorithm.UNKNOWN, PasswordHashDetector.detectAlgorithm(SHA512_HASH));
	}

	@Test
	public void detectAlgorithm_shouldReturnUnknownForSha1Hash() {
		assertEquals(PasswordHashAlgorithm.UNKNOWN, PasswordHashDetector.detectAlgorithm(SHA1_HASH));
	}

	@Test
	public void detectAlgorithm_shouldReturnUnknownForPlainText() {
		assertEquals(PasswordHashAlgorithm.UNKNOWN, PasswordHashDetector.detectAlgorithm("plaintextpassword"));
	}

	@Test
	public void detectAlgorithm_shouldReturnArgon2ForPartialArgon2Prefix() {
		assertEquals(PasswordHashAlgorithm.ARGON2, PasswordHashDetector.detectAlgorithm("$argon2"));
	}

	@Test
	public void detectAlgorithm_shouldReturnUnknownForMalformedArgon2Hash() {
		assertEquals(PasswordHashAlgorithm.UNKNOWN, PasswordHashDetector.detectAlgorithm("argon2id$v=19$m=65536,t=3,p=1"));
	}
}
