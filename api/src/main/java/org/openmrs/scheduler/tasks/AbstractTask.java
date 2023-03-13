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

import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all other task classes.
 */
public abstract class AbstractTask implements Task {
	
	// Logger
	private static final Logger log = LoggerFactory.getLogger(AbstractTask.class);
	
	// Indicates whether the task is currently running
	protected boolean isExecuting = false;
	
	// The task definition of the running task
	protected TaskDefinition taskDefinition;
	
	/**
	 * Default constructor
	 */
	protected AbstractTask() {
	}
	
	/**
	 * Constructor
	 * 
	 * @param taskDefinition the task definition
	 */
	protected AbstractTask(TaskDefinition taskDefinition) {
		log.debug("Initializing " + taskDefinition.getName());
		initialize(taskDefinition);
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#execute()
	 */
	@Override
	public abstract void execute();
	
	/**
	 * @see org.openmrs.scheduler.Task#isExecuting()
	 */
	@Override
	public boolean isExecuting() {
		return isExecuting;
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#initialize(TaskDefinition)
	 */
	@Override
	public void initialize(final TaskDefinition definition) {
		this.taskDefinition = definition;
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#getTaskDefinition()
	 */
	@Override
	public TaskDefinition getTaskDefinition() {
		return this.taskDefinition;
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#shutdown()
	 */
	@Override
	public void shutdown() {
		taskDefinition = null;
	}
	
	/**
	 * Callback method that tells the task that it has started executing.
	 */
	public void startExecuting() {
		this.isExecuting = true;
	}
	
	/**
	 * Callback method that tells the task that it has stopped executing.
	 */
	public void stopExecuting() {
		this.isExecuting = false;
	}
}
