package org.openmrs.api;

/**
 * Represents often fatal errors that occur within the API
 * infrastructure.
 *  
 * @version 1.0
 */
public class APIException extends RuntimeException {

	public static final long serialVersionUID = 12121212L;
	
	public APIException() {
	}

	public APIException(String message) {
		super(message);
	}

	public APIException(String message, Throwable cause) {
		super(message, cause);
	}

	public APIException(Throwable cause) {
		super(cause);
	}

}
