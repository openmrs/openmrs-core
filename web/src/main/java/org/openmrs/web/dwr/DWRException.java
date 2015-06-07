/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

/**
 * This class is no longer needed. If a dwr method throws <b>any</b> error, the user will see a div
 * on the screen filled with the error message.
 * 
 * @deprecated use the appropriate exception instead of this one
 */
@Deprecated
public class DWRException extends Exception {
	
	private static final long serialVersionUID = 6820574097052501261L;
	
	/**
	 * @see java.lang.Exception#Exception(java.lang.String)
	 */
	public DWRException(String message) {
		super(message);
	}
}
