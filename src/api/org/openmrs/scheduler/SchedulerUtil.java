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
