/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.security;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.annotation.Resource;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.PrivilegeListener;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.cache.RolePrivilegeCache;
import org.openmrs.api.cache.RolePrivileges;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.context.UserContext;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

/**
 * Proves {@link OpenmrsAuthorizationManagerFactory} makes Spring Security's built-in
 * {@code hasAuthority(...)}/{@code hasRole(...)} agree with {@code Context.hasPrivilege(String)}
 * and {@link org.openmrs.User#hasRole(String)}.
 * <p>
 * Every privilege and role named here is deliberately <em>unregistered</em> - no row exists for it
 * anywhere, and none is created. That is the whole point. Without this factory the expressions
 * could only grant a name present in {@code OpenmrsAuthenticationToken#getAuthorities()}, which
 * enumerates registered names, so a superuser or {@code Daemon} thread would be denied something
 * {@code @Authorized} and {@code hasPermission(null, ...)} both allow. Core itself checks roughly
 * 50 such privileges (every {@code purge*}), so this is not a theoretical case.
 */
public class OpenmrsAuthorizationManagerFactoryTest extends BaseContextSensitiveTest {

	private static final String UNREGISTERED = "Factory Test Unregistered Privilege";

	private static final String OTHER_UNREGISTERED = "Factory Test Other Unregistered Privilege";

	private static final String ROLE = "Factory Test Role";

	private static final String OTHER_ROLE = "Factory Test Other Role";

	@Autowired
	private HasAuthorityTestService service;

	@Resource(name = "factoryTestPrivilegeListener")
	private RecordingPrivilegeListener listener;

	@Test
	public void hasAuthority_shouldGrantAnUnregisteredPrivilegeToASuperuser() {
		// admin, the default test principal, is a superuser. Reading the authority set would deny this,
		// since no Privilege row means no GrantedAuthority to match - the factory is what makes the
		// expression consult Context.hasPrivilege instead
		assertEquals("ok", service.viaHasAuthority());
	}

	@Test
	public void isAnonymous_shouldGrantWhenLoggedOut() {
		// the inherited default asks the trust resolver whether the token is an
		// AnonymousAuthenticationToken, which an OpenmrsAuthenticationToken never is, so this expression
		// could not be true for anyone - not even a caller who has not logged in
		Context.getUserContext().logout();

		assertEquals("ok", service.viaIsAnonymous());
	}

	@Test
	public void isAnonymous_shouldDenyAnAuthenticatedCaller() {
		// admin, the default test principal, is logged in
		assertThrows(AccessDeniedException.class, service::viaIsAnonymous);
	}

	@Test
	public void isAnonymous_shouldDenyACallerHoldingAProxyPrivilegeWhileLoggedOut() {
		// OpenmrsAuthenticationToken#isAuthenticated() counts a proxy privilege - like a Daemon thread -
		// as authenticated, and neither is a caller that has failed to identify itself
		Context.getUserContext().logout();
		Context.addProxyPrivilege(UNREGISTERED);
		try {
			assertThrows(AccessDeniedException.class, service::viaIsAnonymous);
		} finally {
			Context.removeProxyPrivilege(UNREGISTERED);
		}
	}

	@Test
	public void hasAuthority_shouldDenyWhenLoggedOut() {
		Context.getUserContext().logout();

		assertThrows(AccessDeniedException.class, service::viaHasAuthority);
	}

	@Test
	public void hasAuthority_shouldGrantViaAProxyPrivilegeWhenLoggedOut() {
		Context.getUserContext().logout();
		Context.addProxyPrivilege(UNREGISTERED);
		try {
			assertEquals("ok", service.viaHasAuthority());
		} finally {
			Context.removeProxyPrivilege(UNREGISTERED);
		}
	}

