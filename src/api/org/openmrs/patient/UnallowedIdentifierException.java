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
package org.openmrs.patient;

import org.openmrs.api.APIException;

/**
 *	UnallowedIdentifierException is thrown when a class tries to process an identifier
 *	given certain expectations and those expectations are not held up.  For example,
 *	if a class expects identifiers to consist only of numeric digits and an identifier
 *	contains a non-numeric digit, this exception could be thrown.
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
