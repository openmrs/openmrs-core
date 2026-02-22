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

/**
 * Utility class to sanitize untrusted data before logging, preventing log injection attacks
 * (CWE-117). Replaces carriage return (CR), line feed (LF), and tab characters with underscores so
 * that an attacker cannot forge log entries by injecting log entry separators.
 * 
 * @since 3.0.0
 * @see <a href="https://cwe.mitre.org/data/definitions/117.html">CWE-117</a>
 */
public final class LogSanitizer {
	
	private LogSanitizer() {
		// utility class; do not instantiate
	}
	
	/**
	 * Sanitizes the given string for safe inclusion in log output by replacing newline and tab
	 * characters with underscores.
	 * 
	 * @param value the untrusted string to sanitize; may be {@code null}
	 * @return the sanitized string, or {@code "null"} if the input was {@code null}
	 */
	public static String sanitize(String value) {
		if (value == null) {
			return "null";
		}
		return value.replaceAll("[\\r\\n\\t]", "_");
	}
	
	/**
	 * Sanitizes an arbitrary object's {@link Object#toString()} representation for safe inclusion
	 * in log output.
	 * 
	 * @param value the untrusted object to sanitize; may be {@code null}
	 * @return the sanitized string representation
	 */
	public static String sanitize(Object value) {
		if (value == null) {
			return "null";
		}
		return sanitize(value.toString());
	}
}
