/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
