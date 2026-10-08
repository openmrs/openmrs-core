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
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the aggregation logic itself: which rules match a given request, and how their
 * decisions combine. Rules here use trivial stub {@link AuthorizationManager}s (fixed
 * grant/deny/abstain) rather than real ones like {@code AuthorityAuthorizationManager}, since what
 * is under test is purely how {@link OpenmrsAuthorizationManager} finds and combines matching rules
 * - not any particular rule's own decision logic, which belongs to whatever
 * {@link AuthorizationManager} a rule is built from (see {@link AuthorizedUrlMatcherTest} for the
 * fluent-method side of that).
 */
class OpenmrsAuthorizationManagerTest {

	private static final Supplier<Authentication> NO_AUTHENTICATION = () -> null;

	private static final AuthorizationManager<RequestAuthorizationContext> GRANT = (auth,
	        ctx) -> new AuthorizationDecision(true);

	private static final AuthorizationManager<RequestAuthorizationContext> DENY = (auth,
	        ctx) -> new AuthorizationDecision(false);

	private static final AuthorizationManager<RequestAuthorizationContext> ABSTAIN = (auth, ctx) -> null;

	@Test
	void authorize_shouldGrantWhenNoRuleMatchesThePath() {
		OpenmrsAuthorizationManager manager = new OpenmrsAuthorizationManager(sourceOf(new RuleSpec("/admin/**", DENY)));

		assertTrue(manager.authorize(NO_AUTHENTICATION, contextFor("/unrelated/path")).isGranted());
	}

	@Test
	void authorize_shouldGrantWhenTheMatchingRuleGrants() {
		OpenmrsAuthorizationManager manager = new OpenmrsAuthorizationManager(sourceOf(new RuleSpec("/admin/**", GRANT)));

		assertTrue(manager.authorize(NO_AUTHENTICATION, contextFor("/admin/x")).isGranted());
	}

	@Test
	void authorize_shouldDenyWhenTheMatchingRuleDenies() {
		OpenmrsAuthorizationManager manager = new OpenmrsAuthorizationManager(sourceOf(new RuleSpec("/admin/**", DENY)));

		assertFalse(manager.authorize(NO_AUTHENTICATION, contextFor("/admin/x")).isGranted());
	}

	@Test
	void authorize_shouldGrantWhenTheMatchingRuleAbstains() {
		// an abstaining rule (null result) must not by itself deny the request, matching
		// AuthorizationManagers.allOf's own treatment of an abstaining delegate
		OpenmrsAuthorizationManager manager = new OpenmrsAuthorizationManager(sourceOf(new RuleSpec("/admin/**", ABSTAIN)));

		assertTrue(manager.authorize(NO_AUTHENTICATION, contextFor("/admin/x")).isGranted());
	}

	@Test
	void authorize_shouldDenyWhenAnyOfMultipleMatchingRulesDenies() {
		// two overlapping rules on the same path - e.g. two different modules - must both be
		// satisfied, never either/or
		OpenmrsAuthorizationManager manager = new OpenmrsAuthorizationManager(
		        sourceOf(new RuleSpec("/admin/**", GRANT), new RuleSpec("/admin/x/**", DENY)));

		assertFalse(manager.authorize(NO_AUTHENTICATION, contextFor("/admin/x/y")).isGranted());
	}

	@Test
	void authorize_shouldGrantWhenEveryMatchingRuleGrants() {
		OpenmrsAuthorizationManager manager = new OpenmrsAuthorizationManager(
		        sourceOf(new RuleSpec("/admin/**", GRANT), new RuleSpec("/admin/x/**", GRANT)));

		assertTrue(manager.authorize(NO_AUTHENTICATION, contextFor("/admin/x/y")).isGranted());
	}

	@Test
	void authorize_shouldCombineRulesFromMultipleSources() {
		// two different AuthorizedUrlMatchers beans - e.g. two different modules - contribute
		// separately but are combined into one flat rule set, same as two rules from a single source
		OpenmrsAuthorizationManager manager = new OpenmrsAuthorizationManager(
		        List.of(singleSource(new RuleSpec("/admin/**", GRANT)), singleSource(new RuleSpec("/admin/x/**", DENY))));

		assertFalse(manager.authorize(NO_AUTHENTICATION, contextFor("/admin/x/y")).isGranted());
	}

	@Test
	void authorize_shouldCollectModuleRuleBundlesTheSameWayWebSecurityConfigWould() {
		// mirrors how a module contributes rules now: one AuthorizedUrlMatchers bean bundling every
		// URL pattern it needs to protect - AuthorizedUrlMatcher itself is never registered as a bean
		AnnotationConfigApplicationContext springContext = new AnnotationConfigApplicationContext(ModuleConfig.class);
		try {
			List<AuthorizedUrlMatchers> sources = springContext.getBeanProvider(AuthorizedUrlMatchers.class).stream()
			        .toList();
			assertEquals(1, sources.size());

			OpenmrsAuthorizationManager manager = new OpenmrsAuthorizationManager(sources);

			assertFalse(manager.authorize(NO_AUTHENTICATION, contextFor("/module/x/y")).isGranted());
		} finally {
			springContext.close();
		}
	}

	@Configuration
	static class ModuleConfig {

		@Bean
		AuthorizedUrlMatchers moduleUrlRules() {
			return AuthorizedUrlMatchers.builder().requestMatchers("/module/x/**").access(DENY).build();
		}
	}

	private static List<AuthorizedUrlMatchers> sourceOf(RuleSpec... specs) {
		return List.of(singleSource(specs));
	}

	private static AuthorizedUrlMatchers singleSource(RuleSpec... specs) {
		AuthorizedUrlMatchers.Builder builder = AuthorizedUrlMatchers.builder();
		for (RuleSpec spec : specs) {
			builder.requestMatchers(spec.pattern()).access(spec.manager());
		}
		return builder.build();
	}

	private record RuleSpec(String pattern, AuthorizationManager<RequestAuthorizationContext> manager) {
	}

	private static RequestAuthorizationContext contextFor(String path) {
		return new RequestAuthorizationContext(new MockHttpServletRequest("GET", path));
	}
}
