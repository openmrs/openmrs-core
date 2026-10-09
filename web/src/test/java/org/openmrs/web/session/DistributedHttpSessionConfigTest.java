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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.cache.CacheConfig;
import org.openmrs.api.context.SessionPrincipal;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.CookieSerializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end proof of the Spring Session wiring against the <em>real</em> versions on the classpath
 * (Spring 7 + spring-session-core + infinispan-spring6-embedded): the opt-in condition, the
 * {@code @EnableInfinispanEmbeddedHttpSession} config reusing {@code apiCacheManager}, and an
 * actual save/reload of a {@link SessionPrincipal} through the Infinispan-backed {@code sessions}
 * cache. If the Spring/spring-session versions were incompatible, refreshing the context here would
 * fail.
 */
class DistributedHttpSessionConfigTest {

	@AfterEach
	void clearFlag() {
		System.clearProperty(DistributedHttpSessionCondition.PROPERTY);
	}

	private AnnotationConfigApplicationContext contextWith(boolean distributed) {
		return contextWith(distributed, Collections.emptyMap());
	}

	/**
	 * The {@code @Conditional} resolves the flag via {@code resolve()} (system property), not the
	 * Spring {@code Environment}, so the flag is set as a system property here; only the
	 * {@code session.cookie.*} values (read by {@code @Value}) go into the Environment.
	 */
	private AnnotationConfigApplicationContext contextWith(boolean distributed, Map<String, Object> environmentProperties) {
		if (distributed) {
			System.setProperty(DistributedHttpSessionCondition.PROPERTY, "true");
		}
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		if (!environmentProperties.isEmpty()) {
			ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", environmentProperties));
		}
		ctx.register(CacheConfig.class, DistributedHttpSessionConfig.class);
		ctx.refresh();
		return ctx;
	}

	@Test
	void whenEnabled_sessionRepositoryStoresAndReloadsPrincipalThroughInfinispan() {
		try (AnnotationConfigApplicationContext ctx = contextWith(true)) {
			assertTrue(ctx.getBeanNamesForType(SessionRepository.class).length > 0,
			    "an Infinispan-backed SessionRepository must be created when session.distributed=true");
			assertTrue(ctx.containsBean("springSessionRepositoryFilter"),
			    "the filter web.xml delegates to must be contributed");

			@SuppressWarnings("rawtypes")
			SessionRepository repo = ctx.getBean(SessionRepository.class);
			Session created = (Session) repo.createSession();
			SessionPrincipal principal = new SessionPrincipal("uuid-web", Locale.ENGLISH, 4);
			created.setAttribute("__openmrs_session_principal", principal);
			repo.save(created);

			Session found = (Session) repo.findById(created.getId());
			assertNotNull(found, "the saved session must be retrievable from the Infinispan store");
			assertEquals(principal, found.getAttribute("__openmrs_session_principal"),
			    "the SessionPrincipal must survive the full Spring Session + Infinispan round trip");
		}
	}

	@Test
	void whenEnabled_sessionCookieIsHardened() {
		try (AnnotationConfigApplicationContext ctx = contextWith(true)) {
			CookieSerializer serializer = ctx.getBean(CookieSerializer.class);

			MockHttpServletRequest request = new MockHttpServletRequest();
			MockHttpServletResponse response = new MockHttpServletResponse();
			serializer.writeCookieValue(new CookieSerializer.CookieValue(request, response, "session-id-123"));

			String setCookie = response.getHeader("Set-Cookie");
			assertNotNull(setCookie, "a Set-Cookie header must be written");
			assertTrue(setCookie.contains("JSESSIONID="), "uses the configured cookie name: " + setCookie);
			assertTrue(setCookie.contains("HttpOnly"), "cookie must be HttpOnly: " + setCookie);
			assertTrue(setCookie.contains("SameSite=Lax"), "cookie must set SameSite: " + setCookie);
		}
	}

	@Test
	void whenDisabled_noSessionRepositoryIsCreated() {
		try (AnnotationConfigApplicationContext ctx = contextWith(false)) {
			assertEquals(0, ctx.getBeanNamesForType(SessionRepository.class).length,
			    "the session backend must be inert unless session.distributed=true");
		}
	}

	/**
	 * The {@code session.cookie.*} runtime properties are the only way an operator can adapt the cookie
	 * to their deployment, so the property names and the {@code secure} handling are pinned down here -
	 * a renamed or misspelled property would otherwise be silently ignored.
	 */
	@Test
	void whenSessionCookiePropertiesAreOverridden_theyAreApplied() {
		Map<String, Object> cookieProperties = new HashMap<>();
		cookieProperties.put("session.cookie.name", "OPENMRS_SESSION");
		cookieProperties.put("session.cookie.sameSite", "Strict");
		cookieProperties.put("session.cookie.secure", "true");

		try (AnnotationConfigApplicationContext ctx = contextWith(true, cookieProperties)) {
			String setCookie = sessionCookieFor(ctx, false);

			assertTrue(setCookie.contains("OPENMRS_SESSION="), "the cookie name must be overridable: " + setCookie);
			assertTrue(setCookie.contains("SameSite=Strict"), "the SameSite value must be overridable: " + setCookie);
			assertTrue(setCookie.contains("Secure"),
			    "session.cookie.secure=true must mark the cookie Secure even on a plain request: " + setCookie);
		}
	}

	/**
	 * Left unset, {@code Secure} must follow the scheme of the request that set the cookie, so a
	 * plain-HTTP deployment behind TLS termination does not need the property set at all.
	 */
	@Test
	void whenSessionCookieSecureIsUnset_secureFollowsTheRequestScheme() {
		try (AnnotationConfigApplicationContext ctx = contextWith(true)) {
			assertFalse(sessionCookieFor(ctx, false).contains("Secure"),
			    "a plain HTTP request must not get a Secure cookie that the browser would drop");
			assertTrue(sessionCookieFor(ctx, true).contains("Secure"), "an HTTPS request must get a Secure cookie");
		}
	}

	/**
	 * {@code SameSite=None} is only honoured by browsers on a {@code Secure} cookie, so when an
	 * operator sets it but leaves {@code session.cookie.secure} unset, the serializer must force
	 * {@code Secure} even on a plain-HTTP request rather than emit a cookie the browser silently drops.
	 */
	@Test
	void whenSameSiteIsNoneAndSecureIsUnset_secureIsForced() {
		Map<String, Object> cookieProperties = new HashMap<>();
		cookieProperties.put("session.cookie.sameSite", "None");

		try (AnnotationConfigApplicationContext ctx = contextWith(true, cookieProperties)) {
			String setCookie = sessionCookieFor(ctx, false);

			assertTrue(setCookie.contains("SameSite=None"), "the SameSite value must be applied: " + setCookie);
			assertTrue(setCookie.contains("Secure"), "SameSite=None must force a Secure cookie: " + setCookie);
		}
	}

	/** @param secureRequest whether the request that is setting the cookie arrived over HTTPS */
	private String sessionCookieFor(AnnotationConfigApplicationContext ctx, boolean secureRequest) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setSecure(secureRequest);
		MockHttpServletResponse response = new MockHttpServletResponse();

		ctx.getBean(CookieSerializer.class).writeCookieValue(new CookieSerializer.CookieValue(request, response, "sid"));

		return response.getHeader("Set-Cookie");
	}
}
