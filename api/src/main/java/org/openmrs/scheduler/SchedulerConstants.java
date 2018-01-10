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

public class SchedulerConstants {
	
	// Number of milliseconds per second (used for readability)
	public static final int SCHEDULER_MILLIS_PER_SECOND = 1000;
	
	// 0 second delay added before the initial start of a task
	public static final long SCHEDULER_DEFAULT_DELAY = 0;
	
	public static String SCHEDULER_DEFAULT_USERNAME = "admin";
	
	public static String SCHEDULER_DEFAULT_PASSWORD = "test";
	
	/** The default 'from' address for emails send by the schedule */
	public static final String SCHEDULER_DEFAULT_FROM = "scheduler@openmrs.org";
	
	/** The default 'subject' for emails send by the schedule */
	public static final String SCHEDULER_DEFAULT_SUBJECT = "OpenMRS Scheduler Error";
	
	/** Scheduler admin email enable property - Tell us whether we can send mail or not */
	public static final String SCHEDULER_ADMIN_EMAIL_ENABLED_PROPERTY = "scheduler.admin_email_enabled";
	
	/** Scheduler admin email property - Used to email administrator if a task fails */
	public static final String SCHEDULER_ADMIN_EMAIL_PROPERTY = "scheduler.admin_email";
	
	private SchedulerConstants() {
	}
	
}
