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
 * Represents common exceptions that happen when validating a {@link User}'s password.
 * <p>
 * Preferred use of this class and its subclass are the {@link #PasswordException()} or the
 * {@link #PasswordException(String)}. The empty constructor will create an exception object using
 * without an exception message.
 * <p>
 * For details on what is checked, see {@link OpenmrsUtil#validatePassword(String, String, String)}.
 * 
 * @since 1.5
 */
public class PasswordException extends APIException {
	
	private static final long serialVersionUID = 31620091001L;
	
	public PasswordException() {
		super();
	}
	
	public PasswordException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public PasswordException(String message) {
		super(message);
	}
	
	public PasswordException(Throwable cause) {
		super(cause);
	}
}
