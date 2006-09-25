package org.openmrs.scheduler;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.ContextFactory;

public class SchedulerUtil {

	private static Log log = LogFactory.getLog(SchedulerUtil.class);
	/**
	 * Start the scheduler given the following start up properties.
	 * @param p  properties used to start the service
	 */
	public static void startup(Properties p) {

		// Override the Scheduler constants if specified by the user

		String val = p.getProperty("scheduler.username", null);
		if (val != null) {
			SchedulerConstants.SCHEDULER_USERNAME = val;
			log.warn("Deprecated runtime property: scheduler.username. Value set in global_property in database now.");
		}

		val = p.getProperty("scheduler.password", null);
		if (val != null) {
			SchedulerConstants.SCHEDULER_PASSWORD = val;
			log.warn("Deprecated runtime property: scheduler.username. Value set in global_property in database now.");
		}

		startup();
	}

	/**
	 * Start the scheduler service that is statically associated with the Context class.
	 * 
	 * TODO This should be the only scheduler started, but there's currently 
	 * no way of stopping a user from creating their own SchedulerService.
	 */
	public static void startup() {
		ContextFactory.getContext().getSchedulerService(); // issues .startup() method
	}
	
	/**
	 * Shutdown the scheduler service that is statically associated with the Context class.
	 */
	public static void shutdown() { 
		ContextFactory.getContext().getSchedulerService().shutdown();		
	}

}
