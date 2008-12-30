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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * TODO test all methods in ScheduleService
 */
public class SchedulerServiceTest extends BaseContextSensitiveTest {
	
	private static Log log = LogFactory.getLog(SchedulerServiceTest.class);
	
	@Test
	public void shouldResolveValidTaskClass() throws Exception {
		String className = "org.openmrs.scheduler.tasks.TestTask";
		try {
			Class c = OpenmrsClassLoader.getInstance().loadClass(className);
			Object o = c.newInstance();
			if (o instanceof Task)
				assertTrue("Class " + className + " is a valid Task", true);
			else
				fail("Class " + className + " is not a valid Task");
			
		}
		catch (ClassNotFoundException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void shouldNotResolveInvalidClass() throws Exception {
		String className = "org.openmrs.scheduler.tasks.InvalidTask";
		try {
			Class c = OpenmrsClassLoader.getInstance().loadClass(className);
			Object o = c.newInstance();
			if (o instanceof Task)
				fail("Class " + className + " is not supposed to be a valid Task");
			else
				assertTrue("Class " + className + " is not a valid Task", true);
			
		}
		catch (ClassNotFoundException e) {
			assertTrue("Class " + className + " was not found, as expected", true);
		}
		
	}
	
	@Test
	public void shouldResolveModuleTaskClass() throws Exception {
		
	}
	
	/**
	 * Tests whether the scheduler is handing out the correct execution times for tasks.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldExecuteOnGivenInterval() throws Exception {
		
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
