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

import org.openmrs.util.OpenmrsUtil;

/**
 * Password exception when the length is less than the minimum allowed.
 * <p>
 * For details on what is checked, see {@link OpenmrsUtil#validatePassword(String, String, String)}.
 * 
 * @since 1.5
 */
public class ShortPasswordException extends PasswordException {
	
	private static final long serialVersionUID = 31620091002L;
	
	public ShortPasswordException() {
		super("error.password.length");
	}
	
	public ShortPasswordException(String message) {
		super(message);
	}
}
