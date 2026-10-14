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

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests {@link APIAuthenticationException}.
 */
@SuppressWarnings("deprecation")
public class APIAuthenticationExceptionTest {

	@Test
	public void shouldBeTheDenialSpringSecurityRecognizes() {
		APIAuthenticationException denial = new APIAuthenticationException("Privileges required: Get Users");

		assertInstanceOf(AuthorizationDeniedException.class, denial);
		assertInstanceOf(AccessDeniedException.class, denial);
		assertEquals("Privileges required: Get Users", denial.getMessage());
		assertFalse(denial.isGranted());
	}

	@Test
	public void shouldCarryTheDecisionThatDeniedAccess() {
		AuthorizationDecision decision = new AuthorizationDecision(false);

		APIAuthenticationException denial = new APIAuthenticationException("Privileges required: Get Users", decision);

		assertSame(decision, denial.getAuthorizationResult());
		assertEquals("Privileges required: Get Users", denial.getMessage());
	}

	@Test
	public void constructors_shouldKeepTheMessageAndCauseTheyHadBefore() {
		IllegalStateException cause = new IllegalStateException("underlying failure");

		APIAuthenticationException bare = new APIAuthenticationException();
		assertNull(bare.getMessage());
		assertNull(bare.getCause());

		APIAuthenticationException withMessageAndCause = new APIAuthenticationException("denied", cause);
		assertEquals("denied", withMessageAndCause.getMessage());
		assertSame(cause, withMessageAndCause.getCause());

		// as RuntimeException(Throwable) does
		APIAuthenticationException withCause = new APIAuthenticationException(cause);
		assertEquals(cause.toString(), withCause.getMessage());
		assertSame(cause, withCause.getCause());

		APIAuthenticationException withNullCause = new APIAuthenticationException((Throwable) null);
		assertNull(withNullCause.getMessage());
		assertNull(withNullCause.getCause());
	}
}
