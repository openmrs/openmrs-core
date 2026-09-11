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

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AuthorizedUrlMatcher}'s fluent methods themselves - {@code permitAll()},
 * {@code denyAll()}, {@code hasAuthority(...)}, {@code hasAnyAuthority(...)},
 * {@code hasAllAuthorities(...)}, {@code hasRole(...)}, {@code hasAnyRole(...)},
 * {@code hasAllRoles(...)}, {@code authenticated()}, {@code fullyAuthenticated()},
 * {@code rememberMe()}, and {@code anonymous()}, mirroring every terminal method Spring's own
 * {@code AuthorizeHttpRequestsConfigurer.AuthorizedUrl} offers via {@code requestMatchers(...)} -
 * each wired up to the Spring Security {@link AuthorizationManager} it claims to. Also covers that
 * {@link AuthorizedUrlMatcher#requestMatchers(String...)} matches any one of several patterns. See
 * {@link OpenmrsAuthorizationManagerTest} for how multiple rules combine, which is orthogonal to
 * what a single rule's own decision is.
 */
class AuthorizedUrlMatcherTest {

	@Test
	void permitAll_shouldGrantEvenWithoutAuthentication() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/public/**").permitAll();

		assertTrue(rule.getAuthorizationManager().authorize(() -> null, contextFor("/public/x")).isGranted());
	}

	@Test
	void denyAll_shouldDenyEvenWhenAuthenticated() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/locked/**").denyAll();

		assertFalse(decide(rule, "/locked/x", "Anything"));
	}

	@Test
	void hasAuthority_shouldGrantWhenTheAuthorityIsHeld() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAuthority("Manage Something");

		assertTrue(decide(rule, "/admin/x", "Manage Something"));
	}

	@Test
	void hasAuthority_shouldDenyWhenTheAuthorityIsNotHeld() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAuthority("Manage Something");

		assertFalse(decide(rule, "/admin/x", "Something Else"));
	}

	@Test
	void hasAnyAuthority_shouldGrantWhenAtLeastOneIsHeld() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAnyAuthority("A", "B");

		assertTrue(decide(rule, "/admin/x", "B"));
	}

	@Test
	void hasAnyAuthority_shouldDenyWhenNoneAreHeld() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAnyAuthority("A", "B");

		assertFalse(decide(rule, "/admin/x", "C"));
	}

	@Test
	void hasAllAuthorities_shouldGrantOnlyWhenEveryOneIsHeld() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAllAuthorities("A", "B");

		assertFalse(decide(rule, "/admin/x", "A"));
		assertTrue(decide(rule, "/admin/x", "A", "B"));
	}

	@Test
	void hasRole_shouldCheckForTheRolePrefixedAuthorityNotTheBareRoleName() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").hasRole("Nurse");

		assertFalse(decide(rule, "/admin/x", "Nurse"));
		assertTrue(decide(rule, "/admin/x", "ROLE_Nurse"));
	}

	@Test
	void hasAnyRole_shouldGrantWhenAtLeastOneIsHeld() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAnyRole("Doctor", "Nurse");

		assertTrue(decide(rule, "/admin/x", "ROLE_Nurse"));
		assertFalse(decide(rule, "/admin/x", "ROLE_Clerk"));
	}

	@Test
	void hasAllRoles_shouldGrantOnlyWhenEveryOneIsHeld() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAllRoles("Doctor", "Nurse");

		assertFalse(decide(rule, "/admin/x", "ROLE_Doctor"));
		assertTrue(decide(rule, "/admin/x", "ROLE_Doctor", "ROLE_Nurse"));
	}

	@Test
	void authenticated_shouldGrantOnlyForAnAuthenticatedPrincipal() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").authenticated();

		Authentication authenticated = new TestingAuthenticationToken("user", "pw", "Irrelevant Authority");
		Authentication notAuthenticated = new TestingAuthenticationToken("user", "pw");

		assertTrue(rule.getAuthorizationManager().authorize(() -> authenticated, contextFor("/admin/x")).isGranted());
		assertFalse(rule.getAuthorizationManager().authorize(() -> notAuthenticated, contextFor("/admin/x")).isGranted());
	}

	@Test
	void fullyAuthenticated_shouldGrantForAnOrdinaryAuthenticatedPrincipal() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").fullyAuthenticated();

		Authentication authenticated = new TestingAuthenticationToken("user", "pw", "Irrelevant Authority");

		assertTrue(rule.getAuthorizationManager().authorize(() -> authenticated, contextFor("/admin/x")).isGranted());
	}

	@Test
	void fullyAuthenticated_shouldDenyARememberedPrincipal() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").fullyAuthenticated();

		Authentication remembered = new RememberMeAuthenticationToken("key", "user",
		        List.of(new SimpleGrantedAuthority("Irrelevant Authority")));

		assertFalse(rule.getAuthorizationManager().authorize(() -> remembered, contextFor("/admin/x")).isGranted());
	}

	@Test
	void rememberMe_shouldGrantOnlyForARememberedPrincipal() {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").rememberMe();

		Authentication remembered = new RememberMeAuthenticationToken("key", "user",
		        List.of(new SimpleGrantedAuthority("Irrelevant Authority")));
		Authentication authenticated = new TestingAuthenticationToken("user", "pw", "Irrelevant Authority");

		assertTrue(rule.getAuthorizationManager().authorize(() -> remembered, contextFor("/admin/x")).isGranted());
		assertFalse(rule.getAuthorizationManager().authorize(() -> authenticated, contextFor("/admin/x")).isGranted());
	}

	@Test
	void anonymous_shouldGrantOnlyForTheAnonymousAuthenticationToken() {
		// "anonymous" is a specific token type Spring's AnonymousAuthenticationFilter installs, not merely
		// "not authenticated" - an ordinary unauthenticated token (e.g. failed login) does not qualify
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/public/**").anonymous();

		Authentication authenticated = new TestingAuthenticationToken("user", "pw", "Irrelevant Authority");
		Authentication anonymous = new AnonymousAuthenticationToken("key", "anonymousUser",
		        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

		assertFalse(rule.getAuthorizationManager().authorize(() -> authenticated, contextFor("/public/x")).isGranted());
		assertTrue(rule.getAuthorizationManager().authorize(() -> anonymous, contextFor("/public/x")).isGranted());
	}

	@Test
	void requestMatchers_shouldMatchAnyOfMultiplePatterns() {
		AuthorizedUrlMatcher.Rule rule = AuthorizedUrlMatcher.requestMatchers("/a/**", "/b/**");

		assertTrue(rule.matches(new MockHttpServletRequest("GET", "/a/1")));
		assertTrue(rule.matches(new MockHttpServletRequest("GET", "/b/1")));
		assertFalse(rule.matches(new MockHttpServletRequest("GET", "/c/1")));
	}

	@Test
	void access_shouldUseTheSuppliedManagerDirectly() {
		AuthorizationManager<RequestAuthorizationContext> custom = (auth, ctx) -> new AuthorizationDecision(false);

		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").access(custom);

		assertFalse(rule.getAuthorizationManager().authorize(() -> null, contextFor("/admin/x")).isGranted());
	}

	private static boolean decide(AuthorizedUrlMatcher rule, String path, String... authorities) {
		Authentication authentication = new TestingAuthenticationToken("user", "pw", authorities);
		return rule.getRequestMatcher().matches(new MockHttpServletRequest("GET", path))
		        && rule.getAuthorizationManager().authorize(() -> authentication, contextFor(path)).isGranted();
	}

	private static RequestAuthorizationContext contextFor(String path) {
		return new RequestAuthorizationContext(new MockHttpServletRequest("GET", path));
	}
}
