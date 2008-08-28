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
package org.openmrs.test.scheduler;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openmrs.scheduler.SchedulerUtil;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.testutil.BaseContextSensitiveTest;

/**
 * TODO test all methods in EncounterService
 */
public class SchedulerServiceTest extends BaseContextSensitiveTest {
	
	private static Log log = LogFactory.getLog(SchedulerServiceTest.class);
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldNextExecution() throws Exception {
		
		Calendar startDate = Calendar.getInstance();
		startDate.set(2008, 3, 30, 1, 22, 0);

		//long SECOND_PER_DAY = 86400;
		//long SECOND_PER_HOUR = 3600;
		long SECOND_PER_MINUTE = 60;
		
		TaskDefinition taskDefinition = new TaskDefinition();
		//taskDefinition.setStartTime(startDate.getTime());
		taskDefinition.setStartTime(null);
		taskDefinition.setRepeatInterval(new Long(4 * SECOND_PER_MINUTE));
		
		
		
		Date nextTime = SchedulerUtil.getNextExecution(taskDefinition);
		log.info("Current time: " + new Date());
		log.info("Next time: " + nextTime);
		
		//assertNull("We shouldn't find the encounter after deletion", e);
		
		
	}

	
}
