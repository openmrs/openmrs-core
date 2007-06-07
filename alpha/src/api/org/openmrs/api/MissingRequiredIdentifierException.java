package org.openmrs.api;

import org.openmrs.PatientIdentifier;

public class MissingRequiredIdentifierException extends PatientIdentifierException {

	private static final long serialVersionUID = 1L;

	public MissingRequiredIdentifierException() {
	}

	public MissingRequiredIdentifierException(String message) {
		super(message);
	}

	public MissingRequiredIdentifierException(String message, PatientIdentifier identifier) {
		super(message, identifier);
	}
	public MissingRequiredIdentifierException(String message, Throwable cause) {
		super(message, cause);
	}

	public MissingRequiredIdentifierException(Throwable cause) {
		super(cause);
	}
}
