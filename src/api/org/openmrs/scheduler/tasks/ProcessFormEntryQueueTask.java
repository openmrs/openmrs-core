package org.openmrs.scheduler.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.formentry.FormEntryQueueProcessor;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.TaskConfig;

/**
 * Implementation of a task that process all form entry queues.
 * 
 * NOTE: This class does not need to be StatefulTask as we create the context in
 * the constructor.
 * 
 * @author Justin Miranda
 * @version 1.0
 */
public class ProcessFormEntryQueueTask implements Schedulable {

	// Logger
	private static Log log = LogFactory.getLog(ProcessFormEntryQueueTask.class);

	// Task configuration 
	private TaskConfig taskConfig;
	
	// Instance of form processor
	private static FormEntryQueueProcessor processor = null;
	
	/**
	 * Default Constructor (Uses SchedulerConstants.username and
	 * SchedulerConstants.password
	 * 
	 */
	public ProcessFormEntryQueueTask() {
		if (processor == null)
			processor = new FormEntryQueueProcessor();
	}

	/**
	 * Process the next form entry in the database and then remove the form
	 * entry from the database.
	 */
	public void run() {
		log.debug("Processing form entry queue ... ");
		try {
			if (Context.isAuthenticated() == false)
				authenticate();
			processor.processFormEntryQueue();
		} catch (APIException e) {
			log.error("Error running form entry queue task", e);
			throw e;
		}
	}
	
	/**
	 * Initialize task.
	 * 
	 * @param config
	 */
	public void initialize(TaskConfig config) { 
		this.taskConfig = config;
	}
	
	private void authenticate() {
		try {
			AdministrationService adminService = Context.getAdministrationService();
			Context.authenticate(adminService.getGlobalProperty("scheduler.username"),
				adminService.getGlobalProperty("scheduler.password"));
			
		} catch (ContextAuthenticationException e) {
			log.error("Error authenticating user", e);
		}
	}

}
