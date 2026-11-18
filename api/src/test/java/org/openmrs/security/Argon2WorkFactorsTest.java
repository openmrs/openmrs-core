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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Argon2WorkFactorsTest {

	/**
	 * The work factors reach the encoder as constructor arguments, so an out-of-range value has to be
	 * refused while the bean is being built. The boundaries are the ones Argon2 itself imposes, and
	 * nothing downstream re-checks them once the encoder has been built.
	 */
	@Test
	void workFactor_shouldAcceptTheArgon2Boundaries() {
		assertEquals(1, Argon2WorkFactors.parallelism("1"));
		assertEquals(16777215, Argon2WorkFactors.parallelism("16777215"), "Argon2's lane ceiling");
		assertEquals(1, Argon2WorkFactors.iterations("1"));
		assertEquals(19456, Argon2WorkFactors.memory("19456"));
		assertEquals(8, Argon2WorkFactors.memory("8"), "Argon2's smallest usable memory cost");
	}

	@Test
	void parallelism_shouldRejectValuesArgon2CannotHashWith() {
		IllegalArgumentException tooMany = assertThrows(IllegalArgumentException.class,
		    () -> Argon2WorkFactors.parallelism("16777216"));
		assertTrue(tooMany.getMessage().contains("security.argon2.parallelism"),
		    "the message has to name the property, but was: " + tooMany.getMessage());

		assertThrows(IllegalArgumentException.class, () -> Argon2WorkFactors.parallelism("0"));
		assertThrows(IllegalArgumentException.class, () -> Argon2WorkFactors.parallelism("-1"));
	}

	/**
	 * A memory cost of 0 would be written to the database as a hash carrying {@code m=0}, so without
	 * this check a mistyped value reaches the database as a working-looking password hash that is not
	 * memory-hard at all. Nothing downstream rejects it either, which is why the failure has to happen
	 * before the bean is built.
	 */
	@Test
	void memory_shouldRejectAMemoryCostArgon2CannotUse() {
		IllegalArgumentException zero = assertThrows(IllegalArgumentException.class, () -> Argon2WorkFactors.memory("0"));
		assertTrue(zero.getMessage().contains("security.argon2.memory"),
		    "the message has to name the property, but was: " + zero.getMessage());

		assertThrows(IllegalArgumentException.class, () -> Argon2WorkFactors.memory("-1"));
	}

	@Test
	void iterations_shouldRejectAValueArgon2CannotUse() {
		assertThrows(IllegalArgumentException.class, () -> Argon2WorkFactors.iterations("0"));
		assertThrows(IllegalArgumentException.class, () -> Argon2WorkFactors.iterations("-1"));
	}

	@Test
	void workFactor_shouldRejectAValueThatIsNotANumber() {
		IllegalArgumentException notANumber = assertThrows(IllegalArgumentException.class,
		    () -> Argon2WorkFactors.parallelism("many"));
		assertTrue(notANumber.getMessage().contains("security.argon2.parallelism"),
		    "the message has to name the property, but was: " + notANumber.getMessage());
	}
}
