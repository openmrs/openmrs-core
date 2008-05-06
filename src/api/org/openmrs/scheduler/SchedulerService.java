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

import java.util.Collection;
import java.util.SortedMap;

import org.openmrs.annotation.Authorized;
import org.openmrs.util.OpenmrsMemento;
import org.springframework.transaction.annotation.Transactional;

/**
 *  Defines methods required to schedule a task.  
 */
@Transactional
public interface SchedulerService { 

	/**
	 * Checks the status of a scheduled task.  
	 * 
	 * @param id
	 * @return
	 */
	@Authorized({"Manage Scheduler"})
	public String getStatus(Integer id);
	
	
	/**
	 * Start all tasks that are scheduled to run on startup.
	 */
	@Authorized({"Manage Scheduler"})
	void startup();
	
	/**
	 * Stop all tasks and clean up the scheduler instance.
	 */
	@Authorized({"Manage Scheduler"})
	void shutdown();
	
	/**
	 * Cancel a scheduled task.
	 */
	@Authorized({"Manage Scheduler"})
	void shutdownTask(TaskDefinition task) throws SchedulerException;

	/**
	 * Start a scheduled task.
	 */
	@Authorized({"Manage Scheduler"})
	void scheduleTask(TaskDefinition task) throws SchedulerException;
	
	/**
	 * Stop and start a scheduled task.
	 */
	@Authorized({"Manage Scheduler"})
	void rescheduleTask(TaskDefinition task) throws SchedulerException;
	
	/**
	 * Loop over all currently started tasks and cycle them.
	 * This should be done after the classloader has been changed
	 * (e.g. during module start/stop)
	 */
	@Authorized({"Manage Scheduler"})
	void rescheduleAllTasks() throws SchedulerException;
	
	/**
	 * Get scheduled tasks.
	 *  
	 * @return 	all scheduled tasks
	 */
	@Authorized({"Manage Scheduler"})
	@Transactional(readOnly=true)
	Collection<TaskDefinition> getScheduledTasks();

	/**
	 * Get the list of tasks that are available to be scheduled.  
	 * Eventually, these should go in the database.
	 * 
	 * @return	all available tasks
	 */
	@Authorized({"Manage Scheduler"})
	@Transactional(readOnly=true)
	Collection<TaskDefinition> getRegisteredTasks();

	/**
	 * Get the task with the given identifier.
	 *  
	 * @param	id 		the identifier of the task
	 */
	@Authorized({"Manage Scheduler"})
	@Transactional(readOnly=true)
	TaskDefinition getTask(Integer id);

	/**
	 * Delete the task with the given identifier.
	 *  
	 * @param	id 		the identifier of the task
	 */
	@Authorized({"Manage Scheduler"})
	void deleteTask(Integer id);

	/**
	 * Create the given task
	 *  
	 * @param	task 		the task to be created
	 */
	@Authorized({"Manage Scheduler"})
	void saveTask(TaskDefinition task);
	
	/**
	 * Return SchedulerConstants
	 * @return
	 */
	@Transactional(readOnly=true)
	SortedMap<String,String> getSystemVariables();	
	
	/**
	 * Save the state of the scheduler service to Memento
	 * @return
	 */
	OpenmrsMemento saveToMemento();
	
	/**
	 * Restore the scheduler service to state defined by Memento
	 * @param memento
	 * @return
	 */
	void restoreFromMemento(OpenmrsMemento memento);
}
