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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end proof that {@link WebSecurityConfig}'s assembled {@code SecurityFilterChain} behaves
 * as designed when a real {@code HttpServletRequest} runs through it. Every other test in this
 * package checks one piece in isolation - {@link OpenmrsSecurityContextFilterTest} the filter
 * alone, {@link AuthorizedUrlMatcherTest}/{@link AuthorizedUrlMatchersTest} the rule-building
 * logic, {@link OpenmrsAuthorizationManagerTest} the aggregation logic,
 * {@link WebSecurityConfigTest} only that the {@code springSecurityFilterChain} bean exists and is
 * a {@link Filter} - but none of them dispatch an actual request through the assembled chain the
 * way a servlet container would, so a bug in filter ordering or wiring (e.g.
 * {@link OpenmrsSecurityContextFilter} not actually running before
 * {@code authorizeHttpRequests(...)}) could pass every one of those tests while still being broken
 * at runtime. This test loads the real {@link WebSecurityConfig}, adds one throwaway
 * {@link AuthorizedUrlMatchers} rule the same way a module would, and drives requests through the
 * resulting filter via {@link MockMvc} - no {@code spring-security-test} dependency needed, since
 * {@code springSecurityFilterChain} is just a plain {@link Filter} bean once
 * {@code @EnableWebSecurity} has built it (see {@link WebSecurityConfigTest}'s own javadoc for why
 * that bean name matters).
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

	private MockMvc mockMvc() {
		springContext = new AnnotationConfigApplicationContext(WebSecurityConfig.class, TestUrlRulesConfig.class);
		Filter springSecurityFilterChain = springContext.getBean("springSecurityFilterChain", Filter.class);

		return MockMvcBuilders.standaloneSetup(new TestController()).addFilters(springSecurityFilterChain).build();
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
	}
}
