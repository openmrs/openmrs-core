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

/**
 * @since 2.9.0
 */
public class OutboxExceptionEvent {

	private static final long serialVersionUID = 1L;

	private OutboxException exception;

	private String outboxItemUuid;

	private boolean pendingRetry;

	private int retryCount;

	public OutboxExceptionEvent() {
	}

	public OutboxExceptionEvent(OutboxException exception) {
		this.exception = exception;
	}

	public OutboxExceptionEvent(OutboxException exception, String outboxItemUuid) {
		this.exception = exception;
		this.outboxItemUuid = outboxItemUuid;
	}

	public OutboxException getException() {
		return exception;
	}

	public void setException(OutboxException exception) {
		this.exception = exception;
	}

	public String getOutboxItemUuid() {
		return outboxItemUuid;
	}

	public void setOutboxItemUuid(String outboxItemUuid) {
		this.outboxItemUuid = outboxItemUuid;
	}

	public boolean isPendingRetry() {
		return pendingRetry;
	}

	public void setPendingRetry(boolean pendingRetry) {
		this.pendingRetry = pendingRetry;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
}
