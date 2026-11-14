/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.security.csrf;

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.owasp.csrfguard.token.storage.Token;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifies that the holder keeps CSRFGuard's master and page tokens as a single serializable
 * session attribute (so they replicate with the distributed session), honours the upstream
 * {@code InMemoryTokenHolder} semantics it replaces, and degrades to no-ops when no request is
 * bound to the thread (e.g. the session-destroyed listener's remove).
 */
class SessionBackedTokenHolderTest {

	private static final String SESSION_KEY = "replicated-session-id";

	private final SessionBackedTokenHolder holder = new SessionBackedTokenHolder();

	private MockHttpServletRequest request;

	private MockHttpSession session;

	/** Supplies distinct values in sequence, so minting order is observable. */
	private static Supplier<String> sequencedValues(String prefix) {
		AtomicInteger counter = new AtomicInteger();
		return () -> prefix + counter.incrementAndGet();
	}

	@BeforeEach
	void bindRequest() {
		request = new MockHttpServletRequest();
		session = new MockHttpSession(null, SESSION_KEY);
		request.setSession(session);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
	}

	@AfterEach
	void resetRequestContext() {
		RequestContextHolder.resetRequestAttributes();
	}

	private MockHttpServletRequest bindSecondNodeView() {
		// what replication delivers: the same stored attribute value on a different session id is
		// NOT how it works - the attribute rides the session, so the "other node" view is the same
		// session id with the attribute already present (as Spring Session re-materializes it)
		MockHttpServletRequest otherNodeRequest = new MockHttpServletRequest();
		otherNodeRequest.setSession((MockHttpSession) request.getSession(false));
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(otherNodeRequest));
		return otherNodeRequest;
	}

	@Test
	void createMasterTokenIfAbsent_shouldMintOnceAndReuse() {
		String first = holder.createMasterTokenIfAbsent(SESSION_KEY, sequencedValues("m"));
		String second = holder.createMasterTokenIfAbsent(SESSION_KEY, sequencedValues("m"));

		assertEquals(first, second, "the master token must be minted once per session");
		assertNotNull(holder.getToken(SESSION_KEY), "a token entry must exist after minting");
	}

	@Test
	void createPageTokenIfAbsent_shouldMintMasterAndPageTokensOnFirstUse() {
		Supplier<String> values = sequencedValues("t");

		String pageToken = holder.createPageTokenIfAbsent(SESSION_KEY, "/openmrs/login.htm", values);

		// upstream semantics: the first supplier call produces the page token, the second the master
		assertEquals("t1", pageToken, "the page token must come from the first supplier call");
		assertEquals("t2", holder.getToken(SESSION_KEY).getMasterToken(), "the master token from the second call");
	}

	@Test
	void pageTokens_shouldRoundTripThroughTheSessionAttribute() {
		holder.createMasterTokenIfAbsent(SESSION_KEY, () -> "master");
		holder.setPageToken(SESSION_KEY, "/openmrs/login.htm", "page-1");

		assertEquals("page-1", holder.getPageToken(SESSION_KEY, "/openmrs/login.htm"));
		assertEquals(Map.of("/openmrs/login.htm", "page-1"), holder.getPageTokens(SESSION_KEY));
	}

	@Test
	void getPageToken_shouldReturnNullForUnknownUriOrSession() {
		assertNull(holder.getPageToken("no-such-session", "/openmrs/login.htm"));
		holder.createMasterTokenIfAbsent(SESSION_KEY, () -> "master");
		assertNull(holder.getPageToken(SESSION_KEY, "/openmrs/no-such-page.htm"));
	}

	@Test
	void getToken_shouldReturnNullWhenNoTokensWereMinted() {
		assertNull(holder.getToken(SESSION_KEY));
	}

	@Test
	void tokenView_shouldWriteThroughToTheSession() {
		holder.createMasterTokenIfAbsent(SESSION_KEY, () -> "master");
		Token token = holder.getToken(SESSION_KEY);

		token.setPageToken("/openmrs/locations.htm", "page-2");
		token.setMasterToken("rotated-master");

		assertEquals("page-2", holder.getPageToken(SESSION_KEY, "/openmrs/locations.htm"));
		assertEquals("rotated-master", holder.getToken(SESSION_KEY).getMasterToken());
	}

	@Test
	void rotateAllPageTokens_shouldRotateEveryPageTokenButLeaveTheMasterAlone() {
		holder.createMasterTokenIfAbsent(SESSION_KEY, () -> "master");
		holder.setPageToken(SESSION_KEY, "/openmrs/a.htm", "a-1");
		holder.setPageToken(SESSION_KEY, "/openmrs/b.htm", "b-1");

		AtomicInteger counter = new AtomicInteger();
		holder.rotateAllPageTokens(SESSION_KEY, () -> "rotated-" + counter.incrementAndGet());

		Token token = holder.getToken(SESSION_KEY);
		assertEquals("rotated-1", token.getPageToken("/openmrs/a.htm"));
		assertEquals("rotated-2", token.getPageToken("/openmrs/b.htm"));
		assertEquals("master", token.getMasterToken(), "rotation must not touch the master token");
	}

	@Test
	void regenerateUsedPageToken_shouldReplaceOnlyTheUsedToken() {
		holder.createMasterTokenIfAbsent(SESSION_KEY, () -> "master");
		holder.setPageToken(SESSION_KEY, "/openmrs/a.htm", "a-1");
		holder.setPageToken(SESSION_KEY, "/openmrs/b.htm", "b-1");

		holder.regenerateUsedPageToken(SESSION_KEY, "a-1", () -> "a-2");

		Token token = holder.getToken(SESSION_KEY);
		assertEquals("a-2", token.getPageToken("/openmrs/a.htm"), "the used token must be regenerated");
		assertEquals("b-1", token.getPageToken("/openmrs/b.htm"), "other tokens must be left alone");
	}

	@Test
	void getPageTokens_shouldReturnACopySoCallersCannotBypassTheSession() {
		holder.createMasterTokenIfAbsent(SESSION_KEY, () -> "master");
		holder.setPageToken(SESSION_KEY, "/openmrs/a.htm", "a-1");

		holder.getPageTokens(SESSION_KEY).put("/openmrs/a.htm", "tampered");

		assertEquals("a-1", holder.getPageToken(SESSION_KEY, "/openmrs/a.htm"),
		    "mutating the returned map must not touch session state");
	}

	@Test
	void tokens_shouldSurviveOnWhateverSessionCarriesTheAttribute() {
		// node A mints tokens into its session
		holder.createMasterTokenIfAbsent(SESSION_KEY, () -> "master-A");
		holder.setPageToken(SESSION_KEY, "/openmrs/login.htm", "page-A");

		// replication delivers the stored attribute onto the other node's session; a holder on that
		// node reads the same tokens from the session attribute - its own state is always empty
		SessionBackedTokenHolder holderOnNodeB = new SessionBackedTokenHolder();
		String master = holderOnNodeB.createMasterTokenIfAbsent(SESSION_KEY, () -> "should-not-be-minted");

		assertEquals("master-A", master, "the existing master token must be reused, not re-minted");
		assertEquals("page-A", holderOnNodeB.getPageToken(SESSION_KEY, "/openmrs/login.htm"),
		    "page tokens must travel with the session attribute");
	}

	@Test
	void remove_shouldClearTheTokensWhenTheSessionIsReachable() {
		holder.createMasterTokenIfAbsent(SESSION_KEY, () -> "master");

		holder.remove(SESSION_KEY);

		assertNull(holder.getToken(SESSION_KEY), "removal must clear the stored tokens");
		assertDoesNotThrow(() -> holder.remove(SESSION_KEY), "removal must be idempotent");
	}

	@Test
	void holder_shouldDegradeToNoOpsWhenNoRequestIsBound() {
		RequestContextHolder.resetRequestAttributes();

		assertDoesNotThrow(() -> holder.setMasterToken(SESSION_KEY, "master"));
		assertDoesNotThrow(() -> holder.remove(SESSION_KEY));
		assertNull(holder.getPageToken(SESSION_KEY, "/openmrs/login.htm"));
		assertNull(holder.getToken(SESSION_KEY));
	}

	/**
	 * legacyui's login calls {@code regenerateSession()}, which copies every session attribute to the
	 * freshly-identified session. Tokens minted in the pre-login session must not come along: on
	 * master, the {@code InMemoryTokenHolder} keyed them by session id, so whoever planted a pre-login
	 * session kept a valid token after the victim logged in. The session attribute must carry the id it
	 * was minted under and be rejected in any other session.
	 */
	@Test
	void tokenDoesNotSurviveSessionRegeneration() {
		String preLoginMaster = holder.createMasterTokenIfAbsent(SESSION_KEY, sequencedValues("pre-login-master"));
		assertNotNull(holder.createPageTokenIfAbsent(SESSION_KEY, "/openmrs/login.htm", sequencedValues("pre-login-page")));

		// regenerateSession(): copy every attribute to the freshly-identified session
		MockHttpSession regenerated = new MockHttpSession(null, "regenerated-session-id");
		Enumeration<String> attributeNames = session.getAttributeNames();
		while (attributeNames.hasMoreElements()) {
			String name = attributeNames.nextElement();
			regenerated.setAttribute(name, session.getAttribute(name));
		}
		MockHttpServletRequest regeneratedRequest = new MockHttpServletRequest();
		regeneratedRequest.setSession(regenerated);
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(regeneratedRequest));

		assertNull(holder.getToken("regenerated-session-id"),
		    "tokens copied over by a session regeneration must not be usable in the new session");
		assertNull(holder.getPageToken("regenerated-session-id", "/openmrs/login.htm"),
		    "a page token copied from the pre-login session must not validate after login");

		// the holder recovers by minting fresh tokens bound to the new session id
		String freshMaster = holder.createMasterTokenIfAbsent("regenerated-session-id",
		    sequencedValues("post-login-master"));
		assertNotNull(freshMaster);
		assertNotEquals(preLoginMaster, freshMaster, "the post-login session must get its own master token");
		assertNotNull(holder.createPageTokenIfAbsent("regenerated-session-id", "/openmrs/login.htm",
		    sequencedValues("post-login-page")));
		assertNotNull(holder.getPageToken("regenerated-session-id", "/openmrs/login.htm"));
	}
}
