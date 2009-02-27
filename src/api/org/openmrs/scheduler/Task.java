package org.openmrs.scheduler;

public interface Task {
	
	/**
	 * Executes the task defined in the task definition.
	 */
	void execute();
	
	/**
	 * Initializes the task and sets the task definition.
	 * 
	 * @param config
	 */
	void initialize(TaskDefinition config);
	
	/**
	 * Returns true if the task is currently in its execute() method.
	 * 
	 * @return true if task is executing, false otherwise
	 */
	boolean isExecuting();
	
	/**
	 * Callback method used to clean up resources used during the tasks execution.
	 */
	void shutdown();
	
}
