/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.tasks;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.TaskDefinition;

/**
 * Implementation of a simple task that throws an exception every 10 executions.
 */
public class TestTask extends AbstractTask {
	
	private static int executionCount = 0;
	
	// Logger 
	private Log log = LogFactory.getLog(TestTask.class);
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#initialize(TaskDefinition)
	 */
	public void initialize(TaskDefinition taskDefinition) {
		log.info("Initializing task " + taskDefinition);
	}
	
	public static void setExecutionCount(int executionCount) {
		TestTask.executionCount = executionCount;
	}
	
	/**
	 * @see org.openmrs.scheduler.tasks.AbstractTask#execute()
	 */
	@Override
	public void execute() {
		log.info("Executing task at " + new Date());
		
		setExecutionCount(executionCount + 1);
		// Throw a runtime exception once every ten executions
		if (executionCount % 10 == 0) {
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
