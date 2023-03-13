/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.db;

import java.util.List;

import org.openmrs.api.db.DAOException;
import org.openmrs.scheduler.Schedule;
import org.openmrs.scheduler.TaskDefinition;

/**
 * Scheduler-related database methods.
 * 
 * @version 1.0
 */
public interface SchedulerDAO {
	
	/**
	 * Creates a new task.
	 * 
	 * @param taskDefinition task to be created
	 * @throws DAOException
	 */
	public void createTask(TaskDefinition taskDefinition) throws DAOException;
	
	/**
	 * Get task by internal identifier
	 * 
	 * @param taskId internal task identifier
	 * @return task with given internal identifier
	 * @throws DAOException
	 */
	public TaskDefinition getTask(Integer taskId) throws DAOException;
	
	/**
	 * @see org.openmrs.scheduler.SchedulerService#getTaskByUuid(java.lang.String)
	 * 
	 */
	public TaskDefinition getTaskByUuid(String uuid) throws DAOException;

	/**
	 * Update task
	 * 
	 * @param task to be updated
	 * @throws DAOException
	 */
	public void updateTask(TaskDefinition task) throws DAOException;
	
	/**
	 * Find all tasks in the database
	 * 
	 * @return <code>List&lt;TaskDefinition&gt;</code> of all tasks
	 * @throws DAOException
	 */
	public List<TaskDefinition> getTasks() throws DAOException;
	
	/**
	 * Delete task from database.
	 * 
	 * @param task task to be deleted
	 * @throws DAOException
	 */
	public void deleteTask(TaskDefinition task) throws DAOException;
	
	/**
	 * Delete task from database.
	 * 
	 * @param taskId identifier of task to be deleted
	 * @throws DAOException
	 */
	public void deleteTask(Integer taskId) throws DAOException;
	
	/**
	 * Get schedule by internal identifier
	 * 
	 * @param scheduleId internal schedule identifier
	 * @return schedule with given internal identifier
	 * @throws DAOException
	 */
	public Schedule getSchedule(Integer scheduleId) throws DAOException;
	
	/**
	 * Get task by public name.
	 * 
	 * @param name public task name
	 * @return task with given public name
	 * @throws DAOException
	 */
	public TaskDefinition getTaskByName(String name) throws DAOException;
}
