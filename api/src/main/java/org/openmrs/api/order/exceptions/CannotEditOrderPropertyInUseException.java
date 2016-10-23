/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.order.exceptions;

import org.openmrs.api.APIException;

/**
 * Thrown when trying to edit an order property which is still in use.
 */
public class CannotEditOrderPropertyInUseException extends APIException {
	
	public static final long serialVersionUID = 22121220L;
	
	private CannotEditOrderPropertyInUseException(String message) {
		super(message);
	}
	
	public static CannotEditOrderPropertyInUseException withProperty(String property) {
		return new CannotEditOrderPropertyInUseException(String.format("Order.%s.cannot.edit", property));
	}
}
