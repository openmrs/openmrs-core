package org.openmrs.api;

public class DuplicateIdentifierException extends PatientIdentifierException {

	private static final long serialVersionUID = 1L;

	public DuplicateIdentifierException() {
	}

	public DuplicateIdentifierException(String message) {
		super(message);
	}

	public DuplicateIdentifierException(String message, String identifier) {
		super(message);
		this.setIdentifier(identifier);
	}

	public DuplicateIdentifierException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateIdentifierException(Throwable cause) {
		super(cause);
	}
}
