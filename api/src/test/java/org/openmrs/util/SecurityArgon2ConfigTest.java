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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

/**
 * Tests how {@link Security#createArgon2PasswordEncoder(String, String, String, String, String)}
 * builds the {@code argon2PasswordEncoder} bean from the {@code security.argon2.*} placeholder
 * values: the OWASP defaults are used when a value is missing or not a valid integer, and the
 * work factors a site configures are honoured.
 * <p>
 * Deliberately a plain unit test (no Spring context): the {@code argon2PasswordEncoder} bean is
 * wired through this factory method from the same placeholders, so this exercises the exact
 * parsing the Spring context relies on without booting a server. The work factors are read back
 * from the emitted PHC header.
 */
class SecurityArgon2ConfigTest {

	@Test
	void shouldUseOWASPDefaultsWhenNoValuesAreProvided() {
		Argon2PasswordEncoder encoder = Security.createArgon2PasswordEncoder(null, null, null, null, null);

		String phc = encoder.encode("test");
		assertTrue(phc.startsWith("$argon2id$"));
		assertTrue(phc.contains("m=19456,t=2,p=1"), "Expected OWASP default work factors in PHC, got: " + phc);
	}

	@Test
	void shouldUseTheGivenWorkFactors() {
		Argon2PasswordEncoder encoder = Security.createArgon2PasswordEncoder("16", "32", "2", "65536", "4");

		String phc = encoder.encode("test");
		assertTrue(phc.contains("m=65536,t=4,p=2"), "Expected configured work factors in PHC, got: " + phc);
		assertTrue(encoder.matches("test", phc), "Encoded value must authenticate its raw password");
	}

	@Test
	void shouldFallBackToDefaultsForNonNumericValues() {
		// The argon2PasswordEncoder bean is wired in applicationContext-service.xml from
		// ${security.argon2.*} placeholders via this factory method, so the raw (possibly
		// mistyped) value must be parsed here with the same fallback the resolver documents.
		// Regression test for the startup failure that an int-typed factory method produced:
		// Spring converted a mistyped value (e.g. security.argon2.memory=19456k) to int before
		// this method ran, bypassing the fallback and stopping the context from coming up.
		Argon2PasswordEncoder encoder = Security.createArgon2PasswordEncoder("salty", "long", "lots", "19456k", "many");

		String phc = encoder.encode("test");
		assertTrue(phc.startsWith("$argon2id$"));
		assertTrue(phc.contains("m=19456,t=2,p=1"), "Expected OWASP default work factors in PHC, got: " + phc);

	}
}
