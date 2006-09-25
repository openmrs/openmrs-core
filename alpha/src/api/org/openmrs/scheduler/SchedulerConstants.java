package org.openmrs.scheduler;

public class SchedulerConstants {

	public static int SCHEDULER_MILLIS_PER_SECOND = 1000; 

	
	// These constants should not be used.  Global properties are now used to set 
	//  the username/password.
	// These constants are left in to keep compatibility.  They can be deleted
	//  once people have moved away from runtime properties
	public static String SCHEDULER_USERNAME = "admin";
	public static String SCHEDULER_PASSWORD = "test";
	
}
