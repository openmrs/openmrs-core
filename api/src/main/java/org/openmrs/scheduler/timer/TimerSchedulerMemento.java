/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.timer;

import java.util.HashSet;
import java.util.Set;

import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.OpenmrsMemento;

public class TimerSchedulerMemento extends OpenmrsMemento {
	
	private Set<TaskDefinition> startedTasks = new HashSet<TaskDefinition>();
	
	private static Set<TaskDefinition> errorTasks = new HashSet<TaskDefinition>();
	
	public TimerSchedulerMemento(Set<TaskDefinition> tasks) {
		this.startedTasks = tasks;
	}
	
	@Override
	public Object getState() {
		return startedTasks;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setState(Object state) {
		this.startedTasks = (Set<TaskDefinition>) state;
	}
	
	public Boolean addErrorTask(TaskDefinition task) {
		return errorTasks.add(task);
	}
	
	public Boolean removeErrorTask(TaskDefinition task) {
		return errorTasks.remove(task);
	}
	
	public static Set<TaskDefinition> getErrorTasks() {
		return errorTasks;
	}
	
	public void saveErrorTasks() {
		this.startedTasks.addAll(errorTasks);
	}
	
}
