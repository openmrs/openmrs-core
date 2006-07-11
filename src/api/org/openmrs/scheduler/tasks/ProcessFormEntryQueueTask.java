package org.openmrs.scheduler.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.ContextFactory;
import org.openmrs.formentry.FormEntryQueueProcessor;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.SchedulerConstants;
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

	// Instance of context used during task execution
	private static Context context;

	// Instance of form processor
	private static FormEntryQueueProcessor processor;
	
	/**
	 * Default Constructor (Uses SchedulerConstants.username and
	 * SchedulerConstants.password
	 * 
	 */
	public ProcessFormEntryQueueTask() {
		context = ContextFactory.getContext();
		try {
			context.authenticate(SchedulerConstants.SCHEDULER_USERNAME,
					SchedulerConstants.SCHEDULER_PASSWORD);
		} catch (ContextAuthenticationException e) {
			log.error("Error authenticating user", e);
		}
		processor = new FormEntryQueueProcessor(context);
	}

	
	
	
	/**
	 * Public constructor
	 */
	public ProcessFormEntryQueueTask(String username, String password) {
		context = ContextFactory.getContext();
		try {
			context.authenticate(username, password);
		} catch (ContextAuthenticationException e) {
			log.error("Error authenticating user", e);
		}
		processor = new FormEntryQueueProcessor(context);
	}

	/**
	 * Process the next form entry in the database and then remove the form
	 * entry from the database.
	 */
	public void run() {
		log.debug("Processing form entry queue ... ");
		try {
			processor.processFormEntryQueue();
		} catch (APIException e) {
			log.error(e);
		}		
	}
	
	public void initialize(TaskConfig taskConfig) { 
		return;
	}

}
