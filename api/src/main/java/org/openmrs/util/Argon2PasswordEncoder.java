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

import com.password4j.Password;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Argon2 PasswordEncoder that verifies passwords against Argon2 hashes using Password4j.
 * This encoder handles the Argon2i, Argon2d, and Argon2id variants.
 *
 * @since 2.8.8
 */
public class Argon2PasswordEncoder {

	private static final Logger log = LoggerFactory.getLogger(Argon2PasswordEncoder.class);

	/**
	 * Verify a password against a stored Argon2 hash.
	 *
	 * @param hashedPassword the stored Argon2 hash in standard encoded format
	 * @param password the password to verify
	 * @return true if the password matches the hash, false otherwise
	 */
	public boolean verify(String hashedPassword, String password) {
		try {
			return Password.check(password, hashedPassword).withArgon2();
		}
		catch (Exception e) {
			log.error("Failed to verify Argon2 password hash", e);
			return false;
		}
	}
}
