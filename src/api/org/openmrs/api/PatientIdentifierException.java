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


public class PatientIdentifierException extends APIException {

	private static final long serialVersionUID = 1L;
	
	private PatientIdentifier patientIdentifier;

	public PatientIdentifierException() {
	}

	public PatientIdentifierException(String message) {
		super(message);
	}

	public PatientIdentifierException(String message, PatientIdentifier identifier) {
		super(message);
		this.setPatientIdentifier(identifier);
	}

	public PatientIdentifierException(String message, Throwable cause) {
		super(message, cause);
	}

	public PatientIdentifierException(Throwable cause) {
		super(cause);
	}
	

	public PatientIdentifier getPatientIdentifier() {
		return patientIdentifier;
	}

	public void setPatientIdentifier(PatientIdentifier patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}
}
