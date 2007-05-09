package org.openmrs.scheduler.timer;

import java.util.HashSet;
import java.util.Set;

import org.openmrs.util.OpenmrsMemento;

public class TimerSchedulerMemento extends OpenmrsMemento {
	
	private Set<Integer> startedTasks = new HashSet<Integer>();
	
	private static Set<Integer> errorTasks = new HashSet<Integer>();
	
	public TimerSchedulerMemento(Set<Integer> taskIds) {
		this.startedTasks = taskIds;
	}
	
	@Override
	public Object getState() {
		return startedTasks;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setState(Object state) {
		this.startedTasks = (Set<Integer>)state;
	}
	
	public Boolean addErrorTask(Integer taskId) {
		return errorTasks.add(taskId);
	}
	
	public Boolean removeErrorTask(Integer taskId) {
		return errorTasks.remove(taskId);
	}
	
	public static Set<Integer> getErrorTasks() {
		return errorTasks;
	}

	public void saveErrorTasks() {
		this.startedTasks.addAll(errorTasks);
	}
	
}
