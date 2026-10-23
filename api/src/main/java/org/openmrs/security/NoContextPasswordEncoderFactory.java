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

import java.util.Collections;
import java.util.Map;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Builds the password encoder OpenMRS uses when no Spring context exists yet, for example while the
 * database upgrade wizard authenticates a superuser.
 *
 * @since 2.8.11
 */
public final class NoContextPasswordEncoderFactory {

	/**
	 * Prefix identifying an Argon2 hash, as in <code>{argon2}$argon2id$...</code>.
	 */
	public static final String ARGON2_ID = "argon2";

	private NoContextPasswordEncoderFactory() {
	}

	/**
	 * @return the named encoders, keyed by the prefix each one writes. An empty id for encode selects
	 *         the legacy encoder instead, so newly written values stay unprefixed.
	 */
	public static Map<String, PasswordEncoder> supportedEncoders() {
		return Collections.singletonMap(ARGON2_ID, argon2());
	}

	/**
	 * @return an encoder that verifies every encoder in {@link #supportedEncoders()} as well as the
	 *         unprefixed legacy hashes, so a password written after the {@code openmrsPasswordEncoder}
	 *         opt-in still authenticates. New values are written with the legacy encoder.
	 */
	public static PasswordEncoder create() {
		return new OpenmrsDelegatingPasswordEncoder("", supportedEncoders(), new LegacyOpenmrsPasswordEncoder());
	}

	private static PasswordEncoder argon2() {
		return new Argon2PasswordEncoder(Argon2WorkFactors.SALT_LENGTH, Argon2WorkFactors.HASH_LENGTH,
		        Argon2WorkFactors.DEFAULT_PARALLELISM, Argon2WorkFactors.DEFAULT_MEMORY,
		        Argon2WorkFactors.DEFAULT_ITERATIONS);
	}
}
