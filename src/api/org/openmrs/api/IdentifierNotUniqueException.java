package org.openmrs.api;

import org.openmrs.PatientIdentifier;

public class IdentifierNotUniqueException extends PatientIdentifierException {

	private static final long serialVersionUID = 1L;

	public IdentifierNotUniqueException() {
	}

	public IdentifierNotUniqueException(String message) {
		super(message);
	}

	public IdentifierNotUniqueException(String message, PatientIdentifier identifier) {
		super(message, identifier);
	}

	public IdentifierNotUniqueException(String message, Throwable cause) {
		super(message, cause);
	}

	public IdentifierNotUniqueException(Throwable cause) {
		super(cause);
	}
}
