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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import javax.naming.Reference;

import jakarta.servlet.http.Cookie;

import org.infinispan.Cache;
import org.infinispan.commons.CacheException;
import org.infinispan.commons.TimeoutException;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.common.provider.SpringCache;
import org.infinispan.spring.embedded.session.InfinispanEmbeddedSessionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.cache.CacheConfig;
import org.openmrs.api.cache.DistributedSessionPurger;
import org.openmrs.api.context.Context;
import org.openmrs.web.filter.CookieClearingFilter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.SessionRepositoryFilter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the {@link UnreadableSessionPurgingSessionRepository} wrapper drops a stored
 * session whose value the sessions-cache serialization allow list rejects at the <em>load
 * point</em>, and reports it as absent - so every caller of {@code findById} (the Spring Session
 * wrapper answers even {@code getRequestedSessionId()} through a load) sees an absent session
 * instead of a deserialization failure. A poisoned entry must cost a re-login, not a 500 on every
 * request.
 */
class UnreadableSessionPurgingSessionRepositoryTest {

	private EmbeddedCacheManager cacheManager;

	private Cache<Object, Object> sessions;

	private InfinispanEmbeddedSessionRepository delegate;

	@BeforeEach
	void setUp() throws Exception {
		cacheManager = new DefaultCacheManager("infinispan-api-local.xml");
		sessions = cacheManager.getCache(CacheConfig.SESSIONS_CACHE_NAME);
		delegate = new InfinispanEmbeddedSessionRepository(new SpringCache(sessions));
		delegate.setDefaultMaxInactiveInterval(Duration.ofMinutes(30));
		delegate.setApplicationEventPublisher(event -> {});
	}

	@AfterEach
	void tearDown() {
		if (cacheManager != null) {
			cacheManager.stop();
		}
	}

	private UnreadableSessionPurgingSessionRepository wrappedRepository(DistributedSessionPurger purger) {
		Supplier<DistributedSessionPurger> supplier = purger == null ? () -> null : () -> purger;
		return new UnreadableSessionPurgingSessionRepository(delegate, supplier);
	}

	/**
	 * Creates and saves a real session through the provider repository, then poisons its cache entry.
	 */
	private String createSessionThenPoisonItsEntry() {
		var real = delegate.createSession();
		real.setAttribute("some-attribute", "some-value");
		delegate.save(real);
		String id = real.getId();
		// writes are not allow-listed (the rejection is a deserialization-time check), so a node with
		// a different classpath can leave an entry this node can never read - the poisoning vector
		sessions.put(id, new Reference("poison"));
		return id;
	}

	@Test
	void poisonedEntryIsDroppedAndReportedAsAbsent() {
		String id = createSessionThenPoisonItsEntry();
		UnreadableSessionPurgingSessionRepository wrapped = wrappedRepository(new DistributedSessionPurger(cacheManager));

		assertNull(wrapped.findById(id), "a poisoned entry must be reported as an absent session");
		assertNull(sessions.get(id), "the poisoned entry must be dropped, so the failure does not repeat");
	}

	@Test
	void healthySessionsRoundTripThroughTheWrapper() {
		UnreadableSessionPurgingSessionRepository wrapped = wrappedRepository(null);

		Session created = wrapped.createSession();
		assertNotNull(created);
		created.setAttribute("attr", "value");
		wrapped.save(created);

		Session loaded = wrapped.findById(created.getId());
		assertNotNull(loaded, "a healthy session must load normally");
		assertEquals("value", loaded.getAttribute("attr"));

		wrapped.deleteById(created.getId());
		assertNull(sessions.get(created.getId()));
	}

