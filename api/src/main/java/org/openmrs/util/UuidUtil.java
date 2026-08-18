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

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;

/**
 * Generates the uuids that identify OpenMRS objects.
 * <p>
 * It is preferable to use this class to generate random UUIDs, as the JDK's default implementation
 * can be slow under a high volume of requests. The uuids it generates are not suitable for
 * security-sensitive purposes.
 *
 * @since 2.8.9, 2.9.0, 3.0.0
 */
public class UuidUtil {

	private static final NoArgGenerator UUID_GENERATOR = Generators.randomBasedGenerator(new ThreadLocalRandomSource());

	private UuidUtil() {
	}

	/**
	 * Generates a new uuid.
	 *
	 * @return a newly generated version 4 uuid
	 * @since 2.8.9, 3.0.0, 2.9.0
	 */
	public static UUID newUuid() {
		return UUID_GENERATOR.generate();
	}

	/**
	 * Generates a new uuid in the canonical 36 character string form used by the uuid columns, for
	 * example {@code 6e9a0b1c-6d3a-4f0e-8a1b-2c3d4e5f6a7b}.
	 *
	 * @return a newly generated version 4 uuid as a string
	 * @since 2.8.9, 3.0.0, 2.9.0
	 */
	public static String newUuidString() {
		return newUuid().toString();
	}

	/**
	 * Presents {@link ThreadLocalRandom} as the {@link Random} that java-uuid-generator takes. A single
	 * generator is shared by every thread, so each draw goes through
	 * {@link ThreadLocalRandom#current()} rather than a stored instance, which is only valid on the
	 * thread that obtained it.
	 */
	private static final class ThreadLocalRandomSource extends Random {

		private static final long serialVersionUID = 1L;

		@Override
		public long nextLong() {
			return ThreadLocalRandom.current().nextLong();
		}

		@Override
		public int nextInt() {
			return ThreadLocalRandom.current().nextInt();
		}

		@Override
		public void nextBytes(byte[] bytes) {
			ThreadLocalRandom.current().nextBytes(bytes);
		}

		@Override
		protected int next(int bits) {
			return ThreadLocalRandom.current().nextInt() >>> (Integer.SIZE - bits);
		}
	}
}
