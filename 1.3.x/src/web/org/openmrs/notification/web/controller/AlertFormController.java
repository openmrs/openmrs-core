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
package org.openmrs.notification.web.controller;

import java.text.NumberFormat;
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
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertRecipient;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class AlertFormController extends SimpleFormController {

	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * 
	 * Allows for Integers to be used as values in input tags. Normally, only
	 * strings and lists are expected
	 * 
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#initBinder(javax.servlet.http.HttpServletRequest,
	 *      org.springframework.web.bind.ServletRequestDataBinder)
	 */
	protected void initBinder(HttpServletRequest request,
			ServletRequestDataBinder binder) throws Exception {
		super.initBinder(request, binder);

		Locale locale = Context.getLocale();
		NumberFormat nf = NumberFormat.getInstance(locale);

		// NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
		binder.registerCustomEditor(java.lang.Integer.class,
				new CustomNumberEditor(java.lang.Integer.class, nf, true));
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(
				OpenmrsUtil.getDateFormat(), true, 10));

	}

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView processFormSubmission(HttpServletRequest request,
			HttpServletResponse reponse, Object obj, BindException errors)
			throws Exception {

		Alert alert = (Alert) obj;

		Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		try {
			UserService us = Context.getUserService();

			if (Context.isAuthenticated()) {
				String[] userIdValues = request.getParameter("userIds").split(" ");
				List<Integer> userIds = new Vector<Integer>(); 
				String[] roleValues = request.getParameter("newRoles").split(",");
				List<String> roles = new Vector<String>();
				
				// create user list
				if (userIdValues != null)
					for (String userId : userIdValues) {
						if (!userId.trim().equals(""))
							userIds.add(Integer.valueOf(userId.trim()));
					}
				
				// create role list
				if (roleValues != null)
					for (String role : roleValues) {
						if (!role.trim().equals(""))
							roles.add(role.trim());
					}
				
				
				// remove all recipients not in the userIds list
				List<AlertRecipient> recipientsToRemove = new Vector<AlertRecipient>();
				if (alert.getRecipients() != null) {
					for (AlertRecipient recipient : alert.getRecipients()) {
						Integer userId = recipient.getRecipient().getUserId();
						if (!userIds.contains(userId))
							recipientsToRemove.add(recipient);
					}
				}
				for (AlertRecipient ar : recipientsToRemove)
					alert.removeRecipient(ar);
				
				// add all new users
				if (userIds != null) {
					for (Integer userId : userIds) {
						alert.addRecipient(new User(userId));
					}
				}
				
				// add all new users according to the role(s) selected
				if (roles != null) {
					for (String roleStr : roles) {
						List<User> users = us.getUsersByRole(new Role(roleStr));
						for (User user : users)
							alert.addRecipient(user);
					}
				}
			}
			
			if ((alert.getRecipients() == null || alert.getRecipients().size() == 0)) {
				errors.rejectValue("users", "Alert.recipientRequired");
			}
			
		} 
		catch (Exception e) {
			log.error("Error while processing alert form", e);
			errors.reject(e.getMessage());
		}
		finally {
			Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_USERS);
		}

		return super.processFormSubmission(request, reponse, alert, errors);
	}

	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 * by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object obj, BindException errors)
			throws Exception {

		HttpSession httpSession = request.getSession();
		
		String view = getFormView();

		if (Context.isAuthenticated()) {
			Context.getAlertService().saveAlert((Alert) obj);
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"Alert.saved");
		}

		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time. It tells
	 * Spring the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request)
			throws Exception {

		Alert alert = null;

		if (Context.isAuthenticated()) {
			String a = request.getParameter("alertId");
			if (a != null)
				alert = Context.getAlertService().getAlert(Integer.valueOf(a));
		}

		if (alert == null)
			alert = new Alert();

		return alert;
	}

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object object,
			Errors errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();

		if (Context.isAuthenticated()) {
			map.put("allRoles", Context.getUserService().getAllRoles());
		}

		return map;
	}

}