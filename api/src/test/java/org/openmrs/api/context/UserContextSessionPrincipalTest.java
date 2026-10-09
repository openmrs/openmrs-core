/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Proves the second half of distributed sessions: that a {@link SessionPrincipal} correctly
 * rebuilds an equivalent {@link UserContext} on another node, and that the security-critical edge
 * cases behave (retired/missing user, anonymous, and request-scoped proxy privileges never
 * travelling with the session). Uses the standard test dataset, which ships an active user (butch,
 * 502) and a retired one (bruno, 501).
 */
public class UserContextSessionPrincipalTest extends BaseContextSensitiveTest {

	private static final String ACTIVE_USER_UUID = "c98a1558-e131-11de-babe-001e378eb67e"; // butch, retired=false

	private static final String RETIRED_USER_UUID = "c1d8f5c2-e131-11de-babe-001e378eb67e"; // bruno, retired=true

	private AuthenticationScheme scheme() {
		return Context.getAuthenticationScheme();
	}

	@Test
	void toSessionPrincipal_capturesTheAuthenticatedUserUuid() {
		// BaseContextSensitiveTest authenticates a user for us
		User current = Context.getAuthenticatedUser();
		assertNotNull(current, "precondition: a user is authenticated");

		SessionPrincipal principal = Context.getUserContext().toSessionPrincipal();

		assertEquals(current.getUuid(), principal.getUserUuid());
		assertTrue(principal.isAuthenticated());
	}

	@Test
	void fromSessionPrincipal_rebuildsAuthenticatedUserWithLocaleAndLocation() {
		SessionPrincipal principal = new SessionPrincipal(ACTIVE_USER_UUID, Locale.FRENCH, 1);

		UserContext rebuilt = UserContext.fromSessionPrincipal(scheme(), principal);

		assertTrue(rebuilt.isAuthenticated(), "an active user must be re-established");
		assertEquals(ACTIVE_USER_UUID, rebuilt.getAuthenticatedUser().getUuid());
		assertEquals(Locale.FRENCH, rebuilt.getLocale());
		assertEquals(Integer.valueOf(1), rebuilt.getLocationId());
	}

	@Test
	void fromSessionPrincipal_leavesContextAnonymousWhenUserIsRetired() {
		// stale-identity guard: a retired (disabled) account must not keep acting from a live session
		SessionPrincipal principal = new SessionPrincipal(RETIRED_USER_UUID, Locale.ENGLISH, null);

		UserContext rebuilt = UserContext.fromSessionPrincipal(scheme(), principal);

		assertFalse(rebuilt.isAuthenticated(), "a retired user must not be re-established");
		assertNull(rebuilt.getAuthenticatedUser());
	}

	@Test
	void fromSessionPrincipal_leavesContextAnonymousWhenUserNoLongerExists() {
		SessionPrincipal principal = new SessionPrincipal("00000000-0000-0000-0000-000000000000", Locale.ENGLISH, null);

		UserContext rebuilt = UserContext.fromSessionPrincipal(scheme(), principal);

		assertFalse(rebuilt.isAuthenticated(), "a missing user must not be re-established");
		assertNull(rebuilt.getAuthenticatedUser());
	}

	@Test
	void fromSessionPrincipal_withAnonymousPrincipalYieldsAnonymousContext() {
		SessionPrincipal anonymous = new SessionPrincipal((String) null, Locale.ENGLISH, 2);

		UserContext rebuilt = UserContext.fromSessionPrincipal(scheme(), anonymous);

		assertFalse(rebuilt.isAuthenticated());
		assertNull(rebuilt.getAuthenticatedUser());
		assertEquals(Locale.ENGLISH, rebuilt.getLocale(), "non-identity fields still rebuild");
		assertEquals(Integer.valueOf(2), rebuilt.getLocationId());
	}

	@Test
	void fromSessionPrincipal_withNullPrincipalYieldsEmptyContext() {
		UserContext rebuilt = UserContext.fromSessionPrincipal(scheme(), null);

		assertFalse(rebuilt.isAuthenticated());
		assertNull(rebuilt.getAuthenticatedUser());
	}

	/**
	 * The re-fetch bypasses service authorization, but it can still fail (an unreachable database, a
	 * DAO that rejects the lookup). The failure must propagate rather than silently degrade to an
	 * anonymous context: the servlet filter treats a thrown rebuild as "unknown, leave the session
	 * alone", whereas a silent anonymous context would be read as a logout and invalidate the
	 * replicated session, logging the user out.
	 */
	@Test
	void fromSessionPrincipal_propagatesWhenTheUserLookupFails() {
		ContextDAO realDao = Context.getContextDAO();
		ContextDAO failingDao = mock(ContextDAO.class);
		when(failingDao.getUserByUuid(anyString())).thenThrow(new ContextAuthenticationException("lookup failed"));
		Context.setDAO(failingDao);
		try {
			SessionPrincipal principal = new SessionPrincipal(ACTIVE_USER_UUID, Locale.ENGLISH, 5);

			assertThrows(ContextAuthenticationException.class, () -> UserContext.fromSessionPrincipal(scheme(), principal),
			    "a failed lookup must propagate, not degrade to anonymous");
		} finally {
			Context.setDAO(realDao);
		}
	}

	/**
	 * A transient runtime failure (e.g. a database/Hibernate error) is likewise propagated, so the
	 * filter can keep the session (and the user's login) instead of mistaking it for a logout.
	 */
	@Test
	void fromSessionPrincipal_propagatesWhenTheLookupThrowsARuntimeError() {
		ContextDAO realDao = Context.getContextDAO();
		ContextDAO failingDao = mock(ContextDAO.class);
		when(failingDao.getUserByUuid(anyString())).thenThrow(new IllegalStateException("database unavailable"));
		Context.setDAO(failingDao);
		try {
			SessionPrincipal principal = new SessionPrincipal(ACTIVE_USER_UUID, Locale.ENGLISH, 5);

			assertThrows(IllegalStateException.class, () -> UserContext.fromSessionPrincipal(scheme(), principal),
			    "a runtime lookup error must propagate, not degrade to anonymous");
		} finally {
			Context.setDAO(realDao);
		}
	}

	@Test
	void roundTrip_preservesIdentityButNeverCarriesProxyPrivileges() {
		UserContext original = UserContext.fromSessionPrincipal(scheme(),
		    new SessionPrincipal(ACTIVE_USER_UUID, Locale.ENGLISH, null));

		SessionPrincipal before = original.toSessionPrincipal();
		// a request-scoped privilege escalation must NOT leak into the session snapshot
		original.addProxyPrivilege("Add Allergies");
		SessionPrincipal after = original.toSessionPrincipal();

		assertEquals(before, after, "proxy privileges must not change the marshalled principal");

		UserContext rebuilt = UserContext.fromSessionPrincipal(scheme(), after);
		assertEquals(ACTIVE_USER_UUID, rebuilt.getAuthenticatedUser().getUuid(), "identity survives the round trip");
	}
}
