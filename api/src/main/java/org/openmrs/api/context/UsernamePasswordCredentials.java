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
 * User-pass credentials represent the usual OpenMRS authentication credentials made of a pair username + password.
 * 
 * @since 2.3.0
 */
public class UsernamePasswordCredentials implements Credentials {

	protected String username;
	protected String password;
	
	public static final String SCHEME = "OPENMRS_USERPASS_AUTH_SCHEME";

	public UsernamePasswordCredentials(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

	@Override
	public String getAuthenticationScheme() {
		return SCHEME;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String getClientName() {
		return getUsername();
	}
}