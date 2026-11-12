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

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link OpenmrsAuthorizationFilter} itself - that it reads the
 * {@link org.springframework.security.core.Authentication} live from {@link SecurityContextHolder}
 * at invocation time (rather than some earlier snapshot) and turns an
 * {@link OpenmrsAuthorizationManager} decision into the expected HTTP response. The rule-matching
 * and rule-combination logic this delegates to is already covered by
 * {@link OpenmrsAuthorizationManagerTest}; {@link WebSecurityConfigIntegrationTest} covers this
 * filter running back-to-back with {@code springSecurityFilterChain} the way {@code web.xml} runs
 * them.
 */
class OpenmrsAuthorizationFilterTest {

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void doFilter_shouldPermitWhenNoRuleMatchesTheRequest() throws Exception {
		OpenmrsAuthorizationFilter filter = new OpenmrsAuthorizationFilter(List.of());
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/anything");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}

	@Test
	void doFilter_shouldRespondForbiddenWhenAMatchingRuleDenies() throws Exception {
		AuthorizedUrlMatchers denyAdmin = AuthorizedUrlMatchers.builder().requestMatchers("/admin/**").denyAll().build();
		OpenmrsAuthorizationFilter filter = new OpenmrsAuthorizationFilter(List.of(denyAdmin));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/x");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		filter.doFilter(request, response, chain);

		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
	}

	@Test
	void doFilter_shouldPublishTheDenialAsARequestAttribute() throws Exception {
		// OpenmrsAccessDeniedHandler replaces Spring's default, which would answer 403 without ever
		// looking at the exception - so whatever renders the 403 can report the reason
		AuthorizedUrlMatchers denyAdmin = AuthorizedUrlMatchers.builder().requestMatchers("/admin/**").denyAll().build();
		OpenmrsAuthorizationFilter filter = new OpenmrsAuthorizationFilter(List.of(denyAdmin));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/x");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, mock(FilterChain.class));

		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		assertInstanceOf(AccessDeniedException.class, request.getAttribute(WebAttributes.ACCESS_DENIED_403));
	}

	@Test
	void doFilter_shouldRespondForbiddenRatherThanUnauthorizedForAnAuthenticatedCaller() throws Exception {
		// the counterpart of filterChain_shouldChallengeAnAnonymousRequestToAnAuthenticatedOnlyUrl: once
		// the caller has identified itself, a rule denial is a real authorization failure, so it stays a
		// 403 rather than inviting credentials again
		AuthorizedUrlMatchers denyAdmin = AuthorizedUrlMatchers.builder().requestMatchers("/admin/**").denyAll().build();
		OpenmrsAuthorizationFilter filter = new OpenmrsAuthorizationFilter(List.of(denyAdmin));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/x");
		MockHttpServletResponse response = new MockHttpServletResponse();

		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("admin", "n/a", "ROLE_USER"));

		filter.doFilter(request, response, mock(FilterChain.class));

		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
	}

	@Test
	void doFilter_shouldUseTheAuthenticationCurrentlyInTheSecurityContext() throws Exception {
		// proves this filter reads SecurityContextHolder live at invocation time - the property
		// ModuleFilter-dispatched, Basic-auth-driven module filters (webservices.rest,
		// fhir2) depend on when this filter runs after them
		AuthorizedUrlMatchers requireAuthentication = AuthorizedUrlMatchers.builder().requestMatchers("/admin/**")
		        .authenticated().build();
		OpenmrsAuthorizationFilter filter = new OpenmrsAuthorizationFilter(List.of(requireAuthentication));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/x");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain chain = mock(FilterChain.class);

		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("admin", "n/a", "ROLE_USER"));

		filter.doFilter(request, response, chain);

		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}
}
