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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpSession;

import org.owasp.csrfguard.token.storage.Token;
import org.owasp.csrfguard.token.storage.TokenHolder;
import org.owasp.csrfguard.token.storage.impl.PageTokenValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * A CSRFGuard {@link TokenHolder} that stores the master and page tokens as an HTTP session
 * attribute, so they replicate with the distributed session. The default
 * {@code InMemoryTokenHolder} keeps them in a node-local map, which would reject a form posted to a
 * different node than the one that minted the token.
 * <p>
 * CSRFGuard reaches this holder with the logical session key only, and every call happens inside
 * the request that owns that session, so the session is reached through
 * {@link RequestContextHolder}. The one call outside a request - the session-destroyed listener's
 * {@link #remove(String)} - is a no-op: the tokens already died with the session.
 * <p>
 * Every operation runs under a striped lock keyed by the session key, so overlapping requests
 * cannot race the read-modify-write cycles; across nodes, minting remains last-write-wins
 * (recoverable by reloading the page).
 *
 * @since 3.0.0
 */
public class SessionBackedTokenHolder implements TokenHolder {

	private static final Logger log = LoggerFactory.getLogger(SessionBackedTokenHolder.class);

	/**
	 * Session attribute holding {@link StoredTokens}. Strings and a {@code LinkedHashMap} only, so the
	 * map passes the sessions cache serialization allow list and replicates unchanged.
	 */
	private static final String TOKENS_SESSION_ATTRIBUTE = SessionBackedTokenHolder.class.getName() + ".TOKENS";

	/** Striped locks: a fixed-size lock table; unrelated sessions only contend on a hash collision. */
	private static final int LOCK_STRIPES = 64;

	private final Object[] sessionLocks = new Object[LOCK_STRIPES];

	{
		for (int i = 0; i < LOCK_STRIPES; i++) {
			sessionLocks[i] = new Object();
		}
	}

	private Object lockFor(String sessionKey) {
		return sessionLocks[(sessionKey.hashCode() & 0x7fffffff) % LOCK_STRIPES];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMasterToken(String sessionKey, String value) {
		synchronized (lockFor(sessionKey)) {
			HttpSession session = currentSession(sessionKey);
			if (session == null) {
				log.debug("No request-bound session for key {}; not setting the CSRFGuard master token", sessionKey);
				return;
			}
			StoredTokens tokens = readTokensOrNew(session);
			tokens.setMasterToken(value);
			writeTokens(session, tokens);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createMasterTokenIfAbsent(String sessionKey, Supplier<String> valueSupplier) {
		synchronized (lockFor(sessionKey)) {
			HttpSession session = currentSession(sessionKey);
			if (session == null) {
				log.debug("No request-bound session for key {}; not creating the CSRFGuard master token", sessionKey);
				return null;
			}
			StoredTokens tokens = readTokensOrNew(session);
			if (tokens.getMasterToken() == null) {
				tokens.setMasterToken(valueSupplier.get());
				writeTokens(session, tokens);
			}
			return tokens.getMasterToken();
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Mirrors the upstream behaviour: when there are no tokens for this session yet, both a new master
	 * token and the new page token are minted (the supplier is consumed twice, once for each).
	 */
	@Override
	public String createPageTokenIfAbsent(String sessionKey, String resourceUri, Supplier<String> valueSupplier) {
		synchronized (lockFor(sessionKey)) {
			HttpSession session = currentSession(sessionKey);
			if (session == null) {
				log.debug("No request-bound session for key {}; not creating a CSRFGuard page token", sessionKey);
				return null;
			}
			StoredTokens tokens = readTokens(session);
			if (tokens == null) {
				final String newPageToken = valueSupplier.get();
				tokens = new StoredTokens(valueSupplier.get());
				tokens.setPageToken(resourceUri, newPageToken);
				writeTokens(session, tokens);
				return newPageToken;
			}
			String existing = tokens.getPageTokens().get(resourceUri);
			if (existing == null) {
				existing = valueSupplier.get();
				tokens.setPageToken(resourceUri, existing);
				writeTokens(session, tokens);
				return existing;
			}
			return existing;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Token getToken(String sessionKey) {
		synchronized (lockFor(sessionKey)) {
			HttpSession session = currentSession(sessionKey);
			if (session == null || readTokens(session) == null) {
				return null;
			}
			return new SessionAttributeBackedToken(session);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns {@code null} both when there are no tokens for this session key and when the resource URI
	 * has no token, per this method's contract ("or NULL if there is no token").
	 */
	@Override
	public String getPageToken(String sessionKey, String resourceUri) {
		synchronized (lockFor(sessionKey)) {
			StoredTokens tokens = readTokens(sessionKey);
			return tokens == null ? null : tokens.getPageTokens().get(resourceUri);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPageToken(String sessionKey, String resourceUri, String value) {
		synchronized (lockFor(sessionKey)) {
			StoredTokens tokens = readTokensOrException(sessionKey);
			tokens.setPageToken(resourceUri, value);
			writeTokens(currentSession(sessionKey), tokens);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPageTokens(String sessionKey, Map<String, String> pageTokens) {
		synchronized (lockFor(sessionKey)) {
			StoredTokens tokens = readTokensOrException(sessionKey);
			tokens.setPageTokens(pageTokens);
			writeTokens(currentSession(sessionKey), tokens);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getPageTokens(String sessionKey) {
		synchronized (lockFor(sessionKey)) {
			return readTokensOrException(sessionKey).getPageTokens();
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * A no-op when the session is not reachable from the calling thread (e.g. the session-destroyed
	 * listener): the tokens are session attributes and already went away with the session.
	 */
	@Override
	public void remove(String sessionKey) {
		synchronized (lockFor(sessionKey)) {
			HttpSession session = currentSession(sessionKey);
			if (session != null) {
				session.removeAttribute(TOKENS_SESSION_ATTRIBUTE);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rotateAllPageTokens(String sessionKey, Supplier<String> tokenValueSupplier) {
		synchronized (lockFor(sessionKey)) {
			StoredTokens tokens = readTokensOrException(sessionKey);
			tokens.pageTokens.replaceAll((k, v) -> tokenValueSupplier.get());
			writeTokens(currentSession(sessionKey), tokens);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void regenerateUsedPageToken(String sessionKey, String tokenFromRequest, Supplier<String> tokenValueSupplier) {
		synchronized (lockFor(sessionKey)) {
			StoredTokens tokens = readTokensOrException(sessionKey);
			tokens.pageTokens.replaceAll((k, v) -> v.equals(tokenFromRequest) ? tokenValueSupplier.get() : v);
			writeTokens(currentSession(sessionKey), tokens);
		}
	}

	private HttpSession currentSession(String sessionKey) {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
			return null;
		}
		HttpSession session = servletRequestAttributes.getRequest().getSession(false);
		if (session == null || !session.getId().equals(sessionKey)) {
			return null;
		}
		return session;
	}

	private StoredTokens readTokens(String sessionKey) {
		HttpSession session = currentSession(sessionKey);
		return session == null ? null : readTokens(session);
	}

	private StoredTokens readTokens(HttpSession session) {
		StoredTokens tokens = (StoredTokens) session.getAttribute(TOKENS_SESSION_ATTRIBUTE);
		// tokens minted under another session id (copied over when a login regenerated or rotated the
		// session) belong to the pre-login session, so they must not be valid in this one
		return tokens == null || !session.getId().equals(tokens.sessionId) ? null : tokens;
	}

	private StoredTokens readTokensOrNew(HttpSession session) {
		StoredTokens tokens = readTokens(session);
		return tokens == null ? new StoredTokens() : tokens;
	}

	private StoredTokens readTokensOrException(String sessionKey) {
		StoredTokens tokens = readTokens(sessionKey);
		if (tokens == null) {
			throw new IllegalStateException("Token with the provided session key does not exist!");
		}
		return tokens;
	}

	private void writeTokens(HttpSession session, StoredTokens tokens) {
		tokens.sessionId = session.getId();
		session.setAttribute(TOKENS_SESSION_ATTRIBUTE, tokens);
	}

	/**
	 * A {@link Token} view over the live session attribute: every operation reads the stored state and
	 * writes it back, so all of them replicate with the session. Callers hold the session-key lock
	 * while using the view.
	 */
	private static final class SessionAttributeBackedToken implements Token {

		private final HttpSession session;

		private SessionAttributeBackedToken(HttpSession session) {
			this.session = session;
		}

		private StoredTokens tokens() {
			return (StoredTokens) session.getAttribute(TOKENS_SESSION_ATTRIBUTE);
		}

		private void writeTokens(StoredTokens tokens) {
			session.setAttribute(TOKENS_SESSION_ATTRIBUTE, tokens);
		}

		@Override
		public String getMasterToken() {
			return tokens().getMasterToken();
		}

		@Override
		public void setMasterToken(String masterToken) {
			StoredTokens tokens = tokens();
			tokens.setMasterToken(masterToken);
			writeTokens(tokens);
		}

		@Override
		public String getPageToken(String uri) {
			StoredTokens tokens = tokens();
			String value = tokens.getPageTokens().get(uri);
			if (value == null) {
				throw new IllegalStateException("No page token for " + uri);
			}
			return value;
		}

		@Override
		public PageTokenValue getTimedPageToken(String uri) {
			// creation times are CSRFGuard-internal bookkeeping and are not persisted across nodes;
			// the value is what validation compares
			return PageTokenValue.from(getPageToken(uri));
		}

		@Override
		public void setPageToken(String uri, String pageToken) {
			StoredTokens tokens = tokens();
			tokens.setPageToken(uri, pageToken);
			writeTokens(tokens);
		}

		@Override
		public String setPageTokenIfAbsent(String uri, Supplier<String> valueSupplier) {
			StoredTokens tokens = tokens();
			String existing = tokens.getPageTokens().get(uri);
			if (existing != null) {
				return existing;
			}
			String value = valueSupplier.get();
			tokens.setPageToken(uri, value);
			writeTokens(tokens);
			return value;
		}

		@Override
		public Map<String, String> getPageTokens() {
			return tokens().getPageTokens();
		}

		@Override
		public void setPageTokens(Map<String, String> pageTokens) {
			StoredTokens tokens = tokens();
			tokens.setPageTokens(pageTokens);
			writeTokens(tokens);
		}

		@Override
		public void rotateAllPageTokens(Supplier<String> tokenValueSupplier) {
			StoredTokens tokens = tokens();
			tokens.pageTokens.replaceAll((k, v) -> tokenValueSupplier.get());
			writeTokens(tokens);
		}

		@Override
		public void regenerateUsedPageToken(String tokenFromRequest, Supplier<String> tokenValueSupplier) {
			StoredTokens tokens = tokens();
			tokens.pageTokens.replaceAll((k, v) -> v.equals(tokenFromRequest) ? tokenValueSupplier.get() : v);
			writeTokens(tokens);
		}
	}

	/** The serialized state stored as one session attribute. */
	public static final class StoredTokens implements Serializable {

		private static final long serialVersionUID = 1L;

		/** The session id the tokens were minted in; tokens copied to a regenerated session are invalid. */
		private String sessionId;

		private String masterToken;

		private Map<String, String> pageTokens = new LinkedHashMap<>();

		private StoredTokens() {
		}

		private StoredTokens(String masterToken) {
			this.masterToken = Objects.requireNonNull(masterToken, "Master token cannot be null");
		}

		public String getMasterToken() {
			return masterToken;
		}

		public void setMasterToken(String masterToken) {
			this.masterToken = masterToken;
		}

		public Map<String, String> getPageTokens() {
			return new LinkedHashMap<>(pageTokens);
		}

		public void setPageToken(String resourceUri, String value) {
			pageTokens.put(resourceUri, value);
		}

		public void setPageTokens(Map<String, String> pageTokens) {
			this.pageTokens = new LinkedHashMap<>(pageTokens);
		}
	}
}