	@Test
	void unreadableFailurePurgesTheEntryAndReportsItAsAbsent() {
		List<String> purged = new ArrayList<>();
		DistributedSessionPurger recordingPurger = new DistributedSessionPurger(null) {

			@Override
			public void purge(String sessionId) {
				purged.add(sessionId);
			}
		};
		SessionRepository<Session> failing = failingRepository(
		    new CacheException("ISPN000936: Class 'org.openmrs.LocationTag' blocked by deserialization allow list"));
		UnreadableSessionPurgingSessionRepository wrapped = wrappedRepositoryWithStub(failing, recordingPurger);

		assertNull(wrapped.findById("session-1"), "a poisoned session must be reported as an absent session");
		assertEquals(List.of("session-1"), purged, "the poisoned entry must be dropped through the purger");
	}

	@Test
	void transientCacheFailurePropagatesWithoutPurging() {
		List<String> purged = new ArrayList<>();
		DistributedSessionPurger recordingPurger = new DistributedSessionPurger(null) {

			@Override
			public void purge(String sessionId) {
				purged.add(sessionId);
			}
		};
		SessionRepository<Session> failing = failingRepository(new TimeoutException("replication timed out"));
		UnreadableSessionPurgingSessionRepository wrapped = wrappedRepositoryWithStub(failing, recordingPurger);

		assertThrows(TimeoutException.class, () -> wrapped.findById("healthy-1"),
		    "a transient failure must propagate, so the request fails without replacing the session");
		assertTrue(purged.isEmpty(), "a transient cache error must never purge a healthy entry");
	}

	/**
	 * The failure the reviewers hit end to end: {@code CookieClearingFilter} answers
	 * {@code getRequestedSessionId()} through the Spring Session wrapper, which loads the session - a
	 * poisoned entry used to 500 there, before {@code OpenmrsSecurityContextFilter} was ever entered.
	 * The full chain must serve the request anonymously instead.
	 */
	@Test
	void poisonedEntryServesAnonymouslyThroughTheRealFilterChain() throws Exception {
		String poisonedId = createSessionThenPoisonItsEntry();
		UnreadableSessionPurgingSessionRepository wrapped = wrappedRepository(new DistributedSessionPurger(cacheManager));

		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName("JSESSIONID");
		serializer.setUseBase64Encoding(false);
		CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
		resolver.setCookieSerializer(serializer);
		SessionRepositoryFilter<Session> springSessionFilter = new SessionRepositoryFilter<>(wrapped);
		springSessionFilter.setHttpSessionIdResolver(resolver);

		Properties runtimeProperties = new Properties();
		runtimeProperties.setProperty("cookieClearingFilter.toClear", "JSESSIONID");
		Context.setRuntimeProperties(runtimeProperties);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCookies(new Cookie("JSESSIONID", poisonedId));
		MockHttpServletResponse response = new MockHttpServletResponse();

		boolean[] served = { false };
		try {
			assertDoesNotThrow(() -> springSessionFilter.doFilter(request, response, (rq, rs) -> {
				new CookieClearingFilter().doFilter(rq, rs, (rq2, rs2) -> {
					served[0] = true;
					assertNull(((jakarta.servlet.http.HttpServletRequest) rq2).getSession(false),
					    "the poisoned session must look absent downstream");
				});
			}));
		} finally {
			Context.setRuntimeProperties(new Properties());
		}

		assertTrue(served[0], "the request must be served anonymously, not 500");
		assertFalse(response.isCommitted() && response.getStatus() >= 500, "no 500 may reach the client");
		assertNull(sessions.get(poisonedId), "the poisoned entry must be dropped by the load attempt");
	}

	private SessionRepository<Session> failingRepository(RuntimeException failure) {
		return new SessionRepository<Session>() {

			@Override
			public Session findById(String id) {
				throw failure;
			}

			@Override
			public void save(Session session) {
				// not reached in these tests
			}

			@Override
			public Session createSession() {
				return null;
			}

			@Override
			public void deleteById(String id) {
				// not reached in these tests
			}
		};
	}

	private UnreadableSessionPurgingSessionRepository wrappedRepositoryWithStub(SessionRepository<Session> stub,
	        DistributedSessionPurger purger) {
		Supplier<DistributedSessionPurger> supplier = purger == null ? () -> null : () -> purger;
		return new UnreadableSessionPurgingSessionRepository(stub, supplier);
	}
}
