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
package org.openmrs.web.controller.maintenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 *
 */
public class GlobalPropertyController extends SimpleFormController {
	
	public static final String PROP_NAME = "property";
	
	public static final String PROP_VAL_NAME = "value";
	
	public static final String PROP_DESC_NAME = "description";
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@SuppressWarnings("unchecked")
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	                                BindException errors) throws Exception {
		
		String action = request.getParameter("action");
		if (action == null)
			action = "cancel";
		
		if (action.equals(getMessageSourceAccessor().getMessage("general.save"))) {
			HttpSession httpSession = request.getSession();
			
			if (Context.isAuthenticated()) {
				AdministrationService as = Context.getAdministrationService();
				
				// fetch the backing object
				// and save it to a hashmap for easy retrieval of already-used-GPs
				List<GlobalProperty> formBackingObject = (List<GlobalProperty>) obj;
				Map<String, GlobalProperty> formBackingObjectMap = new HashMap<String, GlobalProperty>();
				for (GlobalProperty prop : formBackingObject) {
					formBackingObjectMap.put(prop.getProperty(), prop);
				}
				
				// the list we'll save to the database
				List<GlobalProperty> globalPropList = new ArrayList<GlobalProperty>();
				
				String[] keys = request.getParameterValues(PROP_NAME);
				String[] values = request.getParameterValues(PROP_VAL_NAME);
				String[] descriptions = request.getParameterValues(PROP_DESC_NAME);
				
				for (int x = 0; x < keys.length; x++) {
					String key = keys[x];
					String val = values[x];
					String desc = descriptions[x];
					
					// try to get an already-used global property for this key
					GlobalProperty tmpGlobalProperty = formBackingObjectMap.get(key);
					
					// if it exists, use that object...just update it
					if (tmpGlobalProperty != null) {
						tmpGlobalProperty.setPropertyValue(val);
						tmpGlobalProperty.setDescription(desc);
						globalPropList.add(tmpGlobalProperty);
					} else {
						// if it doesn't exist, create a new global property
						globalPropList.add(new GlobalProperty(key, val, desc));
					}
				}
				
				try {
					as.saveGlobalProperties(globalPropList);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "GlobalProperty.saved");
					
					// refresh log level from global property(ies)
					OpenmrsUtil.applyLogLevels();
				}
				catch (Exception e) {
					log.error("Error saving properties", e);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "GlobalProperty.not.saved");
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, e.getMessage());
				}
				
				return new ModelAndView(new RedirectView(getSuccessView()));
				
			}
		}
		
		return showForm(request, response, errors);
		
	}
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		if (Context.isAuthenticated()) {
			// return a non-empty list if the user has authenticated properly
			AdministrationService as = Context.getAdministrationService();
			return as.getAllGlobalProperties();
		} else
			return new ArrayList<GlobalProperty>();
	}
	
}
