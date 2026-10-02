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

import java.io.Serializable;

import org.openmrs.api.context.Context;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Lets {@code @PreAuthorize}/{@code @PostAuthorize} expressions check OpenMRS privileges, e.g.
 * {@code @PreAuthorize("hasPermission(null, 'Get Providers')")}. Delegates directly to
 * {@link Context#hasPrivilege(String)} - the same method {@code AuthorizationAdvice} uses for
 * {@code @Authorized} - so a {@code @PreAuthorize}-guarded method and an
 * {@code @Authorized}-guarded method make identical authorization decisions for the same privilege
 * name: both correctly honor superuser status, the implicit Anonymous/Authenticated roles, proxy
 * privileges ({@link Context#addProxyPrivilege(String)}), and the {@code Daemon} thread bypass,
 * none of which can be represented exactly as a flat {@code GrantedAuthority} set (see
 * {@link OpenmrsAuthenticationToken#getAuthorities()}).
 * <p>
 * The built-in {@code hasAuthority('&lt;privilege&gt;')} resolves a privilege the same way, through
 * {@link OpenmrsAuthorizationManagerFactory}, so the two forms are interchangeable and either is
 * correct. This one exists alongside it because {@link PermissionEvaluator} is handed the target
 * object, which {@code hasAuthority(...)}'s bare-authority-name contract has nowhere to put: it is
 * where a per-object permission check belongs if one is added. It is also the form the
 * {@code @PreAuthorize} reference examples in this codebase use - see
 * {@link org.openmrs.api.ProviderService#getProvider(Integer)}.
 * <p>
 * The target object/type parameters are ignored today: like {@code @Authorized}, this only checks
 * whether the current user holds the named privilege, not any per-instance ownership rule. An
 * object-aware check can be added here without touching existing callers.
 * <p>
 * This is a plain predicate: a missing privilege makes it return {@code false}, never throw. That
 * matters because SpEL composes these -
 * {@code hasPermission(null, 'A') or hasPermission(null, 'B')} is the direct translation of a
 * multi-privilege {@code @Authorized({A, B})}, which grants on either privilege, and
 * {@code @PreFilter}/{@code @PostFilter} evaluate the expression once per element and expect a
 * verdict rather than an exception. Throwing here would short-circuit all of that: the {@code or}
 * would never reach its second operand, a negation could never be false, and a post-filter could
 * only pass everything or fail outright.
 * <p>
 * Naming the missing privilege in the denial - the {@code error.privilegesRequired} message
 * {@code AuthorizationAdvice} uses for a denied {@code @Authorized} call, rather than
 * {@code AuthorizationManager}'s own generic "Access Denied" - therefore happens one level out,
 * once the whole expression has been evaluated, in {@link PrivilegeNamingAuthorizationManager}.
 * Every privilege this evaluator finds missing while that class has a scope open is collected by
 * {@link MissingPrivilegeRecorder} and reported there if, and only if, the expression as a whole
 * ends up denying. Naming the privilege is not a meaningful disclosure here: a caller able to reach
 * a {@code @PreAuthorize}-guarded method at all already knows which privilege guards it, since the
 * expression itself ({@code hasPermission(null, '&lt;privilege&gt;')}) is visible in this
 * codebase's source.
 * <p>
 * For a no-value {@code @Authorized}'s equivalent - "just require the caller to be authenticated" -
 * no method here is needed: Spring Security's own built-in
 * {@code @PreAuthorize("isAuthenticated()")} already works correctly, because
 * {@link OpenmrsAuthenticationToken#isAuthenticated()} itself accounts for the same Daemon-thread
 * and proxy-privilege cases {@code AuthorizationAdvice.before} does for a no-value
 * {@code @Authorized}.
 *
 * @since 3.0.0
 */
@Component
public class OpenmrsPermissionEvaluator implements PermissionEvaluator {

	@Override
	public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
		return hasPermission(permission);
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
	        Object permission) {
		return hasPermission(permission);
	}

	private boolean hasPermission(Object permission) {
		if (permission == null) {
			return false;
		}

		String privilege = permission.toString();
		if (Context.hasPrivilege(privilege)) {
			return true;
		}

		MissingPrivilegeRecorder.record(privilege);
		return false;
	}
}
