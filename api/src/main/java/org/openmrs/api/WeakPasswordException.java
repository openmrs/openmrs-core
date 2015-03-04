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

import org.openmrs.User;
import org.openmrs.util.OpenmrsUtil;

/**
 * Password exception when the password is a simple word or matches the {@link User}'s username or
 * system id.
 * <p>
 * For details on what is checked, see {@link OpenmrsUtil#validatePassword(String, String, String)}.
 * 
 * @since 1.5
 */
public class WeakPasswordException extends PasswordException {
	
	private static final long serialVersionUID = 31620091004L;
	
	public WeakPasswordException() {
		super("error.password.weak");
	}
	
	public WeakPasswordException(String message) {
		super(message);
	}
}
