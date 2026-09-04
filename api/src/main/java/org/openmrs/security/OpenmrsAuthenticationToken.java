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
 * The {@link org.springframework.security.core.Authentication} that OpenMRS installs into
 * {@link org.springframework.security.core.context.SecurityContextHolder} for the current thread.
 * It wraps the legacy {@link UserContext}, which remains the authoritative holder of the
 * authenticated {@link User}, proxy privileges, locale, and location for this session/thread - this
 * token exists so that per-thread identity lives under Spring Security's own holder instead of a
 * separate, OpenMRS-only {@link ThreadLocal}, as it did before {@code Context} delegated storage
 * here (see {@link Context#setUserContext(UserContext)}).
 * <p>
 * {@link #getAuthorities()} is a role- and privilege-derived view of the user's authorities, useful
 * for generic Spring Security tooling ({@code hasAuthority(...)}/{@code hasRole(...)} expressions
 * or debug logging). Privilege names become plain authorities; role names become {@code "ROLE_" +}
 * the role name (see {@code ROLE_PREFIX}), matching what Spring's built-in {@code hasRole(...)}/
 * {@code hasAnyRole(...)} expressions look for by default.
 * <p>
 * The anonymous/authenticated implicit roles are included (see {@link UserContext#getAllRoles()}),
 * which already flattens role inheritance, so an inherited role's own privileges and its own
 * {@code ROLE_} authority are both present. A superuser is additionally granted every currently
 * registered {@link org.openmrs.Privilege} (see {@link RolePrivilegeCache#getAllPrivilegeNames()})
 * and every registered {@link org.openmrs.Role} (see {@link RolePrivilegeCache#getAllRoleNames()}),
 * not just their own - mirroring {@link User#hasRole(String)}'s own superuser bypass
 * ({@code ignoreSuperUser} defaults to {@code false} there too) for roles, and the same "satisfies
 * any name" reasoning as privileges, since a flat authority set can otherwise only approximate that
 * for names known in advance. Any privilege currently added via
 * {@link Context#addProxyPrivilege(String)} is included too, read live off
 * {@link UserContext#getProxyPrivileges()} on every call so a privilege added or removed
 * mid-request is reflected immediately, not just what was current when this token was constructed.
 * A {@link Daemon} thread is granted every registered privilege and role unconditionally, mirroring
 * {@link Context#hasPrivilege(String)}'s own Daemon check (independent of
 * {@link org.openmrs.aop.AuthorizationAdvice}'s), before any role is even looked at.
 * <p>
 * One gap remains against {@link Context#hasPrivilege(String)}: a superuser or Daemon thread is
 * only granted privileges and roles that are actually registered, not literally any string, which a
 * flat authority set cannot represent - see {@link RolePrivilegeCache#warnIfUnregistered(String)}
 * for the diagnostic that covers the privilege half of that gap (there is no role equivalent:
 * unlike a privilege name, a role name is always checked against real {@link org.openmrs.Role}
 * membership, so there is no "checked but never registered" case). Real authorization decisions
 * should still go through {@link Context#hasPrivilege(String)} (used by {@code @Authorized}/
 * {@code AuthorizationAdvice}) or {@link OpenmrsPermissionEvaluator} (used by
 * {@code @PreAuthorize}'s {@code hasPermission(null, ...)} form), both of which delegate to the
 * wrapped {@link UserContext} directly rather than to this collection. Unlike
 * {@code hasAuthority(...)}, {@code hasRole(...)} has no existing OpenMRS mechanism to agree with -
 * there is no role-based {@code @Authorized} equivalent - so "correct" here means matching
 * {@link User#hasRole(String)}'s own semantics, not bringing a built-in Spring expression into line
 * with an existing enforcement path.
 *
 * @since 3.0.0
 */
public class OpenmrsAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;

	/**
	 * Prefix Spring Security's built-in {@code hasRole(...)}/{@code hasAnyRole(...)} SpEL expressions
	 * expect - they are sugar for {@code hasAuthority("ROLE_" + role)} using this exact default prefix
	 * (see {@code GrantedAuthorityDefaults}, not customized anywhere in this codebase).
	 */
	private static final String ROLE_PREFIX = "ROLE_";

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
	 * @return every registered privilege and role if called on a {@link Daemon} thread (see the class
	 *         javadoc for why); otherwise the current user's role- and privilege-derived authorities,
	 *         plus every registered privilege and role if the user is a superuser. See the class
	 *         javadoc for why this is still not a complete substitute for
	 *         {@link Context#hasPrivilege(String)}
	 */
	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		if (Daemon.isDaemonThread()) {
			// Mirrors Context.hasPrivilege(String), which grants a Daemon thread everything before it
			// ever looks at UserContext/roles; role membership is irrelevant here for the same reason,
			// so this skips straight past the role/proxy-privilege walk below.
			Set<GrantedAuthority> authorities = new HashSet<>();
			for (String privilegeName : getAllPrivilegeNames()) {
				authorities.add(new SimpleGrantedAuthority(privilegeName));
			}
			for (String roleName : getAllRoleNames()) {
				authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + roleName));
			}
			return Collections.unmodifiableSet(authorities);
		}

		try {
			Set<GrantedAuthority> authorities = new HashSet<>();
			boolean superuser = false;
			for (Role role : userContext.getAllRoles()) {
				RolePrivileges rolePrivileges = getRolePrivileges(role);
				superuser |= rolePrivileges.grantsSuperuser();
				for (String privilegeName : rolePrivileges.getPrivilegeNames()) {
					authorities.add(new SimpleGrantedAuthority(privilegeName));
				}
				if (role.getRole() != null) {
					authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role.getRole()));
				}
			}
			if (superuser) {
				// Mirrors User.hasRole(String)'s own superuser bypass (ignoreSuperUser defaults to
				// false there too): a superuser "has" every role, not just their own.
				for (String privilegeName : getAllPrivilegeNames()) {
					authorities.add(new SimpleGrantedAuthority(privilegeName));
				}
				for (String roleName : getAllRoleNames()) {
					authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + roleName));
				}
			}
			for (String privilegeName : userContext.getProxyPrivileges()) {
				// SimpleGrantedAuthority rejects blank names; a proxy privilege being blank would be a
				// caller bug elsewhere, not something to propagate out of a getter
				if (StringUtils.hasText(privilegeName)) {
					authorities.add(new SimpleGrantedAuthority(privilegeName));
				}
			}
			return Collections.unmodifiableSet(authorities);
		} catch (Exception e) {
			// getAllRoles() only fails if the database is unreachable; fail safe to an empty,
			// non-authoritative authority set rather than propagate from a getter.
			return Collections.emptySet();
		}
	}

	private RolePrivileges getRolePrivileges(Role role) {
		try {
			return getRolePrivilegeCache().getRolePrivileges(role);
		} catch (Exception e) {
			return RolePrivilegeCache.computeRolePrivileges(role);
		}
	}

	/**
	 * @return every currently registered privilege name, or an empty set if the cache component isn't
	 *         available (fail safe, same as {@link #getAuthorities()} as a whole)
	 */
	private Set<String> getAllPrivilegeNames() {
		try {
			return getRolePrivilegeCache().getAllPrivilegeNames();
		} catch (Exception e) {
			return Collections.emptySet();
		}
	}

	/**
	 * @return every currently registered role name, or an empty set if the cache component isn't
	 *         available (fail safe, same as {@link #getAuthorities()} as a whole)
	 */
	private Set<String> getAllRoleNames() {
		try {
			return getRolePrivilegeCache().getAllRoleNames();
		} catch (Exception e) {
			return Collections.emptySet();
		}
	}

	private RolePrivilegeCache getRolePrivilegeCache() {
		return Context.getRegisteredComponent("rolePrivilegeCache", RolePrivilegeCache.class);
	}
}
