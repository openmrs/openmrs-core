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

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.SessionPrincipal;
import org.openmrs.api.context.UserContext;
import org.openmrs.web.WebConstants;
import org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Actually executes {@link OpenmrsFilter}'s distributed-session code path (not just the pure
 * decision) against a real {@link Context} and mock servlet objects: rebuilding the
 * {@link UserContext} from a session principal, writing a principal on login, invalidating on
 * logout, and not creating a session for anonymous requests. The login/logout inside the request
 * are simulated by mutating the thread's {@code UserContext} the way a REST/legacy login handler
 * would.
 */
public class OpenmrsFilterDistributedSessionTest extends BaseWebContextSensitiveTest {

	private static final String ACTIVE_USER_UUID = "c98a1558-e131-11de-babe-001e378eb67e"; // butch, retired=false

	private final OpenmrsFilter filter = new OpenmrsFilter();

	@BeforeEach
	void enableDistributedSessions() throws Exception {
		System.setProperty("session.distributed", "true");
		filter.init(new MockFilterConfig("OpenmrsFilter"));
	}

	@AfterEach
	void disableDistributedSessions() {
		System.clearProperty("session.distributed");
	}

	/**
	 * Runs the filter while preserving the test's own thread-bound UserContext (the filter sets and
	 * clears its own during the request).
	 */
	private void runFilter(MockHttpServletRequest request, MockHttpServletResponse response, FilterChain chain)
	        throws Exception {
		UserContext saved = Context.getUserContext();
		try {
			filter.doFilter(request, response, chain);
		} finally {
			Context.setUserContext(saved);
		}
	}

	private static SessionPrincipal principalFor(String uuid) {
		return new SessionPrincipal(uuid, Locale.ENGLISH, null);
	}

	@Test
	void rebuildsUserContextFromSessionPrincipal() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession(true).setAttribute(WebConstants.OPENMRS_SESSION_PRINCIPAL_HTTPSESSION_ATTR,
		    principalFor(ACTIVE_USER_UUID));
		MockHttpServletResponse response = new MockHttpServletResponse();

		String[] seenDuringRequest = new String[1];
		FilterChain chain = (rq, rs) -> {
			var user = Context.getUserContext().getAuthenticatedUser();
			seenDuringRequest[0] = user == null ? null : user.getUuid();
		};

		runFilter(request, response, chain);

		assertEquals(ACTIVE_USER_UUID, seenDuringRequest[0],
		    "filter must rebuild the authenticated user from the session principal for the request thread");
	}

	@Test
	void refreshesUsernameAttributeWhenThePrincipalIsUnchanged() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		HttpSession httpSession = request.getSession(true);
		httpSession.setAttribute(WebConstants.OPENMRS_SESSION_PRINCIPAL_HTTPSESSION_ATTR, principalFor(ACTIVE_USER_UUID));
		httpSession.setAttribute("username", "stale-name"); // as if the account was renamed mid-session
		MockHttpServletResponse response = new MockHttpServletResponse();

		String[] currentUsername = new String[1];
		FilterChain chain = (rq, rs) -> currentUsername[0] = Context.getUserContext().getAuthenticatedUser().getUsername();

		runFilter(request, response, chain);

		HttpSession session = request.getSession(false);
		assertNotNull(session);
		assertEquals(currentUsername[0], session.getAttribute("username"),
		    "the username attribute must refresh to the current user even when the principal is unchanged");
	}

	@Test
	void toleratesAnIncompatibleStoredPrincipal() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		// a wrong-typed value under the principal key, e.g. a stale/incompatible class after an upgrade
		request.getSession(true).setAttribute(WebConstants.OPENMRS_SESSION_PRINCIPAL_HTTPSESSION_ATTR, "not-a-principal");
		MockHttpServletResponse response = new MockHttpServletResponse();

		boolean[] authenticatedDuringRequest = { true };
		FilterChain chain = (rq,
		        rs) -> authenticatedDuringRequest[0] = Context.getUserContext().getAuthenticatedUser() != null;

		runFilter(request, response, chain); // must not throw ClassCastException

		assertFalse(authenticatedDuringRequest[0],
		    "an incompatible stored principal must degrade to an anonymous context, not throw");
	}

	@Test
	void writesPrincipalToSessionOnLogin() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(); // no session yet
		MockHttpServletResponse response = new MockHttpServletResponse();

		// simulate a login during the request: authenticate + fire the login listener (as
		// Context.authenticate does), which establishes the session with the principal
		FilterChain chain = (rq, rs) -> {
			org.springframework.web.context.request.RequestContextHolder
			        .setRequestAttributes(new org.springframework.web.context.request.ServletRequestAttributes(
			                (jakarta.servlet.http.HttpServletRequest) rq));
			try {
				Context.setUserContext(
				    UserContext.fromSessionPrincipal(Context.getAuthenticationScheme(), principalFor(ACTIVE_USER_UUID)));
				new org.openmrs.web.session.DistributedSessionListener().loggedInOrOut(Context.getAuthenticatedUser(),
				    org.openmrs.UserSessionListener.Event.LOGIN, org.openmrs.UserSessionListener.Status.SUCCESS);
			} finally {
				org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
			}
		};

		runFilter(request, response, chain);

		HttpSession session = request.getSession(false);
		assertNotNull(session, "an authenticated request must create a session to persist the principal");
		SessionPrincipal stored = (SessionPrincipal) session
		        .getAttribute(WebConstants.OPENMRS_SESSION_PRINCIPAL_HTTPSESSION_ATTR);
		assertNotNull(stored, "the principal must be written to the session");
		assertEquals(ACTIVE_USER_UUID, stored.getUserUuid());
	}

	@Test
	void invalidatesSessionOnLogout() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.getSession(true).setAttribute(WebConstants.OPENMRS_SESSION_PRINCIPAL_HTTPSESSION_ATTR,
		    principalFor(ACTIVE_USER_UUID));
		MockHttpServletResponse response = new MockHttpServletResponse();

		// simulate a logout during the request
		FilterChain chain = (rq, rs) -> Context.getUserContext().logout();

		runFilter(request, response, chain);

		assertNull(request.getSession(false), "logout must invalidate the replicated session");
	}

	@Test
	void anonymousRequestCreatesNoSession() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(); // no session
		MockHttpServletResponse response = new MockHttpServletResponse();

		FilterChain chain = (rq, rs) -> {
			// anonymous request: nothing authenticates
		};

		runFilter(request, response, chain);

		assertNull(request.getSession(false), "an anonymous request must not create a replicated session");
	}
}
