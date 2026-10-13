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

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagerFactory;
import org.springframework.security.core.Authentication;

/**
 * Makes Spring Security's built-in {@code hasAuthority(...)}/{@code hasAnyAuthority(...)}/
 * {@code hasAllAuthorities(...)} resolve a privilege through {@link Context#hasPrivilege(String)}
 * instead of looking it up in {@code Authentication#getAuthorities()}, so they agree with
 * {@code @Authorized} on superuser status, the {@code Daemon} bypass, proxy privileges,
 * unregistered names and case-insensitive matching. An authority set could not reproduce those: it
 * has to be enumerable, since {@code AuthorityUtils.authorityListToSet} copies it by iteration.
 * Installed for method security by {@link OpenmrsSecurityConfig} and for URL rules by
 * {@code AuthorizedUrlMatcher.Rule}, so a name means the same thing wherever it is written.
 * <p>
 * {@code AuthorizationManagerFactory} is the supported hook, since
 * {@code SecurityExpressionRoot#hasAuthority(String)} is {@code final} and delegates to it. It must
 * be set on the expression handler OpenMRS registers, <em>not</em> published as a bean:
 * {@code PrePostMethodSecurityConfiguration} would apply a bean to its own internal handler, which
 * the custom one has already replaced, and the factory would be dropped without an error.
 * <p>
 * {@code hasRole(...)} and friends redirect onto {@link User#hasRole(String)}, so a role name means
 * an OpenMRS role with its superuser bypass, read from the user's in-memory role graph - the
 * implicit Anonymous and Authenticated roles are therefore not matched. A {@code Daemon} thread is
 * granted any role. Because installing a custom factory turns off Spring's own {@code ROLE_}
 * stripping, this does it instead: {@code hasRole('X')} and {@code hasRole('ROLE_X')} both mean the
 * role {@code X}, and a role genuinely named {@code ROLE_X} needs {@code access(...)}.
 * <p>
 * Misses are recorded with {@link MissingPrivilegeRecorder} so a denial still names the privilege,
 * as {@code hasPermission} denials do (see {@link PrivilegeNamingAuthorizationManager}); the
 * managers cannot carry the message themselves, because {@code SecurityExpressionRoot} reduces
 * their result to a boolean. Multi-privilege checks short-circuit: {@code Context.hasPrivilege}
 * notifies every {@link org.openmrs.PrivilegeListener} per call, so a redundant check would report
 * a denial that blocked nothing, and naming the first missing privilege matches what
 * {@code AuthorizationAdvice} does for {@code requireAll=true}.
 *
 * @param <T> whatever the resulting managers authorize; ignored, since an OpenMRS privilege check
 *            depends only on the current thread's {@code UserContext}
 * @since 3.0.0
 */
public class OpenmrsAuthorizationManagerFactory<T> implements AuthorizationManagerFactory<T> {

	/**
	 * Decides {@code isAnonymous()} by whether the caller has authenticated, not by token class. The
	 * inherited default looks for an {@code AnonymousAuthenticationToken}, which OpenMRS never
	 * installs, so the expression could never be true.
	 * {@link OpenmrsAuthenticationToken#isAuthenticated()} is the real answer and already covers
	 * {@code Daemon} threads and proxy privileges; any other token type keeps Spring's semantics.
	 * {@code OpenmrsAuthenticationTrustResolver} is the web-tier counterpart.
	 */
	@Override
	public AuthorizationManager<T> anonymous() {
		AuthorizationManager<T> delegate = AuthorizationManagerFactory.super.anonymous();
		return (authentication, object) -> {
			Authentication current = authentication.get();
			if (current instanceof OpenmrsAuthenticationToken) {
				return new AuthorizationDecision(!current.isAuthenticated());
			}

			return delegate.authorize(authentication, object);
		};
	}

	@Override
	public AuthorizationManager<T> hasAuthority(String authority) {
		return (authentication, object) -> new AuthorizationDecision(holdsPrivilege(authority));
	}

	@Override
	public AuthorizationManager<T> hasAnyAuthority(String... authorities) {
		return (authentication, object) -> {
			for (String authority : authorities) {
				if (holdsPrivilege(authority)) {
					return new AuthorizationDecision(true);
				}
			}
			return new AuthorizationDecision(false);
		};
	}

	@Override
	public AuthorizationManager<T> hasAllAuthorities(String... authorities) {
		return (authentication, object) -> {
			for (String authority : authorities) {
				if (!holdsPrivilege(authority)) {
					return new AuthorizationDecision(false);
				}
			}
			return new AuthorizationDecision(true);
		};
	}

	@Override
	public AuthorizationManager<T> hasRole(String role) {
		return (authentication, object) -> new AuthorizationDecision(holdsRole(role));
	}

	@Override
	public AuthorizationManager<T> hasAnyRole(String... roles) {
		return (authentication, object) -> {
			for (String role : roles) {
				if (holdsRole(role)) {
					return new AuthorizationDecision(true);
				}
			}
			return new AuthorizationDecision(false);
		};
	}

	@Override
	public AuthorizationManager<T> hasAllRoles(String... roles) {
		return (authentication, object) -> {
			for (String role : roles) {
				if (!holdsRole(role)) {
					return new AuthorizationDecision(false);
				}
			}
			return new AuthorizationDecision(true);
		};
	}

	private static boolean holdsRole(String role) {
		if (Daemon.isDaemonThread()) {
			return true;
		}

		User user = Context.getAuthenticatedUser();
		// User#hasRole(String) applies the superuser bypass itself
		return user != null && user.hasRole(stripRolePrefix(role));
	}

	/**
	 * Accepts {@code hasRole('ROLE_X')} as a spelling of {@code hasRole('X')}, the way Spring's own
	 * expression root does for its default factory, so an expression written either way resolves the
	 * OpenMRS role named {@code X}.
	 */
	private static String stripRolePrefix(String role) {
		return role.startsWith(OpenmrsAuthenticationToken.ROLE_PREFIX)
		        ? role.substring(OpenmrsAuthenticationToken.ROLE_PREFIX.length())
		        : role;
	}

	private static boolean holdsPrivilege(String privilege) {
		if (PrivilegeResolution.holdsPrivilege(privilege)) {
			return true;
		}

		MissingPrivilegeRecorder.record(privilege);
		return false;
	}
}
