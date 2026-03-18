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

import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.stereotype.Component;

/**
 * Used internally to run legacy tasks.
 *
 * @since 2.9.x
 */
@Component
public class LegacyTask implements TaskHandler<TaskDefinition> {

	@Override
	public void execute(TaskDefinition taskData, TaskContext taskContext) throws Exception {
		Class<?> taskClass;
		try {
			taskClass = OpenmrsClassLoader.getInstance().loadClass(taskData.getTaskClass());
		} catch (ClassNotFoundException e) {
			throw new TaskException("Task class " + taskData.getTaskClass() + " not found", false);
		}
		Object instance = taskClass.getDeclaredConstructor().newInstance();

		if (instance instanceof Task) {
			Task task = (Task) instance;
			task.initialize(taskData);
			task.execute();
		} else {
			throw new TaskException("Task class " + taskData.getTaskClass() + " must implement Task", false);
		}
	}
}
