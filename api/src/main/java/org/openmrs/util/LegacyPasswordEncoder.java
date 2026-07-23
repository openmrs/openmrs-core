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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.openmrs.api.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 ** Password encoder for OpenMRS hashes produced before the SHA-512 algorithm was adopted. This supports
*  the correct SHA-1 hex encoding and the historical buggy SHA-1 encoding that omitted leading zeros.
 */
public final class LegacyPasswordEncoder {
	
	static final String LEGACY_HASH_PREFIX = "{legacy}";
	
	private static final Logger log = LoggerFactory.getLogger(LegacyPasswordEncoder.class);
	
	private LegacyPasswordEncoder() {
	}
	
	/**
	 * @param storedHash hash from the users.password column
	 * @param passwordToHash cleartext password concatenated with the user's salt
	 * @return true if the password matches the legacy hashing algorithm
	 */
	public static boolean matches(String storedHash, String passwordToHash) {
		String hashForComparison = storedHash.startsWith(LEGACY_HASH_PREFIX) ? storedHash.substring(LEGACY_HASH_PREFIX
		        .length()) : storedHash;
		
		return hashForComparison.equals(encodeSha1(passwordToHash))
		        || hashForComparison.equals(encodeIncorrectSha1(passwordToHash));
	}
	
	private static String encodeSha1(String strToEncode) {
		return hexString(digest(strToEncode.getBytes(StandardCharsets.UTF_8), "SHA-1"));
	}
	
	private static String encodeIncorrectSha1(String strToEncode) {
		return incorrectHexString(digest(strToEncode.getBytes(StandardCharsets.UTF_8), "SHA-1"));
	}
	
	private static byte[] digest(byte[] input, String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm).digest(input);
		}
		catch (NoSuchAlgorithmException e) {
			log.error("Can't encode password because the given algorithm: {} was not found", algorithm, e);
			throw new APIException("system.cannot.find.encryption.algorithm", null, e);
		}
	}
	
	private static String hexString(byte[] block) {
		StringBuilder buf = new StringBuilder();
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		for (byte aBlock : block) {
			buf.append(hexChars[(aBlock & 0xf0) >> 4]);
			buf.append(hexChars[aBlock & 0x0f]);
		}
		return buf.toString();
	}
	
	private static String incorrectHexString(byte[] b) {
		if (b == null || b.length < 1) {
			return "";
		}
		StringBuilder s = new StringBuilder();
		for (byte aB : b) {
			s.append(Integer.toHexString(aB & 0xFF));
		}
		return s.toString();
	}
}
