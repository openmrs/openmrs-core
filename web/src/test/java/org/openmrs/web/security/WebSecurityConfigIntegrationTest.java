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

import jakarta.servlet.Filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end proof that {@link WebSecurityConfig}'s {@code springSecurityFilterChain} and
 * {@link OpenmrsAuthorizationFilter}, run back-to-back the way {@code web.xml} runs them (with
 * {@code ModuleFilter} sitting between the two in a real deployment), behave as designed against a
 * real {@code HttpServletRequest}. Every other test in this package checks one piece in isolation -
 * {@link OpenmrsSecurityContextFilterTest} the context-installing filter alone,
 * {@link AuthorizedUrlMatcherTest}/{@link AuthorizedUrlMatchersTest} the rule-building logic,
 * {@link OpenmrsAuthorizationManagerTest} the aggregation logic, {@link WebSecurityConfigTest} only
 * that the {@code springSecurityFilterChain} bean exists and is a {@link Filter} - but none of them
 * dispatch an actual request through both filters together, so a bug in how they combine could pass
 * every one of those tests while still being broken at runtime. This test loads the real
 * {@link WebSecurityConfig}, adds one throwaway {@link AuthorizedUrlMatchers} rule the same way a
 * module would, and drives requests through both resulting filters via {@link MockMvc} - no
 * {@code spring-security-test} dependency needed, since {@code springSecurityFilterChain} is just a
 * plain {@link Filter} bean once {@code @EnableWebSecurity} has built it (see
 * {@link WebSecurityConfigTest}'s own javadoc for why that bean name matters).
 */
class WebSecurityConfigIntegrationTest {

	private AnnotationConfigApplicationContext springContext;

	@AfterEach
	void closeContext() {
		if (springContext != null) {
			springContext.close();
		}
	}

	@Test
	void filterChain_shouldDenyAnAnonymousRequestToAnAuthenticatedOnlyUrl() throws Exception {
		mockMvc().perform(get("/admin/x")).andExpect(status().isForbidden());
	}

	@Test
	void filterChain_shouldPermitAnAnonymousRequestToAnUnprotectedUrl() throws Exception {
		// no AuthorizedUrlMatchers rule covers this path - a request must still reach the controller,
		// proving the whole chain (both filters, then dispatch) runs cleanly when nothing denies it
		mockMvc().perform(get("/public/x")).andExpect(status().isOk());
	}

	@Test
	void filterChain_shouldAuthorizeUsingAuthenticationEstablishedBetweenTheTwoFilters() throws Exception {
		// Stands in for ModuleFilter, which in the real web.xml chain sits between
		// springSecurityFilterChain and openmrsAuthorizationFilter - and for a module's own
		// ModuleFilter-dispatched filter authenticating the request itself (webservices.rest's
		// AuthorizationFilter, fhir2's AuthenticationFilter both call Context.authenticate(...)
		// against a Basic header). openmrsAuthorizationFilter must see this authentication, not the
		// unauthenticated one springSecurityFilterChain installed before this ran.
		Filter simulatedModuleFilterLogin = (request, response, chain) -> {
			SecurityContextHolder.getContext()
			        .setAuthentication(new TestingAuthenticationToken("admin", "n/a", "ROLE_USER"));
			try {
				chain.doFilter(request, response);
			} finally {
				SecurityContextHolder.clearContext();
			}
		};

		mockMvc(simulatedModuleFilterLogin).perform(get("/admin/x")).andExpect(status().isOk());
	}

	@Test
	void filterChain_shouldPublishADenialReachingTheOuterChainAsARequestAttribute() throws Exception {
		// a filter sitting between the two seats fails before openmrsAuthorizationFilter is entered, so
		// this chain's own ExceptionTranslationFilter handles it - OpenmrsAccessDeniedHandler is wired
		// there too, so the reason survives here as well
		AccessDeniedException denial = new AccessDeniedException("Privileges required: Manage Something");
		Filter failsBeforeAuthorization = (request, response, chain) -> {
			throw denial;
		};

		MvcResult result = mockMvc(failsBeforeAuthorization).perform(get("/public/x")).andExpect(status().isForbidden())
		        .andReturn();

		assertSame(denial, result.getRequest().getAttribute(WebAttributes.ACCESS_DENIED_403));
	}

	@Test
	void filterChain_shouldNotAddSecurityHeadersToTheResponse() throws Exception {
		// headers(disable): left at their defaults, HeaderWriterFilter would add X-Frame-Options: DENY
		// (breaking same-origin framing modules like htmlformentryui rely on) and a no-cache
		// Cache-Control (defeating ModuleResourcesServlet's own caching) to every response
		mockMvc().perform(get("/public/x")).andExpect(header().doesNotExist("X-Frame-Options"))
		        .andExpect(header().doesNotExist("Cache-Control"));
	}

	@Test
	void filterChain_shouldLetALogoutRequestReachTheApplication() throws Exception {
		// logout(disable): left enabled, Spring's own LogoutFilter would intercept this request,
		// invalidate the session, and redirect to /login?logout before it ever reached the
		// application's own /logout handling
		mockMvc().perform(get("/logout")).andExpect(status().isOk());
	}

	private MockMvc mockMvc(Filter... between) {
		springContext = new AnnotationConfigApplicationContext(WebSecurityConfig.class, TestUrlRulesConfig.class);
		Filter springSecurityFilterChain = springContext.getBean("springSecurityFilterChain", Filter.class);
		Filter openmrsAuthorizationFilter = springContext.getBean("openmrsAuthorizationFilter", Filter.class);

		Filter[] chain = new Filter[2 + between.length];
		chain[0] = springSecurityFilterChain;
		System.arraycopy(between, 0, chain, 1, between.length);
		chain[chain.length - 1] = openmrsAuthorizationFilter;

		return MockMvcBuilders.standaloneSetup(new TestController()).addFilters(chain).build();
	}

	@Configuration
	static class TestUrlRulesConfig {

		@Bean
		AuthorizedUrlMatchers testUrlRules() {
			return AuthorizedUrlMatchers.builder().requestMatchers("/admin/**").authenticated().build();
		}
	}

	@RestController
	static class TestController {

		@GetMapping("/admin/x")
		String admin() {
			return "admin";
		}

		@GetMapping("/public/x")
		String publicEndpoint() {
			return "public";
		}

		@GetMapping("/logout")
		String logout() {
			return "logout";
		}
	}
}
