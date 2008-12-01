/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.scheduler.web.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
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
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
		
		TaskDefinition task = (TaskDefinition) command;
		
		// assign the properties to the task
		String[] names = request.getParameterValues("propertyName");
		String[] values = request.getParameterValues("propertyValue");
		
		Map<String, String> properties = new HashMap<String, String>();
		
		if (names != null)
			for (int x = 0; x < names.length; x++) {
				if (names[x].length() > 0)
					properties.put(names[x], values[x]);
			}
		
		task.setProperties(properties);
		
		
		// if the user selected a different repeat interval unit, fix repeatInterval
		String units = request.getParameter("repeatIntervalUnits");
		Long interval = task.getRepeatInterval();
		
		if ("minutes".equals(units)) {
			interval = interval * 60;
		}
		else if ("hours".equals(units)) {
			interval = interval * 60 * 60;
		}
		else if ("days".equals(units)) {
			interval = interval * 60 * 60 * 24;
		}
		
		task.setRepeatInterval(interval);
		
		
		return super.processFormSubmission(request, response, task, errors);
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
		
		String view = getFormView();

		TaskDefinition task = (TaskDefinition) command;
		task.setStartTimePattern(DEFAULT_DATE_PATTERN);
		log.info("task started? " + task.getStarted());
		
		Context.getSchedulerService().saveTask(task);

		view = getSuccessView();
		
		Object [] args = new Object[] { task.getName() };
		String success = getMessageSourceAccessor().getMessage("Scheduler.taskForm.saved", args);
		httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
	
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

		  TaskDefinition task = new TaskDefinition();
		
		String taskId = request.getParameter("taskId");
    	if (taskId != null) {
    		task = Context.getSchedulerService().getTask(Integer.valueOf(taskId));	
    	}
	
		// Date format pattern for new and existing (currently disabled, but visible)
		if ( task.getStartTimePattern() == null ) 
			task.setStartTimePattern(DEFAULT_DATE_PATTERN);

		return task;
	  }

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	protected Map<String, String> referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		
		Map<String, String> map = new HashMap<String, String>();
		
		TaskDefinition task = (TaskDefinition)command;
		
		Long interval = task.getRepeatInterval();
		
		if (interval == null || interval < 60)
			map.put("units", "seconds");
		else if (interval < 3600) {
			map.put("units", "minutes");
			task.setRepeatInterval(interval / 60);
		}
		else if (interval < 86400) {
			map.put("units", "hours");
			task.setRepeatInterval(interval / 3600);
		}
		else {
			map.put("units", "days");
			task.setRepeatInterval(interval / 86400);
		}
		
		return map;
	}

	  

	  
}