/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.helper;

import org.openmrs.scheduler.TaskDefinition;

import java.util.List;

public class TaskAction {
	
	public enum Action {
		SCHEDULETASK, SHUTDOWNTASK, RESCHEDULETASK, RESCHEDULEALLTASKS, DELETE, RUNTASK;
	}
	
	public TaskAction() {
	}
	
	public TaskAction(Action action, List<TaskDefinition> tasks, boolean startAll) {
		this.tasks = tasks;
		this.action = action;
		this.allTasks = startAll;
	}
	
	private List<TaskDefinition> tasks;
	
	private Action action;
	
	private boolean allTasks;
	
	public void setTasks(List<TaskDefinition> tasks) {
		this.tasks = tasks;
	}
	
	public List<TaskDefinition> getTasks() {
		return tasks;
	}
	
	public void setAction(Action type) {
		this.action = type;
	}
	
	public Action getAction() {
		return action;
	}
	
	public boolean isAllTasks() {
		return allTasks;
	}
	
	public void setAllModules(boolean allTasks) {
		this.allTasks = allTasks;
	}
	
}
