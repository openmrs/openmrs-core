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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AuthorizedUrlMatchers#builder()}: that chaining
 * {@code requestMatchers(...)} with a named check accumulates one rule per call, that each named
 * check delegates to the same {@link AuthorizedUrlMatcher.Rule} logic
 * {@link AuthorizedUrlMatcherTest} already covers, and that {@code build()} freezes whatever was
 * added up to that point.
 */
class AuthorizedUrlMatchersTest {

	@Test
	void builder_shouldAccumulateOneRulePerRequestMatchersCall() {
		AuthorizedUrlMatchers matchers = AuthorizedUrlMatchers.builder().requestMatchers("/admin/**")
		        .hasAuthority("Manage Something").requestMatchers("/reports/**").hasAnyRole("Doctor", "Nurse").build();

		assertEquals(2, matchers.getAuthorizedUrlMatchers().size());
	}

	@Test
	void builder_shouldKeepEachRuleScopedToItsOwnPattern() {
		AuthorizedUrlMatchers matchers = AuthorizedUrlMatchers.builder().requestMatchers("/admin/**")
		        .hasAuthority("Manage Something").requestMatchers("/reports/**").hasAnyRole("Doctor", "Nurse").build();

		AuthorizedUrlMatcher adminRule = matchers.getAuthorizedUrlMatchers().get(0);
		AuthorizedUrlMatcher reportsRule = matchers.getAuthorizedUrlMatchers().get(1);

		assertTrue(adminRule.getRequestMatcher().matches(new MockHttpServletRequest("GET", "/admin/x")));
		assertFalse(adminRule.getRequestMatcher().matches(new MockHttpServletRequest("GET", "/reports/x")));
		assertTrue(reportsRule.getRequestMatcher().matches(new MockHttpServletRequest("GET", "/reports/x")));
		assertFalse(reportsRule.getRequestMatcher().matches(new MockHttpServletRequest("GET", "/admin/x")));
	}

	@Test
	void builder_shouldAccumulateOneRuleCoveringEveryPathToAModuleServlet() {
		AuthorizedUrlMatchers matchers = AuthorizedUrlMatchers.builder().requestMatchers("/ms/myModule/admin/**")
		        .hasAuthority("Manage Something").build();

		assertEquals(1, matchers.getAuthorizedUrlMatchers().size());
		AuthorizedUrlMatcher rule = matchers.getAuthorizedUrlMatchers().get(0);
		// the shape a module's own links use, plus the three it would otherwise be reachable around at -
		// see AuthorizedUrlMatcher#equivalentPaths
		assertTrue(rule.getRequestMatcher().matches(new MockHttpServletRequest("GET", "/moduleServlet/mymodule/admin/x")));
		assertTrue(rule.getRequestMatcher().matches(new MockHttpServletRequest("GET", "/moduleServlet/admin/x")));
		assertTrue(rule.getRequestMatcher().matches(new MockHttpServletRequest("GET", "/ms/mymodule/admin/x")));
		assertTrue(rule.getRequestMatcher().matches(new MockHttpServletRequest("GET", "/ms/admin/x")));
		assertFalse(rule.getRequestMatcher().matches(new MockHttpServletRequest("GET", "/moduleServlet/other/x")));
	}

	@Test
	void builder_build_shouldNotSeeRulesAddedAfterwards() {
		AuthorizedUrlMatchers.Builder builder = AuthorizedUrlMatchers.builder();
		builder.requestMatchers("/admin/**").hasAuthority("Manage Something");
		AuthorizedUrlMatchers matchers = builder.build();
		builder.requestMatchers("/reports/**").hasAnyRole("Doctor", "Nurse");

		assertEquals(1, matchers.getAuthorizedUrlMatchers().size());
	}
}
