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
package org.openmrs.web.controller.form;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller used to merge duplicate fields
 * <p>
 * This class calls the FormService's mergeDuplicateFields
 */
public class AuditFieldController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	private static final Log log = LogFactory.getLog(AuditFieldController.class);
	
	public AuditFieldController() {
		setCommandName("auditField");
		setCommandClass(java.lang.String.class);
	}
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http
	 *      .HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	                                BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		
		if (Context.isAuthenticated()) {
			view = getSuccessView();
			
			try {
				int i = Context.getFormService().mergeDuplicateFields();
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ARGS, i);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Form.auditSuccess");
			}
			catch (APIException e) {
				log.warn("Error in mergeDuplicateFields", e);
				
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Form.auditError");
			}
		}
		
		return new ModelAndView(new RedirectView(view));
	}
	
}
