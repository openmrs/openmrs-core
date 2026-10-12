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
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Proves the 4 methods converted from {@code @Authorized} to {@code @PreAuthorize} as a reference
 * pattern (see {@link ProviderService#getAllProviders()},
 * {@link ProviderService#getProvider(Integer)},
 * {@link ProviderService#retireProvider(Provider, String)}, {@link UserService#getUser(Integer)})
 * enforce the same privilege as before: allowed for the default (superuser) test context, denied
 * once logged out with an {@link AccessDeniedException} - the same exception {@code @Authorized}
 * itself denies with as of 3.0.0.
 */
public class PreAuthorizeConversionEquivalenceTest extends BaseContextSensitiveTest {

	@Test
	public void getAllProviders_shouldSucceedWhenAuthorized() {
		assertNotNull(Context.getProviderService().getAllProviders());
	}

	@Test
	public void getAllProviders_shouldThrowAccessDeniedExceptionWhenUnauthorized() {
		ProviderService providerService = Context.getProviderService();
		Context.getUserContext().logout();
		assertThrows(AccessDeniedException.class, providerService::getAllProviders);
	}

	@Test
	public void getProvider_shouldThrowAccessDeniedExceptionWhenUnauthorized() {
		ProviderService providerService = Context.getProviderService();
		Context.getUserContext().logout();
		assertThrows(AccessDeniedException.class, () -> providerService.getProvider(1));
	}

	@Test
	public void retireProvider_shouldThrowAccessDeniedExceptionWhenUnauthorized() {
		ProviderService providerService = Context.getProviderService();
		Provider provider = providerService.getProvider(1);
		Context.getUserContext().logout();
		assertThrows(AccessDeniedException.class, () -> providerService.retireProvider(provider, "test reason"));
	}

	@Test
	public void getUser_shouldThrowAccessDeniedExceptionWhenUnauthorized() {
		UserService userService = Context.getUserService();
		Context.getUserContext().logout();
		assertThrows(AccessDeniedException.class, () -> userService.getUser(1));
	}
}
