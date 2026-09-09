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

import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;

/**
 * Resolves a privilege exactly as {@code AuthorizationAdvice} does, including the {@code Get Roles}
 * proxy privilege it brackets its own checks with. That bracket is load-bearing:
 * {@code UserContext.resolvePrivilege} reads the implicit Anonymous and Authenticated roles through
 * the privilege-checked {@code UserService}, so a check that did not hold {@code Get Roles} would
 * re-enter privilege resolution and never terminate.
 */
final class PrivilegeResolution {

	private PrivilegeResolution() {
	}

	static boolean holdsPrivilege(String privilege) {
		Context.addProxyPrivilege(PrivilegeConstants.GET_ROLES);
		try {
			return Context.hasPrivilege(privilege);
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_ROLES);
		}
	}
}
