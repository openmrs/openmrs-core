/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.openmrs.util.OpenmrsUtil;

/**
 * Password exception when the password doesn't comply to the minimum set of required characters.
 * <p>
 * For details on what is checked, see {@link OpenmrsUtil#validatePassword(String, String, String)}.
 * 
 * @since 1.5
 */
public class InvalidCharactersPasswordException extends PasswordException {
	
	private static final long serialVersionUID = 31620091003L;
	
	public InvalidCharactersPasswordException() {
		super("error.password.weak");
	}
	
	public InvalidCharactersPasswordException(String message) {
		super(message);
	}
}
