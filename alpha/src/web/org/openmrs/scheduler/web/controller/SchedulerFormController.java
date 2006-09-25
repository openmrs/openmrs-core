package org.openmrs.scheduler.web.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.TaskConfig;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class SchedulerFormController extends SimpleFormController {
	
	/** 
	 * Logger for this class and subclasses 
	 */
	protected static final Log log = LogFactory.getLog(SchedulerFormController.class);

	// Move this to message.properties or OpenmrsConstants
	public static String DEFAULT_DATE_PATTERN = "MM/dd/yyyy HH:mm:ss";
	public static DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
	  	  
	/**
	 * 
	 * Allows for Integers to be used as values in input tags.
	 *   Normally, only strings and lists are expected 
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest, org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		//NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
		binder.registerCustomEditor(java.lang.Long.class, new CustomNumberEditor(java.lang.Long.class, true));
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(DEFAULT_DATE_FORMAT, true));
	}

	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		String view = getFormView();

		if (context != null && context.isAuthenticated()) {
			TaskConfig task = (TaskConfig) command;
			task.setStartTimePattern(DEFAULT_DATE_PATTERN);
			log.info("task started? " + task.getStarted());
			context.getSchedulerService().updateTask(task);
			view = getSuccessView();
			
			Object [] args = new Object[] { task.getId() };
			String success = getMessageSourceAccessor().getMessage("Scheduler.taskForm.saved", args);
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);

		}
		
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	  protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		HttpSession httpSession = request.getSession();
		Context context = (Context) httpSession.getAttribute(WebConstants.OPENMRS_CONTEXT_HTTPSESSION_ATTR);
		
		TaskConfig task = new TaskConfig();
		
		if (context != null && context.isAuthenticated()) {
			
			String taskId = request.getParameter("taskId");
	    	if (taskId != null) {
	    		task = context.getSchedulerService().getTask(Integer.valueOf(taskId));	
	    	}
		}

		// Date format pattern for new and existing (currently disabled, but visible)
		if ( task.getStartTimePattern() == null ) 
			task.setStartTimePattern(DEFAULT_DATE_PATTERN);

		return task;
	  }
	  
}