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
 * Lets {@code @PreAuthorize}/{@code @PostAuthorize}/{@code @PostFilter} expressions check OpenMRS
 * privileges, e.g. {@code @PostAuthorize("hasPermission(returnObject, 'Get Providers')")}. Spring
 * Security's method security is the preferred way to enforce authorization as of 3.0.0, superseding
 * the deprecated {@link org.openmrs.annotation.Authorized} (see
 * {@code doc/AUTHORIZATION_MIGRATION.md}). It delegates to {@link Context#hasPrivilege(String)},
 * the same method {@code AuthorizationAdvice} uses, so both mechanisms decide a privilege
 * identically - superuser status, the implicit Anonymous/Authenticated roles, proxy privileges and
 * the {@code Daemon} bypass included, none of which a flat authority set can express (see
 * {@link OpenmrsAuthenticationToken#getAuthorities()}).
 * <p>
 * The built-in {@code hasAuthority('&lt;privilege&gt;')} resolves the same way, through
 * {@link OpenmrsAuthorizationManagerFactory}, so neither form is wrong. Use {@code hasAuthority}
 * for a bare privilege check and {@code hasPermission} when the expression names what is being
 * accessed ({@code returnObject}, {@code filterObject}, {@code #someArg}), since only this form is
 * handed a target; {@code hasPermission(null, '&lt;privilege&gt;')} says the same thing as
 * {@code hasAuthority} the long way round. The target is ignored today - this checks only whether
 * the user holds the named privilege.
 * <p>
 * A plain predicate: a missing privilege returns {@code false}, never throws, because SpEL composes
 * these - {@code hasPermission(...) or hasPermission(...)} has to reach its second operand, and
 * {@code @PreFilter}/{@code @PostFilter} evaluate once per element and want a verdict. Throwing
 * would stop the {@code or} short and make a filter pass everything or fail outright. Naming the
 * privilege in a denial therefore happens one level out, in
 * {@link PrivilegeNamingAuthorizationManager}, from what {@link MissingPrivilegeRecorder} collected
 * - only if the whole expression denied.
 * <p>
 * A no-value {@code @Authorized} needs nothing here: {@code @PreAuthorize("isAuthenticated()")} is
 * faithful, since {@link OpenmrsAuthenticationToken#isAuthenticated()} covers the same
 * Daemon-thread and proxy-privilege cases.
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