	@Test
	public void hasAuthority_shouldGrantOnADaemonThreadWhenLoggedOut() {
		Context.getUserContext().logout();
		// CALLS_REAL_METHODS: only isDaemonThread() is overridden, since the privilege-resolution path
		// underneath genuinely calls other Daemon methods
		try (MockedStatic<Daemon> daemon = mockStatic(Daemon.class, Mockito.CALLS_REAL_METHODS)) {
			daemon.when(Daemon::isDaemonThread).thenReturn(true);

			assertEquals("ok", service.viaHasAuthority());
		}
	}

	@Test
	public void hasAuthority_shouldAgreeWithAuthorizedForTheSameUnregisteredPrivilege() {
		// the parity this factory buys: twin methods, same privilege, same verdict both ways round
		assertEquals("ok", service.viaAuthorized());
		assertEquals("ok", service.viaHasAuthority());

		Context.getUserContext().logout();
		try {
			assertThrows(Exception.class, service::viaAuthorized);
			assertThrows(AccessDeniedException.class, service::viaHasAuthority);
		} finally {
			Context.authenticate("admin", "test");
		}
	}

	@Test
	public void hasAuthority_shouldMatchThePrivilegeCaseInsensitively() throws Exception {
		// only possible because the expression resolves through Context.hasPrivilege(String): Spring's
		// own hasAuthority compares GrantedAuthority#getAuthority() by exact string equality, which is
		// what forced privilege matching to be case-sensitive while the authority set was consulted.
		// The role-derived path is the case-insensitive one - a proxy privilege is matched exactly, on
		// master too - so this primes a role granting the privilege in a different casing.
		Cache cache = Context.getRegisteredComponent("apiCacheManager", CacheManager.class)
		        .getCache(RolePrivilegeCache.CACHE_NAME);
		cache.put(RolePrivileges.normalize(ROLE),
		    new RolePrivileges(Collections.singleton(UNREGISTERED.toUpperCase()), false));
		try {
			runAs(userWithRoles(ROLE), () -> assertEquals("ok", service.viaHasAuthority()));
		} finally {
			cache.clear();
		}
	}

	@Test
	public void hasRole_shouldMatchTheRoleCaseInsensitively() throws Exception {
		runAs(userWithRoles(ROLE.toUpperCase()), () -> assertEquals("ok", service.viaHasRole()));
	}

	@Test
	public void hasAuthority_shouldNameTheMissingPrivilegeWhenDenied() {
		Context.getUserContext().logout();

		AccessDeniedException exception = assertThrows(AccessDeniedException.class, service::viaHasAuthority);
		assertEquals(expectedMessage(UNREGISTERED), exception.getMessage());
	}

	@Test
	public void hasAnyAuthority_shouldGrantWhenOnlyTheSecondIsHeld() {
		Context.getUserContext().logout();
		Context.addProxyPrivilege(OTHER_UNREGISTERED);
		try {
			assertEquals("ok", service.viaHasAnyAuthority());
		} finally {
			Context.removeProxyPrivilege(OTHER_UNREGISTERED);
		}
	}

	@Test
	public void hasAnyAuthority_shouldNameEveryMissingPrivilegeWhenDenied() {
		Context.getUserContext().logout();

		AccessDeniedException exception = assertThrows(AccessDeniedException.class, service::viaHasAnyAuthority);
		assertEquals(expectedMessage(UNREGISTERED + "," + OTHER_UNREGISTERED), exception.getMessage());
	}

	@Test
	public void hasAllAuthorities_shouldDenyAndNameOnlyTheMissingOneWhenSomeAreHeld() {
		Context.getUserContext().logout();
		Context.addProxyPrivilege(UNREGISTERED);
		try {
			AccessDeniedException exception = assertThrows(AccessDeniedException.class, service::viaHasAllAuthorities);
			assertEquals(expectedMessage(OTHER_UNREGISTERED), exception.getMessage());
		} finally {
			Context.removeProxyPrivilege(UNREGISTERED);
		}
	}

	// --- hasRole(...), hasAnyRole(...), hasAllRoles(...) via User#hasRole(String) ---

