/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

/**
 * An error of this type is thrown when a concept name is found in the database when one tries to
 * create a new one with the same preferred name in the same locale
 */
public class DuplicateConceptNameException extends APIException {
	
	private static final long serialVersionUID = -1488550917363129782L;
	
	public DuplicateConceptNameException() {
		super("Duplicate concept name found");
	}
	
	public DuplicateConceptNameException(String message) {
		super(message);
	}
}
