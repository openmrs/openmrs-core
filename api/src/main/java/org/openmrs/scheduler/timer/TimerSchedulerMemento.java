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
