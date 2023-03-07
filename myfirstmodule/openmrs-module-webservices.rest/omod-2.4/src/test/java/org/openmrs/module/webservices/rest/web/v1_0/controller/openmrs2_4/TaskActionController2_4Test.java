/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_4;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.helper.TaskServiceWrapper2_4;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_8.TaskActionController1_8Test;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_4.TaskDefinitionResource2_4;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_4.CHRONIC_CARE_UUID;
import static org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_4.EQUIPMENT_MAINTENANCE_UUID;
import static org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_4.NURSE_PRECEPTING_UUID;
import static org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_4.NURSING_EDUCATION_UUID;
import static org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_4.TASK_SCHEDULER_XML;

public class TaskActionController2_4Test extends TaskActionController1_8Test {

	@Autowired
	RestService restService;

    private SchedulerService schedulerService;

	private final TaskServiceWrapper2_4 TASK_SERVICE_WRAPPER = new TaskServiceWrapper2_4();
	
	@Before
	public void setUp() throws Exception {
		executeDataSet(TASK_SCHEDULER_XML);
		this.schedulerService = Context.getSchedulerService();
		TaskDefinitionResource2_4 taskResource = (TaskDefinitionResource2_4) restService
				.getResourceBySupportedClass(TaskDefinition.class);
		taskResource.setTaskServiceWrapper(TASK_SERVICE_WRAPPER);
	}

	private TaskDefinition getTaskByUuid(String uuid) {
		return TASK_SERVICE_WRAPPER.getTaskByUuid(uuid);
	}

	@Test
	public void shouldScheduleTask() throws Exception {
		assertThat(schedulerService.getScheduledTasks(), not(hasItem(getTaskByUuid(EQUIPMENT_MAINTENANCE_UUID))));
		deserialize(handle(newPostRequest(getURI(),
				"{\"action\": \"scheduletask\", \"tasks\":[\"" + EQUIPMENT_MAINTENANCE_UUID + "\"]}")));
		assertThat(schedulerService.getScheduledTasks(), hasItem(getTaskByUuid(EQUIPMENT_MAINTENANCE_UUID)));
	}

	@Test
	public void scheduleTask_shouldDoNothingIfTaskAlreadyScheduled() throws Exception {
		schedulerService.scheduleTask(getTaskByUuid(NURSE_PRECEPTING_UUID));
		assertThat(schedulerService.getScheduledTasks(), hasItem(getTaskByUuid(NURSE_PRECEPTING_UUID)));
		deserialize(handle(newPostRequest(getURI(),
				"{\"action\": \"scheduletask\", \"tasks\":[\"" + NURSE_PRECEPTING_UUID + "\"]}")));
		assertThat(schedulerService.getScheduledTasks(), hasItem(getTaskByUuid(NURSE_PRECEPTING_UUID)));
	}

	@Test
	public void shouldShutdownTask() throws Exception {
		schedulerService.scheduleTask(getTaskByUuid(NURSE_PRECEPTING_UUID));
		assertThat(schedulerService.getScheduledTasks(), hasItem(getTaskByUuid(NURSE_PRECEPTING_UUID)));
		deserialize(handle(newPostRequest(getURI(),
				"{\"action\": \"shutdowntask\", \"tasks\":[\"" + NURSE_PRECEPTING_UUID + "\"]}")));
		assertThat(schedulerService.getScheduledTasks(), not(hasItem(getTaskByUuid(NURSE_PRECEPTING_UUID))));
	}

	@Test
	public void shutdownTask_shouldDoNothingIfTaskAlreadyShutdown() throws Exception {
		assertThat(schedulerService.getScheduledTasks(), not(hasItem(getTaskByUuid(NURSE_PRECEPTING_UUID))));
		assertThat(schedulerService.getRegisteredTasks(), hasItem(getTaskByUuid(NURSE_PRECEPTING_UUID)));
		deserialize(handle(newPostRequest(getURI(),
				"{\"action\": \"shutdowntask\", \"tasks\":[\"" + NURSE_PRECEPTING_UUID + "\"]}")));
		assertThat(schedulerService.getScheduledTasks(), not(hasItem(getTaskByUuid(NURSE_PRECEPTING_UUID))));
		assertThat(schedulerService.getRegisteredTasks(), hasItem(getTaskByUuid(NURSE_PRECEPTING_UUID)));
	}
	
	@Test
	public void shouldRunTask() throws Exception {
		TaskDefinition taskDefinition = getTaskByUuid(CHRONIC_CARE_UUID);
		taskDefinition.setTaskClass(TaskActionController1_8Test.DummyTask.class.getName());
		assertEquals(4, schedulerService.getRegisteredTasks().size());
		int countBefore = count;
		deserialize(handle(newPostRequest(getURI(),
				"{\"action\": \"runtask\", \"tasks\":[\"" + CHRONIC_CARE_UUID + "\"]}")));
		assertThat(schedulerService.getRegisteredTasks(), hasItem(getTaskByUuid(CHRONIC_CARE_UUID)));
		assertEquals(4, schedulerService.getRegisteredTasks().size());
		int countAfter = count;
		assertEquals(++countBefore, countAfter);
	}

	@Test
	public void shouldRescheduleTask() throws Exception {
		schedulerService.scheduleTask(getTaskByUuid(NURSING_EDUCATION_UUID));
		assertThat(schedulerService.getScheduledTasks(), hasItem(getTaskByUuid(NURSING_EDUCATION_UUID)));
		deserialize(handle(newPostRequest(getURI(),
				"{\"action\": \"rescheduletask\", \"tasks\":[\"" + NURSING_EDUCATION_UUID + "\"]}")));
		assertThat(schedulerService.getScheduledTasks(), hasItem(getTaskByUuid(NURSING_EDUCATION_UUID)));
	}

	@Test
	public void shouldDeleteTask() throws Exception {
		assertThat(schedulerService.getRegisteredTasks(), hasItem(getTaskByUuid(NURSING_EDUCATION_UUID)));
		deserialize(handle(newPostRequest(getURI(),
				"{\"action\": \"delete\", \"tasks\":[\"" + NURSING_EDUCATION_UUID + "\"]}")));
		assertThat(schedulerService.getRegisteredTasks(), not(hasItem(getTaskByUuid(NURSING_EDUCATION_UUID))));
	}
}
