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
