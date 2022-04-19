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

import org.openmrs.PatientIdentifier;

public class InvalidIdentifierFormatException extends PatientIdentifierException {
	
	private static final long serialVersionUID = 1L;
	
	private final String format;
	
	public InvalidIdentifierFormatException() {
		format = null;
	}
	
	public InvalidIdentifierFormatException(String message, String format) {
		super(message);
		this.format = format;
	}
	
	public InvalidIdentifierFormatException(String message, PatientIdentifier identifier) {
		super(message, identifier);
		format = null;
	}
	
	public InvalidIdentifierFormatException(String message, Throwable cause) {
		super(message, cause);
		format = null;
	}
	
	public InvalidIdentifierFormatException(Throwable cause) {
		super(cause);
		format = null;
	}
	
	public String getFormat() {
		return format;
	}
	
}
