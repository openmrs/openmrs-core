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
package org.openmrs.scheduler.tasks;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.TaskDefinition;

/**
 * Implementation of the stateful task that sends an email.
 */
public class TestTask extends AbstractTask {
	
	private static int executionCount = 0;
	
	// Logger 
	private Log log = LogFactory.getLog(TestTask.class);
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#initialize(org.openmrs.scheduler.TaskConfig)
	 */
	public void initialize(TaskDefinition taskDefinition) {
		log.info("Initializing task " + taskDefinition);
	}
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {
		log.info("Executing task at " + new Date());
		
		// Throw a runtime exception once every ten executions
		if (++executionCount % 10 == 0) {
			log.info("Throwing a runtime exception in an attempt to break the scheduler");
			throw new RuntimeException();
		}
		
		if (!Context.isAuthenticated()) {
			log.info("Authenticating ...");
			authenticate();
		}
		
	}
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#shutdown()
	 */
	public void shutdown() {
		log.info("Shutting down task ...");
		super.shutdown();
	}
	
}
