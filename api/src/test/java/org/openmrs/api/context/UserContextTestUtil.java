/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import org.openmrs.User;

/**
 * Lets tests authenticate a user directly, without credentials.
 */
public final class UserContextTestUtil {

	private UserContextTestUtil() {
	}

	/**
	 * Makes the user the authenticated one. A user that isn't stored, such as one built in a test,
	 * holds the roles it has in memory when this is called; a stored user holds its stored roles.
	 *
	 * @param userContext the user context to change
	 * @param user the user to authenticate, or null to log out
	 */
	public static void setAuthenticatedUser(UserContext userContext, User user) {
		userContext.setUser(user);
	}
}
