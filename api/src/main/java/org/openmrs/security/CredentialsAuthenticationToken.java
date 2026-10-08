/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.security;

import java.util.Collections;

import org.openmrs.api.context.AuthenticationScheme;
import org.openmrs.api.context.Credentials;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Wraps an OpenMRS {@link Credentials} object so it can be submitted to Spring Security's
 * {@link org.springframework.security.authentication.AuthenticationManager}. This is only the
 * "unauthenticated" request token; {@link AuthenticationSchemeAuthenticationProvider} unwraps the
 * {@link Credentials}, delegates to the configured
 * {@link org.openmrs.api.context.AuthenticationScheme}, and returns an
 * {@link AuthenticatedResultToken} on success.
 *
 * @since 3.0.0
 */
// Sonar (java:S2160) wants equals() for the added fields. Not needed: this token is never compared,
// and the inherited equals() dispatches through getCredentials() below anyway.
@SuppressWarnings("java:S2160")
public class CredentialsAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;

	// never actually serialized (submitted to AuthenticationManager.authenticate(...) and immediately
	// unwrapped on the same thread, not stored in SecurityContextHolder), but
	// AbstractAuthenticationToken implements Serializable, so a non-serializable field here would
	// still be a latent NotSerializableException risk if that ever changed
	private final transient Credentials credentials;

	// likewise never serialized; carried so that the scheme a UserContext was constructed with, rather
	// than the globally configured one, decides how its own authenticate(...) call is answered
	private final transient AuthenticationScheme authenticationScheme;

	public CredentialsAuthenticationToken(Credentials credentials) {
		this(credentials, null);
	}

	/**
	 * @param credentials the credentials to authenticate
	 * @param authenticationScheme the scheme that must answer this request, or <code>null</code> to let
	 *            {@link AuthenticationSchemeAuthenticationProvider} fall back to the globally
	 *            configured one
	 * @since 3.0.0
	 */
	public CredentialsAuthenticationToken(Credentials credentials, AuthenticationScheme authenticationScheme) {
		super(Collections.emptyList());
		this.credentials = credentials;
		this.authenticationScheme = authenticationScheme;
		super.setAuthenticated(false);
	}

	/**
	 * @return the wrapped {@link Credentials}, typed - unlike {@link #getCredentials()}, which returns
	 *         the same value but as the untyped {@code Object}
	 *         {@link org.springframework.security.core.Authentication} itself requires
	 */
	@SuppressWarnings("java:S4144") // see the javadoc above for why this isn't a true duplicate of getCredentials()
	public Credentials getOpenmrsCredentials() {
		return credentials;
	}

	/**
	 * @return the {@link AuthenticationScheme} that must answer this request, or <code>null</code> if
	 *         the globally configured one applies
	 * @since 3.0.0
	 */
	public AuthenticationScheme getAuthenticationScheme() {
		return authenticationScheme;
	}

	@Override
	public Object getCredentials() {
		return credentials;
	}

	@Override
	public Object getPrincipal() {
		return credentials.getClientName();
	}
}
