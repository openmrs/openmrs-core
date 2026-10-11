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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link AuthorizedUrlMatcher} fluent methods that decide purely from the
 * {@link Authentication} handed to them - {@code permitAll()}, {@code denyAll()},
 * {@code authenticated()} and {@code access(...)} - plus the pattern matching of
 * {@link AuthorizedUrlMatcher#requestMatchers(String...)} and
 * {@link AuthorizedUrlMatcher#equivalentPaths(String)}.
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

	/**
	 * Every path {@code ModuleServlet} reaches a servlet named {@code admin} by: it resolves on the
	 * servlet name alone, so the module id segment is optional and any loaded module's id works in it,
	 * and {@code web.xml} maps the servlet under {@code /moduleServlet/*} and {@code /ms/*} both.
	 * {@code /moduleServlet/mymodule/x/admin/y} is excluded because {@code ModuleServlet} reads
	 * {@code x} as the servlet name there, not {@code admin}.
	 */
	@ParameterizedTest
	@CsvSource({ "/moduleServlet/admin, true", "/moduleServlet/admin/y, true", "/moduleServlet/mymodule/admin/y, true",
	        "/moduleServlet/othermodule/admin/y, true", "/ms/admin, true", "/ms/admin/y, true", "/ms/mymodule/admin/y, true",
	        "/moduleServlet/mymodule/x/admin/y, false", "/moduleServlet/notadmin/y, false", "/admin/y, false" })
	void requestMatchers_shouldExpandAModuleServletPatternToEveryPathReachingTheServlet(String path, boolean expected) {
		AuthorizedUrlMatcher.Rule rule = AuthorizedUrlMatcher.requestMatchers("/ms/myModule/admin/**");

		assertEquals(expected, rule.matches(new MockHttpServletRequest("GET", path)));
	}

	@Test
	void requestMatchers_shouldExpandEitherModuleServletPrefixIdentically() {
		assertEquals(AuthorizedUrlMatcher.equivalentPaths("/ms/myModule/admin/**").toList(),
		    AuthorizedUrlMatcher.equivalentPaths("/moduleServlet/myModule/admin/**").toList());
	}

	@ParameterizedTest
	@CsvSource({ "/moduleServlet/admin/reports/q, true", "/ms/mymodule/admin/reports/q, true",
	        "/moduleServlet/admin/reports, true", "/moduleServlet/admin/other, false", "/moduleServlet/admin, false" })
	void requestMatchers_shouldExpandAPathWithinAModuleServletWithoutWideningIt(String path, boolean expected) {
		AuthorizedUrlMatcher.Rule rule = AuthorizedUrlMatcher.requestMatchers("/ms/myModule/admin/reports/**");

		assertEquals(expected, rule.matches(new MockHttpServletRequest("GET", path)));
	}

	/**
	 * The id-less form is the one that cannot be told from a module id, and the form a module's own
	 * links suggest guards one of the four paths - so it is rejected rather than half-expanded. Rules
	 * are built in {@code @Bean} methods, so this surfaces at startup.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "/ms/admin/**", "/moduleServlet/admin/**", "/ms/myModule", "/moduleServlet/myModule", "/ms/",
	        "/ms/myModule/*/x", "/ms/*/admin/**", "/ms/myModule/{servlet}/**" })
	void requestMatchers_shouldRejectAModuleServletPatternNotNamingBothIdAndServlet(String pattern) {
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
		    () -> AuthorizedUrlMatcher.requestMatchers(pattern));

		assertTrue(thrown.getMessage().contains(pattern), thrown.getMessage());
	}

	@Test
	void requestMatchers_shouldRejectABadModuleServletPatternAmongGoodOnes() {
		assertThrows(IllegalArgumentException.class,
		    () -> AuthorizedUrlMatcher.requestMatchers("/ms/myModule/admin/**", "/ms/reports/**"));
	}

	/**
	 * {@code web.xml} maps the {@code openmrs} {@code DispatcherServlet} at {@code /ws/*} as well as by
	 * extension, and MVC matches the path after {@code /ws} while a rule matches the path after the
	 * context path - so {@code /ws} in front of a guarded MVC path reaches the same controller.
	 */
	@ParameterizedTest
	@CsvSource({ "/module/myModule/config.form, true", "/ws/module/myModule/config.form, true",
	        "/module/myModule/other.form, false" })
	void requestMatchers_shouldExpandAnMvcPatternWithItsWsPrefixedTwin(String path, boolean expected) {
		AuthorizedUrlMatcher.Rule rule = AuthorizedUrlMatcher.requestMatchers("/module/myModule/config.form");

		assertEquals(expected, rule.matches(new MockHttpServletRequest("GET", path)));
	}

	/**
	 * Already under {@code /ws/}, so prefixing again would guard a path nothing serves; a pattern
	 * starting with {@code /**} already covers {@code /ws} and could not be prefixed anyway, since
	 * {@code **} cannot appear mid-pattern. Module prefixes are left alone too - {@code /ws} routes to
	 * the DispatcherServlet, which has no handler for them.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "/ws/rest/v1/myModule/**", "/ws", "/**", "/**/config.form" })
	void requestMatchers_shouldNotAddAWsPrefixWhereItWouldNotReachAnything(String pattern) {
		assertEquals(List.of(pattern), AuthorizedUrlMatcher.equivalentPaths(pattern).toList());
	}

	@Test
	void equivalentPaths_shouldExpandAModuleServletPatternToExactlyTheFourShapes() {
		assertEquals(List.of("/moduleServlet/admin/**", "/moduleServlet/*/admin/**", "/ms/admin/**", "/ms/*/admin/**"),
		    AuthorizedUrlMatcher.equivalentPaths("/ms/myModule/admin/**").toList());
	}

	/**
	 * {@code ModuleUtil.getModuleForPath} turns every slash into a dot before walking the id back, so
	 * each mix of dots and slashes reaches the module and {@code getPathForResource} strips the same
	 * length from all of them. The dotted spelling is the one that can be expanded, since nothing marks
	 * where the id ends in any spelling containing slashes.
	 */
	@ParameterizedTest
	@CsvSource({ "/moduleResources/webservices.rest/css/x.css, true", "/moduleResources/webservices/rest/css/x.css, true",
	        "/moduleResources/othermodule/css/x.css, false" })
	void requestMatchers_shouldExpandADottedModuleIdToItsSlashedSpelling(String path, boolean expected) {
		AuthorizedUrlMatcher.Rule rule = AuthorizedUrlMatcher.requestMatchers("/moduleResources/webservices.rest/**");

		assertEquals(expected, rule.matches(new MockHttpServletRequest("GET", path)));
	}

	@Test
	void equivalentPaths_shouldExpandATwoDotModuleIdToEveryMixOfDotsAndSlashes() {
		assertEquals(List.of("/moduleResources/x.y.z/**", "/moduleResources/x.y/z/**", "/moduleResources/x/y.z/**",
		    "/moduleResources/x/y/z/**"), AuthorizedUrlMatcher.equivalentPaths("/moduleResources/x.y.z/**").toList());
	}

	@ParameterizedTest
	@ValueSource(strings = { "/moduleResources/x.y.z/secret.txt", "/moduleResources/x.y/z/secret.txt",
	        "/moduleResources/x/y.z/secret.txt", "/moduleResources/x/y/z/secret.txt" })
	void requestMatchers_shouldGuardEveryMixedSpellingOfATwoDotModuleId(String path) {
		AuthorizedUrlMatcher.Rule rule = AuthorizedUrlMatcher.requestMatchers("/moduleResources/x.y.z/**");

		assertTrue(rule.matches(new MockHttpServletRequest("GET", path)));
	}

	@Test
	void requestMatchers_shouldLeaveAnUndottedModuleResourcesPatternAlone() {
		assertEquals(List.of("/moduleResources/legacyui/**"),
		    AuthorizedUrlMatcher.equivalentPaths("/moduleResources/legacyui/**").toList());
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
