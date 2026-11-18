/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.timer;

import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerSchedulerTask extends TimerTask {
	
	/** The task that will be executed by the JDK timer. */
	private Task task;
	
	/** Logger */
	private static final Logger log = LoggerFactory.getLogger(TimerSchedulerTask.class);

	/**
	 * The capability that proves to {@link Daemon} this class is allowed to act with daemon
     * permissions.
	 */
	private static volatile Daemon.CallerKey daemonCallerKey;

	/** * Public constructor */
	public TimerSchedulerTask(Task task) {
		this.task = task;
	}
	
	/**
	 * * Executes the action to be performed by this timer task.
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run() {
		try {
			Daemon.executeScheduledTask(task, daemonCallerKey());
		}
		catch (Exception t) {
			// Fix #862: IllegalStateException: Timer already cancelled.
			// Suppress error in order to keep the scheduler's Timer from completely failing.
			log.error(
			    "FATAL ERROR: Task [" + task.getClass() + "] failed due to exception [" + t.getClass().getName() + "]", t);
			SchedulerUtil.sendSchedulerError(t);
		}
	}
	
	/**
	 * Save the last execution time in the TaskDefinition
	 */
	private static void saveLastExecutionTime(Task task) {
		TaskDefinition taskDefinition;
		try {
			// We re-get the task definition in case the copy set during the
			// task initialization has become stale.  NOTE: If a task does not
			// extend the abstract class AbstractTask, then it's possible the
			// developer did not actually set the TaskDefintion on the Task.
			// Therefore we might get an NPE below.
			if (task.getTaskDefinition() != null) {
				SchedulerService schedulerService = Context.getSchedulerService();
				taskDefinition = task.getTaskDefinition();
				taskDefinition.setLastExecutionTime(new Date());
				schedulerService.saveTaskDefinition(taskDefinition);
			} else {
				log.warn("Unable to save the last execution time for task. Task.taskDefinition is null in "
				        + task.getClass());
			}
		}
		catch (Exception e) {
			log.warn("Unable to save the last execution time for task ", e);
		}
	}
	
	/**
	 * Shutdown the timer task and invoke the task's shutdown() callback method.
	 */
	public void shutdown() {
		super.cancel();
		task.shutdown();
	}
	
	/**
	 * Executes the given task.
	 */
	public static void execute(Task task) {
		try {
			task.execute();
		} catch (InterruptedException | ExecutionException e) {
			// ignored
		}
		saveLastExecutionTime(task);
	}

	/**
	 * Receives the {@link Daemon} caller key. Called only by {@link Daemon} during its initialization.
	 *
	 * @param callerKey the caller key issued by {@link Daemon}
	 * @since 3.0.0, 2.9.0, 2.8.9
	 */
	public static void setDaemonCallerKey(Daemon.CallerKey callerKey) {
		if (callerKey != null && daemonCallerKey == null) {
			daemonCallerKey = callerKey;
		}
	}

	/**
	 * @return the {@link Daemon} caller key. Protected so that {@link TimerSchedulerTask} subclasses,
	 *         which the old caller check also permitted, can present it when running scheduled tasks.
	 */
	protected static Daemon.CallerKey daemonCallerKey() {
		if (daemonCallerKey == null) {
			// Guarantee Daemon has initialized and therefore handed us the key, regardless of the order in
			// which the two classes were first loaded.
			Daemon.ensureInitialized();
		}
		return daemonCallerKey;
	}
}
