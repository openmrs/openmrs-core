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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.logging.MemoryAppender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Verifies that a purge drops a stored session without touching its value, so an entry whose
 * attribute map fails to deserialize (an allow-listed-out class) is recoverable by removal.
 */
public class DistributedSessionPurgerTest {

	private DefaultCacheManager cacheManager;

	private DistributedSessionPurger purger;

	@BeforeEach
	public void setUp() throws Exception {
		cacheManager = new DefaultCacheManager("infinispan-api-local.xml");
		purger = new DistributedSessionPurger(cacheManager);
	}

	@AfterEach
	public void tearDown() {
		if (cacheManager != null) {
			cacheManager.stop();
		}
	}

	@Test
	public void purge_shouldRemoveTheStoredSession() {
		cacheManager.getCache("sessions").put("session-1", "any value");
		assertEquals("any value", cacheManager.getCache("sessions").get("session-1"));

		purger.purge("session-1");

		assertNull(cacheManager.getCache("sessions").get("session-1"), "the poisoned session must be dropped");
	}

	@Test
	public void purge_shouldTolerateAnUnknownSessionId() {
		assertDoesNotThrow(() -> purger.purge("no-such-session"));
		assertNull(cacheManager.getCache("sessions").get("no-such-session"));
	}

	/**
	 * A session id is a live credential (the session cookie carries it), so even the failure warning
	 * must not log it verbatim: a stable SHA-256 prefix is logged instead, keeping the line
	 * correlatable per session without revealing a replayable id.
	 */
	@Test
	public void purge_shouldNotLogTheRawSessionIdOnFailure() throws Exception {
		MemoryAppender appender = MemoryAppender.newBuilder().setName("DistributedSessionPurgerTest-logCheck")
		        .setLayout(PatternLayout.newBuilder().withPattern("%m").build()).build();
		appender.start();
		Logger log4jLogger = (Logger) LogManager.getLogger(DistributedSessionPurger.class);
		Level originalLevel = log4jLogger.getLevel();
		boolean originalAdditive = log4jLogger.isAdditive();
		log4jLogger.setAdditive(false);
		log4jLogger.setLevel(Level.WARN);
		log4jLogger.addAppender(appender);

		// a cache manager that cannot resolve the cache forces the failure warning
		DistributedSessionPurger failing = new DistributedSessionPurger(mock(EmbeddedCacheManager.class));
		try {
			failing.purge("leaky-session-id");
		} finally {
			log4jLogger.removeAppender(appender);
			log4jLogger.setLevel(originalLevel);
			log4jLogger.setAdditive(originalAdditive);
			((Logger) LogManager.getRootLogger()).getContext().updateLoggers();
			appender.stop();
		}

		String logged = String.join("\n", appender.getLogLines());
		assertFalse(logged.contains("leaky-session-id"), "the raw session id must not reach the logs");
		byte[] digest = MessageDigest.getInstance("SHA-256").digest("leaky-session-id".getBytes(StandardCharsets.UTF_8));
		assertTrue(logged.contains(HexFormat.of().formatHex(digest, 0, 6)),
		    "the log must carry a stable hash of the session id instead");
	}
}
