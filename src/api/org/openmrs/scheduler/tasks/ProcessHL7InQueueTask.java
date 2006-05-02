package org.openmrs.scheduler.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.ContextFactory;
import org.openmrs.hl7.HL7Exception;
import org.openmrs.hl7.HL7InQueueProcessor;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.SchedulerConstants;
import org.openmrs.scheduler.TaskConfig;

/**
 * Implementation of a task that process all form entry queues.
 * 
 * NOTE: This class does not need to be StatefulTask as we create the context in
 * the constructor.
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class ProcessHL7InQueueTask implements Schedulable {

	// Logger
	private static Log log = LogFactory.getLog(ProcessFormEntryQueueTask.class);

	// Instance of context used during task execution
	private static Context context;

	// Instance of hl7 processor
	private static HL7InQueueProcessor processor;

	/**
	 * Default Constructor (Uses SchedulerConstants.username and
	 * SchedulerConstants.password
	 */
	public ProcessHL7InQueueTask() {
		context = ContextFactory.getContext();
		try {
			context.authenticate(SchedulerConstants.SCHEDULER_USERNAME,
					SchedulerConstants.SCHEDULER_PASSWORD);
		} catch (ContextAuthenticationException e) {
			log.error("Error authenticating user", e);
		}
		processor = new HL7InQueueProcessor(context);
	}

	/**
	 * Public constructor
	 */
	public ProcessHL7InQueueTask(String username, String password) {
		context = ContextFactory.getContext();
		try {
			context.authenticate(username, password);
		} catch (ContextAuthenticationException e) {
			log.error("Error authenticating user", e);
		}
		processor = new HL7InQueueProcessor(context);
	}

	/**
	 * Process the next form entry in the database and then remove the form
	 * entry from the database.
	 */
	public void run() {
		try {
			log.debug("Processing HL7 queue ... ");
			processor.processHL7InQueue();
		} catch (HL7Exception e) {
			log.error(e);
		}
	}
	
	/**
	 * Initialize the task.
	 */
	public void initialize(TaskConfig taskConfig) { 
		return;
	}

}
