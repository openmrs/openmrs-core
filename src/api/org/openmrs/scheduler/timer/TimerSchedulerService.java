package org.openmrs.scheduler.timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.SchedulableFactory;
import org.openmrs.scheduler.SchedulerConstants;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskConfig;
import org.openmrs.scheduler.db.SchedulerDAO;
import org.openmrs.util.InsertedOrderComparator;
import org.openmrs.util.OpenmrsMemento;

/**
 *  Simple scheduler service that uses JDK timer to trigger and execute scheduled tasks.
 */
public class TimerSchedulerService implements SchedulerService { 
	
	private static Log log = LogFactory.getLog( TimerSchedulerService.class );

	/**
	 * Scheduled Task Map
	 */
	private static Map<Integer, Timer> scheduledTasksMap = new HashMap<Integer, Timer>();

	/**
	 *  Global data access object context
	 */
	private SchedulerDAO schedulerDAO;
	
	/**
	 *  Default public constructor
	 */
	public TimerSchedulerService() {
		scheduledTasksMap = new HashMap<Integer, Timer>();
	}
	
	/**
	 * Gets the scheduler data access object.
	 * @return
	 */
	public SchedulerDAO getSchedulerDAO() { 
		return this.schedulerDAO;
	}
	
	/**
	 * Sets the scheduler data access object.
	 */
	public void setSchedulerDAO(SchedulerDAO dao) {
		this.schedulerDAO = dao;
	}
	
	/**
	 * Start up hook for the scheduler and all of its scheduled tasks.
	 */
	public void startup() {
		log.debug("Starting scheduler service ...");
		
		// go through all tasks and start them if their startOnStartup flag is true
		Collection <TaskConfig> tasks = getSchedulerDAO().getTasks();
		if (tasks != null) {
			for ( TaskConfig task : tasks ) { 
				try { 
					// If the task is configured to start on startup, we schedule it to run
					// Otherwise it needs to be started manually.
					if (task.getStartOnStartup())
						scheduleTask(task);
				} catch ( Exception e ) { 
					log.error("Could not schedule task for class " + task.getSchedulableClass(), e);
				}
			}
		}
	}
	
	/**
	 * Shutdown hook for the scheduler and all of its scheduled tasks.  
	 */
	public void shutdown() { 
		log.debug("Gracefully shutting down scheduler service ...");
		
		// gracefully shutdown all tasks and remove all references to the timers, scheduler
		try {
			stopTasks();
		}
		catch (SchedulerException e) {
			log.warn("Unable to stop all tasks", e);
		}
		catch (APIException e) {
			// scheduler service wasn't available
		}
	
		// Clean up 
		scheduledTasksMap = null;
	}
	
	/**
	 *  Schedule the given task according to the given schedule.
	 *
	 *  @param  taskId      the identifier of the task to be scheduled
	 *  @param  schedule    the time and interval for the scheduled task
	 */
	public void scheduleTask(Integer taskId) throws SchedulerException { 
		scheduleTask(getTask(taskId));
	} 
	
	/**
	 *  Schedule the given task according to the given schedule.
	 *
	 *  @param  task        the task to be scheduled
	 *  @param  schedule    the time and interval for the scheduled task
	 */
	public void scheduleTask(TaskConfig task) throws SchedulerException {
		// Create wrapper for the task 
		Schedulable schedulable = SchedulableFactory.getInstance().createInstance( task );
		
		// if we were unable to get a class, just quit
		if (schedulable == null)
			return;
		
		TimerTaskWrapper wrapper = new TimerTaskWrapper( schedulable );
			
		// See if there's already a timer and cancel it 
		// Never start a timer task more than once.  You need to create another task for multiple schedules.
		Timer timer = getTimer(task);
		if ( timer != null) {
			timer.cancel();
		}
		// Create a new timer
		timer = new Timer();
		
		// Once this method is called, the timer is set to start at the given start time.
		// NOTE:  We need to adjust the repeat interval as the JDK Timer expects time in milliseconds and 
		// we record by seconds.  
		timer.schedule( 
				wrapper, 
				task.getStartTime(), 
				task.getRepeatInterval()*SchedulerConstants.SCHEDULER_MILLIS_PER_SECOND );             
		
		
		// Keep track of the timer so we can cancel later
		startTimer( task, timer ); 
	} 

