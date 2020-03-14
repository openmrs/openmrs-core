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

/**
 * An instance of this exception is thrown if an attempt is made to get an obs from a ComplexObsHandler
 * with an unsupported view
 * 
 */
public class UnsupportedViewException extends APIException {
	
	public UnsupportedViewException(String message) {
		super(message);
	}
	
	public UnsupportedViewException() {
		super("Obs.error.unsupported.view");
	}
}
