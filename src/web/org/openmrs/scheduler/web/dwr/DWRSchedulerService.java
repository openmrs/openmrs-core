package org.openmrs.scheduler.web.dwr;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.Schedule;
import org.openmrs.scheduler.TaskConfig;



import uk.ltd.getahead.dwr.WebContextFactory;

public class DWRSchedulerService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public boolean isValidSession(Vector objectList) {

		// List to return
		// Object type gives ability to return error strings
		
		Context context = (Context) WebContextFactory.get().getSession()
				.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		HttpServletRequest request = WebContextFactory.get().getHttpServletRequest();
		
		if (context == null) {
			objectList.add("Your session has expired.");
			objectList.add("Please <a href='" + request.getContextPath() + "/logout'>log in</a> again.");
			return false;
		}
		return true;
	}
	
	public void scheduleTask(Integer taskId, String name, String description, String startDate, String startTime, Integer repeatInterval) { 			

		log.debug("Scheduling task " + taskId);
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		try {
			
			Date formattedStartDate = new Date();
			try { 
				log.debug("Start date = " + startDate + ", " + startTime);
				java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
				formattedStartDate = dateFormat.parse(startDate + " " + startTime);
				
				log.debug("Formatted start Date = " + formattedStartDate);
			} catch (Exception e) { 
				log.error(e);
			}
			
			log.debug("Creating new schedule object");
			Schedule schedule = new Schedule( name, description, formattedStartDate, repeatInterval);			

			log.debug("Schedule: " + schedule);
			log.debug("Context: " + context);
			log.debug("Scheduler service: " + context.getSchedulerService());

			//context.getSchedulerService().scheduleTask(task, schedule);
			
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
		}
	}

	public void cancelTask(Integer taskId, Integer scheduleId ) { 			
		log.info("Canceling task " + taskId);
		Context context = (Context) WebContextFactory.get().getSession().getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		try {
			//context.getSchedulerService().cancelTask(taskId, scheduleId);			
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	
}
