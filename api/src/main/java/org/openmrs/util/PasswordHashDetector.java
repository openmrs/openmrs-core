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

import java.util.regex.Pattern;

/**
 * Utility class for detecting the algorithm used to hash a password.
 * This class only identifies the hash format; it does not perform hashing, verification, or migration.
 *
 * @since 2.8.8
 */
public class PasswordHashDetector {

	private static final Pattern ARGON2_PATTERN = Pattern.compile("^\\$argon2(?:id|i|d)\\$.*");

	private static final Pattern SHA_512_PATTERN = Pattern.compile("^[0-9a-f]{128}$");

	private static final Pattern SHA_1_PATTERN = Pattern.compile("^[0-9a-f]{40}$");

	private PasswordHashDetector() {
	}

	/**
	 * Detected password hash algorithm.
	 */
	public enum PasswordHashAlgorithm {
		/**
		 * Argon2 family (Argon2i, Argon2d, Argon2id).
		 */
		ARGON2,

		/**
		 * SHA-512 (current OpenMRS default).
		 */
		SHA_512,

		/**
		 * SHA-1 (legacy).
		 */
		SHA_1,

		/**
		 * Unknown or unrecognized algorithm.
		 */
		UNKNOWN
	}

	/**
	 * Check if the given password hash matches the standard Argon2 encoded format.
	 * Argon2 hashes start with {@code $argon2} (e.g., {@code $argon2id$v=19$m=65536,t=3,p=1$salt$hash}).
	 *
	 * @param hashedPassword the stored password hash to check
	 * @return true if the hash matches the Argon2 format, false otherwise
	 */
	public static boolean isArgon2Hash(String hashedPassword) {
		if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
			return false;
		}
		return ARGON2_PATTERN.matcher(hashedPassword.trim()).matches();
	}

	/**
	 * Detect the password hash algorithm used for the given hash string.
	 *
	 * @param hashedPassword the stored password hash to analyze
	 * @return the detected algorithm, or {@link PasswordHashAlgorithm#UNKNOWN} if unrecognized
	 */
	public static PasswordHashAlgorithm detectAlgorithm(String hashedPassword) {
		if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
			return PasswordHashAlgorithm.UNKNOWN;
		}
		String s = hashedPassword.trim();

		if (ARGON2_PATTERN.matcher(s).matches()) {
			return PasswordHashAlgorithm.ARGON2;
		}

		String lower = s.toLowerCase();
		if (SHA_512_PATTERN.matcher(lower).matches()) {
			return PasswordHashAlgorithm.SHA_512;
		}
		if (SHA_1_PATTERN.matcher(lower).matches()) {
			return PasswordHashAlgorithm.SHA_1;
		}

		return PasswordHashAlgorithm.UNKNOWN;
	}
}
