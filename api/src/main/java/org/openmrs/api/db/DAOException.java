/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import org.openmrs.api.APIException;

/**
 * Represents often fatal errors that occur within the database layer.
 */
public class DAOException extends APIException {

	private static final long serialVersionUID = -185144340435149253L;

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

	/**
	 * Constructor to give the end user a helpful message resolved from the given message code, e.g. a
	 * key from the messages properties file.
	 *
	 * @param messageKey message code to retrieve
	 * @param messageParameters message parameters
	 */
	public DAOException(String messageKey, Object[] messageParameters) {
		super(messageKey, messageParameters);
	}

	/**
	 * Constructor to give the end user a helpful message resolved from the given message code and to
	 * also propagate the parent error exception.
	 *
	 * @param messageKey message code to retrieve
	 * @param messageParameters message parameters
	 * @param cause the parent exception cause that this exception is wrapping around
	 */
	public DAOException(String messageKey, Object[] messageParameters, Throwable cause) {
		super(messageKey, messageParameters, cause);
	}

}
