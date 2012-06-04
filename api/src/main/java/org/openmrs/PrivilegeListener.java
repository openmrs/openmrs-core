/**
 * The contents of this file are subject to the OpenMRS Public License Version
 * 1.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * Copyright (C) OpenMRS, LLC. All Rights Reserved.
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
