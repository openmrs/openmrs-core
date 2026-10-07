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

import java.util.Collections;
import java.util.Map;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Builds the fallback password encoder OpenMRS uses when no Spring context exists yet, for
 * example while the database upgrade wizard authenticates a superuser.
 * <p>
 * The {@code workFactor} methods are referenced from the {@code argon2PasswordEncoder} bean
 * definition, so the range a configured work factor has to fall in lives alongside the rest of the
 * Argon2 policy rather than being spread between the properties file and a comment.
 *
 * @since 2.8.11
 */
public final class PasswordEncoders {

	/**
	 * Prefix identifying an Argon2 hash, as in <code>{argon2}$argon2id$...</code>.
	 */
	public static final String ARGON2_ID = "argon2";

	/**
	 * Salt and hash lengths are fixed at the OWASP-recommended minimums rather than being
	 * configurable: they are not work factors, and a length short enough to be worth tuning is a
	 * length that should not be tuned.
	 */
	private static final int ARGON2_SALT_LENGTH = 16;
	private static final int ARGON2_HASH_LENGTH = 32;

	/**
	 * Defaults for the three tunable work factors. The {@code openmrsPasswordEncoder} bean takes
	 * these from {@code security.argon2.*} in {@code openmrs-runtime.properties}, so an
	 * administrator can raise them without a rebuild. The memory default is 19456 KiB rather than
	 * Spring's 16384 because it is the floor OWASP recommends for Argon2id.
	 */
	private static final int DEFAULT_PARALLELISM = 1;
	private static final int DEFAULT_MEMORY = 19456;
	private static final int DEFAULT_ITERATIONS = 2;

	/**
	 * Bounds the {@code security.argon2.parallelism} property.
	 */
	private static final int MIN_PARALLELISM = 1;
	private static final int MAX_PARALLELISM = 16777215;

	/**
	 * Lower bound for {@code security.argon2.memory}. Argon2 requires at least 8 blocks (RFC 9106).
	 */
	private static final int MIN_MEMORY = 8;

	/**
	 * Lower bound for {@code security.argon2.iterations}.
	 */
	private static final int MIN_ITERATIONS = 1;

	private PasswordEncoders() {
	}

	/**
	 * Validates one Argon2 work factor read from {@code openmrs-runtime.properties} and returns it
	 * for use as a {@link Argon2PasswordEncoder} constructor argument.
	 * <p>
	 * This deliberately fails the application context rather than substituting a value. A
	 * password policy the administrator did not intend is not something to correct quietly on their
	 * behalf, and the alternative is worse than a failed start: a mistyped value would otherwise be
	 * written to the database as a hash that looks deliberate but is not memory-hard at all, which
	 * is what a memory cost of 0 produces.
	 *
	 * @param name the property name, used only to make the failure actionable
	 * @param value the raw configured value
	 * @param min the smallest value Argon2 will accept
	 * @param max the largest value Argon2 will accept
	 * @return the parsed value
	 * @throws IllegalArgumentException if the value is not an integer, or is out of range
	 */
	public static int workFactor(String name, String value, int min, int max) {
		int parsed;
		try {
			parsed = Integer.parseInt(value.trim());
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(name + " must be a whole number, but was '" + value + "'", e);
		}

		if (parsed < min || parsed > max) {
			throw new IllegalArgumentException(name + " must be between " + min + " and " + max + ", but was " + parsed);
		}

		return parsed;
	}

	/**
	 * @return the number of lanes to hash with, from {@code security.argon2.parallelism}
	 */
	public static int parallelismWorkFactor(String value) {
		return workFactor("security.argon2.parallelism", value, MIN_PARALLELISM, MAX_PARALLELISM);
	}

	/**
	 * @return the memory cost in KiB, from {@code security.argon2.memory}
	 */
	public static int memoryWorkFactor(String value) {
		return workFactor("security.argon2.memory", value, MIN_MEMORY, Integer.MAX_VALUE);
	}

	/**
	 * @return the number of passes to hash with, from {@code security.argon2.iterations}
	 */
	public static int iterationsWorkFactor(String value) {
		return workFactor("security.argon2.iterations", value, MIN_ITERATIONS, Integer.MAX_VALUE);
	}

	/**
	 * @return the named encoders, keyed by the prefix each one writes. An empty id for encode
	 *         selects the legacy encoder instead, so newly written values stay unprefixed.
	 */
	public static Map<String, PasswordEncoder> supportedEncoders() {
		return Collections.singletonMap(ARGON2_ID, argon2());
	}

	/**
	 * @return the encoder to use before a Spring context exists, for example while the database
	 *         upgrade wizard authenticates a superuser. It verifies every encoder in
	 *         {@link #supportedEncoders()} as well as the unprefixed legacy hashes, so a password
	 *         written after the {@code openmrsPasswordEncoder} opt-in still authenticates.
	 */
	public static PasswordEncoder noContextEncoder() {
		return new OpenmrsDelegatingPasswordEncoder("", supportedEncoders(), new LegacyOpenmrsPasswordEncoder());
	}

	private static PasswordEncoder argon2() {
		return new Argon2PasswordEncoder(ARGON2_SALT_LENGTH, ARGON2_HASH_LENGTH, DEFAULT_PARALLELISM,
			DEFAULT_MEMORY, DEFAULT_ITERATIONS);
	}
}
