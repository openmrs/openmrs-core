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

/**
 * Wraps OpenMRS's historical password hashing behind Spring Security's {@link PasswordEncoder}:
 * SHA-512 over {@code password + salt}, with the older SHA-1 variants still accepted so existing
 * rows keep authenticating.
 * <p>
 * Callers combine the secret with the salt themselves ({@code secret + salt}) before calling this
 * encoder. {@link #encode(CharSequence)} generates a fresh salt and returns the digest joined with
 * it as {@code hash:salt}; that value is what {@link Security#encodePassword(String)} persists
 * (prefixed with the encoder id, e.g. {@code {legacy}}). {@link #matches(CharSequence, String)}
 * accepts that same shape, as well as a bare hash with no salt part for values written by older
 * OpenMRS versions.
 *
 * @since 2.9.0, 3.0.0
 */
public class LegacyOpenmrsPasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence rawPassword) {
		return hashAndFormat(rawPassword, Security.getRandomToken());
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		if (encodedPassword == null || rawPassword == null) {
			return false;
		}
		return Security.hashMatchesLegacy(rawPassword.toString(), encodedPassword);
	}

	/**
	 * Computes the SHA-512 sum of {@code rawPassword + salt} and returns it joined with the salt as
	 * {@code hash:salt}.
	 *
	 * @param rawPassword the raw password
	 * @param salt the salt to use for this encoding
	 * @return the joined value, {@code hash:salt}
	 */
	private String hashAndFormat(CharSequence rawPassword, String salt) {
		String encoded = Security.encodeString(rawPassword.toString() + salt);
		return encoded + ":" + salt;
	}
}
