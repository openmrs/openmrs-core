package org.openmrs.scheduler;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

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

		Context.getSchedulerService().startup();
	}
	
	/**
	 * Shutdown the scheduler service that is statically associated with the Context class.
	 */
	public static void shutdown() {
		SchedulerService service = null;
		
		// ignores errors while getting the scheduler service 
		try {
			service = Context.getSchedulerService();
		}
		catch (Throwable t) {
			// pass
		}
		
		// doesn't attempt shutdown if there was an error getting the scheduler service
		if (service != null)
			service.shutdown();
		
	}

}
