package org.openmrs.api;

public class InsufficientIdentifiersException extends PatientIdentifierException {

	private static final long serialVersionUID = 1L;

	public InsufficientIdentifiersException() {
	}

	public InsufficientIdentifiersException(String message) {
		super(message);
	}

	public InsufficientIdentifiersException(String message, Throwable cause) {
		super(message, cause);
	}

	public InsufficientIdentifiersException(Throwable cause) {
		super(cause);
	}
}
