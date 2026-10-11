/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.cache;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drops a distributed HTTP session that can no longer be read. Spring Session serialises the whole
 * session attribute map, so one stored value that the serialization allow list rejects makes every
 * read of that session fail - and a plain remove through Spring Session cannot recover it, because
 * the repository's removal path reads the entry too. This purge goes through the raw
 * {@code sessions} cache with {@link Flag#SKIP_LISTENER_NOTIFICATION}: a stray type costs the user
 * a re-login instead of locking them out until the entry expires.
 *
 * @since 3.0.0
 */
public class DistributedSessionPurger {

	private static final Logger log = LoggerFactory.getLogger(DistributedSessionPurger.class);

	private static final String UNSAFE_LOG_CHARACTERS = "[\\r\\n\\t]";

	private final EmbeddedCacheManager cacheManager;

	/**
	 * @param cacheManager the manager that owns the {@code sessions} cache (the
	 *            {@code apiCacheManager})
	 * @since 3.0.0
	 */
	public DistributedSessionPurger(EmbeddedCacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	/**
	 * Removes the stored session entry without reading its value, so an entry that fails to deserialize
	 * is dropped rather than throwing. Idempotent: purging an unknown or already-purged session id does
	 * nothing. Silent by design: every caller purges deliberately (the login listener drops the healthy
	 * pre-login session, the session repository drops one that failed to load) and logs its own reason
	 * - a removal says nothing about the entry's readability.
	 *
	 * @param sessionId the Spring Session id to drop
	 * @since 3.0.0
	 */
	public void purge(String sessionId) {
		if (sessionId == null || sessionId.isBlank()) {
			return;
		}
		try {
			Cache<Object, Object> sessions = cacheManager.getCache(CacheConfig.SESSIONS_CACHE_NAME);
			sessions.getAdvancedCache().withFlags(Flag.SKIP_LISTENER_NOTIFICATION).remove(sessionId);
		} catch (RuntimeException e) {
			log.warn("Could not drop unreadable distributed session {}", loggable(sessionId), e);
		}
	}

	/**
	 * The session id is client-controlled (it comes from a cookie) and must never reach the logs raw.
	 */
	private static String loggable(String sessionId) {
		return sessionId.replaceAll("[\\r\\n\\t]", "_");
	}
}
