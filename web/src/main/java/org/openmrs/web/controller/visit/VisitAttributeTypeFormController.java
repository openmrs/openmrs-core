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
package org.openmrs.web.controller.visit;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.VisitAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for editing visit attribute types.
 * 
 * @since 1.9
 */
public class VisitAttributeTypeFormController extends SimpleFormController {
	
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
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		
		if (Context.isAuthenticated()) {
			VisitAttributeType visitAttributeType = (VisitAttributeType) obj;
			VisitService visitService = Context.getVisitService();
			
			if (request.getParameter("save") != null) {
				visitService.saveVisitAttributeType(visitAttributeType);
				view = getSuccessView();
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "VisitAttributeType.saved");
			}

			// if the user is retiring out the VisitAttributeType
			else if (request.getParameter("retire") != null) {
				String retireReason = request.getParameter("retireReason");
				if (visitAttributeType.getVisitAttributeTypeId() != null && !(StringUtils.hasText(retireReason))) {
					errors.reject("retireReason", "general.retiredReason.empty");
					return showForm(request, response, errors);
				}
				
				visitService.retireVisitAttributeType(visitAttributeType, retireReason);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "VisitAttributeType.retiredSuccessfully");
				
				view = getSuccessView();
			}

			// if the user is purging the visitAttributeType
			else if (request.getParameter("purge") != null) {
				
				try {
					visitService.purgeVisitAttributeType(visitAttributeType);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "VisitAttributeType.purgedSuccessfully");
					view = getSuccessView();
				}
				catch (DataIntegrityViolationException e) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.object.inuse.cannot.purge");
					view = "visitAttributeType.form?visitAttributeTypeId=" + visitAttributeType.getVisitAttributeTypeId();
				}
				catch (APIException e) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.general: " + e.getLocalizedMessage());
					view = "visitAttributeType.form?visitAttributeTypeId=" + visitAttributeType.getVisitAttributeTypeId();
				}
			}

			else if (request.getParameter("unretire") != null) {
				try {
					visitService.unretireVisitAttributeType(visitAttributeType);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "VisitAttributeType.unretiredSuccessfully");
					view = getSuccessView();
				}
				catch (APIException e) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.general: " + e.getLocalizedMessage());
					view = "visitAttributeType.form?visitAttributeTypeId=" + visitAttributeType.getVisitAttributeTypeId();
				}
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
		
		VisitAttributeType visitAttributeType = null;
		
		if (Context.isAuthenticated()) {
			VisitService os = Context.getVisitService();
			String visitAttributeTypeId = request.getParameter("visitAttributeTypeId");
			if (visitAttributeTypeId != null)
				visitAttributeType = os.getVisitAttributeType(Integer.valueOf(visitAttributeTypeId));
		}
		
		if (visitAttributeType == null)
			visitAttributeType = new VisitAttributeType();
		
		return visitAttributeType;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("datatypes", Context.getAttributeService().getDatatypes());
		
		return map;
	}
}
