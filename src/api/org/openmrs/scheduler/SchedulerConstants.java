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

public class SchedulerConstants {
	
	// Number of milliseconds per second (used for readability)
	public static int SCHEDULER_MILLIS_PER_SECOND = 1000; 
	
	// 30 second delay added before the initial start of a task
	public static long SCHEDULER_DEFAULT_DELAY = 30000;
	
	// These constants are left in to keep compatibility.  They can be deleted
	//  once people have moved away from runtime properties
	/** @deprecated */
	public static String SCHEDULER_DEFAULT_USERNAME = "admin";

	/** @deprecated */
	public static String SCHEDULER_DEFAULT_PASSWORD = "test";	

	/** The default 'from' address for emails send by the schedule */
	public final static String SCHEDULER_DEFAULT_FROM = "scheduler@openmrs.org";

	/** The default 'subject' for emails send by the schedule */
	public final static String SCHEDULER_DEFAULT_SUBJECT = "OpenMRS Scheduler Error";

	/** Scheduler username property - Used to create authenticated context for tasks */
	public static String SCHEDULER_USERNAME_PROPERTY = "scheduler.username";
	
	/** Scheduler password property - Used to create authenticated context for tasks */
	public static String SCHEDULER_PASSWORD_PROPERTY = "scheduler.password";

	/** Scheduler admin email enable property - Tell us whether we can send mail or not */ 
	public static String SCHEDULER_ADMIN_EMAIL_ENABLED_PROPERTY = "scheduler.admin_email_enabled";

	/** Scheduler admin email property - Used to email administrator if a task fails */
	public static String SCHEDULER_ADMIN_EMAIL_PROPERTY = "scheduler.admin_email";
	
	
}
