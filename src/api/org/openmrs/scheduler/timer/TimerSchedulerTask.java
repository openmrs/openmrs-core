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

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.scheduler.Task;

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
	public void run() {
		try {
			
			task.execute();
			
		}
		catch (Exception e) {
			// Fix #862: IllegalStateException: Timer already cancelled.
			// Suppress error in order to keep the scheduler's Timer from completely failing.  
			log.error(
			    "FATAL ERROR: Task [" + task.getClass() + "] failed due to exception [" + e.getClass().getName() + "]", e);
			SchedulerUtil.sendSchedulerError(e);
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
