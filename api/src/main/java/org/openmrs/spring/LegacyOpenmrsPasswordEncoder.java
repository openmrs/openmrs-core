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

import org.openmrs.util.Security;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Wraps OpenMRS's historical password hashing behind Spring Security's {@link PasswordEncoder}:
 * SHA-512 over {@code password + salt}, with the older SHA-1 variants still accepted so existing
 * rows keep authenticating.
 *
 * @since 2.8.10
 */
public class LegacyOpenmrsPasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence rawPassword) {
		return Security.encodeString(rawPassword.toString());
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		if (encodedPassword == null || rawPassword == null) {
			return false;
		}
		
		return Security.hashMatches(encodedPassword, rawPassword.toString());
	}
}
