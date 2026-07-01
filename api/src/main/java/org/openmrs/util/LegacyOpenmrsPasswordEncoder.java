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

import org.springframework.security.crypto.password.PasswordEncoder;

public class LegacyOpenmrsPasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence rawPassword) {
		String salt = Security.getRandomToken();
		String hash = Security.encodeString(rawPassword.toString() + salt);
		return hash + ":" + salt;
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		if (encodedPassword == null) {
			return false;
		}
		String encoded = encodedPassword;
		String[] parts = encoded.split(":");
		String hash = parts[0];
		String salt = parts.length > 1 ? parts[1] : "";
		return Security.hashMatches(hash, rawPassword.toString() + salt);
	}
}
