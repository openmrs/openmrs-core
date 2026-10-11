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

import java.net.URI;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
 * what {@link WebSecurityConfig} does and does not publish as beans - but none of them dispatch an
 * actual request through both filters together, so a bug in how they combine could pass every one
 * of those tests while still being broken at runtime. This test loads the real
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
		// a test whose between-filter throws cannot clear the holder itself, and MockMvc runs every
		// request on this thread
		SecurityContextHolder.clearContext();
		if (springContext != null) {
			springContext.close();
		}
	}

	@Test
	void filterChain_shouldChallengeAnAnonymousRequestToAnAuthenticatedOnlyUrl() throws Exception {
		// 401, not 403: a caller with no session has not failed an authorization check so much as not
		// identified itself yet, and the same privilege on a service method answers 401 through
		// webservices.rest or redirects to login under legacyui. OpenmrsAuthenticationTrustResolver is
		// what makes ExceptionTranslationFilter see it as anonymous - Spring's own resolver decides by
		// token class and OpenMRS always installs an OpenmrsAuthenticationToken.
		mockMvc().perform(get("/admin/x")).andExpect(status().isUnauthorized());
	}

	@Test
	void filterChain_shouldChallengeTheWsPrefixedTwinOfAnAuthenticatedOnlyUrl() throws Exception {
		// web.xml maps the openmrs DispatcherServlet at /ws/* as well as by extension, and MVC matches
		// the path after /ws while a rule matches the path after the context path - so without the
		// /ws-prefixed twin equivalentPaths adds, /ws in front of a guarded path reached the same
		// controller unguarded. The rule here names only "/admin/**"
		mockMvc().perform(get("/ws/admin/x")).andExpect(status().isUnauthorized());
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
	void filterChain_shouldChallengeAnAnonymousDenialReachingTheOuterChain() throws Exception {
		// the outer seat's counterpart of filterChain_shouldChallengeAnAnonymousRequestToAnAuthenticatedOnlyUrl:
		// a caller who has not identified itself gets 401 wherever the denial is handled, not 403 from one
		// seat and 401 from the other. This only works because openmrsSecurityContextFilter runs ahead of
		// this chain's ExceptionTranslationFilter - its finally block calls Context.clearUserContext(),
		// which clears SecurityContextHolder, so handling the denial any later would leave the filter
		// unable to tell an anonymous caller from an authenticated one
		Filter failsBeforeAuthorization = (request, response, chain) -> {
			throw new AccessDeniedException("denied");
		};

		mockMvc(failsBeforeAuthorization).perform(get("/public/x")).andExpect(status().isUnauthorized());
	}

	@Test
	void filterChain_shouldPublishADenialReachingTheOuterChainAsARequestAttribute() throws Exception {
		// a filter sitting between the two seats fails before openmrsAuthorizationFilter is entered, so
		// this chain's own ExceptionTranslationFilter handles it - OpenmrsAccessDeniedHandler is wired
		// there too, so the reason survives here as well. The caller is authenticated, which is what
		// keeps this on the 403 path at all: an anonymous one is answered by the entry point instead,
		// and that never consults the access-denied handler (see
		// filterChain_shouldChallengeAnAnonymousDenialReachingTheOuterChain)
		AccessDeniedException denial = new AccessDeniedException("Privileges required: Manage Something");
		Filter failsBeforeAuthorization = (request, response, chain) -> {
			SecurityContextHolder.getContext()
			        .setAuthentication(new TestingAuthenticationToken("admin", "n/a", "ROLE_USER"));
			throw denial;
		};

		MvcResult result = mockMvc(failsBeforeAuthorization).perform(get("/public/x")).andExpect(status().isForbidden())
		        .andReturn();

		assertSame(denial, result.getRequest().getAttribute(WebAttributes.ACCESS_DENIED_403));
	}

	/**
	 * Each shape is one the container cleans up before choosing a servlet while a URL rule matches the
	 * raw request URI - so accepting them is what would let a rule be walked around. The two path
	 * parameters go together: the relaxation that accepted {@code ;jsessionid=} accepted {@code ..;}
	 * with it, and {@code web.xml} asks for cookie-only session tracking so the container never appends
	 * {@code ;jsessionid=}, leaving that shape no legitimate source.
	 * <p>
	 * Sent through the real chain rather than at a hand-built {@code StrictHttpFirewall}, so any route
	 * to a relaxation turns this red - including a {@code WebSecurityCustomizer} calling
	 * {@code web.httpFirewall(...)}, which replaces the strict default while publishing no
	 * {@link org.springframework.security.web.firewall.HttpFirewall} bean. {@code FilterChainProxy}
	 * hands the rejection to its {@code HttpStatusRequestRejectedHandler}, so the caller sees 400
	 * rather than an exception.
	 * <p>
	 * Built from a {@link URI} rather than a String, which is what lets {@code //} pin anything:
	 * {@code get(String)} runs the value through {@code UriComponentsBuilder}, which collapses the
	 * double slash, so the request arrives as {@code /public/admin/x} and reads the same under any
	 * firewall. {@code %2E%2E} is still left out - it comes back 400 through MockMvc whichever firewall
	 * is in place.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "/public/x;jsessionid=ABC123", "/public/x/..;/admin/x", "/public//admin/x" })
	void filterChain_shouldRejectAUrlShapeThatCouldWalkAroundAUrlRule(String requestUri) throws Exception {
		mockMvc().perform(get(URI.create(requestUri))).andExpect(status().isBadRequest());
	}

	@Test
	void filterChain_shouldNotSaveARequestChallengedByTheAuthorizationFilterIntoTheSession() throws Exception {
		// ExceptionTranslationFilter saves the request before handing it to the entry point, and its
		// single-argument constructor would default to HttpSessionRequestCache. Replaying a saved request
		// is RequestCacheAwareFilter's job and that filter is disabled, so an entry here is pure session
		// growth - one per challenged request, each holding a full DefaultSavedRequest
		MvcResult result = mockMvc().perform(get("/admin/x")).andExpect(status().isUnauthorized()).andReturn();

		assertNull(savedRequestIn(result));
	}

	@Test
	void filterChain_shouldNotSaveARequestChallengedByTheOuterChainIntoTheSession() throws Exception {
		// the same for this chain's own ExceptionTranslationFilter, which holds on its own: requestCache's
		// disable() publishes a NullRequestCache as the shared object ExceptionHandlingConfigurer then
		// hands that filter, so nothing here had to be configured for it. This pins that, since the only
		// thing keeping it true is the requestCache(disable) above
		Filter failsBeforeAuthorization = (request, response, chain) -> {
			throw new AccessDeniedException("denied");
		};

		MvcResult result = mockMvc(failsBeforeAuthorization).perform(get("/public/x")).andExpect(status().isUnauthorized())
		        .andReturn();

		assertNull(savedRequestIn(result));
	}

	private Object savedRequestIn(MvcResult result) {
		HttpSession session = result.getRequest().getSession(false);
		// the attribute HttpSessionRequestCache uses; it is package-private on that class
		return (session == null) ? null : session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
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

	@Test
	void contextRefresh_shouldFailWhenAModuleServletRuleDoesNotNameBothIdAndServlet() {
		// the point of rejecting the pattern rather than half-expanding it: a module's rules are built in
		// @Bean methods, so a pattern that would have guarded one of four paths stops the server coming up
		// instead of looking like a working rule
		BeanCreationException thrown = assertThrows(BeanCreationException.class,
		    () -> new AnnotationConfigApplicationContext(WebSecurityConfig.class, BadUrlRulesConfig.class).close());

		assertInstanceOf(IllegalArgumentException.class, thrown.getRootCause());
		assertTrue(thrown.getRootCause().getMessage().contains("/ms/admin/**"), thrown.getRootCause().getMessage());
	}

	/**
	 * Deliberately not {@code @Configuration}: these live in {@code org.openmrs.web.security}, which
	 * {@code applicationContext-service.xml} component-scans, so a stereotype here would register the
	 * bean in every context-sensitive test in the module - and a throwing {@code @Bean} would fail all
	 * of them. {@code @Bean} methods are still honoured when the class is registered explicitly, as
	 * both of these are.
	 */
	static class BadUrlRulesConfig {

		@Bean
		AuthorizedUrlMatchers badUrlRules() {
			// no module id, so which segment is the servlet name cannot be known
			return AuthorizedUrlMatchers.builder().requestMatchers("/ms/admin/**").authenticated().build();
		}
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
