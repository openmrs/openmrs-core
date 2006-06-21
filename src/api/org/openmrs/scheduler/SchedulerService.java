package org.openmrs.scheduler;

import java.util.Collection;

import org.openmrs.api.db.DAOContext;

/**
*  Defines methods required to schedule a task.  
*/
public interface SchedulerService { 

	/**
	 * Set the data access context.
	 */
	public void setDaoContext(DAOContext daoContext);
	
	/**
	 * Start all tasks that should be running
	 */
	public void startup();
	
	/**
	 * Stop all running tasks.
	 *
	 */
	public void stop();
	
	/**
	 * Stop all tasks and clean up.
	 *
	 */
	public void shutdown();
	
	/**
	 * Schedule a recurring task that occurs according to the given schedule (start time and interval).
	 */
	public void scheduleTask(Integer taskId) throws SchedulerException;

	/**
	 * Schedule a recurring task that occurs according to the given schedule (start time and interval).
	 */
	public void scheduleTask(TaskConfig task) throws SchedulerException;	

	/**
	 * Cancel a scheduled task.
	 */
	public void stopTask(Integer taskId) throws SchedulerException;

	/**
	 * Cancel a scheduled task.
	 */
	public void stopTask(TaskConfig task) throws SchedulerException;

	/**
	 * Cancel a scheduled task.
	 */
	public void startTask(Integer taskId) throws SchedulerException;

	/**
	 * Cancel a scheduled task.
	 */
	public void startTask(TaskConfig task) throws SchedulerException;
	
	
	/**
	 * Reschedule a scheduled task.  This changes the existing schedule task.  
	 */
	//public void rescheduleTask( TaskConfig task, Schedule schedule );
	
	/**
	 * Get scheduled tasks.
	 *  
	 * @return 	all scheduled tasks
	 */
	public Collection<TaskConfig> getScheduledTasks();

	/**
	 * Get the list of tasks that are available to be scheduled.  
	 * Eventually, these should go in the database.
	 * 
	 * @return	all available tasks
	 */
	public Collection<TaskConfig> getAvailableTasks();

	/**
	 * Set scheduled tasks.
	 *  
	 * @param	tasks 	the tasks that should be scheduled
	 */
	public void startTasks(Collection<TaskConfig> tasks);

	/**
	 * Set scheduled tasks.
	 *  
	 * @param	tasks 	the tasks that should be scheduled
	 */
	public Collection<TaskConfig> getTasks();

	/**
	 * Get the task with the given identifier.
	 *  
	 * @param	id 		the identifier of the task
	 */
	public TaskConfig getTask(Integer id);

	/**
	 * Delete the task with the given identifier.
	 *  
	 * @param	id 		the identifier of the task
	 */
	public void deleteTask(Integer id);

	/**
	 * Update the given task.
	 *  
	 * @param	task 		the task to be updated
	 */
	public void updateTask(TaskConfig task);

	/**
	 * Create the given task
	 *  
	 * @param	task 		the task to be created
	 */
	public void createTask(TaskConfig task);

}
