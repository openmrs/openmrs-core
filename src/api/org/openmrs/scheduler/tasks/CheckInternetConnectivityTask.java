package org.openmrs.scheduler.tasks;

//import java.net.HttpURLConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.ContextFactory;
import org.openmrs.formentry.FormEntryQueueProcessor;
import org.openmrs.notification.Alert;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.SchedulableFactory;
import org.openmrs.scheduler.SchedulerConstants;
import org.openmrs.scheduler.TaskConfig;
import org.openmrs.util.OpenmrsConstants;

import java.net.URL;
import java.net.URLConnection;
import java.io.IOException;
import java.util.Date;

/**
 *  Simple implementation to check if we have a connection to the internet.
 */
public class CheckInternetConnectivityTask implements Schedulable { 

	/**
	 * Logger 
	 */
	private static Log log = LogFactory.getLog(CheckInternetConnectivityTask.class);
	
	/**
	 * Configuration details for the task (including properties)
	 */
	private TaskConfig taskConfig;

	/**
	 * Context used to get other services
	 */
	private Context context;
	
	/**
	 *  Public constructor.
	 */
	public CheckInternetConnectivityTask() { 
		this.context = ContextFactory.getContext();
		try {
			context.authenticate(SchedulerConstants.SCHEDULER_USERNAME,
					SchedulerConstants.SCHEDULER_PASSWORD);
		} catch (ContextAuthenticationException e) {
			log.error("Error authenticating user", e);
		}
	}

	/**
	 * Initialize task 
	 */
	public void initialize(TaskConfig config) { 
		this.taskConfig = config;
	}
	
	/**
	 * Execute task
	 *  
	 */
	public void run() { 
		
		// TODO url should be provided as a property to taskconfig
		String url = "http://www.google.com:80/index.html";
		try {
			URLConnection connection = new URL( url ).openConnection();
			connection.connect();
		} catch ( IOException ioe ) {
			try { 
				Alert alert = new Alert();
				alert.setText("At " + new Date() + " there was an error reported connecting to the internet address " + url + ": " + ioe);
				// TODO role should be provided as a property to taskconfig
				alert.setRole(context.getUserService().getRole("System Developer"));
				alert.setCreator(context.getUserService().getUserByUsername("admin"));
				context.getAlertService().createAlert(alert);
			} catch (Exception e) { 
				// Uh oh, just log it.
				log.error(e);
			}
		}
	}
}
