/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.webservices.helper.TaskAction;
import org.openmrs.module.webservices.rest.web.MockTaskServiceWrapper;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.TaskActionResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.TaskDefinitionResource1_8;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskActionController1_8Test extends MainResourceControllerTest {
	
	@Autowired
	RestService restService;
	
	protected static int count = 1;
	
	private TaskDefinition testTask = new TaskDefinition(1, "TestTask", "TestTask Description",
	        "org.openmrs.scheduler.tasks.TestTask");
	
	private TaskDefinition testDummyTask = new TaskDefinition(5, "TestDummy", "TestTask Description",
	        DummyTask.class.getName());
	
	private TaskDefinition tempTask = new TaskDefinition(3, "TempTask", "TempTask Description",
	        "org.openmrs.scheduler.tasks.TestTask");
	
	private MockTaskServiceWrapper mockTaskServiceWrapper = new MockTaskServiceWrapper();
	
	@Before
	public void setUp() throws Exception {
		mockTaskServiceWrapper.registeredTasks.addAll(Arrays.asList(testTask, tempTask, testDummyTask));
		
		TaskActionResource1_8 taskActionResource = (TaskActionResource1_8) restService
		        .getResourceBySupportedClass(TaskAction.class);
		taskActionResource.setTaskServiceWrapper(mockTaskServiceWrapper);
		
		TaskDefinitionResource1_8 taskResource = (TaskDefinitionResource1_8) restService
		        .getResourceBySupportedClass(TaskDefinition.class);
		taskResource.setTaskServiceWrapper(mockTaskServiceWrapper);
	}
	
	@Test
	public void shouldScheduleTask() throws Exception {
		//sanity check
		assertThat(mockTaskServiceWrapper.scheduledTasks, not(hasItem(testTask)));
		deserialize(handle(newPostRequest(getURI(), "{\"action\": \"scheduletask\", \"tasks\":[\"" + getTestTaskName()
		        + "\"]}")));
		assertThat(mockTaskServiceWrapper.scheduledTasks, hasItem(testTask));
	}
	
	@Test
	public void shouldShutdownTask() throws Exception {
		mockTaskServiceWrapper.scheduledTasks.add(testTask);
		assertThat(mockTaskServiceWrapper.scheduledTasks, hasItem(testTask));
		deserialize(handle(newPostRequest(getURI(), "{\"action\": \"shutdowntask\", \"tasks\":[\"" + getTestTaskName()
		        + "\"]}")));
		assertThat(mockTaskServiceWrapper.scheduledTasks, not(hasItem(testTask)));
	}
	
	@Test
	public void scheduleTask_shouldDoNothingIfTaskAlreadyScheduled() throws Exception {
		mockTaskServiceWrapper.scheduledTasks.add(testTask);
		//sanity check
		assertThat(mockTaskServiceWrapper.scheduledTasks, hasItem(testTask));
		assertThat(mockTaskServiceWrapper.registeredTasks, hasItem(testTask));
		deserialize(handle(newPostRequest(getURI(), "{\"action\": \"scheduletask\", \"tasks\":[\"" + getTestTaskName()
		        + "\"]}")));
		//check if state preserved
		assertThat(mockTaskServiceWrapper.scheduledTasks, hasItem(testTask));
		assertThat(mockTaskServiceWrapper.registeredTasks, hasItem(testTask));
	}
	
	@Test
	public void shutdownTask_shouldDoNothingIfTaskAlreadyShutdown() throws Exception {
		//sanity check
		assertThat(mockTaskServiceWrapper.scheduledTasks, not(hasItem(testTask)));
		assertThat(mockTaskServiceWrapper.registeredTasks, hasItem(testTask));
		deserialize(handle(newPostRequest(getURI(), "{\"action\": \"shutdowntask\", \"tasks\":[\"" + getTestTaskName()
		        + "\"]}")));
		//check if state preserved
		assertThat(mockTaskServiceWrapper.scheduledTasks, not(hasItem(testTask)));
		assertThat(mockTaskServiceWrapper.registeredTasks, hasItem(testTask));
	}
	
	@Test
	public void shouldDeleteTask() throws Exception {
		//sanity check
		assertThat(mockTaskServiceWrapper.registeredTasks, hasItem(testTask));
		deserialize(handle(newPostRequest(getURI(), "{\"action\": \"delete\", \"tasks\":[\"" + getTestTaskName() + "\"]}")));
		assertThat(mockTaskServiceWrapper.registeredTasks, not(hasItem(testTask)));
	}
	
	@Test
	public void shouldRunTask() throws Exception {
		//sanity check
		assertThat(mockTaskServiceWrapper.registeredTasks, hasItem(testDummyTask));
		Assert.assertEquals(3, mockTaskServiceWrapper.getRegisteredTasks().size());
		//count before manual execution
		int countBefore = count;
		deserialize(handle(newPostRequest(getURI(), "{\"action\": \"runtask\", \"tasks\":[\"" + getTestDummyTaskName()
		        + "\"]}")));
		assertThat(mockTaskServiceWrapper.registeredTasks, hasItem(testDummyTask));
		Assert.assertEquals(3, mockTaskServiceWrapper.getRegisteredTasks().size());
		//count after manual execution
		int countAfter = count;
		Assert.assertEquals(++countBefore, countAfter);
	}
	
	@Override
	@Test(expected = Exception.class)
	public void shouldGetDefaultByUuid() throws Exception {
		super.shouldGetDefaultByUuid();
	}
	
	@Override
	@Test(expected = Exception.class)
	public void shouldGetRefByUuid() throws Exception {
		super.shouldGetRefByUuid();
	}
	
	@Override
	@Test(expected = Exception.class)
	public void shouldGetFullByUuid() throws Exception {
		super.shouldGetFullByUuid();
	}
	
	@Override
	@Test(expected = Exception.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
	
	@Override
	public String getURI() {
		return "taskaction";
	}
	
	@Override
	public String getUuid() {
		return null;
	}
	
	public String getTestTaskName() {
		return "TestTask";
	}
	
	public String getTestDummyTaskName() {
		return "TestDummy";
	}
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	public static class DummyTask implements Task {
		
		@Override
		public void execute() {
			count = count + 1;
		}
		
		@Override
		public TaskDefinition getTaskDefinition() {
			return null;
		}
		
		@Override
		public void initialize(TaskDefinition definition) {
		}
		
		@Override
		public boolean isExecuting() {
			return false;
		}
		
		@Override
		public void shutdown() {
		}
	}
}
