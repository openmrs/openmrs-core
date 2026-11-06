/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event;

/**
 * Issued by the CDC engine upon completed transaction. It can be used to gather events belonging to
 * the same transaction.
 *
 * @since 2.9.0
 */
public class CDCTransactionCompletedEvent extends BaseEvent {

	private static final long serialVersionUID = 1L;

	private String transactionId;

	public CDCTransactionCompletedEvent() {
	}

	public CDCTransactionCompletedEvent(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
}
