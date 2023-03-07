/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest;

/**
 * Used as a workaround for a few places where Spring will not let you throw a checked exception
 * (for example if you want to throw ObjectNotFoundException in a property editor.
 */
public class RuntimeWrappedException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public RuntimeWrappedException(Exception ex) {
		super(ex);
	}
	
}
