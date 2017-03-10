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
 * This exception is thrown on attempt to do some action with order, and there are multiple active
 * orders for the given concept so the action is ambiguous
 * 
 * @since 1.12
 */
public class AmbiguousOrderException extends OrderEntryException {
	
	private static final long serialVersionUID = -2946935560419378572L;
	
	public AmbiguousOrderException(String message) {
		super(message);
	}
	
	public AmbiguousOrderException(String messageKey, Object[] parameters) {
		super(messageKey, parameters);
	}
	
}
