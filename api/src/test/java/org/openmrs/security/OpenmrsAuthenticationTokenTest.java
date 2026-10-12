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
import org.openmrs.Privilege;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.transaction.TestTransaction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers what {@link OpenmrsAuthenticationToken#getAuthorities()} reports: the authenticated user's
 * own roles, the privileges those roles grant, and whatever proxy privileges are active right now.
 * <p>
 * It is worth being explicit about what this collection is <em>not</em>, since the name invites the
 * assumption that it decides something. No authorization check reads it - {@code @Authorized},
 * {@code hasPermission(null, ...)}, the built-in {@code hasAuthority(...)}/{@code hasRole(...)}
 * (see {@link OpenmrsAuthorizationManagerFactory}) and {@code AuthorizedUrlMatcher} rules all
 * resolve through {@link Context#hasPrivilege(String)} or {@link org.openmrs.User#hasRole(String)}
 * instead. So this set makes no attempt to express the superuser or {@code Daemon}-thread bypasses,
 * which grant any name at all and therefore cannot be enumerated; it is a view for generic Spring
 * Security tooling that reads {@code Authentication#getAuthorities()} directly, and for debug
 * logging. The authorization behaviour those bypasses do produce is covered by
 * {@link OpenmrsAuthorizationManagerFactoryTest}.
 */
public class OpenmrsAuthenticationTokenTest extends BaseContextSensitiveTest {

	private static final String PROXY_PRIVILEGE = "Totally Ad Hoc Proxy Privilege";

	private static final String REGISTERED_BUT_NOT_HELD = "Registered But Not Held By Admin";

	@Autowired
	private UserService userService;

	@Test
	public void getAuthorities_shouldContainTheUsersOwnRolesAndTheirPrivileges() {
		// admin's role is RoleConstants.SUPERUSER ("System Developer"), carried as a ROLE_-prefixed
		// authority alongside the plain privilege names its closure grants
		assertTrue(hasAuthority("ROLE_" + org.openmrs.util.RoleConstants.SUPERUSER));
	}

	@Test
	public void getAuthorities_shouldReflectAProxyPrivilegeAddedAndRemovedMidRequest() {
		assertFalse(hasAuthority(PROXY_PRIVILEGE));

		Context.addProxyPrivilege(PROXY_PRIVILEGE);
		try {
			// read live off the UserContext on every call, not snapshotted when the token was built
			assertTrue(hasAuthority(PROXY_PRIVILEGE));
		} finally {
			Context.removeProxyPrivilege(PROXY_PRIVILEGE);
		}

		assertFalse(hasAuthority(PROXY_PRIVILEGE));
	}

	@Test
	public void getAuthorities_shouldNotClaimRegisteredPrivilegesTheUserDoesNotHold() {
		// being a superuser does not put every registered Privilege into the authority set, even though
		// Context.hasPrivilege grants all of them - the distinction the class javadoc describes, and
		// the reason a module must not read authorities to decide access
		registerAndCommit();
		try {
			assertFalse(hasAuthority(REGISTERED_BUT_NOT_HELD));
			assertTrue(Context.hasPrivilege(REGISTERED_BUT_NOT_HELD));
		} finally {
			purgeAndCommit();
		}
	}

	private static boolean hasAuthority(String name) {
		Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication()
		        .getAuthorities();
		return authorities.stream().anyMatch(authority -> name.equals(authority.getAuthority()));
	}

	/**
	 * Saves {@link #REGISTERED_BUT_NOT_HELD} and force-commits, so it is genuinely a registered
	 * {@link Privilege} and not merely absent. Deliberately never assigned to any of admin's roles.
	 */
	private void registerAndCommit() {
		userService.savePrivilege(new Privilege(REGISTERED_BUT_NOT_HELD, "for testing"));
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	/**
	 * Undoes {@link #registerAndCommit()}. Required because force-committing means the normal
	 * end-of-test rollback no longer removes the row.
	 */
	private void purgeAndCommit() {
		Privilege privilege = userService.getPrivilege(REGISTERED_BUT_NOT_HELD);
		if (privilege != null) {
			userService.purgePrivilege(privilege);
		}
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}
}