	/**
	 *  Cancel the given task.
	 *
	 *  @param  task        the task to cancel
	 *  @param  schedule    the schedule to cancel
	 */
	public void stopTask(Integer taskId)  throws SchedulerException{
		stopTask(getTask(taskId));
	}		
	
	/**
	 *  Cancel the given task.
	 *
	 *  @param  task        the task to cancel
	 *  @param  schedule    the schedule to cancel
	 */
	public void stopTask(TaskConfig task) throws SchedulerException { 
		stopTimer(task);
	}

	/**
	 *  Start the given task. 
	 *  
	 *  @param  task        the task to cancel
	 *  @param  schedule    the schedule to cancel
	 */
	public void startTask(Integer taskId) throws SchedulerException {
		TaskConfig task = getTask(taskId);
		startTask(task);
	}		
	
	/**
	 *  Start the given task.  
	 *
	 *  @param  task        the task to cancel
	 *  @param  schedule    the schedule to cancel
	 */
	public void startTask(TaskConfig task) throws SchedulerException { 
		scheduleTask(task);
	}	

	
	/**
	 *  Stop all started tasks.
	 */	
	public void stopTasks() throws SchedulerException {
		// iterate over this (copied) list of tasks and stop them all
		for ( TaskConfig task : getScheduledTasks()) { 
			try { 
				stopTask(task);

			} catch ( SchedulerException e ) { 
				log.error("Could not stop task for class " + task.getSchedulableClass(), e);
			}
		}
	}
	
	/**
	 * Loop over all currently started tasks and cycle them.
	 * This should be done after the classloader has been changed
	 * (e.g. during module start/stop)
	 */
	public void restartTasks() throws SchedulerException {
		for (TaskConfig task : getScheduledTasks()) {
			try {
				stopTask(task);
				startTask(task);
			}
			catch (SchedulerException e) {
				log.error("Error restarting task: " + task.getName(), e);
			}
		}
	}
	
	
	/**
	 *  Register a new task by adding it to our task map with an empty schedule map.
	 *
	 *  @param    task    register a task
	 */
	public void registerTask(TaskConfig task) { 
		scheduledTasksMap.put(task.getId(), null);
	}

	
	/**
	 * Get all scheduled tasks.
	 * 
	 * @return 	all scheduled tasks
	 */
	public Collection<TaskConfig> getScheduledTasks() {
		List<TaskConfig> scheduledTasks = new ArrayList<TaskConfig>();
		
		for (TaskConfig task : getSchedulerDAO().getTasks()) {
			if (task.getStarted())
				scheduledTasks.add(task);
		}

//		for ( Integer taskId : scheduledTaskMap.keySet() ) { 
//			TaskConfig task = getTask(taskId);
//			if (scheduledTaskMap.get(task)!=null) { 
//				scheduledTasks.add(task);
//			}
//		}
		
		return scheduledTasks;
	}
	
	
	/**
	 * Get all available tasks.
	 * 
	 * @return   all available tasks 
	 */	
	public Collection<TaskConfig> getAvailableTasks() { 
		List<TaskConfig> availableTasks = new ArrayList<TaskConfig>();
		for ( TaskConfig task : getSchedulerDAO().getTasks() ) { 
			availableTasks.add(task);
		}
		return availableTasks;
	}
	
	
	/**
	 * Get all tasks.
	 *  
	 * @return	a collection of tasks
	 */
	public Collection<TaskConfig> getTasks() { 
		return getSchedulerDAO().getTasks();
	}

	/**
	 * Get the task with the given identifier.
	 *  
	 * @param	id	the identifier of the task
	 */
	public TaskConfig getTask(Integer id) { 
		log.debug("get task " + id);
		return getSchedulerDAO().getTask(id);
	}

	/**
	 * Get the task with the given identifier.
	 *  
	 * @param	id	the identifier of the task
	 */
	public void createTask(TaskConfig task) { 
		setCreatedMetadata(task);
		getSchedulerDAO().createTask( task );
	}
	
	/**
	 * Get the task with the given identifier.
	 *  
	 * @param	id	the identifier of the task
	 */
	public void updateTask(TaskConfig task) { 
		setChangedMetadata(task);
		getSchedulerDAO().updateTask(task);
	}
	
