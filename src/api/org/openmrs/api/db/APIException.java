package org.openmrs.api.db;

/**
 * Represents often fatal errors that occur within the API
 * infrastructure.
 *  
 * @author Burke Mamlin
 * @version 1.0
 */
public class APIException extends RuntimeException {

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
