/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_4;

import org.openmrs.module.webservices.helper.TaskServiceWrapper2_4;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.TaskDefinitionResource1_8;
import org.openmrs.scheduler.TaskDefinition;

@Resource(name = RestConstants.VERSION_1 + "/taskdefinition", order = 2, supportedClass = TaskDefinition.class,
		supportedOpenmrsVersions = { "2.4.* - 9.*" })
public class TaskDefinitionResource2_4 extends TaskDefinitionResource1_8 {

	private TaskServiceWrapper2_4 taskServiceWrapper = new TaskServiceWrapper2_4();

	public void setTaskServiceWrapper(TaskServiceWrapper2_4 taskServiceWrapper) {
		this.taskServiceWrapper = taskServiceWrapper;
	}
	
	@Override
	public TaskDefinition getByUniqueId(String nameOrUuid) {
		TaskDefinition taskDefinition = taskServiceWrapper.getTaskByUuid(nameOrUuid);
		if (taskDefinition == null) {
			taskDefinition = super.getByUniqueId(nameOrUuid);
		}
		return taskDefinition;
	}
}
