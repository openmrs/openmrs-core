/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.visit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for editing visit types.
 *
 * @since 1.9
 */
public class VisitTypeFormController extends SimpleFormController {
	
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
		//NumberFormat nf = NumberFormat.getInstance(new Locale("en_US"));
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
			VisitType visitType = (VisitType) obj;
			VisitService es = Context.getVisitService();
			
			if (request.getParameter("save") != null) {
				es.saveVisitType(visitType);
				view = getSuccessView();
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "VisitType.saved");
			}

			// if the user is retiring out the VisitType
			else if (request.getParameter("retire") != null) {
				String retireReason = request.getParameter("retireReason");
				if (visitType.getVisitTypeId() != null && !(StringUtils.hasText(retireReason))) {
					errors.reject("retireReason", "general.retiredReason.empty");
					return showForm(request, response, errors);
				}
				
				es.retireVisitType(visitType, retireReason);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "VisitType.retiredSuccessfully");
				
				view = getSuccessView();
			}

			// if the user is unretiring the VisitType
			else if (request.getParameter("unretire") != null) {
				es.unretireVisitType(visitType);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "VisitType.unretiredSuccessfully");
				view = getSuccessView();
			}

			// if the user is purging the visitType
			else if (request.getParameter("purge") != null) {
				
				try {
					es.purgeVisitType(visitType);
					httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "VisitType.purgedSuccessfully");
					view = getSuccessView();
				}
				catch (DataIntegrityViolationException e) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.object.inuse.cannot.purge");
					view = "visitType.form?visitTypeId=" + visitType.getVisitTypeId();
				}
				catch (APIException e) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "error.general: " + e.getLocalizedMessage());
					view = "visitType.form?visitTypeId=" + visitType.getVisitTypeId();
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
		
		VisitType visitType = null;
		
		if (Context.isAuthenticated()) {
			VisitService os = Context.getVisitService();
			String visitTypeId = request.getParameter("visitTypeId");
			if (visitTypeId != null) {
				visitType = os.getVisitType(Integer.valueOf(visitTypeId));
			}
		}
		
		if (visitType == null) {
			visitType = new VisitType();
		}
		
		return visitType;
	}
	
}
