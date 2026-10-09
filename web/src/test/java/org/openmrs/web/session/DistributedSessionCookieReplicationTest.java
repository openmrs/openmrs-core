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

import java.util.Locale;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.infinispan.spring.embedded.session.InfinispanEmbeddedSessionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.SessionPrincipal;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.session.web.http.SessionRepositoryFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Deterministic end-to-end reproduction of the distributed-session request cycle - no cluster, no
 * DB. It wires the real Spring Session {@link SessionRepositoryFilter} around an
 * {@link org.openmrs.web.filter.OpenmrsFilter}-style late {@code getSession(true)} write, over the
 * real Infinispan {@code sessions} cache, then reads the session back through a <em>second</em>
 * repository sharing the same store (node B). This pins down two things the live cluster could not
 * reliably show here: (1) that logging in emits a session cookie, and (2) that the session (and its
 * {@link SessionPrincipal}) is retrievable on another node via that cookie.
 */
class DistributedSessionCookieReplicationTest {

	private static final String PRINCIPAL_ATTR = "__openmrs_session_principal";

	private SpringEmbeddedCacheManager cacheManager;

	@BeforeEach
	void setUp() throws Exception {
		// The clustered production config needs a transport; the local config (same sessions cache
		// definition: java-serialized encoding + allow-list) is loadable single-node and is a faithful
		// stand-in for the store.
		cacheManager = new SpringEmbeddedCacheManager(new DefaultCacheManager("infinispan-api-local.xml"));
	}

	@AfterEach
	void tearDown() {
		if (cacheManager != null) {
			cacheManager.stop();
		}
	}

	private InfinispanEmbeddedSessionRepository newNode() {
		InfinispanEmbeddedSessionRepository repo = new InfinispanEmbeddedSessionRepository(
		        cacheManager.getCache("sessions"));
		repo.setApplicationEventPublisher(event -> {});
		return repo;
	}

	/** A filter that mimics OpenmrsFilter's distributed path: create+write the session in a finally. */
	private static FilterChain openmrsStyleWrite(SessionPrincipal principal) {
		return (rq, rs) -> {
			HttpServletRequest http = (HttpServletRequest) rq;
			try {
				// simulate the REST controller writing a (buffered) response body
				rs.getWriter().write("{\"authenticated\":true}");
			} finally {
				// OpenmrsFilter writes the principal after the chain, once auth state is known
				HttpSession session = http.getSession(true);
				session.setAttribute(PRINCIPAL_ATTR, principal);
			}
		};
	}

	@Test
	void loginEmitsSessionCookieAndSessionReplicatesToAnotherNode() throws Exception {
		SessionPrincipal principal = new SessionPrincipal("user-uuid-xyz", Locale.ENGLISH, 3);

		// --- Node A: login request ---
		SessionRepositoryFilter<?> nodeA = new SessionRepositoryFilter<>(newNode());
		MockHttpServletRequest reqA = new MockHttpServletRequest("GET", "/openmrs/ws/rest/v1/session");
		MockHttpServletResponse respA = new MockHttpServletResponse();

		nodeA.doFilter(reqA, respA, openmrsStyleWrite(principal));

		// (1) a session cookie must be emitted so the client can carry the session
		Cookie sessionCookie = findSessionCookie(respA);
		assertNotNull(sessionCookie, "login must emit a session cookie; got Set-Cookie=" + respA.getHeader("Set-Cookie"));

		// --- Node B: a different node (separate repository) sharing the same store ---
		SessionRepositoryFilter<?> nodeB = new SessionRepositoryFilter<>(newNode());
		MockHttpServletRequest reqB = new MockHttpServletRequest("GET", "/openmrs/ws/rest/v1/session");
		reqB.setCookies(new Cookie(sessionCookie.getName(), sessionCookie.getValue()));
		MockHttpServletResponse respB = new MockHttpServletResponse();

		SessionPrincipal[] seenOnB = new SessionPrincipal[1];
		nodeB.doFilter(reqB, respB, (rq, rs) -> {
			HttpSession session = ((HttpServletRequest) rq).getSession(false);
			if (session != null) {
				seenOnB[0] = (SessionPrincipal) session.getAttribute(PRINCIPAL_ATTR);
			}
		});

		// (2) node B, using only the cookie, must see the replicated session + principal
		assertNotNull(seenOnB[0], "the session/principal must be retrievable on another node via the cookie");
		assertEquals(principal, seenOnB[0], "the principal must survive replication to node B unchanged");
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
