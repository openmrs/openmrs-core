/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.ai;

import org.openmrs.api.APIException;

/**
 * Thrown when an AI service operation is attempted but no AI provider module is registered or the
 * AI service is not currently available.
 * 
 * @since 3.0.0
 */
public class AiServiceUnavailableException extends APIException {
	
	private static final long serialVersionUID = 1L;
	
	public AiServiceUnavailableException() {
		super("No AI service provider is currently available. " + "Please install and configure an AI provider module.");
	}
	
	public AiServiceUnavailableException(String message) {
		super(message);
	}
	
	public AiServiceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
