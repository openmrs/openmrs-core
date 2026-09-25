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
 * {@link OpenmrsAuthenticationToken#getAuthorities()}) - which is why {@code @PreAuthorize}
 * conversions in this codebase use {@code hasPermission(null, '&lt;privilege&gt;')} rather than the
 * built-in {@code hasAuthority('&lt;privilege&gt;')}.
 * <p>
 * The target object/type parameters are ignored: like {@code @Authorized}, this only checks whether
 * the current user holds the named privilege, not any per-instance ownership rule. A future
 * object-aware permission check can be added here without touching existing callers.
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
		return permission != null && Context.hasPrivilege(permission.toString());
	}

	@Override
	public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
	        Object permission) {
		return permission != null && Context.hasPrivilege(permission.toString());
	}
}
