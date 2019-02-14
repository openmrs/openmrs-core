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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default OpenMRS authentication scheme to login with OpenMRS' usernames and passwords.
 * 
 * @see {@link AuthenticationScheme}
 * 
 * @since 2.3.0
 */
public class UserPassAuthenticationScheme extends DaoAuthenticationScheme {

	protected static Log log = LogFactory.getLog(UserPassAuthenticationScheme.class);
	
	@Override
	public Authenticated authenticate(Credentials credentials)
			throws ContextAuthenticationException {

		if (log.isDebugEnabled()) {
			log.debug("Authenticating client: " + credentials.getClientName());
		}
		
		UserPassCredentials userPassCreds = null;
		try {
			userPassCreds = (UserPassCredentials) credentials;
		}
		catch (ClassCastException e) {
			throw new ContextAuthenticationException("The provided credentials could not be used to authenticated with the specified authentication scheme.", e);
		}
		
		return new BasicAuthenticated( getContextDAO().authenticate(userPassCreds.getUsername(), userPassCreds.getPassword()) , UserPassCredentials.SCHEME);
	}
}