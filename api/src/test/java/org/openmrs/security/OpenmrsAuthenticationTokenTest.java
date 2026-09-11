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

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.test.context.transaction.TestTransaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

/**
 * Proves {@link OpenmrsAuthenticationToken#getAuthorities()} grants a superuser every registered
 * {@link Privilege} - not just the ones attached to their roles - so a plain
 * {@code @PreAuthorize("hasAuthority('<privilege>')")}, which Spring evaluates by checking
 * {@link org.springframework.security.core.Authentication#getAuthorities()} directly rather than
 * going through {@link org.openmrs.api.context.Context#hasPrivilege(String)}, now agrees with the
 * rest of the system for a superuser (the default test context every other test in this codebase
 * runs as). The one condition attached to that, documented on {@link OpenmrsAuthenticationToken}'s
 * class javadoc, is proven here too: it only works for a privilege actually registered as a
 * {@link Privilege} row - an unregistered one is still denied, even for a superuser.
 * <p>
 * Registering a privilege has to be committed for real:
 * {@link org.openmrs.api.cache.RolePrivilegeCache} (which backs
 * {@link OpenmrsAuthenticationToken#getAuthorities()}) reloads the registered-privilege set on a
 * separate {@code Daemon} thread's own connection, which cannot see this test method's own
 * uncommitted transaction until it commits - see {@link #registerAndCommit()}.
 * <p>
 * Also proves the {@code Daemon} thread case: {@code Daemon.isDaemonThread()} is mocked true on the
 * test thread (via Mockito, since the real {@link org.openmrs.api.context.Daemon#runNewDaemonTask}
 * requires a privileged {@code CallerKey} test code doesn't have) rather than run through a real
 * daemon thread, so the test calls the real {@code @PreAuthorize}-guarded service method directly
 * while mocked - this exercises the real AOP/SpEL path, not just {@code getAuthorities()} in
 * isolation. The mock uses {@code Mockito.CALLS_REAL_METHODS} as its default answer so only the
 * explicitly-stubbed method diverges from the real implementation - a bare {@code mockStatic(...)}
 * would also null out {@code Daemon.runNewDaemonTask(...)}, which
 * {@link org.openmrs.api.cache.RolePrivilegeCache} genuinely needs to call underneath this same
 * call.
 * <p>
 * {@code hasRole(...)} gets the same coverage, plus the one case {@code hasAuthority(...)} can't
 * exercise: a role granted purely through the superuser/Daemon bypass that the test principal does
 * not actually hold (admin's own role is "System Developer", never {@link #TEST_ROLE}) - proving
 * the bypass itself, not just the minimal "a user's own role works" case.
 */
public class OpenmrsAuthenticationTokenTest extends BaseContextSensitiveTest {

	private static final String TEST_PRIVILEGE = "Test HasAuthority Privilege";

	private static final String UNREGISTERED_PRIVILEGE = "Definitely Never Registered Privilege";

	private static final String PROXY_PRIVILEGE = "Totally Ad Hoc Proxy Privilege";

	private static final String TEST_ROLE = "Test HasRole Role";

	private static final String UNREGISTERED_ROLE = "Definitely Never Registered Role";

	@Autowired
	private UserService userService;

	@Autowired
	private HasAuthorityTestService hasAuthorityTestService;

	@Test
	public void getAuthorities_shouldGrantASuperuserAFreshlyRegisteredPrivilegeWithoutReLogin() {
		assertFalse(currentSuperuserHasAuthority(TEST_PRIVILEGE));

		registerAndCommit();
		try {
			// same session, no re-login - the authority set is live, not a snapshot taken at login
			assertTrue(currentSuperuserHasAuthority(TEST_PRIVILEGE));
		} finally {
			purgeAndCommit();
		}
	}

	@Test
	public void hasAuthority_shouldDenyEvenASuperuserForAnUnregisteredPrivilege() {
		assertThrows(AccessDeniedException.class, hasAuthorityTestService::requireUnregisteredPrivilege);
	}

	@Test
	public void hasAuthority_shouldAllowASuperuserOnceThePrivilegeIsRegistered() {
		registerAndCommit();
		try {
			assertEquals("ok", hasAuthorityTestService.requireRegisteredPrivilege());
		} finally {
			purgeAndCommit();
		}
	}

	@Test
	public void getAuthorities_shouldReflectAProxyPrivilegeAddedAndRemovedMidRequest() {
		// unlike a superuser's registered-privilege grant, a proxy privilege needs no Privilege row at
		// all - it is a raw string Context.addProxyPrivilege accepts directly
		assertFalse(currentSuperuserHasAuthority(PROXY_PRIVILEGE));

		Context.addProxyPrivilege(PROXY_PRIVILEGE);
		try {
			assertTrue(currentSuperuserHasAuthority(PROXY_PRIVILEGE));
		} finally {
			Context.removeProxyPrivilege(PROXY_PRIVILEGE);
		}

		assertFalse(currentSuperuserHasAuthority(PROXY_PRIVILEGE));
	}

	@Test
	public void hasAuthority_shouldAllowAccessGrantedSolelyByAProxyPrivilege() {
		// logged out entirely - only the proxy privilege stands between this call and denial, the same
		// pattern Context.addProxyPrivilege's own javadoc describes for scoped internal operations
		Context.getUserContext().logout();

		Context.addProxyPrivilege(PROXY_PRIVILEGE);
		try {
			assertEquals("ok", hasAuthorityTestService.requireProxyPrivilege());
		} finally {
			Context.removeProxyPrivilege(PROXY_PRIVILEGE);
		}
	}

