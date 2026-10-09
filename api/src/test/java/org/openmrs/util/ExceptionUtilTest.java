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

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.db.DAOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExceptionUtilTest {

	/**
	 * @see ExceptionUtil#rethrowAPIAuthenticationException(Throwable)
	 */
	@Test
	public void rethrowAPIAuthenticationException_shouldRethrowALegacyAuthorizationFailure() {
		APIAuthenticationException denial = new APIAuthenticationException("Privileges required: Get Users");
		Exception wrapped = new DAOException("Unable to deserialize object", denial);

		APIAuthenticationException rethrown = assertThrows(APIAuthenticationException.class,
		    () -> ExceptionUtil.rethrowAPIAuthenticationException(wrapped));

		assertSame(denial, rethrown);
	}

	/**
	 * A method converted from {@code @Authorized} to {@code @PreAuthorize} denies with Spring
	 * Security's exception instead, which shares no supertype with {@link APIAuthenticationException} -
	 * so a caller relying on this would otherwise bury the denial in its own exception (see
	 * {@code HibernateSerializedObjectDAO.convertSerializedObject}).
	 *
	 * @see ExceptionUtil#rethrowAPIAuthenticationException(Throwable)
	 */
	@Test
	public void rethrowAPIAuthenticationException_shouldRethrowASpringSecurityAuthorizationFailure() {
		AuthorizationDeniedException denial = new AuthorizationDeniedException("Privileges required: Get Users");
		Exception wrapped = new DAOException("Unable to deserialize object", denial);

		AccessDeniedException rethrown = assertThrows(AccessDeniedException.class,
		    () -> ExceptionUtil.rethrowAPIAuthenticationException(wrapped));

		assertSame(denial, rethrown);
	}

	/**
	 * @see ExceptionUtil#rethrowIfCause(Throwable,Class)
	 */
	@Test
	public void rethrowIfCause_shouldAllowAnIntermediateExceptionToBeRethrown() throws Exception {
		try {
			List<Class<? extends RuntimeException>> chain = Arrays.asList(NullPointerException.class,
			    IllegalArgumentException.class, IllegalStateException.class);
			throwExceptionChain(chain);

		} catch (Exception ex) {
			int numFound = 0;

			// Should be able to find the innermost NPE
			Exception innermost = null;
			try {
				ExceptionUtil.rethrowIfCause(ex, NullPointerException.class);
			} catch (Exception cause) {
				assertNull(cause.getCause());
				innermost = cause;
				++numFound;
			}

			// Should be able to find the middle IllegalArgumentException
			try {
				ExceptionUtil.rethrowIfCause(ex, IllegalArgumentException.class);
			} catch (Exception middle) {
				assertEquals(innermost, middle.getCause());
				++numFound;
			}

			// Should be able to find the outermost IllegalStateException
			try {
				ExceptionUtil.rethrowIfCause(ex, IllegalStateException.class);
			} catch (Exception outer) {
				assertEquals(ex, outer);
				++numFound;
			}

			assertEquals(3, numFound);
		}
	}

	/**
	 * Recursively builds up an exception chain with the requested exception classes in it.
	 *
	 * @param classesInChain the start of the list is the root cause, and the end of the list is the
	 *            outermost wrapped exception
	 */
	private void throwExceptionChain(List<Class<? extends RuntimeException>> classesInChain) throws Exception {
		if (classesInChain.size() > 1) {
			try {
				throwExceptionChain(classesInChain.subList(0, classesInChain.size() - 1));
			} catch (Exception ex) {
				Class<? extends Exception> outer = classesInChain.get(classesInChain.size() - 1);
				Constructor<? extends Exception> constructor = outer.getConstructor(Throwable.class);
				throw constructor.newInstance(ex);
			}
		} else {
			// length should be 1
			Class<? extends Exception> classToThrow = classesInChain.get(0);
			throw classToThrow.newInstance();
		}
	}
}
