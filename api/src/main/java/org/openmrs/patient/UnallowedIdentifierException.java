/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.patient;

import org.openmrs.api.APIException;

/**
 * UnallowedIdentifierException is thrown when a class tries to process an identifier given certain
 * expectations and those expectations are not held up. For example, if a class expects identifiers
 * to consist only of numeric digits and an identifier contains a non-numeric digit, this exception
 * could be thrown.
 */
public class UnallowedIdentifierException extends APIException {
	
	/**
	 * Compiler generated serial version uid.
	 */
	private static final long serialVersionUID = -1460246384367910860L;
	
	public UnallowedIdentifierException() {
	}
	
	public UnallowedIdentifierException(String message) {
		super(message);
	}
	
	public UnallowedIdentifierException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public UnallowedIdentifierException(Throwable cause) {
		super(cause);
	}
}
