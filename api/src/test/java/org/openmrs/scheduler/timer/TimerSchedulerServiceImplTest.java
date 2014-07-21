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

import static org.junit.Assert.assertNotNull;

import java.util.Calendar;

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests methods in TimerSchedulerServiceImpl
 */
public class TimerSchedulerServiceImplTest extends BaseContextSensitiveTest {
	
	/**
	 * Tests whether the TimerScheduler schedules tasks even if the repeatInterval is zero.
	 * 
	 * @see {@link TimerSchedulerServiceImpl#scheduleTask(TaskDefinition)}
	 */
	@Test
	@Verifies(value = "should handle zero repeat interval", method = "scheduleTask(TaskDefinition)")
	public void scheduleTask_shouldHandleZeroRepeatInterval() throws Exception {
		
		// Represents the start time of the task (right now)
		Calendar startTime = Calendar.getInstance();
		
		// Define repeatInterval as zero
		Long repeatInterval = new Long(0);
		
		String taskName = "TestTask";
		String className = "org.openmrs.scheduler.tasks.TestTask";
		
		Boolean startOnStartup = false;
		
		// Create the new task
		TaskDefinition taskDefinition = new TaskDefinition();
		taskDefinition.setName(taskName);
		taskDefinition.setTaskClass(className);
		taskDefinition.setStartTime(startTime.getTime());
		taskDefinition.setRepeatInterval(repeatInterval);
		taskDefinition.setStartOnStartup(startOnStartup);
		
		Task clientTask = null;
		
		TimerSchedulerServiceImpl t = new TimerSchedulerServiceImpl();
		clientTask = t.scheduleTask(taskDefinition);
		
		// without this commit there seems to be a table lock left on the SCHEDULER_TASK_CONFIG table, see TRUNK-4212
		Context.flushSession();
		getConnection().commit();
		
		// Assert that the clientTask is not null, i.e. the sheduleTask was able to successfully schedule in case of zero repeatInterval.
		assertNotNull(
		    "The clientTask variable is null, so either the TimerSchedulerServiceImpl.scheduleTask method hasn't finished or didn't get run",
		    clientTask);
	}
	
}