	@Test
	public void hasRole_shouldGrantASuperuserARoleTheyDoNotHold() {
		// admin's own role is System Developer, not this one; User#hasRole's superuser bypass is what
		// grants it, and the role need not be registered any more than a privilege does
		assertEquals("ok", service.viaHasRole());
	}

	@Test
	public void hasRole_shouldGrantANonSuperuserWhoHoldsTheRole() throws Exception {
		runAs(userWithRoles(ROLE), () -> assertEquals("ok", service.viaHasRole()));
	}

	@Test
	public void hasRole_shouldDenyANonSuperuserWhoDoesNotHoldTheRole() throws Exception {
		runAs(userWithRoles("Factory Test Some Other Role"),
		    () -> assertThrows(AccessDeniedException.class, service::viaHasRole));
	}

	@Test
	public void hasRole_shouldAcceptEitherSpellingOfTheRoleName() throws Exception {
		// Spring strips a leading ROLE_ only for its own DefaultAuthorizationManagerFactory, so this one
		// does the stripping itself: both spellings resolve the OpenMRS role named ROLE
		runAs(userWithRoles(ROLE), () -> {
			assertEquals("ok", service.viaHasRole());
			assertEquals("ok", service.viaHasPrefixedRole());
		});
	}

	@Test
	public void hasRole_shouldDenyWhenLoggedOut() {
		Context.getUserContext().logout();

		assertThrows(AccessDeniedException.class, service::viaHasRole);
	}

	@Test
	public void hasRole_shouldGrantOnADaemonThreadWhenLoggedOut() {
		Context.getUserContext().logout();
		try (MockedStatic<Daemon> daemon = mockStatic(Daemon.class, Mockito.CALLS_REAL_METHODS)) {
			daemon.when(Daemon::isDaemonThread).thenReturn(true);

			assertEquals("ok", service.viaHasRole());
		}
	}

	@Test
	public void hasAnyRole_shouldGrantWhenOnlyTheSecondIsHeld() throws Exception {
		runAs(userWithRoles(OTHER_ROLE), () -> assertEquals("ok", service.viaHasAnyRole()));
	}

	@Test
	public void hasAllRoles_shouldRequireEveryRole() throws Exception {
		runAs(userWithRoles(ROLE), () -> assertThrows(AccessDeniedException.class, service::viaHasAllRoles));
		runAs(userWithRoles(ROLE, OTHER_ROLE), () -> assertEquals("ok", service.viaHasAllRoles()));
	}

	@Test
	public void hasAnyAuthority_shouldNotReportADenialForANameItNeverNeededToCheck() {
		// Context.hasPrivilege notifies every PrivilegeListener on each call, so a multi-name check
		// that evaluated every name would tell listeners the caller lacks a privilege that never
		// blocked anything - a module auditing through that hook would log a denial that did not
		// happen, and nothing throws. @Authorized({A, B}) returns on the first grant for the same
		// reason (see AuthorizationAdviceTest#before_shouldNotifyListenersAboutCheckedPrivileges,
		// which asserts lacksPrivileges is empty on a granted call).
		Context.getUserContext().logout();
		Context.addProxyPrivilege(UNREGISTERED);
		try {
			listener.lacking.clear();

			assertEquals("ok", service.viaHasAnyAuthority());

			assertThat(listener.lacking, not(hasItem(OTHER_UNREGISTERED)));
		} finally {
			Context.removeProxyPrivilege(UNREGISTERED);
		}
	}

	@Test
	public void hasAllAuthorities_shouldNotCheckNamesPastTheFirstMissingOne() {
		// the mirror: once one name is missing the call is denied, so evaluating the rest only emits
		// further listener events. OTHER_UNREGISTERED sits after UNREGISTERED in the expression.
		Context.getUserContext().logout();
		try {
			listener.lacking.clear();

			assertThrows(AccessDeniedException.class, service::viaHasAllAuthorities);

			assertThat(listener.lacking, not(hasItem(OTHER_UNREGISTERED)));
		} finally {
			Context.getUserContext().logout();
		}
	}

