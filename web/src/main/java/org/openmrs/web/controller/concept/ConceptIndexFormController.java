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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ConceptIndexFormController extends SimpleFormController {
	
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
			String s = request.getParameter("conceptId");
			String successMessageKey;
			if (s != null && !s.equals("")) {
				try {
					s = s.trim();
					// if they put in something like 1230-2322
					if (s.contains("-")) {
						String[] parts = s.split("-");
						String start = parts[0].trim();
						String end = parts[1].trim();
						Context.getConceptService().updateConceptIndexes(Integer.valueOf(start), Integer.valueOf(end));
					} else {
						// they put in an integer
						Concept c = Context.getConceptService().getConcept(Integer.valueOf(s));
						if (c != null) {
							log.debug("c.conceptId: " + c.getConceptId());
							Context.getConceptService().updateConceptIndex(c);
						}
					}
				}
				catch (ArrayIndexOutOfBoundsException aioobe) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ConceptWord.rangeError");
					return showForm(request, response, errors);
				}
				catch (NumberFormatException nfe) {
					httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ConceptWord.rangeError");
					return showForm(request, response, errors);
				}
				successMessageKey = "ConceptWord.updated";
			} else {
				Context.getConceptService().updateConceptIndexes();
				successMessageKey = "ConceptWord.updateInProgress";
			}
			
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, successMessageKey);
			
		}
		
		return new ModelAndView(new RedirectView(view));
	}
}
