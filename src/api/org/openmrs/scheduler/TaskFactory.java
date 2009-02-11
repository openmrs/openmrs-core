package org.openmrs.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.scheduler.tasks.TaskThreadedInitializationWrapper;
import org.openmrs.util.OpenmrsClassLoader;

/**
 */
public class TaskFactory {
	
	/** Singleton instance of the schedulable factory */
	private final static TaskFactory factory = new TaskFactory();
	
	/** Logger */
	private static Log log = LogFactory.getLog(TaskFactory.class);
	
	/** Private constructor */
	private TaskFactory() {
	}
	
	/**
	 * Gets an instance of the schedulable factory
	 * 
	 * @return
	 */
	public static TaskFactory getInstance() {
		return factory;
	}
	
	/**
	 * Creates a new instance of Schedulable used to run tasks. By default the returned task will be
	 * the given task wrapped with the {@link TaskThreadedInitializationWrapper} class so that the
	 * {@link Task#initialize(TaskDefinition)} method runs in a new thread.
	 * 
	 * @param taskConfig
	 * @return
	 * @throws SchedulerException
	 */
	public Task createInstance(TaskDefinition taskDefinition) throws SchedulerException {
		try {
			
			// Retrieve the appropriate class
			Class<?> taskClass = OpenmrsClassLoader.getInstance().loadClass(taskDefinition.getTaskClass());
			
			// Create a new instance of the schedulable class 
			Task task = new TaskThreadedInitializationWrapper((Task) taskClass.newInstance());
			
			if (log.isDebugEnabled()) {
				log.debug("initializing " + taskClass.getName());
			}
			// Initialize the schedulable object
			task.initialize(taskDefinition);
			
			return task;
		}
		catch (ClassNotFoundException cnfe) {
			log.error("OpenmrsClassLoader could not load class: " + taskDefinition.getTaskClass()
			        + ".  Probably due to a module not being loaded");
			
			if (log.isDebugEnabled())
				log.debug("Full error trace of ClassNotFoundException", cnfe);
			
			throw new SchedulerException(cnfe);
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				// don't need to log errors here necessarily.  If its needed, the calling method can log it.
				log.debug("Error creating new task for class " + taskDefinition.getTaskClass(), e);
			}
			
			throw new SchedulerException(e);
		}
	}
}
