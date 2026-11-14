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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.infinispan.spring.embedded.session.InfinispanEmbeddedSessionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.SessionPrincipal;
import org.openmrs.web.security.csrf.SessionBackedTokenHolder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic end-to-end reproduction of the distributed-session request cycle on two real
 * cluster nodes - no DB. It starts two {@code apiCacheManager}s from the clustered production
 * configuration ({@code infinispan-api.xml}), clustered within this JVM over
 * {@code jgroups-in-jvm.xml} (the same setup {@code GlobalPropertyCacheClusterTest} uses), wires
 * the real Spring Session {@link SessionRepositoryFilter} around an
 * {@link org.openmrs.web.security.OpenmrsSecurityContextFilter}-style late {@code getSession(true)}
 * write on node A, and reads the session back through a separate repository on node B. Because the
 * nodes are distinct cache managers, this exercises real cross-node replication - the marshalling
 * of the session delta through the {@code sessions} cache's java-serialized encoding and allow list
 * - rather than two readers of one local store.
 */
class DistributedSessionCookieReplicationTest {

	private static final String PRINCIPAL_ATTR = "__openmrs_session_principal";

	private static final String CLUSTER_NAME = "distributed-session-replication-test";

	private DefaultCacheManager nodeA;

	private DefaultCacheManager nodeB;

	@AfterEach
	void stopNodes() {
		for (DefaultCacheManager node : new DefaultCacheManager[] { nodeA, nodeB }) {
			if (node != null) {
				try {
					node.stop();
				} catch (RuntimeException e) {
					// keep freeing the rest
				}
			}
		}
		nodeA = null;
		nodeB = null;
	}

	/** The two-member-view latch {@code GlobalPropertyCacheClusterTest}'s helper uses. */
	@Listener
	private static final class TwoMemberLatch {

		private final CountDownLatch formed = new CountDownLatch(1);

		@ViewChanged
		public void viewChanged(ViewChangedEvent event) {
			if (event.getNewMembers().size() >= 2) {
				formed.countDown();
			}
		}
	}

	/** One clustered node: production {@code infinispan-api.xml} over the in-JVM loopback stack. */
	private DefaultCacheManager startNode(String nodeName) throws Exception {
		ConfigurationBuilderHolder holder = new ParserRegistry().parseFile("infinispan-api.xml");
		holder.getGlobalConfigurationBuilder().transport().clusterName(CLUSTER_NAME).nodeName(nodeName)
		        .transport(new JGroupsTransport(new org.jgroups.JChannel("org/openmrs/web/session/jgroups-in-jvm.xml")));
		return new DefaultCacheManager(holder, true);
	}

	private InfinispanEmbeddedSessionRepository newRepository(DefaultCacheManager node) {
		SpringEmbeddedCacheManager springManager = new SpringEmbeddedCacheManager(node);
		InfinispanEmbeddedSessionRepository repo = new InfinispanEmbeddedSessionRepository(
		        springManager.getCache("sessions"));
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
		// --- start both nodes and wait for the two-member view, so the writes below really replicate ---
		nodeA = startNode("nodeA");
		TwoMemberLatch twoMembers = new TwoMemberLatch();
		nodeA.addListener(twoMembers);
		nodeB = startNode("nodeB");
		assertTrue(twoMembers.formed.await(30, TimeUnit.SECONDS) || nodeA.getMembers().size() >= 2,
		    "the two in-JVM nodes must form one cluster; A=" + nodeA.getMembers() + " B=" + nodeB.getMembers());

		SessionPrincipal principal = new SessionPrincipal("user-uuid-xyz", Locale.ENGLISH, 3);

		// --- Node A: login request ---
		SessionRepositoryFilter<?> filterA = new SessionRepositoryFilter<>(newRepository(nodeA));
		MockHttpServletRequest reqA = new MockHttpServletRequest("GET", "/openmrs/ws/rest/v1/session");
		MockHttpServletResponse respA = new MockHttpServletResponse();

		filterA.doFilter(reqA, respA, openmrsStyleWrite(principal));

		// (1) a session cookie must be emitted so the client can carry the session
		Cookie sessionCookie = findSessionCookie(respA);
		assertNotNull(sessionCookie, "login must emit a session cookie; got Set-Cookie=" + respA.getHeader("Set-Cookie"));

		// --- Node B: a different node (separate repository over its own cluster member) ---
		SessionRepositoryFilter<?> filterB = new SessionRepositoryFilter<>(newRepository(nodeB));
		MockHttpServletRequest reqB = new MockHttpServletRequest("GET", "/openmrs/ws/rest/v1/session");
		reqB.setCookies(new Cookie(sessionCookie.getName(), sessionCookie.getValue()));
		MockHttpServletResponse respB = new MockHttpServletResponse();

		SessionPrincipal[] seenOnB = new SessionPrincipal[1];
		filterB.doFilter(reqB, respB, (rq, rs) -> {
			HttpSession session = ((HttpServletRequest) rq).getSession(false);
			if (session != null) {
				seenOnB[0] = (SessionPrincipal) session.getAttribute(PRINCIPAL_ATTR);
			}
		});

		// (2) node B, using only the cookie, must see the replicated session + principal
		assertNotNull(seenOnB[0], "the session/principal must be retrievable on another node via the cookie");
		assertEquals(principal, seenOnB[0], "the principal must survive replication to node B unchanged");
	}