	/**
	 * Delete the task with the given identifier.
	 *  
	 * @param	id	the identifier of the task
	 */
	public void deleteTask(Integer id) {
		
		// try to stop the task (ignore errors)
		TaskConfig task = getTask(id);
		if (task.getStarted()) {
			try {
				stopTask(task);
			}
			catch (SchedulerException e) {
				// pass
			}
		}
		
		// delete the task
		getSchedulerDAO().deleteTask(id);
	}

	
	/**
	 * Convenience method for setting all metadata fields.
	 * @param task
	 */
	public void setCreatedMetadata(TaskConfig task) { 		
		if (task.getCreatedBy() == null) task.setCreatedBy(Context.getAuthenticatedUser());
		if (task.getDateCreated() == null) task.setDateCreated(new Date());
		setChangedMetadata(task);
	}
	
	/**
	 * Convenience method for setting the changed by and changed date fields
	 * @param task
	 */
	public void setChangedMetadata(TaskConfig task) {
		task.setChangedBy(Context.getAuthenticatedUser());
		task.setDateChanged(new Date());
	}
	
	public SortedMap<String,String> getSystemVariables() {
		TreeMap<String,String> systemVariables = new TreeMap<String,String>(new InsertedOrderComparator());
		// scheduler username and password can be found in the global properties
		systemVariables.put("SCHEDULER_MILLIS_PER_SECOND", String.valueOf(SchedulerConstants.SCHEDULER_MILLIS_PER_SECOND));
		return systemVariables;
	}
	
  //*******************************************************************************************
  // 						T I M E R   M E T H O D S
  //*******************************************************************************************  
  
	/**
	 *  Get the timer task associated with the given task and schedule.
	 *
	 *  @param  task
	 *  @param  schedule
	 */
  	private Timer getTimer(TaskConfig task) { 
		log.debug("Getting timer for task " + task.getId());
  		return scheduledTasksMap.get( task.getId() );
	}
  
  
  	/**
  	 *  Add a timer to the map of scheduled tasks.
  	 *
  	 *  @param  task  the task that has been scheduled
  	 */
  	private void startTimer( TaskConfig task, Timer timer ) { 
  		log.debug("Starting timer for task " + task.getId());
  		
  		//  Add the new timer 
  		scheduledTasksMap.put(task.getId(), timer);
  		
  		// Update task that has been started
  		if ( timer != null ) {
	  		task.setStarted(true);
	  		updateTask(task);
  		}
  	}

	/**
	 * Remove the timer associated with the task and schedule.
	 *
	 * @param  task
	 * @param  schedule
	 */
	private void stopTimer(TaskConfig task) throws SchedulerException { 		
		log.debug("Stopping timer for task " + task.getId());

		// Cancel the timer
		Timer timer = scheduledTasksMap.remove( task.getId() );
		if (timer != null) { 
			timer.cancel();
			timer = null;
		}
		else
			throw new SchedulerException("Timer: " + timer + " was not found and hence cannot be stopped");
		
		// Update task that has been started
		task.setStarted(false);
  		updateTask(task);
		
	}
	
	
	/**
	 * Saves and stops all active tasks
	 * 
	 * @returns OpenmrsMemento
	 */
	public OpenmrsMemento saveToMemento() {
			
		Set<Integer> tasks = new HashSet<Integer>();
		
		for (TaskConfig task : getScheduledTasks()) {
			tasks.add(task.getId());
			try { 
				stopTask(task);
			} catch ( Exception e ) { 
				// just swallow exceptions
				log.debug("Unable to stop task while saving memento " + task.getName(), e);
			}
		}
		
		TimerSchedulerMemento memento = new TimerSchedulerMemento(tasks);
		memento.saveErrorTasks();
		
		return memento;
	}
	
	/**
	 * 
	 */
	public void restoreFromMemento(OpenmrsMemento memento) {
		
		if (memento != null && memento instanceof TimerSchedulerMemento) {
			TimerSchedulerMemento timerMemento = (TimerSchedulerMemento)memento;
			
			Set<Integer> tasks = (HashSet<Integer>) timerMemento.getState();
			
			// try to start all of the tasks that were stopped right before this restore
			for (Integer i : tasks) {
				try {
					startTask(i);
				}
				catch (Exception e) {
					// essentially swallow exceptions
					log.debug("EXPECTED ERROR IF STOPPING THIS TASK'S MODULE: Unable to start task with id " + i, e);
					
					// save this errored task and try again next time we restore
					timerMemento.addErrorTask(i);
				}
			}
		}
	}
	
}