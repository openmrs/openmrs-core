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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/**
 * Boots the real Spring context with {@code security.passwordEncoder=argon2} set, exactly the
 * configuration an administrator opts in to via openmrs-runtime.properties, and pins what that
 * property must do through the {@code openmrsPasswordEncoder} bean wired in
 * applicationContext-service.xml: newly written passwords are argon2-prefixed while previously
 * stored unprefixed legacy hashes keep authenticating.
 */
@TestPropertySource(properties = "security.passwordEncoder=argon2")
public class OpenmrsPasswordEncoderContextTest extends BaseContextSensitiveTest {
	
	@Test
	public void optingInToArgon2_shouldWriteArgon2PrefixedHashesWhileLegacyHashesStillAuthenticate() {
		PasswordEncoder encoder = applicationContext.getBean("openmrsPasswordEncoder", PasswordEncoder.class);
		
		String prefixed = encoder.encode("password");
		assertTrue(prefixed.startsWith("{argon2}$argon2id$"), "expected an argon2-prefixed PHC hash, got: " + prefixed);
		assertTrue(encoder.matches("password", prefixed));
		assertFalse(encoder.matches("wrongPassword", prefixed));
		assertFalse(encoder.upgradeEncoding(prefixed), "a freshly written hash must not be offered for re-encoding");
		
		String legacyHash = new LegacyOpenmrsPasswordEncoder().encode("password");
		assertFalse(legacyHash.startsWith("{"), "sanity: the generated legacy hash must be unprefixed");
		assertTrue(encoder.matches("password", legacyHash), "a legacy hash stored before the opt-in must still authenticate");
		assertFalse(encoder.matches("wrongPassword", legacyHash));
		assertTrue(encoder.upgradeEncoding(legacyHash), "a legacy hash must be flagged for re-encoding after the opt-in");
	}
}