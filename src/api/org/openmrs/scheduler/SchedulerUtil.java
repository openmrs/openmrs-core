package org.openmrs.scheduler;

import java.util.Properties;

import org.openmrs.api.context.ContextFactory;

public class SchedulerUtil {

	/**
	 * Start the scheduler given the following start up properties.
	 * @param p  properties used to start the service
	 */
	public static void startup(Properties p) {

		// Override the Scheduler constants if specified by the user

		String val = p.getProperty("scheduler.username", null);
		if (val != null)
			SchedulerConstants.SCHEDULER_USERNAME = val;

		val = p.getProperty("scheduler.password", null);
		if (val != null)
			SchedulerConstants.SCHEDULER_PASSWORD = val;

		startup();
	}

	/**
	 * Start the scheduler service that is statically associated with the Context class.
	 * 
	 * TODO This should be the only scheduler started, but there's currently 
	 * no way of stopping a user from creating their own SchedulerService.
	 */
	public static void startup() {
		ContextFactory.getContext().getSchedulerService().startup();
	}
	
	/**
	 * Shutdown the scheduler service that is statically associated with the Context class.
	 */
	public static void shutdown() { 
		ContextFactory.getContext().getSchedulerService().shutdown();		
	}

}
