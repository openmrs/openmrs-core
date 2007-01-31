package org.openmrs.api;

public class InvalidIdentifierFormatException extends PatientIdentifierException {

	private static final long serialVersionUID = 1L;

	private String format; 
	
	public InvalidIdentifierFormatException() {
	}

	public InvalidIdentifierFormatException(String message) {
		super(message);
	}

	public InvalidIdentifierFormatException(String message, String identifier) {
		super(message);
		this.setIdentifier(identifier);
	}

	public InvalidIdentifierFormatException(String message, String identifier, String format) {
		super(message);
		this.setIdentifier(identifier);
		this.setFormat(format);
	}

	public InvalidIdentifierFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidIdentifierFormatException(Throwable cause) {
		super(cause);
	}


	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}
