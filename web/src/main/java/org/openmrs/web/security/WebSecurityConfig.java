/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.security;

import java.util.List;

import org.openmrs.security.OpenmrsAuthenticationToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Registers the {@code SecurityFilterChain} behind the {@code springSecurityFilterChain} seat in
 * {@code web.xml}, where {@link OpenmrsSecurityContextFilter} replaces
 * {@code org.openmrs.web.filter.OpenmrsFilter}. {@code @EnableWebSecurity} wraps every
 * {@code SecurityFilterChain} bean in its own bean of that name, which is what {@code web.xml}
 * resolves - hence the different bean name on the method below.
 * <p>
 * Most Spring Security defaults are disabled, because OpenMRS already owns that concern:
 * <ul>
 * <li>{@code securityContext} - {@link OpenmrsSecurityContextFilter} installs and clears the
 * context itself, persisting the {@code UserContext} under OpenMRS's own session attribute.</li>
 * <li>{@code csrf} - OWASP's {@code CsrfGuardFilter} stays the sole CSRF authority; it injects
 * tokens into legacy UI forms and AJAX calls, which Spring Security has no equivalent for.</li>
 * <li>{@code anonymous} - the holder is never empty while a session is open, so that filter has
 * nothing to do and could only overwrite the installed {@link OpenmrsAuthenticationToken}.</li>
 * <li>{@code headers} - the defaults add {@code X-Frame-Options: DENY}, breaking the same-origin
 * framing htmlformentryui depends on, and a no-cache {@code Cache-Control} that defeats
 * {@code /moduleResources/**} caching. Worth adding deliberately, not as a side effect.</li>
 * <li>{@code logout} - {@code LogoutFilter} would intercept any {@code /logout} request before the
 * webapp's own handling of it.</li>
 * <li>{@code requestCache} - nothing here replays a {@code SavedRequest}. Its {@code disable()}
 * also publishes a {@code NullRequestCache}, which this chain's {@code ExceptionTranslationFilter}
 * picks up; {@link OpenmrsAuthorizationFilter} asks for one explicitly, building its own.</li>
 * <li>{@code servletApi} - nothing reads {@code getUserPrincipal()}/{@code isUserInRole(...)}
 * instead of {@code Context}.</li>
 * <li>{@code sessionManagement} - concurrent-session limits and session fixation are policies
 * OpenMRS has never enforced.</li>
 * </ul>
 * <p>
 * {@code exceptionHandling(...)} matches {@link OpenmrsAuthorizationFilter}'s denial handling, and
 * {@code authorizeHttpRequests(...)} is a blanket {@code permitAll()}: {@link AuthorizedUrlMatcher}
 * rules are enforced by {@link OpenmrsAuthorizationFilter} instead, which {@code web.xml} places
 * after {@code ModuleFilter} so a module authenticating from its own filter is not denied first -
 * see that class for the reasoning.
 * <p>
 * {@code FilterChainProxy}'s {@code StrictHttpFirewall} is deliberately left at its defaults: a URL
 * rule matches the raw request URI, while the container decodes and normalizes before choosing a
 * servlet, so relaxing semicolons or encoded slashes and periods would let a rule be walked around
 * with {@code /x/..;/admin/y}. {@code web.xml} asks for
 * {@code <tracking-mode>COOKIE</tracking-mode>} so the {@code ;jsessionid=} a container would
 * otherwise append never needs allowing.
 *
 * @since 3.0.0
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	@Bean
	public OpenmrsSecurityContextFilter openmrsSecurityContextFilter() {
		return new OpenmrsSecurityContextFilter();
	}

	@Bean(name = "openmrsAuthorizationFilter")
	public OpenmrsAuthorizationFilter openmrsAuthorizationFilter(List<AuthorizedUrlMatchers> authorizedUrlMatchers) {
		return new OpenmrsAuthorizationFilter(authorizedUrlMatchers);
	}

	// Sonar (java:S4502) and CodeQL flag csrf(disable) below; deliberate - CsrfGuardFilter is the sole
	// CSRF authority, see this class's javadoc.
	@SuppressWarnings("java:S4502")
	@Bean
	public SecurityFilterChain openmrsSecurityFilterChain(HttpSecurity http,
	        OpenmrsSecurityContextFilter openmrsSecurityContextFilter) {
		http.securityContext(AbstractHttpConfigurer::disable).csrf(AbstractHttpConfigurer::disable) // codeql[java/spring-disabled-csrf-protection]: CsrfGuardFilter (see class javadoc) is the sole CSRF authority; enabling this too would collide with it
		        .anonymous(AbstractHttpConfigurer::disable).headers(AbstractHttpConfigurer::disable)
		        .logout(AbstractHttpConfigurer::disable).requestCache(AbstractHttpConfigurer::disable)
		        .servletApi(AbstractHttpConfigurer::disable).sessionManagement(AbstractHttpConfigurer::disable)
		        // ahead of ExceptionTranslationFilter, not just AuthorizationFilter: this filter clears
		        // SecurityContextHolder in a finally, leaving a later denial no Authentication to classify
		        .addFilterBefore(openmrsSecurityContextFilter, ExceptionTranslationFilter.class)
		        .exceptionHandling(WebSecurityConfig::sameDenialHandlingAsTheAuthorizationFilter)
		        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}

	/**
	 * Gives this chain's {@code ExceptionTranslationFilter} the same denial handling
	 * {@link OpenmrsAuthorizationFilter} gives its own: 401 for a caller who has not identified itself,
	 * 403 naming the missing privilege for one who has.
	 * <p>
	 * The trust resolver goes in through an {@link ObjectPostProcessor} because
	 * {@code ExceptionHandlingConfigurer} has no setter for one and ignores an
	 * {@code AuthenticationTrustResolver} bean. An anonymous class, not a lambda: post-processors are
	 * dispatched by their resolved type argument, and an unresolvable one is applied to everything.
	 */
	private static void sameDenialHandlingAsTheAuthorizationFilter(ExceptionHandlingConfigurer<HttpSecurity> exceptions) {
		exceptions.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
		        .accessDeniedHandler(new OpenmrsAccessDeniedHandler())
		        .withObjectPostProcessor(new ObjectPostProcessor<ExceptionTranslationFilter>() {

			        @Override
			        public ExceptionTranslationFilter postProcess(ExceptionTranslationFilter filter) {
				        filter.setAuthenticationTrustResolver(new OpenmrsAuthenticationTrustResolver());
				        return filter;
			        }
		        });
	}
}
