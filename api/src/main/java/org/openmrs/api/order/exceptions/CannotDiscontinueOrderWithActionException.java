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

import static org.openmrs.Order.Action.DISCONTINUE;

import org.openmrs.api.APIException;

/**
 * Thrown with trying to discontinue an order with a specific action that cannot be discontinued.
 */
public class CannotDiscontinueOrderWithActionException extends APIException {

	public static final long serialVersionUID = 22121215L;
	
	public CannotDiscontinueOrderWithActionException() {
		super("Order.action.cannot.discontinued", new Object[] { DISCONTINUE });
	}
}