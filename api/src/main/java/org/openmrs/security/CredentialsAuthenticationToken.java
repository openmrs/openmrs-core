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
public class CredentialsAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;

	private final Credentials credentials;

	public CredentialsAuthenticationToken(Credentials credentials) {
		super(Collections.emptyList());
		this.credentials = credentials;
		super.setAuthenticated(false);
	}

	public Credentials getOpenmrsCredentials() {
		return credentials;
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
