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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.UserSessionListener.Event;
import org.openmrs.UserSessionListener.Status;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Covers the request-bound session-lifecycle behaviour of {@link DistributedSessionListener}:
 * session id rotation on login (fixation protection), invalidation on logout, and safe no-ops for
 * failed and non-web logins. The principal-establishment side (which needs a running
 * {@code Context}) is proven end-to-end in {@code OpenmrsFilterSpringSessionCookieTest}; here the
 * principal write is a best-effort no-op because no context is bound.
 */
class DistributedSessionListenerTest {

	private final DistributedSessionListener listener = new DistributedSessionListener();

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
		request.getSession().setAttribute("keep", "me");

		listener.loggedInOrOut(new User(), Event.LOGIN, Status.SUCCESS);

		assertNotEquals(before, request.getSession().getId(), "session id must change on login");
		assertEquals("me", request.getSession().getAttribute("keep"), "session attributes must be preserved");
	}

	@Test
	void doesNotRotateOnFailedLogin() {
		MockHttpServletRequest request = bindRequestWithSession();
		String before = request.getSession().getId();

		listener.loggedInOrOut(new User(), Event.LOGIN, Status.FAIL);

		assertEquals(before, request.getSession().getId(), "a failed login must not rotate the session");
	}

	@Test
	void invalidatesSessionOnLogout() {
		MockHttpServletRequest request = bindRequestWithSession();

		listener.loggedInOrOut(new User(), Event.LOGOUT, Status.SUCCESS);

		assertNull(request.getSession(false), "logout must invalidate the (replicated) session");
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
	}

	/**
	 * Logging out can race with an expiry on another node, which already invalidated the replicated
	 * session. The logout must still succeed quietly rather than fail the request.
	 */
	@Test
	void completesLogoutWhenTheSessionIsAlreadyInvalidated() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSession(new AlreadyInvalidatedSession());
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		assertDoesNotThrow(() -> listener.loggedInOrOut(new User(), Event.LOGOUT, Status.SUCCESS));
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
