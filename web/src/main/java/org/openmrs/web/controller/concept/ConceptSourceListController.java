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
package org.openmrs.web.controller.concept;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptSource;
import org.openmrs.ImplementationId;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ConceptSourceListController extends SimpleFormController {
	
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
	 * @should retire concept source
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		if (Context.isAuthenticated()) {
			
			String[] conceptSourceIdList = request.getParameterValues("conceptSourceId");
			ConceptService cs = Context.getConceptService();
			
			String success = "";
			String error = "";
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String deleted = msa.getMessage("general.retired");
			String notDeleted = msa.getMessage("general.cannot.retire");
			String retireReason = request.getParameter("retireReason");
			if (retireReason == null || retireReason.length() == 0) {
				error = getMessageSourceAccessor().getMessage("general.retiredReason.empty");
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
				return showForm(request, response, errors);
			}
			
			if (conceptSourceIdList != null) {
				for (String conceptSourceId : conceptSourceIdList) {
					try {
						ConceptSource source = cs.getConceptSource(Integer.valueOf(conceptSourceId));
						cs.retireConceptSource(source, retireReason);
						if (!success.equals(""))
							success += "<br>";
						success += conceptSourceId + " " + deleted;
					}
					catch (APIException e) {
						log.warn("Error deleting concept source", e);
						if (!error.equals(""))
							error += "<br>";
						error += conceptSourceId + " " + notDeleted;
					}
				}
			}
			
			view = getSuccessView();
			if (!success.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
			if (!error.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
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
		
		//default empty Object
		List<ConceptSource> conceptSourceList = new Vector<ConceptSource>();
		
		//only fill the Object if the user has authenticated properly
		if (Context.isAuthenticated()) {
			ConceptService cs = Context.getConceptService();
			conceptSourceList = cs.getAllConceptSources();
		}
		
		return conceptSourceList;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		List<ConceptSource> conceptSources = (List<ConceptSource>) command;
		Map<String, Object> map = new HashMap<String, Object>();
		
		ImplementationId implId = Context.getAdministrationService().getImplementationId();
		
		// make available the source that corresponds to the implementation id 
		if (implId != null) {
			for (ConceptSource conceptSource : conceptSources) {
				if (conceptSource.getHl7Code().equals(implId.getImplementationId()))
					map.put("implIdSource", conceptSource);
			}
		}
		
		return map;
	}
	
}
