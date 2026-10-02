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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.infinispan.spring.embedded.session.InfinispanEmbeddedSessionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.UserSessionListener.Event;
import org.openmrs.UserSessionListener.Status;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.SessionPrincipal;
import org.openmrs.api.context.UserContext;
import org.openmrs.web.session.DistributedSessionListener;
import org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The most faithful non-cluster proof of the end-to-end distributed-session flow: the <em>real</em>
 * {@link OpenmrsFilter} distributed path and the real {@link DistributedSessionListener}, wrapped
 * by the real Spring Session {@link SessionRepositoryFilter} over the real Infinispan
 * {@code sessions} cache, with a real authenticated {@link Context}. It asserts that a login
 * request emits a session cookie - <em>even when the response is committed mid-request, as REST
 * controllers do</em> - and that a second node (separate repository, shared store) re-authenticates
 * the user via that cookie. This is what the live cluster login-across-nodes test would show, made
 * deterministic.
 */
public class OpenmrsFilterSpringSessionCookieTest extends BaseWebContextSensitiveTest {

	private static final String ACTIVE_USER_UUID = "c98a1558-e131-11de-babe-001e378eb67e"; // butch, active

	private final DistributedSessionListener sessionListener = new DistributedSessionListener();

	private SpringEmbeddedCacheManager cacheManager;

	private OpenmrsFilter openmrsFilter;

	@BeforeEach
	void enable() throws Exception {
		System.setProperty("session.distributed", "true");
		cacheManager = new SpringEmbeddedCacheManager(new DefaultCacheManager("infinispan-api-local.xml"));
		openmrsFilter = new OpenmrsFilter();
		openmrsFilter.init(new MockFilterConfig("OpenmrsFilter"));
	}

	@AfterEach
	void disable() {
		System.clearProperty("session.distributed");
		if (cacheManager != null) {
			cacheManager.stop();
		}
	}

	private SessionRepositoryFilter<?> newNode() {
		InfinispanEmbeddedSessionRepository repo = new InfinispanEmbeddedSessionRepository(
		        cacheManager.getCache("sessions"));
		repo.setApplicationEventPublisher(event -> {});
		return new SessionRepositoryFilter<>(repo);
	}

	/**
	 * A chain that mimics a real REST login: authenticate during the request (which fires the login
	 * listener that establishes the session), then write and <b>commit</b> the response like a
	 * controller does.
	 */
	private FilterChain loginChain(boolean commitResponse) {
		return (rq, rs) -> {
			HttpServletRequest http = (HttpServletRequest) rq;
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(http));
			try {
				Context.setUserContext(UserContext.fromSessionPrincipal(Context.getAuthenticationScheme(),
				    new SessionPrincipal(ACTIVE_USER_UUID, Locale.ENGLISH, null)));
				// this is what Context.authenticate() does via notifyUserSessionListener
				sessionListener.loggedInOrOut(Context.getAuthenticatedUser(), Event.LOGIN, Status.SUCCESS);
				rs.getWriter().write("{\"authenticated\":true}");
				if (commitResponse) {
					rs.flushBuffer();
				}
			} finally {
				RequestContextHolder.resetRequestAttributes();
			}
		};
	}

	private Cookie login(boolean commitResponse) throws Exception {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/openmrs/ws/rest/v1/session");
		MockHttpServletResponse resp = new MockHttpServletResponse();
		UserContext saved = Context.getUserContext();
		try {
			newNode().doFilter(req, resp, (rq, rs) -> openmrsFilter.doFilter(rq, rs, loginChain(commitResponse)));
		} finally {
			Context.setUserContext(saved);
		}
		Cookie cookie = findSessionCookie(resp);
		assertNotNull(cookie, "login must emit a session cookie (commitResponse=" + commitResponse + "); Set-Cookie="
		        + resp.getHeader("Set-Cookie"));
		return cookie;
	}

	@Test
	void loginEmitsCookieAndSessionAuthenticatesOnAnotherNode() throws Exception {
		Cookie cookie = login(false);

		String userOnB = authenticatedUserOnNodeB(cookie);
		assertEquals(ACTIVE_USER_UUID, userOnB, "the same user must be authenticated on node B via the cookie");
	}

	@Test
	void loginEmitsCookieEvenWhenResponseIsCommittedDuringTheRequest() throws Exception {
		// The regression that live testing surfaced: a REST controller commits the response before the
		// filter chain unwinds, so a session created only in OpenmrsFilter's post-chain step could never
		// send its Set-Cookie. Establishing it at the login event (mid-request) fixes this.
		Cookie cookie = login(true);

		String userOnB = authenticatedUserOnNodeB(cookie);
		assertEquals(ACTIVE_USER_UUID, userOnB, "the session must still cross to node B after a committed login");
	}

	private String authenticatedUserOnNodeB(Cookie cookie) throws Exception {
		MockHttpServletRequest reqB = new MockHttpServletRequest("GET", "/openmrs/ws/rest/v1/session");
		reqB.setCookies(new Cookie(cookie.getName(), cookie.getValue()));
		MockHttpServletResponse respB = new MockHttpServletResponse();

		String[] userOnB = new String[1];
		FilterChain observe = (rq,
		        rs) -> userOnB[0] = Context.getAuthenticatedUser() == null ? null : Context.getAuthenticatedUser().getUuid();

		UserContext saved = Context.getUserContext();
		try {
			newNode().doFilter(reqB, respB, (rq, rs) -> openmrsFilter.doFilter(rq, rs, observe));
		} finally {
			Context.setUserContext(saved);
		}
		assertNotNull(userOnB[0], "node B must re-establish the authenticated user from the replicated session");
		return userOnB[0];
	}

	private static Cookie findSessionCookie(MockHttpServletResponse response) {
		for (Cookie c : response.getCookies()) {
			if (c.getValue() != null && !c.getValue().isEmpty() && c.getMaxAge() != 0) {
				return c;
			}
		}
		return null;
	}
}