	/**
	 * CSRFGuard's default token holder keeps tokens in a node-local map, so with the distributed
	 * session a form could be POSTed to a different node than the one that minted the token - and
	 * rejected. {@link SessionBackedTokenHolder} stores them as a session attribute instead, which
	 * rides the same replication this test proves for the principal: mint on node A, validate on node
	 * B.
	 */
	@Test
	void csrfTokensMintedOnOneNodeMustValidateOnAnother() throws Exception {
		nodeA = startNode("nodeA");
		TwoMemberLatch twoMembers = new TwoMemberLatch();
		nodeA.addListener(twoMembers);
		nodeB = startNode("nodeB");
		assertTrue(twoMembers.formed.await(30, TimeUnit.SECONDS) || nodeA.getMembers().size() >= 2,
		    "the two in-JVM nodes must form one cluster; A=" + nodeA.getMembers() + " B=" + nodeB.getMembers());

		SessionBackedTokenHolder holder = new SessionBackedTokenHolder();
		String pageUri = "/openmrs/login.htm";

		// --- node A: mint the master and the page token inside the filter chain's request context ---
		SessionRepositoryFilter<?> filterA = new SessionRepositoryFilter<>(newRepository(nodeA));
		MockHttpServletRequest reqA = new MockHttpServletRequest("GET", "/openmrs/login.htm");
		MockHttpServletResponse respA = new MockHttpServletResponse();
		String[] minted = new String[1];
		filterA.doFilter(reqA, respA, (rq, rs) -> {
			HttpServletRequest http = (HttpServletRequest) rq;
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(http));
			try {
				HttpSession session = http.getSession(true);
				String sessionKey = session.getId();
				holder.createMasterTokenIfAbsent(sessionKey, () -> "master-A");
				holder.setPageToken(sessionKey, pageUri, "page-token-A");
				minted[0] = holder.getPageToken(sessionKey, pageUri);
			} finally {
				RequestContextHolder.resetRequestAttributes();
			}
		});
		assertNotNull(minted[0], "node A must mint the page token into the session");

		// --- node B: the same cookie; the token map must have replicated with the session ---
		SessionRepositoryFilter<?> filterB = new SessionRepositoryFilter<>(newRepository(nodeB));
		MockHttpServletRequest reqB = new MockHttpServletRequest("GET", "/openmrs/login.htm");
		Cookie cookieFromA = findSessionCookie(respA);
		reqB.setCookies(new Cookie(cookieFromA.getName(), cookieFromA.getValue()));
		MockHttpServletResponse respB = new MockHttpServletResponse();
		String[] validatedOnB = new String[1];
		filterB.doFilter(reqB, respB, (rq, rs) -> {
			HttpServletRequest http = (HttpServletRequest) rq;
			RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(http));
			try {
				HttpSession session = http.getSession(false);
				if (session != null) {
					String sessionKey = session.getId();
					validatedOnB[0] = holder.createPageTokenIfAbsent(sessionKey, pageUri, () -> "must-not-be-minted-on-B");
				}
			} finally {
				RequestContextHolder.resetRequestAttributes();
			}
		});

		assertNotNull(validatedOnB[0], "node B must find the token minted on node A");
		assertEquals("page-token-A", validatedOnB[0], "the page token must survive replication unchanged");
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
