/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.tasks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;

/**
 * This class executes the Task.initialize method in a new thread. Extend this class if you want
 * your {@link #initialize(TaskDefinition)} method to run in a new thread (and hence not hold up the
 * "startup" processes)
 */
public class TaskThreadedInitializationWrapper implements Task {
	
	// Logger 
	private Log log = LogFactory.getLog(TaskThreadedInitializationWrapper.class);
	
	private Task task;
	
	private boolean initialized = false;
	
	private final Lock lock = new ReentrantLock();
	
	private final Condition initializedCond = lock.newCondition();
	
	/**
	 * Default constructor to create this wrapper
	 * 
	 * @param task the Task to wrap around
	 */
	public TaskThreadedInitializationWrapper(Task task) {
		this.task = task;
	}
	
	/**
	 * @throws InterruptedException
	 * @see org.openmrs.scheduler.Task#execute() Executes the task defined in the task definition
	 *      but waits until the initialize method has finished
	 */
	public void execute() {
		lock.lock();
		try {
			while (!initialized) {
				initializedCond.await();
			}
		}
		catch (InterruptedException e) {
			log.error("Task could not be initialized hence not be executed.", e);
			return;
		}
		finally {
			lock.unlock();
		}
		
		task.execute();
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#initialize(org.openmrs.scheduler.TaskDefinition) Initializes
	 *      the task and sets the task definition. This method is non-blocking by executing in a new
	 *      thread.
	 */
	public void initialize(final TaskDefinition config) {
		Runnable r = new Runnable() {
			
			public void run() {
				lock.lock();
				try {
					task.initialize(config);
					initialized = true;
					initializedCond.signalAll();
				}
				finally {
					lock.unlock();
				}
			}
		};
		
		new Thread(r).start();
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#getTaskDefinition()
	 */
	public TaskDefinition getTaskDefinition() {
		return task != null ? task.getTaskDefinition() : null;
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#isExecuting()
	 */
	public boolean isExecuting() {
		return task.isExecuting();
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#shutdown()
	 */
	public void shutdown() {
		task.shutdown();
	}
}
