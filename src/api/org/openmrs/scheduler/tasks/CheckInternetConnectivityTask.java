package org.openmrs.scheduler.tasks;

//import java.net.HttpURLConnection;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.TaskConfig;

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
	 *  Public constructor.
	 */
	public CheckInternetConnectivityTask() { 
	}

	/**
	 * Initialize task.
	 * 
	 * @param config
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
				if (Context.isAuthenticated() == false)
					authenticate();
				String text = "At " + new Date() + " there was an error reported connecting to the internet address " + url + ": " + ioe;
				// TODO role should be provided as a property to taskconfig
				Role role = Context.getUserService().getRole("System Developer");
				Collection<User> users = Context.getUserService().getUsersByRole(role);
				Context.getAlertService().createAlert(text, users);
			} catch (Exception e) { 
				// Uh oh, just log it.
				log.error(e);
			}
		}
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
