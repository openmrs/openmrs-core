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

/**
 * Authentication schemes define and require their own credentials.
 * Any client authenticating against a given scheme must supply appropriate credentials.
 * 
 * @since 2.3.0
 */
public interface Credentials {
	
	/**
	 * @return The authentication scheme that should be used with those credentials.
	 */
	public String getAuthenticationScheme();
	
	/**
	 * A string that identifies the owner of the credentials by a name.
	 * Typically in the case of a user the client would simply be identified with its username. 
	 */
	public String getClientName();
	
}
