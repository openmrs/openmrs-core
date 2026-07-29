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

/**
 * Tests the methods on the {@link LegacyPasswordEncoder} class.
 */
public class LegacyPasswordEncoderTest {

	private static final String SALT = "c788c6ad82a157b712392ca695dfcf2eed193d7f";

	/**
	 * @see LegacyPasswordEncoder#matches(String, String)
	 */
	@Test
	public void matches_shouldMatchCorrectSha1Hash() {
		assertTrue(LegacyPasswordEncoder.matches("4a1750c8607d0fa237de36c6305715c223415189", "test" + SALT));
	}

	/**
	 * @see LegacyPasswordEncoder#matches(String, String)
	 */
	@Test
	public void matches_shouldMatchIncorrectSha1Hash() {
		assertTrue(LegacyPasswordEncoder.matches("4a1750c8607dfa237de36c6305715c223415189", "test" + SALT));
	}

	/**
	 * @see LegacyPasswordEncoder#matches(String, String)
	 */
	@Test
	public void matches_shouldMatchHashWithLegacyPrefix() {
		assertTrue(LegacyPasswordEncoder.matches(LegacyPasswordEncoder.LEGACY_HASH_PREFIX
		        + "4a1750c8607d0fa237de36c6305715c223415189", "test" + SALT));
	}

	/**
	 * @see LegacyPasswordEncoder#matches(String, String)
	 */
	@Test
	public void matches_shouldNotMatchSha512Hash() {
		String sha512Hash = "1d1436658853aceceadd72e92f1ae9089a0000fbb38cea519ce34eae9f28523930ecb212177dbd607d83dc275fde3e9ca648deb557d503ad0bcd01a955a394b2";
		assertFalse(LegacyPasswordEncoder.matches(sha512Hash, "test" + SALT));
	}
}
