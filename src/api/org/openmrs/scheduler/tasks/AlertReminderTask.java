package org.openmrs.scheduler.tasks;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.ContextFactory;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertRecipient;
import org.openmrs.notification.Message;
import org.openmrs.scheduler.Schedulable;
import org.openmrs.scheduler.SchedulerConstants;
import org.openmrs.scheduler.TaskConfig;

/**
 *  Sample implementation of task that shows how to send emails to users/roles via message service.
 *
 */
public class AlertReminderTask implements Schedulable { 

	// Logger 
	private Log log = LogFactory.getLog( AlertReminderTask.class );

	// Instance of context used during task execution
	private Context context;

	// Instance of configuration information for task
	private TaskConfig taskConfig;

	/**
	 * Public constructor
	 *
	 */
	public AlertReminderTask() { 
		this.context = ContextFactory.getContext();
		try {
			context.authenticate(SchedulerConstants.SCHEDULER_USERNAME,
					SchedulerConstants.SCHEDULER_PASSWORD);
		} catch (ContextAuthenticationException e) {
			log.error("Error authenticating user", e);
		}		
	}
	
	/**
	 *  Set the context.
	 *
	 *  @param  Context  context
	 */
	public void setContext( Context context ) { 
		this.context = context;
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
	 * Send alert reminder email to user(s) associated with the alert.
	 */
	public void run() {
		try { 
			// TODO Change to getAllAlerts(Boolean includeRead, Boolean includeExpired);
			Collection<Alert> alerts = context.getAlertService().getAllAlerts(false);
			Collection<User> users = new HashSet<User>();
			Message message = context.getMessageService().create("Alert Reminder", "You have unread alerts.");

			for (Alert alert : alerts) { 
				log.debug("Send email to alert recipient(s) ...");
				if (alert.isAlertRead() == false && alert.getRecipients() != null) {
					for (AlertRecipient recipient : alert.getRecipients()) {
						if (recipient.isAlertRead() == false && recipient.getRecipient() != null)
							users.add(recipient.getRecipient());
					}
				}
			}
			
			// Send a message to each person only once
			context.getMessageService().send(message, users);
		} 
		catch (Exception e) { 
			log.error(e);
			e.printStackTrace();
		}
	
	}

}
