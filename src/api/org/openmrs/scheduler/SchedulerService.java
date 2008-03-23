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
	 * Set the data access context.
	 */
	//public void setDaoContext(DAOContext daoContext);
	
	/**
	 * Start all tasks that should be running
	 */
	public void startup();
	
	/**
	 * Stop all tasks and clean up.
	 *
	 */
	public void shutdown();
	
	/**
	 * Schedule a recurring task that occurs according to the given schedule (start time and interval).
	 */
	public void scheduleTask(Integer taskId) throws SchedulerException;

	/**
	 * Schedule a recurring task that occurs according to the given schedule (start time and interval).
	 */
	public void scheduleTask(TaskConfig task) throws SchedulerException;	

	/**
	 * Cancel a scheduled task.
	 */
	public void stopTask(Integer taskId) throws SchedulerException;

	/**
	 * Cancel a scheduled task.
	 */
	public void stopTask(TaskConfig task) throws SchedulerException;

	/**
	 * Cancel a scheduled task.
	 */
	public void startTask(Integer taskId) throws SchedulerException;

	/**
	 * Cancel a scheduled task.
	 */
	public void startTask(TaskConfig task) throws SchedulerException;
	
	/**
	 * Loop over all currently started tasks and cycle them.
	 * This should be done after the classloader has been changed
	 * (e.g. during module start/stop)
	 */
	public void restartTasks() throws SchedulerException;
	
	/**
	 * Get scheduled tasks.
	 *  
	 * @return 	all scheduled tasks
	 */
	@Transactional(readOnly=true)
	public Collection<TaskConfig> getScheduledTasks();

	/**
	 * Get the list of tasks that are available to be scheduled.  
	 * Eventually, these should go in the database.
	 * 
	 * @return	all available tasks
	 */
	@Transactional(readOnly=true)
	public Collection<TaskConfig> getAvailableTasks();

	/**
	 * Get scheduled tasks.
	 *  
	 * @return	tasks 	the tasks that are be scheduled
	 */
	@Authorized({"Manage Scheduler"})
	@Transactional(readOnly=true)
	public Collection<TaskConfig> getTasks();

	/**
	 * Get the task with the given identifier.
	 *  
	 * @param	id 		the identifier of the task
	 */
	@Authorized({"Manage Scheduler"})
	@Transactional(readOnly=true)
	public TaskConfig getTask(Integer id);

	/**
	 * Delete the task with the given identifier.
	 *  
	 * @param	id 		the identifier of the task
	 */
	@Authorized({"Manage Scheduler"})
	public void deleteTask(Integer id);

	/**
	 * Update the given task.
	 *  
	 * @param	task 		the task to be updated
	 */
	@Authorized({"Manage Scheduler"})
	public void updateTask(TaskConfig task);

	/**
	 * Create the given task
	 *  
	 * @param	task 		the task to be created
	 */
	@Authorized({"Manage Scheduler"})
	public void createTask(TaskConfig task);

	/**
	 * Return SchedularConstants
	 * @return
	 */
	@Transactional(readOnly=true)
	public SortedMap<String,String> getSystemVariables();
	
	/**
	 * Save the state of the scheduler service to Memento
	 * @return
	 */
	public OpenmrsMemento saveToMemento();
	
	/**
	 * Restore the scheduler service to state defined by Memento
	 * @param memento
	 * @return
	 */
	public void restoreFromMemento(OpenmrsMemento memento);
}
