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

public class PatientIdentifierException extends APIException {
	
	private static final long serialVersionUID = 1L;
	
	private final PatientIdentifier patientIdentifier;
	
	public PatientIdentifierException() {
		patientIdentifier = null;
	}
	
	public PatientIdentifierException(String message) {
		super(message);
		patientIdentifier = null;
	}
	
	public PatientIdentifierException(String message, PatientIdentifier identifier) {
		super(message);
		patientIdentifier = identifier;
	}
	
	public PatientIdentifierException(String message, Throwable cause) {
		super(message, cause);
		patientIdentifier = null;
	}
	
	public PatientIdentifierException(Throwable cause) {
		super(cause);
		patientIdentifier = null;
	}
	
	public PatientIdentifier getPatientIdentifier() {
		return patientIdentifier;
	}
	

}
