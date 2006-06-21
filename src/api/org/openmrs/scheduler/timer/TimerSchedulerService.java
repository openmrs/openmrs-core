package org.openmrs.scheduler.timer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.SchedulableFactory;
import org.openmrs.scheduler.SchedulerConstants;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskConfig;

/**
 *  Simple scheduler service that uses JDK timer to trigger and execute scheduled tasks.
 */
public class TimerSchedulerService implements SchedulerService { 
	
	/**
	 *  Logger
	 */ 
	private static Log log = LogFactory.getLog( TimerSchedulerService.class );

	/**
	 * Reference to the context.
	 */
	private Context context;
	
	/**
	 *  Global data access object context
	 *  
	 *  TODO I think this should actually be the instance of the specific DAO that is needed by the service (SchedulerDAO).
	 */
	private DAOContext daoContext;
	
	/**
	 * Scheduled Task Map
	 */
	private Map<Integer, Timer> scheduledTaskMap;
  
	/**
	 *  Default public constructor
	 */
	public TimerSchedulerService(Context context) {
		this.context = context;
		this.scheduledTaskMap = new HashMap<Integer, Timer>();
	}
	
	/**
	 * Sets the DAO context for this service.
	 */
	public void setDaoContext(DAOContext daoContext) {
		log.info("Setting DAO Context to " + daoContext);
		this.daoContext = daoContext;
	}
		
	/**
	 * Start up hook for the scheduler and all of its scheduled tasks.
	 */
	public void startup() {
		log.debug("Starting scheduler service ...");
		// TODO go through all tasks and start them if their startOnStartup flag is true
		Collection <TaskConfig> tasks = daoContext.getSchedulerDAO().getTasks();
		startTasks( tasks );
	}
	
	/**
	 * Shutdown hook for the scheduler and all of its scheduled tasks.  
	 */
	public void shutdown() { 
		log.debug("Gracefully shutting down scheduler service ...");
		// TODO gracefully shutdown all tasks and remove all references to the timers, scheduler
		stop();
	
		// Clean up 
		daoContext = null;
		scheduledTaskMap = null;
	}
	
