/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.SessionPrincipal;
import org.openmrs.web.filter.OpenmrsFilter.SessionWrite;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Exhaustively covers {@link OpenmrsFilter#decideSessionWrite} - the compare-and-save logic that
 * decides what the distributed-session filter persists at the end of a request. This is the
 * branchy, security-relevant part (login, logout, become-user, anonymous suppression), kept
 * side-effect-free so it can be tested without a servlet container or database.
 */
class OpenmrsFilterSessionWriteTest {

	private static SessionPrincipal auth(String uuid, Locale locale) {
		return new SessionPrincipal(uuid, locale, null);
	}

	private static SessionPrincipal anon(Locale locale) {
		return new SessionPrincipal((String) null, locale, null);
	}

	@Test
	void anonymousRequestWithNoPriorSession_writesNothing() {
		// key anti-DoS behaviour: an anonymous request must not spawn a replicated session here
		assertEquals(SessionWrite.NONE, OpenmrsFilter.decideSessionWrite(null, anon(Locale.ENGLISH)));
	}

	@Test
	void bothNull_writesNothing() {
		assertEquals(SessionWrite.NONE, OpenmrsFilter.decideSessionWrite(null, null));
	}

	@Test
	void loginFromNoSession_writes() {
		assertEquals(SessionWrite.WRITE, OpenmrsFilter.decideSessionWrite(null, auth("u1", Locale.ENGLISH)));
	}

	@Test
	void loginFromAnonymousSession_writes() {
		assertEquals(SessionWrite.WRITE, OpenmrsFilter.decideSessionWrite(anon(Locale.ENGLISH), auth("u1", Locale.ENGLISH)));
	}

	@Test
	void logout_invalidates() {
		assertEquals(SessionWrite.INVALIDATE,
		    OpenmrsFilter.decideSessionWrite(auth("u1", Locale.ENGLISH), anon(Locale.ENGLISH)));
	}

	@Test
	void logoutToNullContext_invalidates() {
		assertEquals(SessionWrite.INVALIDATE, OpenmrsFilter.decideSessionWrite(auth("u1", Locale.ENGLISH), null));
	}

	@Test
	void unchangedAuthenticated_writesNothing() {
		assertEquals(SessionWrite.NONE,
		    OpenmrsFilter.decideSessionWrite(auth("u1", Locale.ENGLISH), auth("u1", Locale.ENGLISH)));
	}

	@Test
	void localeChangeWhileAuthenticated_writes() {
		assertEquals(SessionWrite.WRITE,
		    OpenmrsFilter.decideSessionWrite(auth("u1", Locale.ENGLISH), auth("u1", Locale.FRENCH)));
	}

	@Test
	void becomeUser_writes() {
		assertEquals(SessionWrite.WRITE,
		    OpenmrsFilter.decideSessionWrite(auth("u1", Locale.ENGLISH), auth("u2", Locale.ENGLISH)));
	}

	@Test
	void changeToExistingAnonymousPrincipal_writes() {
		assertEquals(SessionWrite.WRITE, OpenmrsFilter.decideSessionWrite(anon(Locale.ENGLISH), anon(Locale.FRENCH)));
	}
}
