/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler;

public interface Task {
	
	/**
	 * Executes the task defined in the task definition.
	 */
	public void execute();
	
	/**
	 * Initializes the task and sets the task definition.
	 * 
	 * @param definition
	 */
	public void initialize(TaskDefinition definition);
	
	/**
	 * Returns the task definition associated with this task.
	 * 
	 * @return a task definition
	 */
	public TaskDefinition getTaskDefinition();
	
	/**
	 * Returns true if the task is currently in its execute() method.
	 * 
	 * @return true if task is executing, false otherwise
	 */
	boolean isExecuting();
	
	/**
	 * Callback method used to clean up resources used during the tasks execution.
	 */
	void shutdown();
	
}
