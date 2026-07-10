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

import org.springframework.security.crypto.password.PasswordEncoder;

public class LegacyOpenmrsPasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence rawPassword) {
		return hashAndFormat(rawPassword, Security.getRandomToken());
	}

	/**
	 * Encodes a password using a specific salt instead of generating a new one.
	 * Used for password changes where the existing salt must be preserved
	 * (e.g., to keep secret-answer hashes valid).
	 *
	 * @param rawPassword the password to encode
	 * @param salt the salt to use (must not be null or empty)
	 * @return the encoded password as {@code hash:salt}
	 */
	public String encodeWithSalt(CharSequence rawPassword, String salt) {
		if (salt == null || salt.isEmpty()) {
			throw new IllegalArgumentException("Salt must not be null or empty");
		}
		return hashAndFormat(rawPassword, salt);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		if (encodedPassword == null) {
			return false;
		}
		String[] parts = Security.parseEncodedPassword(encodedPassword);
		return Security.hashMatches(parts[0], rawPassword.toString() + parts[1]);
	}

	private String hashAndFormat(CharSequence rawPassword, String salt) {
		String hash = Security.encodeString(rawPassword.toString() + salt);
		return hash + ":" + salt;
	}
}
