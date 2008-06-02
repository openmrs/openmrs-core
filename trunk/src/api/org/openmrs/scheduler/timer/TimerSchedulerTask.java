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
import org.openmrs.scheduler.Task;

public class TimerSchedulerTask extends TimerTask { 
		/**	 *  The task that will be executed by the JDK timer. 	 */	private Task task; 	/**	 *  Public constructor	 */	public TimerSchedulerTask(Task task) { 
		this.task = task;	}

	/**	 * Executes the action to be performed by this timer task.  
	 *  
     * @see java.util.TimerTask#run()
	 */	public void run() { 
		task.execute();	}

	/**
	 * Shutdown the timer task and invoke the task's shutdown() callback method.
	 */
	public void shutdown() { 
		super.cancel();
		task.shutdown();
	}

}
