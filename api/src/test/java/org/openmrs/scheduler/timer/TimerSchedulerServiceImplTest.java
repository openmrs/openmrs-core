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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Calendar;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Tests methods in TimerSchedulerServiceImpl
 */
public class TimerSchedulerServiceImplTest extends BaseContextSensitiveTest {
	
	/**
	 * Tests whether the TimerScheduler schedules tasks even if the repeatInterval is zero.
	 * 
	 * @throws SchedulerException
	 * @see TimerSchedulerServiceImpl#scheduleTask(TaskDefinition)
	 */
	@Test
	public void scheduleTask_shouldHandleZeroRepeatInterval() throws SchedulerException {
		
		// Represents the start time of the task (right now)
		Calendar startTime = Calendar.getInstance();
		
		// Define repeatInterval as zero
		Long repeatInterval = 0L;
		
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
		
		clientTask = Context.getSchedulerService().scheduleTask(taskDefinition);
		
		// without this commit there seems to be a table lock left on the SCHEDULER_TASK_CONFIG table, see TRUNK-4212
		Context.flushSession();
		
		// Assert that the clientTask is not null, i.e. the sheduleTask was able to successfully schedule in case of zero repeatInterval.
		assertNotNull(clientTask, "The clientTask variable is null, so either the TimerSchedulerServiceImpl.scheduleTask method hasn't finished or didn't get run");
	}
	
}
