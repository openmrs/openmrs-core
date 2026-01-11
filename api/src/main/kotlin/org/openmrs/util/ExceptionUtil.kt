/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util

import org.apache.commons.lang3.exception.ExceptionUtils
import org.openmrs.api.APIAuthenticationException

/**
 * Utility methods for dealing with exceptions
 * @since 1.8.4
 */
object ExceptionUtil {
	
	/**
	 * If any cause in the exception chain is an instance of causeType, then rethrow that exception 
	 *
	 * @param thrown
	 * @param causeType must be a [RuntimeException] so that we can throw it
	 * <strong>Should</strong> allow an intermediate exception to be rethrown
	 */
	@JvmStatic
	fun rethrowIfCause(thrown: Throwable, causeType: Class<out RuntimeException>) {
		val index = ExceptionUtils.indexOfType(thrown, causeType)
		if (index >= 0) {
			throw ExceptionUtils.getThrowables(thrown)[index] as RuntimeException
		}
	}
	
	/**
	 * If any cause in the given exception chain is an APIAuthenticationException, rethrow that 
	 *
	 * @param thrown
	 */
	@JvmStatic
	fun rethrowAPIAuthenticationException(thrown: Throwable) {
		rethrowIfCause(thrown, APIAuthenticationException::class.java)
	}
}
