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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

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
 * {@link OpenmrsAuthenticationToken#getAuthorities()}) - which is why {@code @PreAuthorize}
 * conversions in this codebase use {@code hasPermission(null, '&lt;privilege&gt;')} rather than the
 * built-in {@code hasAuthority('&lt;privilege&gt;')}.
 * <p>
 * The target object/type parameters are ignored: like {@code @Authorized}, this only checks whether
 * the current user holds the named privilege, not any per-instance ownership rule. A future
 * object-aware permission check can be added here without touching existing callers.
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
 * Every privilege this evaluator finds missing while that class has a recording scope open is
 * collected via {@link #beginRecording()}/{@link #endRecording(Set)} and reported there if, and
 * only if, the expression as a whole ends up denying. Naming the privilege is not a meaningful
 * disclosure here: a caller able to reach a {@code @PreAuthorize}-guarded method at all already
 * knows which privilege guards it, since the expression itself
 * ({@code hasPermission(null, '&lt;privilege&gt;')}) is visible in this codebase's source.
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

	/**
	 * Privilege names found missing within the innermost {@link PrivilegeNamingAuthorizationManager}
	 * recording scope currently open on this thread, in the order they were checked.
	 * <p>
	 * <code>null</code> when no scope is open, in which case a miss is not recorded at all. That is the
	 * {@code @PreFilter}/{@code @PostFilter} case: those interceptors evaluate the expression per
	 * element and never produce a denial message, so nothing would ever drain the names.
	 */
	private static final ThreadLocal<Set<String>> missingPrivileges = new ThreadLocal<>();

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

		Set<String> recording = missingPrivileges.get();
		if (recording != null) {
			recording.add(privilege);
		}

		return false;
	}

	/**
	 * Opens a recording scope on the current thread, so that every privilege this evaluator
	 * subsequently finds missing is collected for {@link #endRecording(Set)} to report.
	 *
	 * @return whatever scope was open before this call, to be handed back to {@link #endRecording(Set)}
	 *         so that a nested scope restores its enclosing one instead of discarding it
	 */
	static Set<String> beginRecording() {
		Set<String> enclosing = missingPrivileges.get();
		missingPrivileges.set(new LinkedHashSet<>());
		return enclosing;
	}

	/**
	 * Closes the innermost recording scope, making <code>enclosing</code> the active one again.
	 *
	 * @param enclosing the value {@link #beginRecording()} returned
	 * @return the privilege names found missing within the scope just closed, in the order they were
	 *         checked; never <code>null</code>
	 */
	static Set<String> endRecording(Set<String> enclosing) {
		Set<String> recorded = missingPrivileges.get();
		if (enclosing == null) {
			missingPrivileges.remove();
		} else {
			missingPrivileges.set(enclosing);
		}

		return recorded != null ? recorded : Collections.emptySet();
	}
}
