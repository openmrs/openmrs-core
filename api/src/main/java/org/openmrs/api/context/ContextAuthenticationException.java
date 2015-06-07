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

import org.openmrs.api.APIException;

/**
 * This exception is thrown when a user attempts to access a service or object that they do not have
 * rights to.
 */
public class ContextAuthenticationException extends APIException {
	
	public static final long serialVersionUID = 22323L;
	
	public ContextAuthenticationException() {
		super();
	}
	
	public ContextAuthenticationException(String message) {
		super(message);
	}
	
	public ContextAuthenticationException(Throwable cause) {
		super(cause);
	}
	
}
