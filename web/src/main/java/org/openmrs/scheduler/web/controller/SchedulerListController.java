/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.scheduler.web.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.SchedulerException;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class SchedulerListController extends SimpleFormController {
	
	/**
	 * Logger for this class and subclasses
	 */
	protected static final Log log = LogFactory.getLog(SchedulerListController.class);
	
	/**
	 * Service context used to communicate with the services layer. TODO This is not used yet
	 * because we get the context from the session.
	 */
	//private Context context;
	/**
	 * Set the context.
	 * 
	 * @param context
	 */
	//public void setContext(Context context) { 
	//	this.context = context;
	//}
	/**
	 * Allows for Integers to be used as values in input tags. Normally, only strings and lists are
	 * expected
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);
		binder.registerCustomEditor(java.lang.Integer.class, new CustomNumberEditor(java.lang.Integer.class, true));
	}
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		//
		//Locale locale = request.getLocale();
		String view = getFormView();
		StringBuffer success = new StringBuffer();
		StringBuffer error = new StringBuffer();
		String action = request.getParameter("action");
		MessageSourceAccessor msa = getMessageSourceAccessor();
		
		String[] taskList = request.getParameterValues("taskId");
		
		SchedulerService schedulerService = Context.getSchedulerService();
		
		if (taskList != null) {
			
			for (String taskId : taskList) {
				
				// Argument to pass to the success/error message
				Object[] args = new Object[] { taskId };
				
				try {
					
					TaskDefinition task = schedulerService.getTask(Integer.valueOf(taskId));
					
					// If we can get the name, let's use it
					if (task != null) {
						args = new Object[] { task.getName() };
					}
					
					if (action.equals(msa.getMessage("Scheduler.taskList.delete"))) {
						if (!task.getStarted()) {
							schedulerService.deleteTask(Integer.valueOf(taskId));
							success.append(msa.getMessage("Scheduler.taskList.deleted", args));
						} else {
							error.append(msa.getMessage("Scheduler.taskList.deleteNotAllowed", args));
						}
					} else if (action.equals(msa.getMessage("Scheduler.taskList.stop"))) {
						schedulerService.shutdownTask(task);
						success.append(msa.getMessage("Scheduler.taskList.stopped", args));
					} else if (action.equals(msa.getMessage("Scheduler.taskList.start"))) {
						schedulerService.scheduleTask(task);
						success.append(msa.getMessage("Scheduler.taskList.started", args));
					}
				}
				catch (APIException e) {
					log.warn("Error processing schedulerlistcontroller task", e);
					error.append(msa.getMessage("Scheduler.taskList.error", args));
				}
				catch (SchedulerException ex) {
					log.error("Error processing schedulerlistcontroller task", ex);
					error.append(msa.getMessage("Scheduler.taskList.error", args));
				}
			}
		} else {
			error.append(msa.getMessage("Scheduler.taskList.requireTask"));
		}
		
		view = getSuccessView();
		
		if (!"".equals(success.toString())) {
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success.toString());
		}
		if (!"".equals(error.toString())) {
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error.toString());
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		// Get all tasks that are available to be executed
		return Context.getSchedulerService().getRegisteredTasks();
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		Collection<TaskDefinition> tasks = (Collection<TaskDefinition>) command;
		Map<TaskDefinition, String> intervals = new HashMap<TaskDefinition, String>();
		MessageSourceAccessor msa = getMessageSourceAccessor();
		
		for (TaskDefinition task : tasks) {
			
			Long interval = task.getRepeatInterval();
			
			if (interval < 60) {
				intervals.put(task, interval + " " + msa.getMessage("Scheduler.scheduleForm.repeatInterval.units.seconds"));
			} else if (interval < 3600) {
				intervals.put(task, interval / 60 + " "
				        + msa.getMessage("Scheduler.scheduleForm.repeatInterval.units.minutes"));
			} else if (interval < 86400) {
				intervals.put(task, interval / 3600 + " "
				        + msa.getMessage("Scheduler.scheduleForm.repeatInterval.units.hours"));
			} else {
				intervals.put(task, interval / 86400 + " "
				        + msa.getMessage("Scheduler.scheduleForm.repeatInterval.units.days"));
			}
		}
		map.put("intervals", intervals);
		
		return map;
	}
	
}
