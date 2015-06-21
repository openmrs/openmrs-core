/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.encounter;

import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class LocationListController extends SimpleFormController {
	
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
		
		String view = getFormView();
		if (Context.isAuthenticated()) {
			StringBuilder success = new StringBuilder();
			String error = "";
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			
			String[] locationList = request.getParameterValues("locationId");
			if (locationList != null) {
				LocationService ls = Context.getLocationService();
				
				String deleted = msa.getMessage("general.deleted");
				String notDeleted = msa.getMessage("Location.cannot.delete");
				for (String p : locationList) {
					//TODO convenience method deleteLocation(Integer) ??
					try {
						ls.purgeLocation(ls.getLocation(Integer.valueOf(p)));
						if (!"".equals(success.toString())) {
							success.append("<br/>");
						}
						success.append(p).append(" ").append(deleted);
					}
					catch (DataIntegrityViolationException e) {
						error = handleLocationIntegrityException(e, error, notDeleted);
					}
					catch (APIException e) {
						error = handleLocationIntegrityException(e, error, notDeleted);
					}
				}
			} else {
				error = msa.getMessage("Location.select");
			}
			
			view = getSuccessView();
			if (!"".equals(success.toString())) {
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success.toString());
			}
			if (!"".equals(error)) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
			}
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
	/**
	 * Logs a location delete data integrity violation exception and returns a user friedly message
	 * of the problem that occured.
	 *
	 * @param e the exception.
	 * @param error the error message.
	 * @param notDeleted the not deleted error message.
	 * @return the formatted error message.
	 */
	private String handleLocationIntegrityException(Exception e, String error, String notDeleted) {
		log.warn("Error deleting location", e);
		if (!"".equals(error)) {
			error += "<br/>";
		}
		error += notDeleted;
		return error;
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		//default empty Object
		List<Location> locationList = new Vector<Location>();
		
		//only fill the Object is the user has authenticated properly
		if (Context.isAuthenticated()) {
			LocationService ls = Context.getLocationService();
			locationList = ls.getAllLocations();
		}
		
		return locationList;
	}
}
