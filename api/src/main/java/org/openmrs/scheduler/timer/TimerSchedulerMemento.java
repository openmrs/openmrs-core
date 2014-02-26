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
package org.openmrs.scheduler.timer;

import java.util.HashSet;
import java.util.Set;

import org.openmrs.util.OpenmrsMemento;

public class TimerSchedulerMemento extends OpenmrsMemento {
	
	private Set<Integer> startedTaskIds = new HashSet<Integer>();
	
	private static Set<Integer> errorTaskIds = new HashSet<Integer>();
	
	public TimerSchedulerMemento(Set<Integer> taskIds) {
		this.startedTaskIds = taskIds;
	}
	
	@Override
	public Object getState() {
		return startedTaskIds;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setState(Object state) {
		this.startedTaskIds = (Set<Integer>) state;
	}
	
	public Boolean addErrorTask(Integer taskId) {
		return errorTaskIds.add(taskId);
	}
	
	public Boolean removeErrorTask(Integer taskId) {
		return errorTaskIds.remove(taskId);
	}
	
	public static Set<Integer> getErrorTasks() {
		return errorTaskIds;
	}
	
	public void saveErrorTasks() {
		this.startedTaskIds.addAll(errorTaskIds);
	}
	
}
