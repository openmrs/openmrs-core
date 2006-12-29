package org.openmrs.scheduler.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.hl7.HL7InQueueProcessor;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.TaskConfig;

import ca.uhn.hl7v2.HL7Exception;

/**
 * Implementation of a task that process all form entry queues.
 * 
 * NOTE: This class does not need to be StatefulTask as we create the context in
 * the constructor.
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.1
 * 
 * 1.1 - made processor static to ensure only one HL7 processor runs
 */
public class ProcessHL7InQueueTask implements Schedulable {

	// Logger
	private static Log log = LogFactory.getLog(ProcessHL7InQueueTask.class);
	
	// Instance of configuration information for task
	private TaskConfig taskConfig;

	// Instance of hl7 processor
	private static HL7InQueueProcessor processor = null;

	/**
	 * Default Constructor (Uses SchedulerConstants.username and
	 * SchedulerConstants.password
	 */
	public ProcessHL7InQueueTask() {
		if (processor == null)
			processor = new HL7InQueueProcessor();
	}

	/**
	 * Process the next form entry in the database and then remove the form
	 * entry from the database.
	 */
	public void run() {
		Context.openSession();
		try {
			log.debug("Processing HL7 queue ... ");
			if (Context.isAuthenticated() == false)
				authenticate();
			processor.processHL7InQueue();
		} catch (HL7Exception e) {
			log.error("Error running hl7 in queue task", e);
			throw new APIException("Error running hl7 error queue task", e);
		} finally {
			Context.closeSession();
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
