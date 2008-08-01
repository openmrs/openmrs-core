package org.openmrs.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.scheduler.tasks.*;

/**
 * 
 * @author jmiranda
 */
public class TaskFactory {

	/**
	 *  Singleton instance of the schedulable factory
	 */
	private final static TaskFactory factory = new TaskFactory();
	
	/**
	 * Logger 
	 */
	private static Log log = LogFactory.getLog(TaskFactory.class);
	  
	/**
	 * Private constructor 
	 */
	private TaskFactory() { }
	
	/**
	 * Gets an instance of the schedulable factory
	 * @return
	 */
	public static TaskFactory getInstance() { 
		return factory;
	}
  
	/**
	 * Creates a new instance of Schedulable used to run tasks. 
	 * @param taskConfig
	 * @return
	 * @throws SchedulerException
	 */
	public Task createInstance(TaskDefinition taskDefinition) throws SchedulerException {
		try {
			
			// Retrieve the appropriate class
			Class taskClass = 
				OpenmrsClassLoader.getInstance().loadClass( taskDefinition.getTaskClass() );
			
			// Create a new instance of the schedulable class 
			Task task = 
				(Task) taskClass.newInstance();

			if (log.isDebugEnabled()) { 
				log.debug("initializing " + taskClass.getName());
			}
			// Initialize the schedulable object
			task.initialize(taskDefinition);
			
			return task;
		}
		catch (ClassNotFoundException cnfe) {
			log.error("OpenmrsClassLoader could not load class: " + taskDefinition.getTaskClass() + ".  Probably due to a module not being loaded");
			
			if (log.isDebugEnabled())
				log.debug("Full error trace of ClassNotFoundException", cnfe);
			
			return null;
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				// don't need to log errors here necessarily.  If its needed, the calling method can log it.
				log.debug("Error creating new task for class " + taskDefinition.getTaskClass(), e );
			}
			
			throw new SchedulerException(e);
		}
	}
}