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
package org.openmrs.scheduler.timer;

import java.util.Date;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.PrivilegeConstants;

public class TimerSchedulerTask extends TimerTask {
	
	/** The task that will be executed by the JDK timer. */
	private Task task;
	
	/** Logger */
	private static Log log = LogFactory.getLog(TimerSchedulerTask.class);
	
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
			Daemon.executeScheduledTask(task);
			
			if (!Context.isSessionOpen()) {
				Context.openSession();
			}
			saveLastExecutionTime();
		}
		catch (Throwable t) {
			// Fix #862: IllegalStateException: Timer already cancelled.
			// Suppress error in order to keep the scheduler's Timer from completely failing.
			log.error(
			    "FATAL ERROR: Task [" + task.getClass() + "] failed due to exception [" + t.getClass().getName() + "]", t);
			SchedulerUtil.sendSchedulerError(t);
		}
		finally {
			if (Context.isSessionOpen()) {
				Context.closeSession();
			}
		}
	}
	
	/**
	 * Save the last execution time in the TaskDefinition
	 */
	private void saveLastExecutionTime() {
		TaskDefinition taskDefinition = null;
		try {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
			
			// We re-get the task definition in case the copy set during the
			// task initialization has become stale.  NOTE: If a task does not
			// extend the abstract class AbstractTask, then it's possible the
			// developer did not actually set the TaskDefintion on the Task.
			// Therefore we might get an NPE below.
			if (task.getTaskDefinition() != null) {
				SchedulerService schedulerService = Context.getSchedulerService();
				taskDefinition = task.getTaskDefinition();
				taskDefinition.setLastExecutionTime(new Date());
				schedulerService.saveTask(taskDefinition);
			} else {
				log.warn("Unable to save the last execution time for task. Task.taskDefinition is null in "
				        + task.getClass());
			}
		}
		catch (Exception e) {
			log.warn("Unable to save the last execution time for task ", e);
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
		}
	}
	
	/**
	 * Shutdown the timer task and invoke the task's shutdown() callback method.
	 */
	public void shutdown() {
		super.cancel();
		task.shutdown();
	}
	
}
