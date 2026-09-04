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

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

/**
 * A variation of Spring's <tt>DelegatingPasswordEncoder</tt> that falls back to the
 * supplied <tt>fallbackEncoder</tt> without using a prefix.
 * <p/>
 * Spring's <tt>DelegatingPasswordEncoder</tt> winds up prepending the "{<encoder>}"
 * to the password. However, we want to support existing passwords with no formatting,
 * so with this variation, our default fallback generates and validates passwords
 * without that prefix.
 */
public class OpenmrsDelegatingPasswordEncoder implements PasswordEncoder {

	private final PasswordEncoder defaultEncoder;

	private final String idForEncode;

	private final Map<String, PasswordEncoder> idToPasswordEncoder;

	private final PasswordEncoder fallbackEncoder;

	public OpenmrsDelegatingPasswordEncoder(String idForEncode, Map<String, PasswordEncoder> idToPasswordEncoder, PasswordEncoder fallbackEncoder) {
		if (idForEncode == null || idForEncode.isEmpty()) {
			this.defaultEncoder = fallbackEncoder;
		} else if (!idToPasswordEncoder.containsKey(idForEncode)) {
			throw new IllegalArgumentException("The encoder named '" + idForEncode + "' is not configured for this instance of OpenmrsDelegatingPasswordEncoder");
		} else {
			defaultEncoder = idToPasswordEncoder.get(idForEncode);
		}

		this.idForEncode = idForEncode;
		this.idToPasswordEncoder = idToPasswordEncoder;
		this.fallbackEncoder = fallbackEncoder;
	}

	@Override
	public String encode(CharSequence rawPassword) {
		if (idForEncode == null || idForEncode.isEmpty() || defaultEncoder instanceof LegacyOpenmrsPasswordEncoder) {
			return defaultEncoder.encode(rawPassword);
		}
		return "{" + idForEncode + "}" + defaultEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(CharSequence rawPassword, String prefixedPassword) {
		if (rawPassword == null && prefixedPassword == null) {
			return true;
		}
		String id = extractId(prefixedPassword);
		String encodedPassword = prefixedPassword;
		// if we have an id
		if (id != null && !id.isEmpty()) {
			encodedPassword = encodedPassword.substring(encodedPassword.indexOf("}") + 1);
		}
		PasswordEncoder encoder = idToPasswordEncoder.get(id);
		if (encoder == null) {
			// An unprefixed value is a legacy hash (SHA-1/SHA-512) that the encoder new
			// passwords are written with cannot parse. It must go to the fallback encoder;
			// routing it to defaultEncoder instead would reject every existing account the
			// moment a site opts in to an id-based encoder such as argon2.
			if (id == null) {
				return fallbackEncoder.matches(rawPassword, encodedPassword);
			}
			return defaultEncoder.matches(rawPassword, encodedPassword);
		}

		return encoder.matches(rawPassword, encodedPassword);
	}

	@Override
	public boolean upgradeEncoding(String prefixedPassword) {
		String id = extractId(prefixedPassword);
		// an unprefixed value is a legacy hash; it must be upgraded to the encoder
		// named by idForEncode so that it can be validated after opt-in
		if (id == null) {
			return idForEncode != null && !idForEncode.isEmpty();
		}
		String encodedPassword = prefixedPassword.substring(prefixedPassword.indexOf("}") + 1);
		PasswordEncoder encoder = idToPasswordEncoder.get(id);
		if (encoder == null) {
			// An unrecognized prefix should go to the fallback encoder (the same path that
			// matches() takes for unknown ids), not to the default encoder. This keeps the
			// delegation in sync between matches() and upgradeEncoding().
			return fallbackEncoder.upgradeEncoding(encodedPassword);
		}
		return encoder.upgradeEncoding(encodedPassword);
	}

	private String extractId(String prefixEncodedPassword) {
		if (prefixEncodedPassword == null) {
			return null;
		}
		int start = prefixEncodedPassword.indexOf("{");
		if (start != 0) {
			return null;
		}
		int end = prefixEncodedPassword.indexOf("}", start);
		if (end < 0) {
			return null;
		}
		return prefixEncodedPassword.substring(start + 1, end);
	}
}
