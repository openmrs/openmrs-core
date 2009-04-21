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
