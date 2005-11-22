package org.openmrs.api.db;

/**
 * Represents an access violation
 * 
 * @author Burke Mamlin
 * @version 1.0
 */
public class PermissionException extends SecurityException {

	public PermissionException() {
	}

	public PermissionException(String message) {
		super(message);
	}

	public PermissionException(String message, Throwable cause) {
		super(message, cause);
	}

	public PermissionException(Throwable cause) {
		super(cause);
	}

}
