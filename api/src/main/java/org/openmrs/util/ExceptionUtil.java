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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openmrs.api.APIAuthenticationException;
import org.springframework.security.access.AccessDeniedException;

/**
 * Utility methods for dealing with exceptions
 *
 * @since 1.8.4
 */
public class ExceptionUtil {

	private ExceptionUtil() {
	}

	/**
	 * If any cause in the exception chain is an instance of causeType, then rethrow that exception
	 * <p>
	 * <strong>Should</strong> allow an intermediate exception to be rethrown
	 *
	 * @param thrown
	 * @param causeType must be a {@link RuntimeException} so that we can throw it
	 */
	public static void rethrowIfCause(Throwable thrown, Class<? extends RuntimeException> causeType) {
		int index = ExceptionUtils.indexOfType(thrown, causeType);
		if (index >= 0) {
			throw (RuntimeException) ExceptionUtils.getThrowables(thrown)[index];
		}
	}

	/**
	 * If any cause in the given exception chain is an authorization failure, rethrow that. Any Spring
	 * Security {@link AccessDeniedException} counts, and the deprecated
	 * {@link APIAuthenticationException} is one; where a chain holds more than one, an
	 * {@code APIAuthenticationException} is preferred. The name is kept for compatibility, though it
	 * now reads narrower than it behaves.
	 *
	 * @param thrown
	 */
	public static void rethrowAPIAuthenticationException(Throwable thrown) {
		rethrowIfCause(thrown, APIAuthenticationException.class);
		rethrowIfCause(thrown, AccessDeniedException.class);
	}

}
