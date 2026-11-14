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

import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.infinispan.spring.embedded.session.configuration.EnableInfinispanEmbeddedHttpSession;
import org.openmrs.api.cache.CacheConfig;
import org.openmrs.api.cache.DistributedSessionPurger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.SessionRepository;
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
	public static final String SESSION_CACHE_NAME = CacheConfig.SESSIONS_CACHE_NAME;

	private static final Logger log = LoggerFactory.getLogger(DistributedHttpSessionConfig.class);

	@PostConstruct
	public void announce() {
		if (DistributedHttpSessionCondition.isEnabled()) {
			log.info("Distributed HTTP session backend enabled: sessions are stored in the Infinispan '{}' cache",
			    SESSION_CACHE_NAME);
		}
	}

	/**
	 * Wraps the {@link SessionRepository} bean the provider creates (via
	 * {@code @EnableInfinispanEmbeddedHttpSession}) in
	 * {@link UnreadableSessionPurgingSessionRepository}, so a stored session that fails to deserialize
	 * is dropped and reported as absent at the load point. Every session read funnels through
	 * {@code findById} - including {@code CookieClearingFilter}'s {@code getRequestedSessionId()},
	 * which {@code web.xml} runs above {@code OpenmrsSecurityContextFilter} - so an unhandled poisoned
	 * entry would 500 every request carrying its cookie before any filter-level handling could run.
	 * <p>
	 * Static so it is instantiated before ordinary beans and cannot pull the cache manager into early
	 * initialization; the {@link ObjectProvider} defers the purger lookup to the first failure, so this
	 * post-processor never triggers eager initialization either.
	 *
	 * @since 3.0.0
	 */
	@Bean
	public static BeanPostProcessor unreadableSessionPurgingPostProcessor(
	        ObjectProvider<DistributedSessionPurger> purgerProvider) {
		return new BeanPostProcessor() {

			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) {
				if (bean instanceof SessionRepository<?> repository
				        && !(bean instanceof UnreadableSessionPurgingSessionRepository)) {
					return new UnreadableSessionPurgingSessionRepository(repository, purgerProvider::getIfAvailable);
				}
				return bean;
			}
		};
	}

	/**
	 * Contributed here (not component-scanned) so it is created only when this conditional config is
	 * active and lands in the context {@code Context.getRegisteredComponents} consults.
	 *
	 * @since 3.0.0
	 */
	@Bean
	public DistributedSessionListener distributedSessionListener(DistributedSessionPurger sessionPurger) {
		return new DistributedSessionListener(sessionPurger);
	}

	/**
	 * Lets the web layer drop a stored session whose attribute map fails to deserialize (see
	 * {@link DistributedSessionPurger}); created only when this conditional config is active, so it is
	 * never consulted when the feature is off.
	 *
	 * @since 3.0.0
	 */
	@Bean
	public DistributedSessionPurger distributedSessionPurger(SpringEmbeddedCacheManager cacheManager) {
		return new DistributedSessionPurger(cacheManager.getNativeCacheManager());
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
