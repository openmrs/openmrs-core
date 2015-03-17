/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.maintenance;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ImplementationId;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.validator.ImplementationIdValidator;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This controller controls all uploading and syncing the Implementation Id with the implementation
 * id server
 */
public class ImplementationIdFormController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Actions taken when the form is submitted
	 *
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView onSubmit(HttpServletRequest req, HttpServletResponse response, Object object,
	        BindException exceptions) throws Exception {
		
		ImplementationId implId = (ImplementationId) object;
		
		new ImplementationIdValidator().validate(implId, exceptions);
		
		if (exceptions.hasErrors()) {
			return showForm(req, response, exceptions);
		}
		
		try {
			Context.getAdministrationService().setImplementationId(implId);
			req.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ImplementationId.validatedId");
		}
		catch (APIException e) {
			log.warn("Unable to set implementation id", e);
			exceptions.reject(e.getMessage());
			req.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
			return showForm(req, response, exceptions);
		}
		
		return new ModelAndView(new RedirectView(getSuccessView()));
	}
	
	/**
	 * The object that backs the form. The class of this object (String) is set in the servlet
	 * descriptor file
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		
		// get the impl id from the database that is the implementation id
		ImplementationId implId = Context.getAdministrationService().getImplementationId();
		
		if (implId != null) {
			return implId;
		} else {
			return new ImplementationId();
		}
	}
	
}
