/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.util;

import org.openmrs.annotation.AddOnStartup;

/**
 * Contains all role names and their descriptions. Some of role names may be
 * marked with AddOnStartup annotation.
 * 
 * @see org.openmrs.annotation.AddOnStartup
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
