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

import org.openmrs.PatientIdentifier;

public class IdentifierNotUniqueException extends PatientIdentifierException {
	
	private static final long serialVersionUID = 1L;
	
	public IdentifierNotUniqueException() {
	}
	
	public IdentifierNotUniqueException(String message) {
		super(message);
	}
	
	public IdentifierNotUniqueException(String message, PatientIdentifier identifier) {
		super(message, identifier);
	}
	
	public IdentifierNotUniqueException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public IdentifierNotUniqueException(Throwable cause) {
		super(cause);
	}
}
