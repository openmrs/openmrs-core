/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.openmrs.annotation.AddOnStartup;

/**
 * Contains all role names and their descriptions. Some of role names may be marked with
 * AddOnStartup annotation.
 * 
 * @see org.openmrs.annotation.AddOnStartup
 * @since 1.8
 */
public class RoleConstants {
	
	@AddOnStartup(description = "Assigned to Administrators of OpenMRS. Gives additional access to change core aspects of the system.")
	public static final String SUPERUSER = "System Developer";
	
	@AddOnStartup(description = "Privileges for non-authenticated users.")
	public static final String ANONYMOUS = "Anonymous";
	
	@AddOnStartup(description = "Privileges gained once authentication has been established.")
	public static final String AUTHENTICATED = "Authenticated";
	
	@AddOnStartup(description = "All users with the 'Provider' role will appear as options in the default Infopath ")
	public static final String PROVIDER = "Provider";
	
}
