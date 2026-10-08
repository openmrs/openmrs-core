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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.cache.RolePrivilegeCache;
import org.openmrs.api.cache.RolePrivileges;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.security.PrivilegeNamingAuthorizationManager;
import org.openmrs.web.test.jupiter.BaseWebContextSensitiveTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Covers the {@link AuthorizedUrlMatcher.Rule} methods that resolve an OpenMRS privilege or role -
 * {@code hasAuthority}, {@code hasAnyAuthority}, {@code hasAllAuthorities}, {@code hasRole},
 * {@code hasAnyRole}, {@code hasAllRoles}. Since {@code OpenmrsAuthorizationManagerFactory} backs
 * all six, they answer from the current thread's {@code UserContext} rather than from
 * {@code Authentication#getAuthorities()}, so these cases need a real OpenMRS context - which is
 * why they live here rather than in the plain unit tests of {@link AuthorizedUrlMatcherTest}, where
 * the {@code Context}-free methods ({@code permitAll}, {@code denyAll}, {@code authenticated},
 * {@code access}, pattern matching) remain.
 * <p>
 * The privileges and roles named below are deliberately unregistered: a superuser holds them anyway
 * through {@code Context.hasPrivilege(String)} and {@code User#hasRole(String)}, which is exactly
 * the parity with {@code @Authorized} that the authority set could never express.
 */
public class AuthorizedUrlMatcherPrivilegeRuleTest extends BaseWebContextSensitiveTest {

	private static final String UNREGISTERED = "Url Rule Unregistered Privilege";

	private static final String OTHER_UNREGISTERED = "Url Rule Other Unregistered Privilege";

	private static final String ROLE = "Url Rule Role";

	private static final String OTHER_ROLE = "Url Rule Other Role";

	@Test
	public void hasAuthority_shouldGrantAnUnregisteredPrivilegeToASuperuser() {
		assertTrue(decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAuthority(UNREGISTERED)));
	}

	@Test
	public void hasAuthority_shouldDenyWhenLoggedOut() {
		Context.getUserContext().logout();

		assertFalse(decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAuthority(UNREGISTERED)));
	}

	@Test
	public void hasAuthority_shouldIgnoreTheAuthenticationsOwnAuthorities() {
		// the clearest statement of the change: the rule reads the UserContext, so an Authentication
		// carrying the authority is irrelevant, and so is one carrying none
		Context.getUserContext().logout();
		AuthorizedUrlMatcher rule = AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAuthority(UNREGISTERED);
		Authentication holdsItAsAnAuthority = new TestingAuthenticationToken("user", "pw", UNREGISTERED);

		assertFalse(
		    rule.getAuthorizationManager().authorize(() -> holdsItAsAnAuthority, contextFor("/admin/x")).isGranted());
	}

	@Test
	public void hasAuthority_shouldGrantViaAProxyPrivilegeWhenLoggedOut() {
		Context.getUserContext().logout();
		Context.addProxyPrivilege(UNREGISTERED);
		try {
			assertTrue(decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAuthority(UNREGISTERED)));
		} finally {
			Context.removeProxyPrivilege(UNREGISTERED);
		}
	}

	@Test
	public void hasAnyAuthority_shouldGrantWhenOnlyTheSecondIsHeld() {
		Context.getUserContext().logout();
		Context.addProxyPrivilege(OTHER_UNREGISTERED);
		try {
			assertTrue(
			    decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAnyAuthority(UNREGISTERED, OTHER_UNREGISTERED)));
		} finally {
			Context.removeProxyPrivilege(OTHER_UNREGISTERED);
		}
	}

	@Test
	public void hasAllAuthorities_shouldRequireEveryPrivilege() {
		Context.getUserContext().logout();
		Context.addProxyPrivilege(UNREGISTERED);
		try {
			assertFalse(decide(
			    AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAllAuthorities(UNREGISTERED, OTHER_UNREGISTERED)));
			assertTrue(decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAllAuthorities(UNREGISTERED)));
		} finally {
			Context.removeProxyPrivilege(UNREGISTERED);
		}
	}

	@Test
	public void hasAuthority_shouldMatchThePrivilegeCaseInsensitively() throws Exception {
		// the URL rules inherit this from Context.hasPrivilege(String) too, now that they no longer
		// compare authority strings. Role-derived privileges are the case-insensitive path; a proxy
		// privilege is matched exactly.
		Cache cache = Context.getRegisteredComponent("apiCacheManager", CacheManager.class)
		        .getCache(RolePrivilegeCache.CACHE_NAME);
		cache.put(RolePrivileges.normalize(ROLE),
		    new RolePrivileges(Collections.singleton(UNREGISTERED.toUpperCase()), false));
		try {
			runAs(userWithRoles(ROLE),
			    () -> assertTrue(decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAuthority(UNREGISTERED))));
		} finally {
			cache.clear();
		}
	}

	@Test
	public void hasRole_shouldGrantASuperuserARoleTheyDoNotHold() {
		assertTrue(decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasRole(ROLE)));
	}

	@Test
	public void hasRole_shouldAcceptEitherSpellingOfTheRoleName() throws Exception {
		runAs(userWithRoles(ROLE), () -> {
			assertTrue(decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasRole(ROLE)));
			assertTrue(decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasRole("ROLE_" + ROLE)));
		});
	}

	@Test
	public void hasRole_shouldDenyANonSuperuserWhoDoesNotHoldTheRole() throws Exception {
		runAs(userWithRoles(OTHER_ROLE),
		    () -> assertFalse(decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasRole(ROLE))));
	}

	@Test
	public void hasAnyRole_shouldGrantWhenOnlyTheSecondIsHeld() throws Exception {
		runAs(userWithRoles(OTHER_ROLE),
		    () -> assertTrue(decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAnyRole(ROLE, OTHER_ROLE))));
	}

	@Test
	public void hasAllRoles_shouldRequireEveryRole() throws Exception {
		runAs(userWithRoles(ROLE),
		    () -> assertFalse(decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAllRoles(ROLE, OTHER_ROLE))));
		runAs(userWithRoles(ROLE, OTHER_ROLE),
		    () -> assertTrue(decide(AuthorizedUrlMatcher.requestMatchers("/admin/**").hasAllRoles(ROLE, OTHER_ROLE))));
	}

	@Test
	public void builderHasAuthority_shouldProduceTheSameRuleAsTheMatcherDoes() {
		// a proxy privilege is the discriminator: it satisfies a privilege check and not a role check,
		// so this fails if RuleBuilder.hasAuthority ever delegates to the wrong Rule method
		AuthorizedUrlMatcher rule = AuthorizedUrlMatchers.builder().requestMatchers("/admin/**").hasAuthority(UNREGISTERED)
		        .build().getAuthorizedUrlMatchers().get(0);

		Context.getUserContext().logout();
		assertFalse(decide(rule));
		Context.addProxyPrivilege(UNREGISTERED);
		try {
			assertTrue(decide(rule));
		} finally {
			Context.removeProxyPrivilege(UNREGISTERED);
		}
	}

	@Test
	public void builderHasAnyRole_shouldProduceTheSameRuleAsTheMatcherDoes() throws Exception {
		AuthorizedUrlMatcher rule = AuthorizedUrlMatchers.builder().requestMatchers("/admin/**").hasAnyRole(ROLE, OTHER_ROLE)
		        .build().getAuthorizedUrlMatchers().get(0);

		runAs(userWithRoles(OTHER_ROLE), () -> assertTrue(decide(rule)));
		runAs(userWithRoles("Url Rule Unrelated Role"), () -> assertFalse(decide(rule)));
	}

	@Test
	public void deniedRule_shouldNameTheMissingPrivilege() {
		// the composition OpenmrsAuthorizationFilter builds: the rules manager wrapped so a denial
		// reports error.privilegesRequired rather than AuthorizationFilter's hard-coded "Access Denied".
		// The wrapper returns that exception as the result rather than throwing it, so method security
		// can route a denial through @HandleAuthorizationDenied - OpenmrsAuthorizationFilter's adapter
		// is what throws it (see deniedRequest_shouldPublishTheNamedDenialAsARequestAttribute, which
		// drives the real filter).
		Context.getUserContext().logout();
		AuthorizationManager<RequestAuthorizationContext> manager = new PrivilegeNamingAuthorizationManager<>(
		        new OpenmrsAuthorizationManager(List.of(
		            AuthorizedUrlMatchers.builder().requestMatchers("/admin/**").hasAuthority(UNREGISTERED).build())));

		AuthorizationResult result = manager.authorize(() -> null, contextFor("/admin/x"));

		assertFalse(result.isGranted());
		AccessDeniedException denial = assertInstanceOf(AccessDeniedException.class, result);
		assertEquals(expectedMessage(UNREGISTERED), denial.getMessage());
	}

	@Test
	public void deniedRequestFromAnAuthenticatedCaller_shouldPublishTheNamedDenialAsARequestAttribute() throws Exception {
		// the whole path end to end for a caller who has identified itself: rule denies ->
		// PrivilegeNamingAuthorizationManager names the privilege -> ExceptionTranslationFilter routes it
		// to OpenmrsAccessDeniedHandler as a 403 -> the exception is published for the error page
		OpenmrsAuthorizationFilter filter = new OpenmrsAuthorizationFilter(
		        List.of(AuthorizedUrlMatchers.builder().requestMatchers("/admin/**").hasAuthority(UNREGISTERED).build()));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/x");
		MockHttpServletResponse response = new MockHttpServletResponse();

		// a real, non-superuser principal, so the rule denies on the privilege rather than on identity
		runAs(userWithRoles(ROLE), () -> {
			try {
				filter.doFilter(request, response, mock(FilterChain.class));
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		});

		assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
		AccessDeniedException published = (AccessDeniedException) request.getAttribute(WebAttributes.ACCESS_DENIED_403);
		assertEquals(expectedMessage(UNREGISTERED), published.getMessage());
	}

	@Test
	public void deniedRequestFromAnAnonymousCaller_shouldChallengeWithoutPublishingTheDenial() throws Exception {
		// an anonymous denial goes to the authentication entry point instead, which never consults the
		// access-denied handler - so there is no ACCESS_DENIED_403 attribute, and the privilege name is
		// deliberately not disclosed to a caller who has not identified itself yet
		Context.getUserContext().logout();
		OpenmrsAuthorizationFilter filter = new OpenmrsAuthorizationFilter(
		        List.of(AuthorizedUrlMatchers.builder().requestMatchers("/admin/**").hasAuthority(UNREGISTERED).build()));
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/x");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, mock(FilterChain.class));

		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
		assertNull(request.getAttribute(WebAttributes.ACCESS_DENIED_403));
	}

	private static String expectedMessage(String privilege) {
		return Context.getMessageSourceService().getMessage("error.privilegesRequired", new Object[] { privilege },
		    Locale.getDefault());
	}

	private static boolean decide(AuthorizedUrlMatcher rule) {
		return rule.getRequestMatcher().matches(new MockHttpServletRequest("GET", "/admin/x"))
		        && rule.getAuthorizationManager().authorize(() -> null, contextFor("/admin/x")).isGranted();
	}

	private static RequestAuthorizationContext contextFor(String path) {
		return new RequestAuthorizationContext(new MockHttpServletRequest("GET", path));
	}

	private User userWithRoles(String... roleNames) {
		User user = new User();
		for (String roleName : roleNames) {
			user.addRole(new Role(roleName));
		}
		return user;
	}

	private void runAs(User user, Runnable action) throws IllegalAccessException {
		UserContext userContext = Context.getUserContext();
		User previous = userContext.getAuthenticatedUser();
		try {
			FieldUtils.getField(UserContext.class, "user", true).set(userContext, user);
			action.run();
		} finally {
			FieldUtils.getField(UserContext.class, "user", true).set(userContext, previous);
		}
	}
}
