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

import jakarta.annotation.PostConstruct;

import org.infinispan.spring.embedded.session.configuration.EnableInfinispanEmbeddedHttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Opt-in Spring Session configuration that stores the HTTP session in the shared Infinispan
 * {@code sessions} cache (reusing the existing {@code apiCacheManager}), so it can be replicated to
 * every node. Inert unless {@code session.distributed=true} (see
 * {@link DistributedHttpSessionCondition}); when active it contributes the
 * {@code springSessionRepositoryFilter} bean that {@code web.xml} delegates to.
 * <p>
 * <strong>Transport security is a deployment responsibility:</strong> the replicated session
 * travels over the Infinispan/JGroups transport, unencrypted by default. Operators must secure it
 * via a custom JGroups stack supplied through {@code cache.stack} / {@code cache.config}.
 *
 * @since 3.0.0
 */
@Configuration
@Conditional(DistributedHttpSessionCondition.class)
@EnableInfinispanEmbeddedHttpSession(cacheName = DistributedHttpSessionConfig.SESSION_CACHE_NAME)
public class DistributedHttpSessionConfig {

	/** Must match the {@code sessions} cache defined in {@code infinispan-api(-local).xml}. */
	public static final String SESSION_CACHE_NAME = "sessions";

	private static final Logger log = LoggerFactory.getLogger(DistributedHttpSessionConfig.class);

	@PostConstruct
	public void announce() {
		log.info("Distributed HTTP session backend enabled: sessions are stored in the Infinispan '{}' cache",
		    SESSION_CACHE_NAME);
	}

	/**
	 * Contributed here (not component-scanned) so it is created only when this conditional config is
	 * active and lands in the context {@code Context.getRegisteredComponents} consults.
	 *
	 * @since 3.0.0
	 */
	@Bean
	public DistributedSessionListener distributedSessionListener() {
		return new DistributedSessionListener();
	}

	/**
	 * Hardens the session cookie ({@code HttpOnly}, {@code SameSite=Lax}).
	 * {@code session.cookie.secure} is unset by default so {@code Secure} follows HTTPS; set it
	 * {@code true} to force it.
	 *
	 * @since 3.0.0
	 */
	@Bean
	public CookieSerializer cookieSerializer(@Value("${session.cookie.name:JSESSIONID}") String cookieName,
	        @Value("${session.cookie.sameSite:Lax}") String sameSite, @Value("${session.cookie.secure:}") String secure) {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName(cookieName);
		serializer.setUseHttpOnlyCookie(true);
		serializer.setSameSite(sameSite);
		if (secure != null && !secure.isBlank()) {
			serializer.setUseSecureCookie(Boolean.parseBoolean(secure));
		} else if ("None".equalsIgnoreCase(sameSite)) {
			// Browsers only honour SameSite=None on Secure cookies; force it so the cookie is not
			// silently dropped when the operator sets SameSite=None but leaves session.cookie.secure unset.
			serializer.setUseSecureCookie(true);
		}
		return serializer;
	}
}
