/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler;

/**
 * Indicates an issue with the task. You may use it to prevent retries.
 *
 * @since 2.9.x
 */
public class TaskException extends Exception {

	private final boolean doNotRetry;

	public TaskException(String message) {
		super(message);
		doNotRetry = false;
	}

	/**
	 * @param message
	 * @param doNotRetry if true, the task will not be retried <b>(false by default)</b>
	 */
	public TaskException(String message, boolean doNotRetry) {
		super(message);
		this.doNotRetry = doNotRetry;
	}

	public TaskException(String message, Throwable cause) {
		super(message, cause);
		doNotRetry = false;
	}

	/**
	 * @param message
	 * @param doNotRetry if true, the task will not be retried <b>(false by default)</b>
	 * @param cause
	 */
	public TaskException(String message, boolean doNotRetry, Throwable cause) {
		super(message, cause);
		this.doNotRetry = doNotRetry;
	}

	public boolean isDoNotRetry() {
		return doNotRetry;
	}
}
