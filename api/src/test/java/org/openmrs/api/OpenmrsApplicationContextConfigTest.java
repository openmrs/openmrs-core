/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.junit.jupiter.api.Test;
import org.openmrs.util.Security;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenmrsApplicationContextConfigTest {

	@Test
	void passwordEncoder_shouldEncodeWithBcryptAndMatchSha512LegacyFormat() {
		PasswordEncoder passwordEncoder = new OpenmrsApplicationContextConfig().passwordEncoder();
		String rawPassword = "rawPasswordWithSalt";

		String bcryptEncoded = passwordEncoder.encode(rawPassword);
		assertTrue(bcryptEncoded.startsWith("{bcrypt}"));
		assertTrue(passwordEncoder.matches(rawPassword, bcryptEncoded));

		String sha512Legacy = "{sha512}" + Security.encodeString(rawPassword);
		assertTrue(passwordEncoder.matches(rawPassword, sha512Legacy));
	}
}
