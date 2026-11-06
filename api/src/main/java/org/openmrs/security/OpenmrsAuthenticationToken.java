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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.cache.RolePrivilegeCache;
import org.openmrs.api.cache.RolePrivileges;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.api.context.UserContext;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

/**
 * The {@link org.springframework.security.core.Authentication} OpenMRS installs into
 * {@link org.springframework.security.core.context.SecurityContextHolder}. It wraps the legacy
 * {@link UserContext}, which stays the authoritative holder of the authenticated {@link User},
 * proxy privileges, locale and location; this token is the copy
 * {@link Context#setUserContext(UserContext)} publishes so Spring Security has something to
 * evaluate.
 * <p>
 * {@link #getAuthorities()} is a view for generic tooling and debug logging only - privileges as
 * plain authorities, roles as {@code "ROLE_" +} the role name. No OpenMRS check reads it:
 * {@code @Authorized}, {@code hasPermission}, {@code hasAuthority}/{@code hasRole} (see
 * {@link OpenmrsAuthorizationManagerFactory}) and URL rules all resolve through
 * {@link Context#hasPrivilege(String)} or {@link User#hasRole(String)} against the wrapped
 * {@link UserContext}. Proxy privileges are read live, so one added mid-request shows up at once.
 * <p>
 * The set holds what the user actually holds, which is why it cannot be read as an authorization
 * answer: {@link Context#hasPrivilege(String)}'s superuser and {@link Daemon} bypasses grant any
 * name, including unregistered ones, and an enumerable set cannot express that -
 * {@code AuthorityUtils.authorityListToSet} copies by iteration rather than consulting the
 * collection. A module after the real answer has to ask {@code Context} or {@link User}.
 *
 * @since 3.0.0
 */
// Sonar (java:S2160) wants equals() for the added field. Not needed: this token is never compared,
// and the inherited equals() dispatches through getPrincipal() below anyway.
@SuppressWarnings("java:S2160")
public class OpenmrsAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;

	/**
	 * Prefix Spring Security's built-in {@code hasRole(...)}/{@code hasAnyRole(...)} SpEL expressions
	 * expect - they are sugar for {@code hasAuthority("ROLE_" + role)} using this exact default prefix
	 * (see {@code GrantedAuthorityDefaults}, not customized anywhere in this codebase).
	 */
	static final String ROLE_PREFIX = "ROLE_";

	private final UserContext userContext;

	public OpenmrsAuthenticationToken(UserContext userContext) {
		super(Collections.emptyList());
		this.userContext = userContext;
		super.setAuthenticated(userContext.isAuthenticated());
	}

	/**
	 * @return the wrapped, authoritative {@link UserContext} for the current thread
	 */
	public UserContext getUserContext() {
		return userContext;
	}

	@Override
	public Object getPrincipal() {
		return userContext.getAuthenticatedUser();
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	/**
	 * @return true if {@link UserContext#isAuthenticated()}, or if the caller is running on a
	 *         {@link Daemon} thread, or holds any proxy privilege
	 *         ({@link Context#addProxyPrivilege(String)}) - the same three-way check a no-value
	 *         {@code @Authorized} makes (see {@code AuthorizationAdvice.before}), so that Spring
	 *         Security's own built-in {@code @PreAuthorize("isAuthenticated()")} is a faithful
	 *         equivalent, not just an approximation that misses the Daemon and proxy-privilege cases.
	 *         {@code UserContext#isAuthenticated()} alone (just {@code user != null}) has no notion of
	 *         either.
	 */
	@Override
	public boolean isAuthenticated() {
		return userContext.isAuthenticated() || Daemon.isDaemonThread() || userContext.hasProxyPrivileges();
	}

	@Override
	public String getName() {
		User user = userContext.getAuthenticatedUser();
		return user != null ? user.getUsername() : "anonymousUser";
	}

	/**
	 * @return the current user's own roles, the privileges those roles grant (inheritance already
	 *         flattened), and any privilege currently added via
	 *         {@link Context#addProxyPrivilege(String)}. Deliberately not a substitute for
	 *         {@link Context#hasPrivilege(String)} - see the class javadoc
	 */
	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		try {
			Set<GrantedAuthority> authorities = new HashSet<>();
			addRoleAndPrivilegeAuthorities(authorities);
			addProxyPrivilegeAuthorities(authorities);
			return Collections.unmodifiableSet(authorities);
		} catch (Exception e) {
			// getAllRoles() only fails if the database is unreachable; fail safe to an empty,
			// non-authoritative authority set rather than propagate from a getter.
			return Collections.emptySet();
		}
	}

	/**
	 * Adds the authorities for the current user's own roles and their privileges (both flattened
	 * through {@link #getRolePrivileges(Role)}) into {@code authorities}.
	 */
	private void addRoleAndPrivilegeAuthorities(Set<GrantedAuthority> authorities) throws Exception {
		for (Role role : userContext.getAllRoles()) {
			for (String privilegeName : getRolePrivileges(role).getPrivilegeNames()) {
				authorities.add(new SimpleGrantedAuthority(privilegeName));
			}
			if (role.getRole() != null) {
				authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role.getRole()));
			}
		}
	}

	private void addProxyPrivilegeAuthorities(Set<GrantedAuthority> authorities) {
		for (String privilegeName : userContext.getProxyPrivileges()) {
			// SimpleGrantedAuthority rejects blank names; a proxy privilege being blank would be a
			// caller bug elsewhere, not something to propagate out of a getter
			if (StringUtils.hasText(privilegeName)) {
				authorities.add(new SimpleGrantedAuthority(privilegeName));
			}
		}
	}

	private RolePrivileges getRolePrivileges(Role role) {
		try {
			return getRolePrivilegeCache().getRolePrivileges(role);
		} catch (Exception e) {
			return RolePrivilegeCache.computeRolePrivileges(role);
		}
	}

	private RolePrivilegeCache getRolePrivilegeCache() {
		return Context.getRegisteredComponent("rolePrivilegeCache", RolePrivilegeCache.class);
	}
}
