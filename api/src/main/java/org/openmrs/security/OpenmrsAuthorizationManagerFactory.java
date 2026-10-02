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

/**
 * Makes Spring Security's own built-in {@code hasAuthority(...)}, {@code hasAnyAuthority(...)} and
 * {@code hasAllAuthorities(...)} expressions resolve an OpenMRS privilege through
 * {@link Context#hasPrivilege(String)} rather than by looking the name up in
 * {@code Authentication#getAuthorities()}. Installed for method security by
 * {@link OpenmrsSecurityConfig} and for URL rules by {@code AuthorizedUrlMatcher.Rule} (web
 * module), so a privilege or role name means the same thing wherever it is written.
 * <p>
 * This is what makes the built-in expressions agree with {@code @Authorized} instead of diverging
 * from it. A flat {@code GrantedAuthority} set has to be enumerable -
 * {@code AuthorityUtils.authorityListToSet} copies it by iteration into a {@code HashSet<String>},
 * so a collection answering "yes" to anything cannot be expressed. Reading the authority set
 * therefore could not reproduce {@link Context#hasPrivilege(String)}'s superuser and
 * {@code Daemon}-thread bypasses, and {@code hasAuthority('Purge Users')} would deny a superuser
 * that {@code @Authorized} and {@code hasPermission(null, 'Purge Users')} both allow. Routing the
 * expression through {@code Context.hasPrivilege(String)} instead sidesteps enumeration entirely:
 * superuser status, the {@code Daemon} bypass, proxy privileges, unregistered names and OpenMRS's
 * case-insensitive name matching all behave exactly as they do for {@code @Authorized}.
 * <p>
 * The hook is {@code AuthorizationManagerFactory}, new in Spring Security 7:
 * {@code SecurityExpressionRoot#hasAuthority(String)} is {@code final} and delegates to it, so this
 * is the supported way in. It is installed by handing an instance to
 * {@code AbstractSecurityExpressionHandler#setAuthorizationManagerFactory(AuthorizationManagerFactory)}
 * on the handler OpenMRS registers (see {@link OpenmrsSecurityConfig}), <em>not</em> by publishing
 * it as a bean: {@code PrePostMethodSecurityConfiguration} applies a factory bean to its own
 * internal expression handler, which a custom handler bean has already replaced on the
 * interceptors, so a factory bean would be dropped on the floor without an error.
 * <p>
 * {@code hasRole(...)}, {@code hasAnyRole(...)} and {@code hasAllRoles(...)} are redirected the
 * same way, onto {@link User#hasRole(String)} - so they honor its superuser bypass and its
 * case-insensitive comparison, and a role name means an OpenMRS role, matched against the user's
 * own role graph and everything it inherits. Unlike the privilege side this reads the {@link User}
 * rather than the role-privilege cache, so it sees that in-memory graph rather than a freshly
 * loaded one, and the implicit Anonymous and Authenticated roles {@code UserContext#getAllRoles()}
 * adds are not matched. A {@code Daemon} thread is granted any role, mirroring
 * {@link Context#hasPrivilege(String)}'s own Daemon short-circuit on the privilege side.
 * <p>
 * Spring strips a leading {@code ROLE_} from {@code hasRole('ROLE_X')} only when the factory in use
 * is its own {@code DefaultAuthorizationManagerFactory}, so installing this one turns that
 * passivity hack off and the stripping happens here instead: {@code hasRole('X')} and
 * {@code hasRole('ROLE_X')} both resolve the OpenMRS role named {@code X}. The consequence is that
 * a role whose name genuinely starts with {@code ROLE_} cannot be reached through
 * {@code hasRole(...)}, since the prefix is removed before the lookup - {@code access(...)} with a
 * hand-written manager is the way to check such a name. The prefix plays no other part, surviving
 * only in {@link OpenmrsAuthenticationToken#getAuthorities()} for generic tooling that reads the
 * authority set directly.
 * <p>
 * A privilege miss is recorded with {@link MissingPrivilegeRecorder} so that a denial still names
 * the privilege, the same way {@code hasPermission(null, ...)} denials do (see
 * {@link PrivilegeNamingAuthorizationManager}). Multi-privilege checks short-circuit, both because
 * {@link Context#hasPrivilege(String)} notifies every registered
 * {@link org.openmrs.PrivilegeListener} on each call - so evaluating a name whose answer cannot
 * change the outcome would report a denial that never blocked anything - and because it costs
 * nothing in the message: a {@code hasAnyAuthority} denial has evaluated every name anyway, and a
 * {@code hasAllAuthorities} denial then names the first missing privilege, exactly as
 * {@code AuthorizationAdvice} does for a {@code requireAll=true} {@code @Authorized}. The managers
 * returned here cannot carry that message themselves, which is the obvious thing to try:
 * {@code SecurityExpressionRoot} invokes them through a private {@code isGranted(...)} that reduces
 * the {@code AuthorizationResult} to {@code result != null && result.isGranted()}, so a custom
 * result - message and all - is discarded before anything downstream could read it. Throwing from a
 * manager would get the message out, but it is also what reintroduces the SpEL short-circuiting
 * that {@link OpenmrsPermissionEvaluator}'s javadoc describes, since these managers run inside the
 * expression rather than around it. Roles need no equivalent: there is no role counterpart of the
 * {@code error.privilegesRequired} message, so those checks short-circuit normally.
 *
 * @param <T> whatever the resulting managers authorize; ignored, since an OpenMRS privilege check
 *            depends only on the current thread's {@code UserContext}
 * @since 3.0.0
 */
public class OpenmrsAuthorizationManagerFactory<T> implements AuthorizationManagerFactory<T> {

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
		if (Context.hasPrivilege(privilege)) {
			return true;
		}

		MissingPrivilegeRecorder.record(privilege);
		return false;
	}
}
