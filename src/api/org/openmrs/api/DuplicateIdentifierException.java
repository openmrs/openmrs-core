package org.openmrs.api;

import org.openmrs.PatientIdentifier;

public class DuplicateIdentifierException extends PatientIdentifierException {

	private static final long serialVersionUID = 1L;

	public DuplicateIdentifierException() {
	}

	public DuplicateIdentifierException(String message) {
		super(message);
	}

	public DuplicateIdentifierException(String message, PatientIdentifier identifier) {
		super(message, identifier);
	}
	public DuplicateIdentifierException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateIdentifierException(Throwable cause) {
		super(cause);
	}
}
