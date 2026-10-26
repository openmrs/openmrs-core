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

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

/**
 * The Argon2id hashing policy: fixed salt and hash lengths, default work factors, and the
 * validators the {@code argon2PasswordEncoder} bean definition uses to read the
 * {@code security.argon2.*} runtime properties.
 *
 * @since 2.8.11
 */
public final class Argon2WorkFactors {

	/**
	 * Salt and hash lengths are fixed at the OWASP-recommended minimums rather than being configurable:
	 * they are not work factors, and a length short enough to be worth tuning is a length that should
	 * not be tuned.
	 */
	static final int SALT_LENGTH = 16;

	static final int HASH_LENGTH = 32;

	/**
	 * Defaults for the three tunable work factors. The {@code openmrsPasswordEncoder} bean takes these
	 * from {@code security.argon2.*} in {@code openmrs-runtime.properties}, so an administrator can
	 * raise them without a rebuild.
	 * <p>
	 * The values are the minimum Argon2id configuration recommended by the OWASP Password Storage Cheat
	 * Sheet: 19 MiB of memory, 2 iterations and 1 degree of parallelism.
	 */
	static final int DEFAULT_PARALLELISM = 1;

	static final int DEFAULT_MEMORY = 19456;

	static final int DEFAULT_ITERATIONS = 2;

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

	private Argon2WorkFactors() {
	}

	/**
	 * Validates one Argon2 work factor read from {@code openmrs-runtime.properties} and returns it for
	 * use as a {@link Argon2PasswordEncoder} constructor argument.
	 * <p>
	 * This deliberately fails the application context rather than substituting a value. A password
	 * policy the administrator did not intend is not something to correct quietly on their behalf, and
	 * the alternative is worse than a failed start: a mistyped value would otherwise be written to the
	 * database as a hash that looks deliberate but is not memory-hard at all, which is what a memory
	 * cost of 0 produces.
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
		} catch (NumberFormatException e) {
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
	public static int parallelism(String value) {
		return workFactor("security.argon2.parallelism", value, MIN_PARALLELISM, MAX_PARALLELISM);
	}

	/**
	 * @return the memory cost in KiB, from {@code security.argon2.memory}
	 */
	public static int memory(String value) {
		return workFactor("security.argon2.memory", value, MIN_MEMORY, Integer.MAX_VALUE);
	}

	/**
	 * @return the number of passes to hash with, from {@code security.argon2.iterations}
	 */
	public static int iterations(String value) {
		return workFactor("security.argon2.iterations", value, MIN_ITERATIONS, Integer.MAX_VALUE);
	}
}