	@Test
	public void factory_shouldOverrideEveryAuthorityAndRoleMethod() {
		// Guard against an override going missing: every method on AuthorizationManagerFactory has a
		// default that consults Authentication#getAuthorities(), so dropping one compiles cleanly and
		// silently reverts that expression to authority-set semantics. The Authentication handed in
		// here carries no authorities at all, so every default would deny, while admin - the superuser
		// test principal - satisfies Context.hasPrivilege/User#hasRole for any name.
		OpenmrsAuthorizationManagerFactory<Object> factory = new OpenmrsAuthorizationManagerFactory<>();
		Supplier<Authentication> noAuthorities = () -> new TestingAuthenticationToken("admin", "n/a");

		assertTrue(factory.hasAuthority(UNREGISTERED).authorize(noAuthorities, null).isGranted());
		assertTrue(factory.hasAnyAuthority(UNREGISTERED).authorize(noAuthorities, null).isGranted());
		assertTrue(factory.hasAllAuthorities(UNREGISTERED).authorize(noAuthorities, null).isGranted());
		assertTrue(factory.hasRole(ROLE).authorize(noAuthorities, null).isGranted());
		assertTrue(factory.hasAnyRole(ROLE).authorize(noAuthorities, null).isGranted());
		assertTrue(factory.hasAllRoles(ROLE).authorize(noAuthorities, null).isGranted());
	}

	private User userWithRoles(String... roleNames) {
		User user = new User();
		for (String roleName : roleNames) {
			user.addRole(new Role(roleName));
		}
		return user;
	}

	/**
	 * Swaps the authenticated user on the current {@link UserContext} for a synthetic, non-superuser
	 * one, so the {@link User#hasRole(String)} path can be exercised without its superuser bypass
	 * short-circuiting every check. Same approach as {@code UserContextHasPrivilegeTest}.
	 */
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

	private String expectedMessage(String privileges) {
		return Context.getMessageSourceService().getMessage("error.privilegesRequired", new Object[] { privileges },
		    Locale.getDefault());
	}

	/**
	 * Records the privilege names reported as <em>not</em> held, so a test can assert that a name whose
	 * answer could not change the outcome was never checked.
	 */
	@Component("factoryTestPrivilegeListener")
	public static class RecordingPrivilegeListener implements PrivilegeListener {

		private final Set<String> lacking = new LinkedHashSet<>();

		@Override
		public void privilegeChecked(User user, String privilege, boolean hasPrivilege) {
			if (!hasPrivilege) {
				lacking.add(privilege);
			}
		}
	}

	@Service
	public static class HasAuthorityTestService {

		@PreAuthorize("hasAuthority('" + UNREGISTERED + "')")
		public String viaHasAuthority() {
			return "ok";
		}

		@Authorized(UNREGISTERED)
		public String viaAuthorized() {
			return "ok";
		}

		@PreAuthorize("hasAnyAuthority('" + UNREGISTERED + "', '" + OTHER_UNREGISTERED + "')")
		public String viaHasAnyAuthority() {
			return "ok";
		}

		@PreAuthorize("hasAllAuthorities('" + UNREGISTERED + "', '" + OTHER_UNREGISTERED + "')")
		public String viaHasAllAuthorities() {
			return "ok";
		}

		@PreAuthorize("hasRole('" + ROLE + "')")
		public String viaHasRole() {
			return "ok";
		}

		@PreAuthorize("hasRole('ROLE_" + ROLE + "')")
		public String viaHasPrefixedRole() {
			return "ok";
		}

		@PreAuthorize("hasAnyRole('" + ROLE + "', '" + OTHER_ROLE + "')")
		public String viaHasAnyRole() {
			return "ok";
		}

		@PreAuthorize("hasAllRoles('" + ROLE + "', '" + OTHER_ROLE + "')")
		public String viaHasAllRoles() {
			return "ok";
		}

		@PreAuthorize("isAnonymous()")
		public String viaIsAnonymous() {
			return "ok";
		}
	}
}
