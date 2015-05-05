/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.concept;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptSource;
import org.openmrs.ImplementationId;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ConceptSourceFormController extends SimpleFormController {
	
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
			
			if (request.getParameter("retire") != null) {
				String retireReason = request.getParameter("retireReason");
				ConceptSource conceptSource = (ConceptSource) obj;
				if (!StringUtils.hasText(retireReason)) {
					errors.reject("retireReason", "general.retiredReason.empty");
					return showForm(request, response, errors);
				}
				
				conceptSource.setRetireReason(retireReason);
				conceptSource.setRetired(true);
				
				Context.getConceptService().saveConceptSource(conceptSource);
				view = getSuccessView();
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptSource.retired");
			} else if (request.getParameter("restore") != null) {
				ConceptSource conceptSource = (ConceptSource) obj;
				conceptSource.setRetireReason(null);
				conceptSource.setRetired(false);
				
				Context.getConceptService().saveConceptSource(conceptSource);
				view = getSuccessView();
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptSource.restored");
			} else if (request.getParameter("purge") != null) {
				ConceptSource conceptSource = (ConceptSource) obj;
				try {
					Context.getConceptService().purgeConceptSource(conceptSource);
					view = getSuccessView();
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptSource.purged");
				}
				catch (DataIntegrityViolationException e) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.object.inuse.cannot.purge");
					return showForm(request, response, errors);
				}
			} else {
				ConceptSource conceptSource = (ConceptSource) obj;
				Context.getConceptService().saveConceptSource(conceptSource);
				view = getSuccessView();
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptSource.saved");
			}
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
		
		ConceptSource conceptSource = null;
		
		if (Context.isAuthenticated()) {
			ConceptService cs = Context.getConceptService();
			String conceptSourceId = request.getParameter("conceptSourceId");
			if (conceptSourceId != null) {
				conceptSource = cs.getConceptSource(Integer.valueOf(conceptSourceId));
			}
		}
		
		if (conceptSource == null) {
			conceptSource = new ConceptSource();
		}
		
		return conceptSource;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		ConceptSource conceptSource = (ConceptSource) command;
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		ImplementationId implId = Context.getAdministrationService().getImplementationId();
		
		if (implId != null && implId.getImplementationId().equals(conceptSource.getHl7Code())) {
			map.put("isImplementationId", true);
		}
		
		return map;
	}
	
}
