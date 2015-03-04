/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.db.DAOException;
import org.openmrs.scheduler.Schedule;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.db.SchedulerDAO;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 */
public class HibernateSchedulerDAO implements SchedulerDAO {
	
	/**
	 * Logger
	 */
	private static final Log log = LogFactory.getLog(HibernateSchedulerDAO.class);
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Default Public constructor
	 */
	public HibernateSchedulerDAO() {
	}
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * Creates a new task.
	 * 
	 * @param task to be created
	 * @throws DAOException
	 */
	public void createTask(TaskDefinition task) throws DAOException {
		// add all data minus the password as a new user
		sessionFactory.getCurrentSession().save(task);
	}
	
	/**
	 * Get task by internal identifier
	 * 
	 * @param taskId internal task identifier
	 * @return task with given internal identifier
	 * @throws DAOException
	 */
	public TaskDefinition getTask(Integer taskId) throws DAOException {
		TaskDefinition task = (TaskDefinition) sessionFactory.getCurrentSession().get(TaskDefinition.class, taskId);
		
		if (task == null) {
			log.warn("Task '" + taskId + "' not found");
			throw new ObjectRetrievalFailureException(TaskDefinition.class, taskId);
		}
		return task;
	}
	
	/**
	 * Get task by public name.
	 * 
	 * @param name public task name
	 * @return task with given public name
	 * @throws DAOException
	 */
	public TaskDefinition getTaskByName(String name) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(TaskDefinition.class).add(
		    Restrictions.eq("name", name));
		
		TaskDefinition task = (TaskDefinition) crit.uniqueResult();
		
		if (task == null) {
			log.warn("Task '" + name + "' not found");
			throw new ObjectRetrievalFailureException(TaskDefinition.class, name);
		}
		return task;
	}
	
	/**
	 * Update task
	 * 
	 * @param task to be updated
	 * @throws DAOException
	 */
	public void updateTask(TaskDefinition task) throws DAOException {
		sessionFactory.getCurrentSession().merge(task);
	}
	
	/**
	 * Find all tasks in the database
	 * 
	 * @return <code>List<TaskDefinition></code> of all tasks
	 * @throws DAOException
	 */
	@SuppressWarnings("unchecked")
	public List<TaskDefinition> getTasks() throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(TaskDefinition.class).list();
	}
	
	/**
	 * Delete task from database.
	 * 
	 * @param taskId <code>Integer</code> identifier of task to be deleted
	 * @throws DAOException
	 */
	public void deleteTask(Integer taskId) throws DAOException {
		TaskDefinition taskConfig = getTask(taskId);
		deleteTask(taskConfig);
	}
	
	/**
	 * Delete task from database.
	 * 
	 * @param taskConfig <code>TaskDefinition</code> of task to be deleted
	 * @throws DAOException
	 */
	public void deleteTask(TaskDefinition taskConfig) throws DAOException {
		sessionFactory.getCurrentSession().delete(taskConfig);
	}
	
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
	public Schedule getSchedule(Integer scheduleId) throws DAOException {
		Schedule schedule = (Schedule) sessionFactory.getCurrentSession().get(Schedule.class, scheduleId);
		
		if (schedule == null) {
			log.error("Schedule '" + scheduleId + "' not found");
			throw new ObjectRetrievalFailureException(Schedule.class, scheduleId);
		}
		return schedule;
	}
	
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
	//public Set<Schedule> getAllSchedules() throws DAOException;
	/**
	 * Delete schedule from database.
	 * 
	 * @param schedule schedule to be deleted
	 * @throws DAOException
	 */
	//public void deleteSchedule(Schedule schedule) throws DAOException;	
}
