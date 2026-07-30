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

import java.security.SecureRandom;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.UUID;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;

/**
 * Generates the uuids that identify OpenMRS objects.
 * <p>
 * It is preferable to use this class to generate random UUIDs, as the JDK's default implementation can be slow under a high volume of requests.
 * <p>
 * A cryptographically strong source is not needed here: uuids identify objects, they are not
 * secrets. Access to a record is granted by privilege checks rather than by a uuid being
 * unguessable, and security-sensitive tokens (such as user activation keys) are generated
 * separately. This class therefore hands java-uuid-generator a thread-confined random number
 * generator: each thread draws from its own {@link SplittableRandom} stream, so threads never queue
 * up behind a shared generator. {@link SecureRandom} is read exactly once, to seed the root stream
 * that all of the others are split from, which is what keeps uuids unique across threads, restarts
 * and servers.
 * <p>
 * Generated values are canonical, 36 character, version 4 (random) uuid strings, exactly as before,
 * so they still fit the 38 character uuid columns and existing uuids are unaffected. Should
 * time-ordered (version 7) uuids ever be wanted for their better insert locality on the uuid
 * indexes, the generator below is the only place that needs to change.
 *
 * @since 3.0.0
 */
public class UuidUtil {

	/**
	 * Seeded once per JVM from a cryptographically strong source and thereafter only ever split. The
	 * cost of the strong source does not matter for a single draw, while the seed quality it provides
	 * does: it is what stops the streams of different JVMs and servers from overlapping.
	 * <p>
	 * Reading {@link SecureRandom} exactly once and then letting it go is the whole point here rather
	 * than an oversight, so the "save and re-use this Random" warning is suppressed.
	 */
	@SuppressWarnings("java:S2119")
	private static final SplittableRandom ROOT = new SplittableRandom(new SecureRandom().nextLong());

	/**
	 * The random number stream a thread draws its uuids from.
	 */
	private static final ThreadLocal<SplittableRandom> STREAMS = ThreadLocal.withInitial(UuidUtil::newStream);

	/**
	 * java-uuid-generator's version 4 generator, drawing from the thread-confined streams above.
	 * <p>
	 * Two properties of {@code RandomBasedGenerator} are relied on here, and both are pinned by
	 * {@code UuidUtilTest}, because supplying a generator buys nothing if either of them changes: it
	 * does not synchronize on the generator it is handed, and it draws through
	 * {@link Random#nextLong()} rather than {@link Random#nextBytes(byte[])} for anything that is not a
	 * {@link SecureRandom}. Both hold as of 5.2.0.
	 */
	private static final NoArgGenerator UUID_GENERATOR = Generators.randomBasedGenerator(new ThreadConfinedRandom());

	private UuidUtil() {
	}

	/**
	 * Hands out the stream for a thread that is generating its first uuid. Splitting gives the stream
	 * its own increment as well as its own starting point, so that streams stay independent of one
	 * another rather than being offsets into a single shared sequence.
	 * <p>
	 * The root has to be guarded because {@link SplittableRandom#split()} advances the instance it is
	 * called on, but the monitor is entered once per thread rather than once per uuid, and it is held
	 * for nothing more than a few arithmetic operations.
	 *
	 * @return the calling thread's own random number stream
	 */
	private static SplittableRandom newStream() {
		synchronized (ROOT) {
			return ROOT.split();
		}
	}

	/**
	 * Generates a new uuid.
	 *
	 * @return a newly generated version 4 uuid
	 * @since 3.0.0
	 */
	public static UUID newUuid() {
		return UUID_GENERATOR.generate();
	}

	/**
	 * Generates a new uuid in the canonical 36 character string form used by the uuid columns, for
	 * example {@code 6e9a0b1c-6d3a-4f0e-8a1b-2c3d4e5f6a7b}.
	 *
	 * @return a newly generated version 4 uuid as a string
	 * @since 3.0.0
	 */
	public static String newUuidString() {
		return newUuid().toString();
	}

	/**
	 * A {@link Random} that serves every draw from the calling thread's own stream. It holds no state
	 * of its own, so handing the single generator instance to every thread is safe and, unlike the
	 * {@link SecureRandom} java-uuid-generator would use by default, involves no locking.
	 */
	private static final class ThreadConfinedRandom extends Random {

		private static final long serialVersionUID = 1L;

		@Override
		public long nextLong() {
			return STREAMS.get().nextLong();
		}

		@Override
		public int nextInt() {
			return STREAMS.get().nextInt();
		}

		@Override
		public void nextBytes(byte[] bytes) {
			STREAMS.get().nextBytes(bytes);
		}

		@Override
		protected int next(int bits) {
			return STREAMS.get().nextInt() >>> (Integer.SIZE - bits);
		}
	}
}
