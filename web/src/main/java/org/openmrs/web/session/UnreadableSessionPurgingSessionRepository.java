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

import java.util.function.Supplier;

import org.infinispan.commons.CacheException;
import org.infinispan.commons.marshall.MarshallingException;
import org.openmrs.api.cache.DistributedSessionPurger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

/**
 * Wraps the Spring Session {@link SessionRepository} so a stored session whose value fails to
 * deserialize is dropped and reported as absent at the <em>load point</em>. Every session read in
 * the request chain funnels through {@code findById} - including {@code CookieClearingFilter}'s
 * {@code getRequestedSessionId()}, which {@code web.xml} runs above
 * {@code OpenmrsSecurityContextFilter} and which Spring Session answers by loading the session - so
 * a poisoned entry (one stored value the sessions-cache serialization allow list rejects, e.g.
 * written by a node with a different classpath) would otherwise throw on every request carrying its
 * cookie, before any filter-level handling could run. With this wrapper every caller sees an absent
 * session instead: the request proceeds anonymously and the entry is dropped, so it costs the user
 * a re-login rather than a 500 until the entry expires.
 * <p>
 * A transient cache failure (e.g. a replication timeout) is propagated, not swallowed: failing the
 * request leaves the stored entry and the browser's session cookie untouched, so the user's login
 * survives until the cache recovers. Reporting the session as absent instead would mint an
 * anonymous replacement whose cookie replaces the browser's - silently logging the user out.
 *
 * @since 3.0.0
 */
public class UnreadableSessionPurgingSessionRepository implements SessionRepository<Session> {

	private static final Logger log = LoggerFactory.getLogger(UnreadableSessionPurgingSessionRepository.class);

	private final SessionRepository<? extends Session> delegate;

	/**
	 * Resolved lazily, on the first failure: the purger owns the {@code sessions} cache, so an eager
	 * reference would pull the cache manager into early initialization.
	 */
	private final Supplier<DistributedSessionPurger> purgerSupplier;

	/**
	 * @param delegate the repository bean created for {@code @EnableInfinispanEmbeddedHttpSession}
	 * @param purgerSupplier supplies the {@link DistributedSessionPurger} at failure time; may yield
	 *            {@code null}, in which case the poisoned entry degrades to anonymous without being
	 *            dropped
	 * @since 3.0.0
	 */
	public UnreadableSessionPurgingSessionRepository(SessionRepository<? extends Session> delegate,
	    Supplier<DistributedSessionPurger> purgerSupplier) {
		this.delegate = delegate;
		this.purgerSupplier = purgerSupplier;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * A stored session that cannot be deserialized is dropped (so the failure does not repeat on every
	 * request) and reported as absent, whatever the caller: the request proceeds anonymously.
	 */
	@Override
	public Session findById(String id) {
		try {
			return delegate.findById(id);
		} catch (RuntimeException e) {
			if (isUnreadableSession(e)) {
				log.warn("Distributed session failed to deserialize; dropping it and serving the request anonymously", e);
				purge(id);
				return null;
			}
			// A transient cache failure must NOT degrade to an absent session: the request would mint an
			// anonymous replacement whose cookie replaces the browser's valid one, silently logging the
			// user out. Fail the request instead - the stored entry and the user's login survive until
			// the cache recovers.
			throw e;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(Session session) {
		repository().save(session);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Session createSession() {
		return repository().createSession();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteById(String id) {
		repository().deleteById(id);
	}

	private void purge(String id) {
		DistributedSessionPurger purger = purgerSupplier.get();
		if (purger != null) {
			purger.purge(id);
		}
	}

	/**
	 * @return whether the failure chain says the session's stored value cannot be decoded (an
	 *         allow-list rejection or a marshalling failure), as opposed to a transient cache error
	 */
	private static boolean isUnreadableSession(RuntimeException failure) {
		for (Throwable t = failure; t != null && t != t.getCause(); t = t.getCause()) {
			if (t instanceof MarshallingException) {
				return true;
			}
			if (t instanceof CacheException && String.valueOf(t.getMessage()).startsWith("ISPN000936")) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private SessionRepository<Session> repository() {
		return (SessionRepository<Session>) delegate;
	}
}