	/**
	 * Temporarily stop all scheduled tasks.
	 */
	public void stop() { 
		log.debug("Stopping scheduler service ...");
		// TODO gracefull stop all tasks 
		stopTasks();
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
	 *  TODO Need this to be involved in a transaction.
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
	 *  TODO Need this to be involved in a transaction.
	 *
	 *  @param  task        the task to cancel
	 *  @param  schedule    the schedule to cancel
	 */
	public void startTask(TaskConfig task) throws SchedulerException { 
		scheduleTask(task);
	}	

	
	/**
	 *  Start all tasks.
	 *  
	 *  @param    tasks     the tasks that should be scheduled 
	 */	
	public void startTasks(Collection<TaskConfig> tasks) { 
		if ( tasks != null ) { 
			for ( TaskConfig task : tasks ) { 
				try { 
					// If the task is configured to start on startup, we schedule it to run
					// Otherwise it needs to be started manually.
					if (task.getStartOnStartup())
						scheduleTask(task);
				} catch ( SchedulerException e ) { 
					log.error("Could not schedule task for class " + task.getSchedulableClass(), e);
				}
			}
		}
	}	

	/**
	 *  Stop all tasks.
	 */	
	public void stopTasks() { 
		for ( Integer id : scheduledTaskMap.keySet() ) { 
			TaskConfig task = null;
			try { 
				task = getTask(id);
				stopTask(task);

			} catch ( SchedulerException e ) { 
				log.error("Could not stop task for class " + task.getSchedulableClass(), e);
			}
		}
	}		
	
	
	/**
	 *  Start all tasks.  This would be used by spring to start all tasks.
	 *  
	 *  @param    tasks     the tasks that should be scheduled 
	 */	
	public void setTasks(Collection<TaskConfig> tasks) { 
		startTasks(tasks);
	}
	
	
	/**
	 *  Register a new task by adding it to our task map with an empty schedule map.
	 *
	 *  @param    task    register a task
	 */
	public void registerTask(TaskConfig task) { 
		scheduledTaskMap.put(task.getId(), null);
	}

	
	/**
	 * Get all scheduled tasks.
	 * 
	 * @return 	all scheduled tasks
	 */
	public Collection<TaskConfig> getScheduledTasks() {
		List<TaskConfig> scheduledTasks = new ArrayList<TaskConfig>();
		for ( Integer taskId : scheduledTaskMap.keySet() ) { 
			TaskConfig task = getTask(taskId);
			if (scheduledTaskMap.get(task)!=null) { 
				scheduledTasks.add(task);
			}
		}
		return scheduledTasks;
	}
	
	
	/**
	 * Get all available tasks.
	 * 
	 * @return   all available tasks 
	 */	
	public Collection<TaskConfig> getAvailableTasks() { 
		List<TaskConfig> availableTasks = new ArrayList<TaskConfig>();
		for ( Integer taskId : scheduledTaskMap.keySet() ) { 
			availableTasks.add(getTask(taskId));
		}
		return availableTasks;
	}
	
	
	/**
	 * Get all tasks.
	 *  
	 * @return	a collection of tasks
	 */
	public Collection<TaskConfig> getTasks() { 
		return daoContext.getSchedulerDAO().getTasks();
	}

	/**
	 * Get the task with the given identifier.
	 *  
	 * @param	id	the identifier of the task
	 */
	public TaskConfig getTask(Integer id) { 
		log.debug("get task " + id);
		return daoContext.getSchedulerDAO().getTask(id);
	}

	/**
	 * Get the task with the given identifier.
	 *  
	 * @param	id	the identifier of the task
	 */
	public void createTask(TaskConfig task) { 
		setCreatedMetadata(task);
		daoContext.getSchedulerDAO().createTask( task );
	}
	
	/**
	 * Get the task with the given identifier.
	 *  
	 * @param	id	the identifier of the task
	 */
	public void updateTask(TaskConfig task) { 
		setChangedMetadata(task);
		daoContext.getSchedulerDAO().updateTask(task);
	}
	
	/**
	 * Delete the task with the given identifier.
	 *  
	 * @param	id	the identifier of the task
	 */
	public void deleteTask(Integer id) { 
		daoContext.getSchedulerDAO().deleteTask(id);
	}

	
	/**
	 * Convenience method for setting all metadata fields.
	 * @param task
	 */
	public void setCreatedMetadata(TaskConfig task) { 		
		if (task.getCreatedBy() == null) task.setCreatedBy(context.getAuthenticatedUser());
		if (task.getDateCreated() == null) task.setDateCreated(new Date());
		setChangedMetadata(task);
	}
	
	/**
	 * Convenience method for setting the changed by and changed date fields
	 * @param task
	 */
	public void setChangedMetadata(TaskConfig task) {
		task.setChangedBy(context.getAuthenticatedUser());
		task.setDateChanged(new Date());
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
  		return scheduledTaskMap.get( task.getId() );
	}
  
  
  	/**
  	 *  Add a timer to the map of scheduled tasks.
  	 *
  	 *  TODO: Do we need to check to see if map already has timer task for this schedule?
  	 *  TODO: This method needs to be involved in a transaction BADLY.
  	 *  
  	 *  @param  task  the task that has been scheduled
  	 */
  	private void startTimer( TaskConfig task, Timer timer ) { 
  		log.debug("Starting timer for task " + task.getId());
  		
  		//  Add the new timer 
  		scheduledTaskMap.put(task.getId(), timer);
  		
  		// Update task that has been started
  		if ( timer != null ) {
	  		task.setStarted(true);
	  		updateTask(task);
  		}
  	}

	/**
	 * Remove the timer associated with the task and schedule.
	 *
	 * TODO: Need to add transactions around this  
  	 * TODO: This method needs to be involved in a transaction BADLY.
  	 *  
	 * @param  task
	 * @param  schedule
	 */
	private void stopTimer(TaskConfig task) { 		
		log.debug("Stopping timer for task " + task.getId());

		// Cancel the timer
		Timer timer = scheduledTaskMap.get( task.getId() );
		if (timer != null) { 
			timer.cancel();
			timer = null;
		}
		// Remove the timer 
		// Causes a ConcurrentModificationException when we call stopTimer() within for loop
		//scheduledTaskMap.remove(task.getId());

		// Update task to be not-started
		task.setStarted(false);
		updateTask(task);
	}
	
}