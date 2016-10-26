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
 * Thrown when an attempt to unvoid an order fails.
 */
public class CannotUnvoidOrderException extends APIException {
	
	public static final long serialVersionUID = 22121315L;
	
	public CannotUnvoidOrderException(String action) {
		super("Order.action.cannot.unvoid", new Object[] { action });
	}
}
