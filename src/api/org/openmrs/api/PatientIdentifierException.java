package org.openmrs.api;


public class PatientIdentifierException extends APIException {

	private static final long serialVersionUID = 1L;
	
	private String identifier;

	public PatientIdentifierException() {
	}

	public PatientIdentifierException(String message) {
		super(message);
	}

	public PatientIdentifierException(String message, String identifier) {
		super(message);
		this.setIdentifier(identifier);
	}

	public PatientIdentifierException(String message, Throwable cause) {
		super(message, cause);
	}

	public PatientIdentifierException(Throwable cause) {
		super(cause);
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}
