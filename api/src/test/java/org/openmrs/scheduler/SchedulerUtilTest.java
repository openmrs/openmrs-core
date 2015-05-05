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
