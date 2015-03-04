/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
