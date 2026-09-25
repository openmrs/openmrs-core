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

import org.openmrs.api.context.Authenticated;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Wraps an OpenMRS {@link Authenticated} result. Returned by
 * {@link AuthenticationSchemeAuthenticationProvider} on successful authentication and unwrapped by
 * {@link org.openmrs.api.context.UserContext#authenticate(org.openmrs.api.context.Credentials)},
 * which is the only intended caller - this type is an internal transport between the two (public
 * only because those two classes live in different packages) and is never itself stored in
 * {@link org.springframework.security.core.context.SecurityContextHolder} (that role belongs to
 * {@link OpenmrsAuthenticationToken}).
 *
 * @since 3.0.0
 */
public class AuthenticatedResultToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;

	private final Authenticated authenticated;

	public AuthenticatedResultToken(Authenticated authenticated) {
		super(Collections.emptyList());
		this.authenticated = authenticated;
		super.setAuthenticated(true);
	}

	public Authenticated getAuthenticatedResult() {
		return authenticated;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return authenticated.getUser();
	}
}
