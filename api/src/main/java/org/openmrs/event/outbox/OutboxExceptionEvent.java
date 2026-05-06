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

import org.springframework.context.ApplicationEvent;

/**
 * @since 2.9.x
 */
public class OutboxExceptionEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;
	
	public OutboxExceptionEvent(OutboxException item) {
		super(item);
	}
	
	public OutboxException getException() {
		return (OutboxException) getSource();
	}
}
