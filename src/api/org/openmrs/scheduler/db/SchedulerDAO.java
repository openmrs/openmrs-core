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
	 * Update task
	 * 
	 * @param task to be updated
	 * @throws DAOException
	 */
	public void updateTask(TaskDefinition task) throws DAOException;
	
	/**
	 * Find all tasks in the database
	 * 
	 * @return <code>List<TaskDefinition></code> of all tasks
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
	 * Creates a new schedule.
	 * 
	 * @param schedule to be created
	 * @throws DAOException
	 */
	//public void createSchedule(Schedule schedule) throws DAOException;

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
	
	/**
	 * Update a schedule.
	 * 
	 * @param schedule to be updated
	 * @throws DAOException
	 */
	//public void updateSchedule(Schedule schedule) throws DAOException;
	/**
	 * Get all schedules.
	 * 
	 * @return set of all schedules in the database
	 * @throws DAOException
	 */
	//public Set<Schedule> getSchedules() throws DAOException;
	/**
	 * Delete schedule from database.
	 * 
	 * @param schedule schedule to be deleted
	 * @throws DAOException
	 */
	//public void deleteSchedule(Schedule schedule) throws DAOException;
}
