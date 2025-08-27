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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.db.SchedulerDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 */
public class HibernateSchedulerDAO implements SchedulerDAO {
	
	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(HibernateSchedulerDAO.class);
	
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
	@Override
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
	@Override
	public TaskDefinition getTask(Integer taskId) throws DAOException {
		TaskDefinition task = sessionFactory.getCurrentSession().get(TaskDefinition.class, taskId);
		
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
	@Override
	public TaskDefinition getTaskByName(String name) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<TaskDefinition> cq = cb.createQuery(TaskDefinition.class);
		Root<TaskDefinition> root = cq.from(TaskDefinition.class);

		cq.where(cb.equal(root.get("name"), name));

		TaskDefinition task = session.createQuery(cq).uniqueResult();

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
	@Override
	public void updateTask(TaskDefinition task) throws DAOException {
		sessionFactory.getCurrentSession().merge(task);
	}
	
	/**
	 * Find all tasks in the database
	 * 
	 * @return <code>List&lt;TaskDefinition&gt;</code> of all tasks
	 * @throws DAOException
	 */
	@Override
	public List<TaskDefinition> getTasks() throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<TaskDefinition> cq = cb.createQuery(TaskDefinition.class);
		cq.from(TaskDefinition.class);

		return session.createQuery(cq).getResultList();
	}

	/**
	 * Delete task from database.
	 * 
	 * @param taskId <code>Integer</code> identifier of task to be deleted
	 * @throws DAOException
	 */
	@Override
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
	@Override
	public void deleteTask(TaskDefinition taskConfig) throws DAOException {
		sessionFactory.getCurrentSession().delete(taskConfig);
	}
	
	/**
	 * 
	 * @see org.openmrs.scheduler.db.SchedulerDAO#getTaskByUuid(java.lang.String)
	 */
	@Override
	public TaskDefinition getTaskByUuid(String uuid) throws DAOException {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, TaskDefinition.class, uuid);
	}
}