	@Test
	public void hasAuthority_shouldAllowAccessOnADaemonThreadRegardlessOfRoleOrSuperuserStatus() {
		// register while still admin - logging out first would also block registerAndCommit() itself,
		// which needs Manage Privileges
		registerAndCommit();
		try {
			// now logged out entirely - no roles, not superuser - only the mocked Daemon status should
			// grant this
			Context.getUserContext().logout();

			// CALLS_REAL_METHODS: only isDaemonThread() is overridden below; RolePrivilegeCache genuinely
			// needs Daemon.runNewDaemonTask(...) to keep working for real underneath this call
			try (MockedStatic<Daemon> daemon = mockStatic(Daemon.class, Mockito.CALLS_REAL_METHODS)) {
				daemon.when(Daemon::isDaemonThread).thenReturn(true);

				assertEquals("ok", hasAuthorityTestService.requireRegisteredPrivilege());
			}
		} finally {
			// purgeAndCommit() itself needs Manage Privileges, so log back in before it runs
			Context.authenticate("admin", "test");
			purgeAndCommit();
		}
	}

	@Test
	public void hasAuthority_shouldDenyOnADaemonThreadForAnUnregisteredPrivilege() {
		// a Daemon thread grants every registered privilege, not literally any string - same limit as
		// the superuser case
		try (MockedStatic<Daemon> daemon = mockStatic(Daemon.class, Mockito.CALLS_REAL_METHODS)) {
			daemon.when(Daemon::isDaemonThread).thenReturn(true);

			assertThrows(AccessDeniedException.class, hasAuthorityTestService::requireUnregisteredPrivilege);
		}
	}

	@Test
	public void hasRole_shouldAllowASuperuserForARoleTheyDoNotHold() {
		// admin's own role is "System Developer" (RoleConstants.SUPERUSER), not this one - proves the
		// superuser bypass mirroring User.hasRole(String), not just the minimal "own role" case
		registerRoleAndCommit();
		try {
			assertEquals("ok", hasAuthorityTestService.requireRegisteredRole());
		} finally {
			purgeRoleAndCommit();
		}
	}

	@Test
	public void hasRole_shouldDenyEvenASuperuserForAnUnregisteredRole() {
		assertThrows(AccessDeniedException.class, hasAuthorityTestService::requireUnregisteredRole);
	}

	@Test
	public void hasRole_shouldAllowAccessOnADaemonThreadRegardlessOfRoleOrSuperuserStatus() {
		registerRoleAndCommit();
		try {
			Context.getUserContext().logout();

			try (MockedStatic<Daemon> daemon = mockStatic(Daemon.class, Mockito.CALLS_REAL_METHODS)) {
				daemon.when(Daemon::isDaemonThread).thenReturn(true);

				assertEquals("ok", hasAuthorityTestService.requireRegisteredRole());
			}
		} finally {
			Context.authenticate("admin", "test");
			purgeRoleAndCommit();
		}
	}

	@Test
	public void hasRole_shouldDenyOnADaemonThreadForAnUnregisteredRole() {
		try (MockedStatic<Daemon> daemon = mockStatic(Daemon.class, Mockito.CALLS_REAL_METHODS)) {
			daemon.when(Daemon::isDaemonThread).thenReturn(true);

			assertThrows(AccessDeniedException.class, hasAuthorityTestService::requireUnregisteredRole);
		}
	}

	private static boolean currentSuperuserHasAuthority(String privilege) {
		Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
		        .getAuthorities();
		return authorities.stream().anyMatch(authority -> authority.getAuthority().equalsIgnoreCase(privilege));
	}

	/**
	 * Saves {@link #TEST_PRIVILEGE} and force-commits the transaction, so the daemon-thread reload
	 * behind {@link OpenmrsAuthenticationToken#getAuthorities()} can see it. Leaves a fresh transaction
	 * started for the rest of the test method.
	 */
	private void registerAndCommit() {
		userService.savePrivilege(new Privilege(TEST_PRIVILEGE, "for testing"));
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	/**
	 * Undoes {@link #registerAndCommit()}. Required because force-committing means the normal
	 * end-of-test rollback no longer undoes the registration.
	 */
	private void purgeAndCommit() {
		Privilege privilege = userService.getPrivilege(TEST_PRIVILEGE);
		if (privilege != null) {
			userService.purgePrivilege(privilege);
		}
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	/**
	 * Saves {@link #TEST_ROLE} and force-commits, same reasoning as {@link #registerAndCommit()}.
	 * Deliberately never assigned to admin, so tests using it prove the superuser/Daemon bypass rather
	 * than a role admin actually holds.
	 */
	private void registerRoleAndCommit() {
		userService.saveRole(new Role(TEST_ROLE, "for testing"));
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	/**
	 * Undoes {@link #registerRoleAndCommit()}.
	 */
	private void purgeRoleAndCommit() {
		Role role = userService.getRole(TEST_ROLE);
		if (role != null) {
			userService.purgeRole(role);
		}
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	@Service
	public static class HasAuthorityTestService {

		@PreAuthorize("hasAuthority('" + TEST_PRIVILEGE + "')")
		public String requireRegisteredPrivilege() {
			return "ok";
		}

		@PreAuthorize("hasAuthority('" + UNREGISTERED_PRIVILEGE + "')")
		public String requireUnregisteredPrivilege() {
			return "ok";
		}

		@PreAuthorize("hasAuthority('" + PROXY_PRIVILEGE + "')")
		public String requireProxyPrivilege() {
			return "ok";
		}

		@PreAuthorize("hasRole('" + TEST_ROLE + "')")
		public String requireRegisteredRole() {
			return "ok";
		}

		@PreAuthorize("hasRole('" + UNREGISTERED_ROLE + "')")
		public String requireUnregisteredRole() {
			return "ok";
		}
	}
}
