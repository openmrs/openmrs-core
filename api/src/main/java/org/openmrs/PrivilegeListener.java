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

/**
 * Any beans implementing this interface will be picked up to listen to the API
 * privilege checks.
 * 
 * @since 1.10
 */
public interface PrivilegeListener {
	
	/**
	 * Called whenever a privilege is checked.
	 * 
	 * @param user
	 * @param privilege
	 * @param hasPrivilege 
	 */
	public void privilegeChecked(User user, String privilege, boolean hasPrivilege);
}
