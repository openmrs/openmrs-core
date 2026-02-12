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

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class ScheduledWithLockTest extends BaseContextSensitiveTest {

	@Autowired
	private TestTask testTask;

	@Autowired
	private TestComponentTask testComponentTask;

	@Test
	public void shouldExecuteScheduledTask() throws InterruptedException {
		long start = System.currentTimeMillis();
		while (testTask.getExecutions() == 0 && (System.currentTimeMillis() - start) < 10000) {
			Thread.sleep(100);
		}
		Assert.assertTrue("Task should have been executed", testTask.getExecutions() > 0);
	}
	
	@Test
	public void shouldExecuteScheduledTaskInComponent() throws InterruptedException {
		long start = System.currentTimeMillis();
		while (testComponentTask.getExecutions() == 0 && (System.currentTimeMillis() - start) < 10000) {
			Thread.sleep(100);
		}
		Assert.assertTrue("Task should have been executed", testComponentTask.getExecutions() > 0);
	}

	@Configuration
	public static class TestConfig {

		@Bean
		public TestTask testTask() {
			return new TestTask();
		}
	}

	public static class TestTask {

		private int executions = 0;

		@ScheduledWithLock(name = "testTask", fixedRate = 100)
		public void someTask() {
			executions++;
		}

		public int getExecutions() {
			return executions;
		}
	}
}
