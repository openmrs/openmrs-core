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
 * {@code Context.setUserContext(...)}/{@code Context.clearUserContext()} (which, since 3.0.0, also
 * publish a copy into {@code SecurityContextHolder} alongside the private {@code ThreadLocal} that
 * remains the actual store of record - see {@code Context.userContextHolder}'s own javadoc),
 * persisting the underlying {@code UserContext} on the {@code HttpSession} under OpenMRS's own
 * attribute key, not Spring Security's. Its own {@code SecurityContextHolderFilter}/persistence
 * would be redundant and risks two systems both trying to own the same per-request state.</li>
 * <li>{@code csrf(disable)} - the OWASP {@code CsrfGuardFilter} remains the sole CSRF authority, by
 * deliberate choice: it already auto-injects its token into every form/AJAX call across the legacy
 * UI (JS-based, {@code injectIntoForms}/{@code Ajax} in {@code csrfguard.properties}), which Spring
 * Security's own CSRF support has no equivalent for. Running both would mean two independent,
 * conflicting token mechanisms.</li>
 * <li>{@code anonymous(disable)} - {@code SecurityContextHolder} is never empty while a session is
 * open (even before login), so Spring Security's own anonymous-authentication filter has nothing to
 * do; disabling it avoids any chance of it overwriting the already-installed
 * {@code OpenmrsAuthenticationToken}.</li>
 * <li>{@code headers(disable)} - left at their defaults, {@code HeaderWriterFilter} adds
 * {@code X-Frame-Options: DENY} and a no-cache {@code Cache-Control} to every response,
 * unconditionally, to every path, whether or not it carries an {@code AuthorizedUrlMatcher} rule.
 * {@code DENY} breaks same-origin framing wherever the proxy in front of this webapp doesn't itself
 * send a CSP {@code frame-ancestors} (O3's reference gateway does, but the header here would still
 * apply to any deployment without one) - O3's HTML Form Entry wrapper depends on framing to iframe
 * {@code /htmlformentryui/htmlform/*.page} and read its {@code contentDocument}. The no-cache
 * directive defeats caching for {@code /moduleResources/**}, since {@code ModuleResourcesServlet}
 * only ever sets {@code Last-Modified} itself, so every module resource would be re-downloaded on
 * every page view. Security headers are worth having - just as their own, deliberate change (e.g.
 * {@code frameOptions(sameOrigin)} rather than {@code DENY}), not a side effect of this one.</li>
 * <li>{@code logout(disable)} - left enabled, {@code LogoutFilter} intercepts <em>any</em> request
 * (GET, POST, PUT, or DELETE) to {@code /logout}, invalidates the session, and redirects to
 * {@code /login?logout} - before that request ever reaches the actual webapp, which has its own
 * {@code /logout} handling.</li>
 * <li>{@code requestCache(disable)} - {@code RequestCacheAwareFilter} replays a
 * {@code SavedRequest} after a redirect-driven login flow, which nothing here uses;
 * {@link OpenmrsSecurityContextFilter} installs the {@code Authentication} directly from the
 * session's existing {@code UserContext}; a module implementing its own redirect-to-login
 * (legacyui's {@code LoginServlet}) already handles its own post-login redirect.</li>
 * <li>{@code servletApi(disable)} - {@code SecurityContextHolderAwareRequestFilter} wraps the
 * request so {@code HttpServletRequest.getUserPrincipal()}/{@code isUserInRole(...)} delegate to
 * Spring Security's {@code SecurityContextHolder}; nothing in core or the modules this integrates
 * with reads those servlet-API methods instead of {@code Context}, so the wrapper is pure
 * overhead.</li>
 * <li>{@code sessionManagement(disable)} - {@code SessionManagementFilter} exists to enforce
 * concurrent-session limits and session-fixation protection, both policy decisions
 * {@link OpenmrsSecurityContextFilter} does not make (and OpenMRS has never enforced); leaving it
 * enabled would add an unrelated, unconfigured session policy on top of the existing one.</li>
 * <li>{@code exceptionHandling(...)} keeps Spring's default behavior but swaps in
 * {@link OpenmrsAccessDeniedHandler}, so a denial reaching this chain publishes its
 * {@code AccessDeniedException} as a request attribute rather than discarding the reason. The same
 * handler is installed on {@link OpenmrsAuthorizationFilter}'s own
 * {@code ExceptionTranslationFilter}; this one covers what never gets that far, such as a filter
 * between the two failing before {@code openmrsAuthorizationFilter} is entered.</li>
 * <li>{@code authorizeHttpRequests(...)} is a blanket {@code permitAll()} here, deliberately - real
 * {@link AuthorizedUrlMatcher} enforcement happens later, in the standalone
 * {@link OpenmrsAuthorizationFilter} registered directly in {@code web.xml} <em>after</em>
 * {@code ModuleFilter}, not as part of this composite chain. {@code @EnableWebSecurity} offers no
 * way to place one of several {@code SecurityFilterChain} beans at a different point in the servlet
 * filter order - every one folds into this same {@code springSecurityFilterChain} seat, which runs
 * before {@code ModuleFilter}. That is wrong for URL rules specifically: some modules authenticate
 * a request from inside their own {@code ModuleFilter}-dispatched filter rather than an existing
 * session - webservices.rest's {@code AuthorizationFilter} and fhir2's {@code AuthenticationFilter}
 * both call {@code Context.authenticate(...)} against a Basic header, deliberately never failing
 * the request themselves ("fail-open at the filter, fail-closed at the service"). A rule enforced
 * here, before either ran, would deny that request before it had a chance to authenticate, valid
 * credentials or not. See {@link OpenmrsAuthorizationFilter}'s own javadoc for the full reasoning
 * and why {@link OpenmrsSecurityContextFilter} does not move along with the enforcement.</li>
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

	@Bean(name = "openmrsAuthorizationFilter")
	public OpenmrsAuthorizationFilter openmrsAuthorizationFilter(List<AuthorizedUrlMatchers> authorizedUrlMatchers) {
		return new OpenmrsAuthorizationFilter(authorizedUrlMatchers);
	}

	// Sonar (java:S4502) and CodeQL (java/spring-disabled-csrf-protection) both flag csrf(disable)
	// below as security-sensitive; see this class's javadoc for why CsrfGuardFilter remaining the
	// sole CSRF authority is deliberate, not an oversight.
	@SuppressWarnings("java:S4502")
	@Bean
	public SecurityFilterChain openmrsSecurityFilterChain(HttpSecurity http,
	        OpenmrsSecurityContextFilter openmrsSecurityContextFilter) {
		http.securityContext(AbstractHttpConfigurer::disable).csrf(AbstractHttpConfigurer::disable) // codeql[java/spring-disabled-csrf-protection]: CsrfGuardFilter (see class javadoc) is the sole CSRF authority; enabling this too would collide with it
		        .anonymous(AbstractHttpConfigurer::disable).headers(AbstractHttpConfigurer::disable)
		        .logout(AbstractHttpConfigurer::disable).requestCache(AbstractHttpConfigurer::disable)
		        .servletApi(AbstractHttpConfigurer::disable).sessionManagement(AbstractHttpConfigurer::disable)
		        .addFilterBefore(openmrsSecurityContextFilter, AuthorizationFilter.class)
		        .exceptionHandling(exceptions -> exceptions.accessDeniedHandler(new OpenmrsAccessDeniedHandler()))
		        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
		return http.build();
	}
}
