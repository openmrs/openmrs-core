/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.openmrs.api.context.UserContext;

/**
 * Allows to track down which privileges are checked during code execution and
 * makes it easier for an administrator to assign required privileges to users.
 * <p> 
 * Beans implementing this interface will be picked up to listen to any
 * privilege checks. Listeners will be notified about any call to {@link UserContext#hasPrivilege(java.lang.String)}.
 *
 * @since 1.8.4, 1.9.1, 1.10
 */
public interface PrivilegeListener {
	
	/**
	 * Called whenever a privilege is checked.
	 *
	 * @param user the authenticated user or <code>null</code> if not authenticated
	 * @param privilege the checked privilege
	 * @param hasPrivilege <code>true</code> if the authenticated user has the required privilege or if it is a proxy privilege
	 * @since 1.8.4, 1.9.1, 1.10
	 */
	public void privilegeChecked(User user, String privilege, boolean hasPrivilege);
}
