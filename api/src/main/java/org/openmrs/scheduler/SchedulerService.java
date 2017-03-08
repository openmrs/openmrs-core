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

import java.util.Collection;
import java.util.SortedMap;

import org.openmrs.annotation.Authorized;
import org.openmrs.annotation.Logging;
import org.openmrs.api.OpenmrsService;
import org.openmrs.util.OpenmrsMemento;

/**
 * Defines methods required to schedule a task.
 */
public interface SchedulerService extends OpenmrsService {
	
	/**
	 * Checks the status of a scheduled task.
	 * 
	 * @param id
	 * @return the <code>String</code> status of the task with the given identifier
	 */
	@Authorized( { "Manage Scheduler" })
	public String getStatus(Integer id);
	
	/**
	 * Start all tasks that are scheduled to run on startup.
	 */
	@Override
	@Authorized( { "Manage Scheduler" })
	public void onStartup();
	
	/**
	 * Stop all tasks and clean up the scheduler instance.
	 */
	@Override
	@Authorized( { "Manage Scheduler" })
	public void onShutdown();
	
	/**
	 * Cancel a scheduled task.
	 * 
	 * @param task the <code>TaskDefinition</code> for the task to cancel
	 */
	@Authorized( { "Manage Scheduler" })
	public void shutdownTask(TaskDefinition task) throws SchedulerException;
	
	/**
	 * Start a scheduled task as specified in a TaskDefinition.
	 * 
	 * @param task TaskDefinition to start
	 * @return the started <code>Task</code>, or null if there was a problem instantiating or
	 *         scheduling the task
	 */
	@Authorized( { "Manage Scheduler" })
	public Task scheduleTask(TaskDefinition task) throws SchedulerException;
	
	/**
	 * Stop and start a scheduled task.
	 * 
	 * @param task the <code>TaskDefinition</code> to reschedule
	 */
	@Authorized( { "Manage Scheduler" })
	public Task rescheduleTask(TaskDefinition task) throws SchedulerException;
	
	/**
	 * Loop over all currently started tasks and cycle them. This should be done after the
	 * classloader has been changed (e.g. during module start/stop)
	 */
	@Authorized( { "Manage Scheduler" })
	public void rescheduleAllTasks() throws SchedulerException;
	
	/**
	 * Get scheduled tasks.
	 * 
	 * @return all scheduled tasks
	 */
	@Authorized( { "Manage Scheduler" })
	public Collection<TaskDefinition> getScheduledTasks();
	
	/**
	 * Get the list of tasks that are available to be scheduled. Eventually, these should go in the
	 * database.
	 * 
	 * @return all available tasks
	 */
	@Authorized( { "Manage Scheduler" })
	public Collection<TaskDefinition> getRegisteredTasks();
	
	/**
	 * Get the task with the given identifier.
	 * 
	 * @param id the identifier of the task
	 */
	@Authorized( { "Manage Scheduler" })
	public TaskDefinition getTask(Integer id);
	
	/**
	 * Get the task with the given name.
	 * 
	 * @param name name of the task
	 */
	@Authorized( { "Manage Scheduler" })
	public TaskDefinition getTaskByName(String name);
	
	/**
	 * Delete the task with the given identifier.
	 * 
	 * @param id the identifier of the task
	 */
	@Authorized( { "Manage Scheduler" })
	public void deleteTask(Integer id);
	
	/**
	 * Create the given task
	 *
	 * @param task the task to be created
	 * @should save task to the database
	 */
	@Authorized( { "Manage Scheduler" })
	@Logging(ignore = true)
	public void saveTaskDefinition(TaskDefinition task);
	
	/**
	 * Return SchedulerConstants
	 * 
	 * @return SortedMap&lt;String, String&gt;
	 */
	public SortedMap<String, String> getSystemVariables();
	
	/**
	 * Save the state of the scheduler service to Memento
	 * 
	 * @return OpenmrsMemento that contains data about this serive.
	 */
	public OpenmrsMemento saveToMemento();
	
	/**
	 * Restore the scheduler service to state defined by Memento
	 * 
	 * @param memento
	 */
	public void restoreFromMemento(OpenmrsMemento memento);
	
	/**
	 * Schedules a task for execution if not already running
	 * @param taskDef
	 * @since 1.10
	 */
	public void scheduleIfNotRunning(TaskDefinition taskDef);
	
}
