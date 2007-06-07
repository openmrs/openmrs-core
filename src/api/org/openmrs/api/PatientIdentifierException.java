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
