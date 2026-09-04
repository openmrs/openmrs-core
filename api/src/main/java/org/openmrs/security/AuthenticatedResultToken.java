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
// Sonar (java:S2160) wants equals() overridden because this subclass adds a field beyond what
// AbstractAuthenticationToken's own equals() compares; not done because it's never warranted - this
// token is never placed in a Set/Map or otherwise compared (see the class javadoc: constructed and
// unwrapped within a single method call), and the inherited equals() already dispatches through the
// overridden getPrincipal() below, so it is not blind to this class's added state either
@SuppressWarnings("java:S2160")
public class AuthenticatedResultToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;

	// never actually serialized (see the class javadoc: this token is a transport between two methods
	// on the same thread, not stored in SecurityContextHolder), but AbstractAuthenticationToken
	// implements Serializable, so a non-serializable field here would still be a latent
	// NotSerializableException risk if that ever changed
	private final transient Authenticated authenticated;

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
