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
package org.openmrs.scheduler;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.openmrs.test.Verifies;

public class SchedulerUtilTest {
	
	/**
	 * Tests whether the scheduler is handing out the correct execution times for tasks.
	 * 
	 * @see {@link SchedulerUtil#getNextExecution(TaskDefinition)}
	 */
	@Test
	@Verifies(value = "should get the correct repeat interval", method = "getNextExecution(TaskDefinition)")
	public void getNextExecution_shouldGetTheCorrectRepeatInterval() throws Exception {
		
		// Represents the start time of the task (right now)
		Calendar startTime = Calendar.getInstance();
		
		// Execute task every 4 minutes
		Long repeatInterval = new Long(4 * 60);
		
		// Create the new task
		TaskDefinition taskDefinition = new TaskDefinition();
		taskDefinition.setStartTime(startTime.getTime());
		taskDefinition.setRepeatInterval(repeatInterval);
		
		// Add four minutes to the start time
		startTime.add(Calendar.MINUTE, 4);
		
		// Get the next scheduled execution time for this task 
		Date nextTime = SchedulerUtil.getNextExecution(taskDefinition);
		
		// Assert that the next execution time is equal to startTime + 4 minutes
		assertEquals(startTime.getTime(), nextTime);
		
	}
	
}
