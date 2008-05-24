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
package org.openmrs.api.context;

import org.openmrs.api.APIException;

/**
 * This exception is thrown when a user attempts to access a service
 * or object that they do not have rights to.  
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
