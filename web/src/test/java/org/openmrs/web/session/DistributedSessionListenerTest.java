/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.session;

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.UserSessionListener.Event;
import org.openmrs.UserSessionListener.Status;
import org.openmrs.api.cache.DistributedSessionPurger;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the request-bound session-lifecycle behaviour of {@link DistributedSessionListener}:
 * rotation of the pre-login session id on login (fixation protection), dropping the pre-login
 * session's stored entry through the purger, and safe no-ops for logout, failed and non-web logins.
 * The principal-establishment side (which needs a running {@code Context}) is proven end-to-end in
 * {@code OpenmrsSecurityContextFilterSpringSessionCookieTest}; here the principal write is a
 * best-effort no-op because no context is bound.
 */
class DistributedSessionListenerTest {

	private final List<String> purged = new ArrayList<>();

	private final DistributedSessionListener listener = new DistributedSessionListener(new DistributedSessionPurger(null) {

		@Override
		public void purge(String sessionId) {
			purged.add(sessionId);
		}
	});

	@AfterEach
	void clearRequestContext() {
		RequestContextHolder.resetRequestAttributes();
	}

	private MockHttpServletRequest bindRequestWithSession() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession(true); // establish a pre-login session
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		return request;
	}

	@Test
	void rotatesSessionIdOnSuccessfulLogin() {
		MockHttpServletRequest request = bindRequestWithSession();
		String before = request.getSession().getId();

		listener.loggedInOrOut(new User(), Event.LOGIN, Status.SUCCESS);

		assertNotEquals(before, request.getSession().getId(), "session id must change on login");
	}

	@Test
	void purgesThePreLoginSessionIdOnSuccessfulLogin() {
		MockHttpServletRequest request = bindRequestWithSession();
		String preLoginId = request.getSession().getId();

		listener.loggedInOrOut(new User(), Event.LOGIN, Status.SUCCESS);

		assertEquals(List.of(preLoginId), purged,
		    "the pre-login session's stored entry must be dropped so an attacker-chosen id stops resolving");
	}

	/**
	 * The authentication module's {@code AuthenticationSession} holds the session handle across
	 * {@code Context.authenticate()} and keeps using it afterwards, so the rotation must re-key the
	 * session without invalidating the handle (the module documents the same constraint in its own
	 * {@code regenerateHttpSession()}).
	 */
	@Test
	void loginKeepsTheSessionHandleUsableForCallersHoldingIt() {
		MockHttpServletRequest request = bindRequestWithSession();
		HttpSession handle = request.getSession(true);
		handle.setAttribute("module.state", "two-factor-pending");
		String preLoginId = handle.getId();

		listener.loggedInOrOut(new User(), Event.LOGIN, Status.SUCCESS);

		assertDoesNotThrow(() -> handle.setAttribute("module.state", "authenticated"),
		    "a handle held across login must stay usable (the authentication module depends on it)");
		assertEquals("authenticated", handle.getAttribute("module.state"),
		    "attributes must carry over the rotation without an invalidate/recreate cycle");
		assertNotEquals(preLoginId, handle.getId(), "the id must still rotate");
	}

	@Test
	void doesNotRotateOrPurgeOnFailedLogin() {
		MockHttpServletRequest request = bindRequestWithSession();
		String before = request.getSession().getId();

		listener.loggedInOrOut(new User(), Event.LOGIN, Status.FAIL);

		assertEquals(before, request.getSession().getId(), "a failed login must not rotate the session");
		assertTrue(purged.isEmpty(), "a failed login must not drop the session's stored entry");
	}

	/**
	 * Logging out can race with an expiry on another node, which already invalidated the replicated
	 * session. The listener does not invalidate on logout at all - the filter handles the
	 * end-of-request invalidate, and a caller (e.g. legacyui's LogoutServlet) may invalidate its own
	 * handle; either way this must stay quiet rather than fail the request.
	 */
	@Test
	void doesNothingOnLogout() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new AlreadyInvalidatedSession());
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		assertDoesNotThrow(() -> listener.loggedInOrOut(new User(), Event.LOGOUT, Status.SUCCESS));
		assertDoesNotThrow(() -> listener.loggedInOrOut(new User(), Event.LOGOUT, Status.FAIL));
		assertTrue(purged.isEmpty(), "logout must not purge anything from the listener");
	}

	/**
	 * A replicated session store can reject an id rotation. Login must still complete - refusing to
	 * authenticate over a session-fixation rotation would lock a user out of the system, whereas
	 * leaving the id alone is the lesser risk and is logged.
	 */
	@Test
	void completesLoginWhenSessionIdRotationIsRejected() {
		UnrotatableRequest request = new UnrotatableRequest();
		request.getSession(true);
		String before = request.getSession().getId();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		assertDoesNotThrow(() -> listener.loggedInOrOut(new User(), Event.LOGIN, Status.SUCCESS));
		assertEquals(before, request.getSession().getId(), "a rejected rotation must leave the id untouched");
		assertTrue(purged.isEmpty(), "a rejected rotation must not drop the session's stored entry");
	}

	@Test
	void doesNotThrowWhenNoSessionExistsAndNoContext() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		assertDoesNotThrow(() -> listener.loggedInOrOut(new User(), Event.LOGIN, Status.SUCCESS));
	}

	@Test
	void doesNothingForNonWebLogin() {
		// no request bound to the thread (e.g. a Daemon/background login)
		assertDoesNotThrow(() -> listener.loggedInOrOut(new User(), Event.LOGIN, Status.SUCCESS));
	}

	/** A request whose container cannot rotate the session id, e.g. because the store lost it. */
	private static class UnrotatableRequest extends MockHttpServletRequest {

		@Override
		public String changeSessionId() {
			throw new IllegalStateException("no session id available");
		}
	}

	/** A session that another node has already expired out of the replicated store. */
	private static class AlreadyInvalidatedSession extends MockHttpSession {

		@Override
		public void invalidate() {
			throw new IllegalStateException("session already invalidated");
		}
	}
}
