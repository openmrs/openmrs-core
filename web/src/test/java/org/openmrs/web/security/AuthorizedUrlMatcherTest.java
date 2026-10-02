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

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link AuthorizedUrlMatcher} fluent methods that decide purely from the
 * {@link Authentication} handed to them - {@code permitAll()}, {@code denyAll()},
 * {@code authenticated()} and {@code access(...)} - plus the pattern matching of
 * {@link AuthorizedUrlMatcher#requestMatchers(String...)}.
 * <p>
 * {@code hasAuthority(...)}, {@code hasAnyAuthority(...)}, {@code hasAllAuthorities(...)},
 * {@code hasRole(...)}, {@code hasAnyRole(...)} and {@code hasAllRoles(...)} are covered by
 * {@link AuthorizedUrlMatcherPrivilegeRuleTest} instead: they answer from the current thread's
 * {@code UserContext} rather than the authority set, so they need a real OpenMRS context and cannot
 * be driven by a hand-built token here. See {@link OpenmrsAuthorizationManagerTest} for how
 * multiple rules combine, which is orthogonal to what a single rule's own decision is.
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

		Authentication authenticated = new TestingAuthenticationToken("user", "pw", "Anything");

		assertFalse(rule.getAuthorizationManager().authorize(() -> authenticated, contextFor("/locked/x")).isGranted());
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

	private static RequestAuthorizationContext contextFor(String path) {
		return new RequestAuthorizationContext(new MockHttpServletRequest("GET", path));
	}
}
