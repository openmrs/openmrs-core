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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default OpenMRS authentication scheme to login with OpenMRS' usernames and passwords.
 *
 * @see AuthenticationScheme
 *
 * @since 2.3.0
 */
public class UsernamePasswordAuthenticationScheme extends DaoAuthenticationScheme {

	private static final Logger log = LoggerFactory.getLogger(UsernamePasswordAuthenticationScheme.class);

	@Override
	public Authenticated authenticate(Credentials credentials)
		throws ContextAuthenticationException {
		log.debug("Authenticating client: {}", credentials.getClientName());

		if (!(credentials instanceof UsernamePasswordCredentials)) {
			throw new ContextAuthenticationException(
				"The provided credentials could not be used to authenticated with the specified authentication scheme.");
		}

		UsernamePasswordCredentials userPassCreds = (UsernamePasswordCredentials) credentials;
		return new BasicAuthenticated(getContextDAO().authenticate(userPassCreds.getUsername(), userPassCreds.getPassword()),
			UsernamePasswordCredentials.SCHEME);
	}

}
