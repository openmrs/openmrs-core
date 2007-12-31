package org.openmrs.api.db;

import org.openmrs.api.APIException;

/**
 * Represents often fatal errors that occur within the database
 * layer.
 *  
 * @version 1.0
 */
public class DAOException extends APIException {

	public DAOException() {
	}

	public DAOException(String message) {
		super(message);
	}

	public DAOException(String message, Throwable cause) {
		super(message, cause);
	}

	public DAOException(Throwable cause) {
		super(cause);
	}

}
