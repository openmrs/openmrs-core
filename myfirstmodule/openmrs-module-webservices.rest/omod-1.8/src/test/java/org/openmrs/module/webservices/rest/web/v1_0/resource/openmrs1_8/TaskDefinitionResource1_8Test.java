/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.scheduler.TaskDefinition;

import java.util.Date;

public class TaskDefinitionResource1_8Test extends BaseDelegatingResourceTest<TaskDefinitionResource1_8, TaskDefinition> {
	
	@Override
	public TaskDefinition newObject() {
		//Create test Task Definition
		TaskDefinition taskDefinition = new TaskDefinition();
		taskDefinition.setId(1);
		taskDefinition.setName("TestTask");
		taskDefinition.setStarted(false);
		taskDefinition.setRepeatInterval(10L);
		taskDefinition.setStartTime(new Date());
		taskDefinition.setTaskClass("org.openmrs.scheduler.tasks.TestTask");
		return taskDefinition;
	}
	
	@Override
	public String getDisplayProperty() {
		return "TestTask";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.TASK_DEFINITION_UUID;
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		assertPropEquals("uuid", getObject().getUuid());
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("startTime", getObject().getStartTime());
		assertPropEquals("started", getObject().getStarted());
		assertPropEquals("taskClass", getObject().getTaskClass());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		assertPropEquals("uuid", getObject().getUuid());
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("startTime", getObject().getStartTime());
		assertPropEquals("started", getObject().getStarted());
		assertPropEquals("taskClass", getObject().getTaskClass());
		assertPropEquals("lastExecutionTime", getObject().getLastExecutionTime());
		assertPropEquals("startTimePattern", getObject().getStartTimePattern());
		assertPropEquals("properties", getObject().getProperties());
	}
	
	@Override
	public void validateRefRepresentation() throws Exception {
		assertPropEquals("uuid", getObject().getUuid());
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("taskClass", getObject().getTaskClass());
	}
}
