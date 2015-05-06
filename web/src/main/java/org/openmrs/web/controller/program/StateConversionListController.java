/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.program;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptStateConversion;
import org.openmrs.api.APIException;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class StateConversionListController extends SimpleFormController {
	
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
			String[] conversionIdList = request.getParameterValues("conceptStateConversionId");
			ProgramWorkflowService pws = Context.getProgramWorkflowService();
			
			StringBuilder success = new StringBuilder("");
			StringBuilder error = new StringBuilder("");
			int numDeleted = 0;
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			String deleted = msa.getMessage("general.deleted");
			String notDeleted = msa.getMessage("general.cannot.delete");
			String textConversion = msa.getMessage("Program.conversion");
			String noneDeleted = msa.getMessage("Program.conversion.nonedeleted");
			if (conversionIdList != null) {
				for (String id : conversionIdList) {
					try {
						pws.purgeConceptStateConversion(pws.getConceptStateConversion(Integer.valueOf(id)));
						if (!"".equals(success.toString())) {
							success.append("<br/>");
						}
						success.append(textConversion).append(" ").append(id).append(" ").append(deleted);
						numDeleted++;
					}
					catch (APIException e) {
						log.warn("Error deleting concept state conversion", e);
						if (!"".equals(error.toString())) {
							error.append("<br/>");
						}
						error.append(textConversion).append(" ").append(id).append(" ").append(notDeleted);
					}
				}
				
				if (numDeleted > 3) {
					success = new StringBuilder(numDeleted).append(" ").append(deleted);
				}
			} else {
				success.append(noneDeleted);
			}
			view = getSuccessView();
			if (!"".equals(success.toString())) {
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success.toString());
			}
			if (!"".equals(error.toString())) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error.toString());
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
		
		//default empty Object
		List<ConceptStateConversion> conversionList = new ArrayList<ConceptStateConversion>();
		
		//only fill the Object if the user has authenticated properly
		if (Context.isAuthenticated()) {
			ProgramWorkflowService ps = Context.getProgramWorkflowService();
			conversionList = ps.getAllConceptStateConversions();
		}
		
		return conversionList;
	}
}
