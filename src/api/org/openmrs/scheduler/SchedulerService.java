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
import org.openmrs.api.OpenmrsService;
import org.openmrs.util.OpenmrsMemento;
import org.springframework.transaction.annotation.Transactional;

/**
 *  Defines methods required to schedule a task.  
 */
@Transactional
public interface SchedulerService extends OpenmrsService { 

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
	public void onStartup();
	
	/**
	 * Stop all tasks and clean up the scheduler instance.
	 */
	@Authorized({"Manage Scheduler"})
	public void onShutdown();
	
	/**
	 * Cancel a scheduled task.
	 */
	@Authorized({"Manage Scheduler"})
	public void shutdownTask(TaskDefinition task) throws SchedulerException;

	/**
	 * Start a scheduled task as specified in a TaskDefinition.
	 * 
	 * @param task definition of the task to start
	 * @return the started task, or null if there was a problem instantiating or scheduling the task
	 */
	@Authorized({"Manage Scheduler"})
	public Task scheduleTask(TaskDefinition task) throws SchedulerException;
	
	/**
	 * Stop and start a scheduled task.
	 */
	@Authorized({"Manage Scheduler"})
	public Task rescheduleTask(TaskDefinition task) throws SchedulerException;
	
	/**
	 * Loop over all currently started tasks and cycle them.
	 * This should be done after the classloader has been changed
	 * (e.g. during module start/stop)
	 */
	@Authorized({"Manage Scheduler"})
	public void rescheduleAllTasks() throws SchedulerException;
	
	/**
	 * Get scheduled tasks.
	 *  
	 * @return 	all scheduled tasks
	 */
	@Authorized({"Manage Scheduler"})
	@Transactional(readOnly=true)
	public Collection<TaskDefinition> getScheduledTasks();

	/**
	 * Get the list of tasks that are available to be scheduled.  
	 * Eventually, these should go in the database.
	 * 
	 * @return	all available tasks
	 */
	@Authorized({"Manage Scheduler"})
	@Transactional(readOnly=true)
	public Collection<TaskDefinition> getRegisteredTasks();

	/**
	 * Get the task with the given identifier.
	 *  
	 * @param	id 		the identifier of the task
	 */
	@Authorized({"Manage Scheduler"})
	@Transactional(readOnly=true)
	public TaskDefinition getTask(Integer id);
	

	/**
	 * Get the task with the given name.
	 *  
	 * @param	name name of the task
	 */
	@Authorized({"Manage Scheduler"})
	@Transactional(readOnly=true)
	public TaskDefinition getTaskByName(String name);
	
	

	/**
	 * Delete the task with the given identifier.
	 *  
	 * @param	id 		the identifier of the task
	 */
	@Authorized({"Manage Scheduler"})
	public void deleteTask(Integer id);

	/**
	 * Create the given task
	 *  
	 * @param	task 		the task to be created
	 */
	@Authorized({"Manage Scheduler"})
	public void saveTask(TaskDefinition task);
	
	/**
	 * Return SchedulerConstants
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
