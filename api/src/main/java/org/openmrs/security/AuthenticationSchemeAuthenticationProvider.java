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

import org.openmrs.api.context.Authenticated;
import org.openmrs.api.context.AuthenticationScheme;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Bridges Spring Security's
 * {@link org.springframework.security.authentication.AuthenticationManager} to OpenMRS's pluggable
 * {@link AuthenticationScheme} SPI, so a module that Spring-wires its own scheme bean - already the
 * supported override, see {@link Context#getAuthenticationScheme()} - is what Spring Security ends
 * up calling. There is one authentication path in the system; this provider resolves no scheme of
 * its own, it invokes the one it is given.
 * <p>
 * On failure the deliberately generic {@link ContextAuthenticationException} message is preserved,
 * so nothing leaks whether the username or the password was wrong.
 *
 * @since 3.0.0
 */
@Component
public class AuthenticationSchemeAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (!(authentication instanceof CredentialsAuthenticationToken)) {
			return null;
		}

		CredentialsAuthenticationToken request = (CredentialsAuthenticationToken) authentication;

		// the requesting UserContext's own scheme wins: UserContext(AuthenticationScheme) is public API
		// and pre-3.0.0 authenticate(...) invoked that instance, not the globally configured one
		AuthenticationScheme authenticationScheme = request.getAuthenticationScheme();
		if (authenticationScheme == null) {
			authenticationScheme = Context.getAuthenticationScheme();
		}

		Authenticated authenticated;
		try {
			authenticated = authenticationScheme.authenticate(request.getOpenmrsCredentials());
		} catch (ContextAuthenticationException e) {
			throw new BadCredentialsException(e.getMessage(), e);
		}

		return new AuthenticatedResultToken(authenticated);
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return CredentialsAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
