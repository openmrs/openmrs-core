package org.openmrs.api;

import org.openmrs.PatientIdentifier;

public class InvalidCheckDigitException extends PatientIdentifierException {

	private static final long serialVersionUID = 1L;

	public InvalidCheckDigitException() {
	}

	public InvalidCheckDigitException(String message) {
		super(message);
	}

	public InvalidCheckDigitException(String message, PatientIdentifier identifier) {
		super(message, identifier);
	}

	public InvalidCheckDigitException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidCheckDigitException(Throwable cause) {
		super(cause);
	}
}
