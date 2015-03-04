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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.scheduler.tasks.TaskThreadedInitializationWrapper;
import org.openmrs.util.OpenmrsClassLoader;

/**
 */
public class TaskFactory {
	
	/** Singleton instance of the schedulable factory */
	private static final TaskFactory factory = new TaskFactory();
	
	/** Logger */
	private static Log log = LogFactory.getLog(TaskFactory.class);
	
	/** Private constructor */
	private TaskFactory() {
	}
	
	/**
	 * Gets an instance of the schedulable factory
	 */
	public static TaskFactory getInstance() {
		return factory;
	}
	
	/**
	 * Creates a new instance of Schedulable used to run tasks. By default the returned task will be
	 * the given task wrapped with the {@link TaskThreadedInitializationWrapper} class so that the
	 * {@link Task#initialize(TaskDefinition)} method runs in a new thread.
	 *
	 * @param taskDefinition
	 * @return the created Task
	 * @throws SchedulerException
	 */
	public Task createInstance(TaskDefinition taskDefinition) throws SchedulerException {
		try {
			
			// Retrieve the appropriate class
			Class<?> taskClass = OpenmrsClassLoader.getInstance().loadClass(taskDefinition.getTaskClass());
			
			// Create a new instance of the schedulable class 
			Task task = new TaskThreadedInitializationWrapper((Task) taskClass.newInstance());
			
			if (log.isDebugEnabled()) {
				log.debug("initializing " + taskClass.getName());
			}
			// Initialize the schedulable object
			task.initialize(taskDefinition);
			
			return task;
		}
		catch (ClassNotFoundException cnfe) {
			log.error("OpenmrsClassLoader could not load class: " + taskDefinition.getTaskClass()
			        + ".  Probably due to a module not being loaded");
			
			if (log.isDebugEnabled()) {
				log.debug("Full error trace of ClassNotFoundException", cnfe);
			}
			
			throw new SchedulerException("could not load class", cnfe);
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				// don't need to log errors here necessarily.  If its needed, the calling method can log it.
				log.debug("Error creating new task for class " + taskDefinition.getTaskClass(), e);
			}
			
			throw new SchedulerException("error creating new task for class " + taskDefinition.getTaskClass(), e);
		}
	}
}
