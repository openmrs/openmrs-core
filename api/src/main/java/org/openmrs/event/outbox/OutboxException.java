/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event.outbox;

import org.openmrs.event.EventException;

/**
 * @since 2.9.0
 */
public class OutboxException extends EventException {

	private static final long serialVersionUID = 1L;

	public OutboxException() {
	}

	public OutboxException(String message) {
		super(message);
	}

	public OutboxException(String message, Throwable cause) {
		super(message, cause);
	}

	public OutboxException(Throwable cause) {
		super(cause);
	}
}
