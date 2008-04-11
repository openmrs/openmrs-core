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
	
	
	// These constants should not be used.  Global properties are now used to set 
	//  the username/password.
	// These constants are left in to keep compatibility.  They can be deleted
	//  once people have moved away from runtime properties
	public static String SCHEDULER_USERNAME = "admin";
	public static String SCHEDULER_PASSWORD = "test";	
}
