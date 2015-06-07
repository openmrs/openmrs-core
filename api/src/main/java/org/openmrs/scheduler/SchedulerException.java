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

public class SchedulerException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4462693049954360187L;
	
	public SchedulerException() {
		super();
	}
	
	public SchedulerException(Throwable cause) {
		super(cause);
	}
	
	public SchedulerException(String message) {
		super(message);
	}
	
	public SchedulerException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
