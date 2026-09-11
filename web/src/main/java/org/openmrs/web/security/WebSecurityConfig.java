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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

/**
 * Registers the {@code SecurityFilterChain} that backs the {@code springSecurityFilterChain} seat
 * in {@code web.xml} (inserted between {@code CookieClearingFilter} and {@code CSRFGuard} -
 * {@code OpenmrsSecurityContextFilter} occupies the seat
 * {@code org.openmrs.web.filter.OpenmrsFilter} used to) - {@code @EnableWebSecurity} collects every
 * {@code SecurityFilterChain} bean (by type, not name) and wraps them in its own auto-registered
 * {@code Filter} bean, which it names {@code springSecurityFilterChain}; that framework-owned bean,
 * not this one, is what {@code DelegatingFilterProxy} resolves from {@code web.xml}, so this method
 * deliberately uses a different bean name to avoid colliding with it.
 * <ul>
 * <li>{@code securityContext(disable)} - {@link OpenmrsSecurityContextFilter}, added to this chain
 * below, installs/clears the current thread's
 * {@link org.springframework.security.core.context.SecurityContext} itself via
 * {@code Context.setUserContext(...)}/{@code Context.clearUserContext()} (see {@code Context}'s
 * delegation to {@code SecurityContextHolder} since 3.0.0), persisting the underlying
 * {@code UserContext} on the {@code HttpSession} under OpenMRS's own attribute key, not Spring
 * Security's. Its own {@code SecurityContextHolderFilter}/persistence would be redundant and risks
 * two systems both trying to own the same per-request state.</li>
 * <li>{@code csrf(disable)} - the OWASP {@code CsrfGuardFilter} remains the sole CSRF authority, by
 * deliberate choice: it already auto-injects its token into every form/AJAX call across the legacy
 * UI (JS-based, {@code injectIntoForms}/{@code Ajax} in {@code csrfguard.properties}), which Spring
 * Security's own CSRF support has no equivalent for. Running both would mean two independent,
 * conflicting token mechanisms.</li>
 * <li>{@code anonymous(disable)} - {@code SecurityContextHolder} is never empty while a session is
 * open (even before login), so Spring Security's own anonymous-authentication filter has nothing to
 * do; disabling it avoids any chance of it overwriting the already-installed
 * {@code OpenmrsAuthenticationToken}.</li>
 * <li>{@code authorizeHttpRequests(...)} delegates to {@link OpenmrsAuthorizationManager}, built
 * from every {@link AuthorizedUrlMatchers} bean core or a module has registered - one bean per
 * module, bundling all of its own {@link AuthorizedUrlMatcher} rules (see
 * {@link AuthorizedUrlMatchers}' javadoc for why that is a single bean per module rather than one
 * bean per rule, and {@link AuthorizedUrlMatcher}'s own javadoc for why gathering them once here,
 * rather than re-querying per request, is correct). legacyui's
 * {@code LoginServlet}/{@code AuthorizationHandlerInterceptor} and webservices.rest's deliberately
 * "fail-open at the filter, fail-closed at the service" {@code AuthorizationFilter} both still
 * handle their own request-level logic underneath this - a path with no
 * {@code AuthorizedUrlMatcher} is unaffected by this layer, exactly as it was under the previous
 * blanket {@code permitAll()}.</li>
 * </ul>
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

	@Bean
	public SecurityFilterChain openmrsSecurityFilterChain(HttpSecurity http,
	        OpenmrsSecurityContextFilter openmrsSecurityContextFilter, List<AuthorizedUrlMatchers> authorizedUrlMatchers)
	        throws Exception {
		http.securityContext(AbstractHttpConfigurer::disable).csrf(AbstractHttpConfigurer::disable)
		        .anonymous(AbstractHttpConfigurer::disable)
		        // must run before authorizeHttpRequests below: that needs the Authentication this
		        // filter installs (via Context.setUserContext(...)) to already be in place
		        .addFilterBefore(openmrsSecurityContextFilter, AuthorizationFilter.class).authorizeHttpRequests(
		            auth -> auth.anyRequest().access(new OpenmrsAuthorizationManager(authorizedUrlMatchers)));
		return http.build();
	}
}
