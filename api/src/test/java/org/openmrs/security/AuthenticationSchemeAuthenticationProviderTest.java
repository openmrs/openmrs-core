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

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.AuthenticationScheme;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.TestUsernameAuthenticationScheme;
import org.openmrs.api.context.TestUsernameCredentials;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Proves which {@link AuthenticationScheme} answers a request submitted to the
 * {@code authenticationManager}.
 * <p>
 * {@code UserContext(AuthenticationScheme)} is public API ({@code @since 2.3.0}) and before 3.0.0
 * its {@code authenticate(...)} invoked that instance directly, so the scheme a caller passed has
 * to keep deciding now that authentication is routed through Spring Security. Resolving
 * {@link Context#getAuthenticationScheme()} here regardless would ignore it quietly - the caller
 * would still authenticate, just against someone else's scheme.
 * <p>
 * Each case is told apart by the name the resulting {@code Authenticated} carries, which is set by
 * whichever scheme ran: {@code "test-scheme"} from {@link TestUsernameAuthenticationScheme}, and
 * {@link UsernamePasswordCredentials#SCHEME} from the configured default.
 */
public class AuthenticationSchemeAuthenticationProviderTest extends BaseContextSensitiveTest {

	private final AuthenticationSchemeAuthenticationProvider provider = new AuthenticationSchemeAuthenticationProvider();

	@Test
	public void authenticate_shouldUseTheSchemeCarriedOnTheToken() {
		// the configured scheme cannot authenticate TestUsernameCredentials at all - it rejects any
		// credentials that are not UsernamePasswordCredentials - so reaching "test-scheme" is only
		// possible if the scheme on the token is the one consulted
		CredentialsAuthenticationToken request = new CredentialsAuthenticationToken(new TestUsernameCredentials("admin"),
		        new TestUsernameAuthenticationScheme());

		Authentication result = provider.authenticate(request);

		assertEquals("test-scheme", authenticatedSchemeOf(result));
	}

	@Test
	public void authenticate_shouldFallBackToTheConfiguredSchemeWhenTheTokenCarriesNone() {
		// a module may well build the token itself, through the single-argument constructor
		CredentialsAuthenticationToken request = new CredentialsAuthenticationToken(
		        new UsernamePasswordCredentials("admin", "test"));

		Authentication result = provider.authenticate(request);

		assertEquals(UsernamePasswordCredentials.SCHEME, authenticatedSchemeOf(result));
	}

	private String authenticatedSchemeOf(Authentication result) {
		AuthenticatedResultToken token = assertInstanceOf(AuthenticatedResultToken.class, result);
		return token.getAuthenticatedResult().getAuthenticationScheme();
	}
}
