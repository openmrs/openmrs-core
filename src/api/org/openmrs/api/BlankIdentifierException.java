package org.openmrs.api;

import org.openmrs.PatientIdentifier;

public class BlankIdentifierException extends PatientIdentifierException {

	private static final long serialVersionUID = 1L;

	public BlankIdentifierException() {
	}

	public BlankIdentifierException(String message) {
		super(message);
	}

	public BlankIdentifierException(String message, PatientIdentifier identifier) {
		super(message, identifier);
	}
	public BlankIdentifierException(String message, Throwable cause) {
		super(message, cause);
	}

	public BlankIdentifierException(Throwable cause) {
		super(cause);
	}
}
