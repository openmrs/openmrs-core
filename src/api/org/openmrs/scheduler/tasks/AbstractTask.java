package org.openmrs.scheduler.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.scheduler.SchedulerConstants;
import org.openmrs.scheduler.Task;
import org.openmrs.scheduler.TaskDefinition;

/**
 * Base class for all other task classes.
 * 
 * @author Justin Miranda
 */
@SuppressWarnings("unused")
public abstract class AbstractTask implements Task {
	
	// Logger 
	private Log log = LogFactory.getLog(AbstractTask.class);
	
	// Indicates whether the task is currently running
	protected boolean isExecuting = false;
	
	// The task definition of the running task 
	protected TaskDefinition taskDefinition;
	
	/**
	 * Default constructor
	 */
	protected AbstractTask() {
	}
	
	/**
	 * Constructor
	 * 
	 * @param definition the task definition
	 */
	protected AbstractTask(TaskDefinition taskDefinition) {
		initialize(taskDefinition);
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#execute()
	 */
	public abstract void execute();
	
	/**
	 * @see org.openmrs.scheduler.Task#isExecuting()
	 */
	public boolean isExecuting() {
		return isExecuting;
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#initialize(org.openmrs.scheduler.TaskConfig)
	 */
	public void initialize(final TaskDefinition definition) {
		this.taskDefinition = definition;
	}
	
	/**
	 * @see org.openmrs.scheduler.Task#shutdown()
	 */
	public void shutdown() {
		taskDefinition = null;
	}
	
	/**
	 * Callback method that tells the task that it has started executing.
	 */
	public void startExecuting() {
		this.isExecuting = true;
	}
	
	/**
	 * Callback method that tells the task that it has stopped executing.
	 */
	public void stopExecuting() {
		this.isExecuting = false;
	}
	
	/**
	 * Authenticate the context so the task can call service layer.
	 */
	protected void authenticate() {
		try {
			AdministrationService adminService = Context.getAdministrationService();
			Context.authenticate(adminService.getGlobalProperty(SchedulerConstants.SCHEDULER_USERNAME_PROPERTY),
			    adminService.getGlobalProperty(SchedulerConstants.SCHEDULER_PASSWORD_PROPERTY));
			
		}
		catch (ContextAuthenticationException e) {
			log.error("Error authenticating user", e);
		}
	}
	
}
