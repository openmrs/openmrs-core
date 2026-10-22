/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationResult;

/**
 * Represents often fatal errors that occur within the API infrastructure involving a user's lack of
 * privileges. In certain presentation environments, this exception is caught and the user is
 * redirected to the login page where they can provide new or higher credentials.
 * <p>
 * As of 3.0.0 it is a Spring Security {@link AuthorizationDeniedException}, and no longer an
 * {@link APIException}. Core denies with it from the deprecated {@code @Authorized}, from a
 * {@code @PreAuthorize} or {@code @PostAuthorize} check that names the missing privilege or in
 * which {@code isAuthenticated()} denied, and from the explicit checks that threw it before 3.0.0,
 * so code written to catch it keeps recognizing those denials, and code catching
 * {@link org.springframework.security.access.AccessDeniedException} recognizes them as well.
 *
 * @deprecated as of 3.0.0, catch and throw Spring Security's
 *             {@link org.springframework.security.access.AccessDeniedException} instead, which
 *             every denial is, including one an expression makes without naming a privilege
 */
@Deprecated(since = "3.0.0")
public class APIAuthenticationException extends AuthorizationDeniedException {

	public static final long serialVersionUID = 12121213L;

	/**
	 * Default empty constructor. It is more common to use the
	 * {@link #APIAuthenticationException(String)} constructor to provide some context to the user as to
	 * where/why the authentication has failed
	 */
	public APIAuthenticationException() {
		super((String) null);
	}

	/**
	 * Common constructor taking in a message to give the user some context as to where/why the
	 * authentication failed.
	 *
	 * @param message String describing where/why the authentication failed
	 */
	public APIAuthenticationException(String message) {
		super(message);
	}

	/**
	 * Common constructor taking in a message to give the user some context as to where/why the
	 * authentication failed.
	 *
	 * @param message String describing where/why the authentication failed
	 * @param cause error further up the stream that caused this authentication failure
	 */
	public APIAuthenticationException(String message, Throwable cause) {
		// AuthorizationDeniedException takes no cause
		super(message);
		initCause(cause);
	}

	/**
	 * Constructor giving the user a further cause exception reason that caused this authentication
	 * failure
	 *
	 * @param cause error further up the stream that caused this authentication failure
	 */
	public APIAuthenticationException(Throwable cause) {
		// the message RuntimeException(Throwable) gave before 3.0.0
		super(cause == null ? null : cause.toString());
		initCause(cause);
	}

	/**
	 * Constructor for a denial that carries the authorization decision behind it, as Spring Security's
	 * method security returns one.
	 *
	 * @param message String describing where/why the authentication failed
	 * @param result the decision that denied access
	 * @since 3.0.0
	 */
	public APIAuthenticationException(String message, AuthorizationResult result) {
		super(message, result);
	}

}
