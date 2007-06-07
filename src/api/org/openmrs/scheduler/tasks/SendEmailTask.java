package org.openmrs.scheduler.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.TaskConfig;

/**
 *  Implementation of the stateful task that sends an email.
 *
 */
public class SendEmailTask implements Schedulable { 

	// Logger 
	private Log log = LogFactory.getLog( SendEmailTask.class );

	// Instance of configuration information for task
	private TaskConfig taskConfig;

	/**
	 * Initialize task.
	 * 
	 * @param config
	 */
	public void initialize(TaskConfig config) { 
		this.taskConfig = config;
	} 
	/** 
	 *  Process the next form entry in the database and then remove the form entry from the database.
	 *
	 *
	 */
	public void run() {		
		log.debug("Send email ...");
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
