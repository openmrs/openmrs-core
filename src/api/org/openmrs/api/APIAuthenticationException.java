package org.openmrs.api;

/**
 * Represents often fatal errors that occur within the API
 * infrastructure involving a user's lack of privileges.
 *  

 * @version 1.0
 */
public class APIAuthenticationException extends APIException {

	public static final long serialVersionUID = 12121213L;
	
	public APIAuthenticationException() {
	}

	public APIAuthenticationException(String message) {
		super(message);
	}

	public APIAuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}

	public APIAuthenticationException(Throwable cause) {
		super(cause);
	}

}
