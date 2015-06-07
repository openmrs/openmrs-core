/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.util.PrivilegeConstants;

public class SchedulerUtil {
	
	private static Log log = LogFactory.getLog(SchedulerUtil.class);
	
	/**
	 * Start the scheduler given the following start up properties.
	 * 
	 * @param p properties used to start the service
	 */
	public static void startup(Properties p) {
		// Override the Scheduler constants if specified by the user
		
		String val = p.getProperty("scheduler.username", null);
		if (val != null) {
			SchedulerConstants.SCHEDULER_DEFAULT_USERNAME = val;
			log.warn("Deprecated runtime property: scheduler.username. Value set in global_property in database now.");
		}
		
		val = p.getProperty("scheduler.password", null);
		if (val != null) {
			SchedulerConstants.SCHEDULER_DEFAULT_PASSWORD = val;
			log.warn("Deprecated runtime property: scheduler.username. Value set in global_property in database now.");
		}
		
		// TODO: do this for all services
		try {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
			Context.getSchedulerService().onStartup();
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
		}
	}
	
	/**
	 * Shutdown the scheduler service that is statically associated with the Context class.
	 */
	public static void shutdown() {
		SchedulerService service = null;
		
		// ignores errors while getting the scheduler service 
		try {
			service = Context.getSchedulerService();
		}
		catch (Exception e) {
			// pass
		}
		
		// TODO: Do this for all services
		try {
			Context.addProxyPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
			// doesn't attempt shutdown if there was an error getting the scheduler service
			if (service != null) {
				service.onShutdown();
			}
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_SCHEDULER);
		}
		
	}
	
	/**
	 * Sends an email with system information and the given exception
	 * 
	 * @param error
	 */
	public static void sendSchedulerError(Throwable throwable) {
		try {
			Context.openSession();
			
			Boolean emailIsEnabled = Boolean.valueOf(Context.getAdministrationService().getGlobalProperty(
			    SchedulerConstants.SCHEDULER_ADMIN_EMAIL_ENABLED_PROPERTY));
			
			if (emailIsEnabled) {
				// Email addresses seperated by commas
				String recipients = Context.getAdministrationService().getGlobalProperty(
				    SchedulerConstants.SCHEDULER_ADMIN_EMAIL_PROPERTY);
				
				// Send message if 
				if (recipients != null) {
					
					// TODO need to use the default sender for the application 
					String sender = SchedulerConstants.SCHEDULER_DEFAULT_FROM;
					String subject = SchedulerConstants.SCHEDULER_DEFAULT_SUBJECT + " : " + throwable.getClass().getName();
					StringBuilder message = new StringBuilder();
					message.append("\n\nStacktrace\n============================================\n");
					message.append(SchedulerUtil.getExceptionAsString(throwable));
					message.append("\n\nSystem Variables\n============================================\n");
					for (Map.Entry<String, String> entry : Context.getAdministrationService().getSystemVariables()
					        .entrySet()) {
						message.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
					}
					
					// TODO need to the send the IP information for the server instance that is running this task
					
					log.debug("Sending scheduler error email to [" + recipients + "] from [" + sender + "] with subject ["
					        + subject + "]:\n" + message);
					Context.getMessageService().sendMessage(recipients, sender, subject, message.toString());
				}
				
			}
			
		}
		catch (Exception e) {
			// Log, but otherwise suppress errors
			log.warn("Could not send scheduler error email: ", e);
		}
		finally {
			Context.closeSession();
		}
	}
	
	/**
	 * @param e
	 * @return <code>String</code> representation of the given exception
	 */
	public static String getExceptionAsString(Throwable t) {
		return ExceptionUtils.getStackTrace(t);
	}
	
	/**
	 * Gets the next execution time based on the initial start time (possibly years ago, depending
	 * on when the task was configured in OpenMRS) and the repeat interval of execution. We need to
	 * calculate the "next execution time" because the scheduled time is most likely in the past and
	 * the JDK timer will run the task X number of times from the start time until now in order to
	 * catch up. The assumption is that this is not the desired behavior -- we just want to execute
	 * the task on its next execution time. For instance, say we had a scheduled task that ran every
	 * 24 hours at midnight. In the database, the task would likely have a past start date (e.g.
	 * 04/01/2006 12:00am). If we scheduled the task using the JDK Timer
	 * scheduleAtFixedRate(TimerTask task, Date startDate, int interval) method and passed in the
	 * start date above, the JDK Timer would execute this task once for every day between the start
	 * date and today, which would lead to hundreds of unnecessary (and likely expensive)
	 * executions.
	 * 
	 * @see java.util.Timer
	 * @param taskDefinition the task definition to be executed
	 * @return the next "future" execution time for the given task
	 * @should get the correct repeat interval
	 */
	public static Date getNextExecution(TaskDefinition taskDefinition) {
		Calendar nextTime = Calendar.getInstance();
		
		try {
			Date firstTime = taskDefinition.getStartTime();
			
			if (firstTime != null) {
				
				// Right now
				Date currentTime = new Date();
				
				// If the first time is actually in the future, then we use that date/time
				if (firstTime.after(currentTime)) {
					return firstTime;
				}
				
				// The time between successive runs (e.g. 24 hours)
				long repeatInterval = taskDefinition.getRepeatInterval().longValue();
				if (repeatInterval == 0) {
					// task is one-shot so just return the start time
					return firstTime;
				}
				
				// Calculate time between the first time the process was run and right now (e.g. 3 days, 15 hours)
				long betweenTime = currentTime.getTime() - firstTime.getTime();
				
				// Calculate the last time the task was run   (e.g. 15 hours ago)
				long lastTime = (betweenTime % (repeatInterval * 1000));
				
				// Calculate the time to add to the current time (e.g. 24 hours - 15 hours = 9 hours)
				long additional = ((repeatInterval * 1000) - lastTime);
				
				nextTime.setTime(new Date(currentTime.getTime() + additional));
				
				log.debug("The task " + taskDefinition.getName() + " will start at " + nextTime.getTime());
			}
		}
		catch (Exception e) {
			log.error("Failed to get next execution time for " + taskDefinition.getName(), e);
		}
		
		return nextTime.getTime();
	}
	
}
