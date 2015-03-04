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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Daemon;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;

/**
 * Base class for all other task classes.
 */
public abstract class AbstractTask implements Task {
	
	// Logger
	private static final Log log = LogFactory.getLog(AbstractTask.class);
	
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
	public abstract void execute();
	
	/**
	 * @see org.openmrs.scheduler.Task#isExecuting()
	 */
	public boolean isExecuting() {
		return isExecuting;
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#initialize(TaskDefinition)
	 */
	public void initialize(final TaskDefinition definition) {
		this.taskDefinition = definition;
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#getTaskDefinition()
	 */
	public TaskDefinition getTaskDefinition() {
		return this.taskDefinition;
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#shutdown()
	 */
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
	
	/**
	 * @deprecated this method is not used anymore. All threads are run as the {@link Daemon} user
	 */
	@Deprecated
	protected void authenticate() {
		// do nothing
	}
}
