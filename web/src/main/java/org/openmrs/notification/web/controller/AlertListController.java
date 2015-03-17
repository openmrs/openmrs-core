/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.web.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertService;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class AlertListController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
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
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		Locale locale = Context.getLocale();
		
		if (Context.isAuthenticated()) {
			AlertService as = Context.getAlertService();
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String msg = "";
			
			// expire the selected alerts
			String[] alertIds = request.getParameterValues("alertId");
			if (alertIds != null) {
				for (String alertIdString : alertIds) {
					Integer alertId = Integer.parseInt(alertIdString);
					Alert a = as.getAlert(alertId);
					a.setDateToExpire(new Date());
					as.saveAlert(a);
				}
				
				msg = msa.getMessage("Alert.expired", new Object[] { alertIds.length }, locale);
			} else {
				msg = msa.getMessage("Alert.select");
			}
			
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, msg);
			return new ModelAndView(new RedirectView(getSuccessView()));
		}
		
		// The user isn't authenticated or their session has expired
		return showForm(request, response, errors);
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		
		//map containing the privilege and true/false whether the privilege is core or not
		List<Alert> alertList = new Vector<Alert>();
		
		//only fill the list if the user has authenticated properly
		if (Context.isAuthenticated()) {
			AlertService as = Context.getAlertService();
			boolean b = new Boolean(request.getParameter("includeExpired"));
			alertList = as.getAllAlerts(b);
		}
		
		return alertList;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object object, Errors errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("today", new Date());
		
		return map;
	}
	
}
