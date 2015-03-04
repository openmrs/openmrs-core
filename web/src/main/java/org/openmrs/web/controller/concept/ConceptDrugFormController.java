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
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.propertyeditor.ConceptEditor;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ConceptDrugFormController extends SimpleFormController {
	
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
		binder.registerCustomEditor(java.lang.Double.class, new CustomNumberEditor(java.lang.Double.class, true));
		binder.registerCustomEditor(Concept.class, new ConceptEditor());
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
			Drug drug = (Drug) obj;
			ConceptService conceptService = Context.getConceptService();
			if (request.getParameter("retireDrug") != null) {
				String retireReason = request.getParameter("retireReason");
				if (drug.getId() != null && (retireReason == null || retireReason.length() == 0)) {
					errors.reject("retireReason", "ConceptDrug.retire.reason.empty");
					return showForm(request, response, errors);
				}
				
				conceptService.retireDrug(drug, retireReason);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptDrug.retiredSuccessfully");
			}

			// if this obs is already voided and needs to be unvoided
			else if (request.getParameter("unretireDrug") != null) {
				conceptService.unretireDrug(drug);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptDrug.unretiredSuccessfully");
			} else {
				conceptService.saveDrug(drug);
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptDrug.saved");
			}
			
			view = getSuccessView();
			
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
		
		Drug drug = null;
		
		if (Context.isAuthenticated()) {
			ConceptService cs = Context.getConceptService();
			String id = request.getParameter("drugId");
			if (id != null) {
				drug = cs.getDrug(Integer.valueOf(id));
			}
		}
		
		if (drug == null) {
			drug = new Drug();
		}
		
		return drug;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errs) throws Exception {
		
		Drug drug = (Drug) obj;
		
		Map<String, Object> map = new HashMap<String, Object>();
		String defaultVerbose = "false";
		
		if (Context.isAuthenticated()) {
			if (drug.getConcept() != null) {
				map.put("conceptName", drug.getConcept().getName(request.getLocale()));
			}
			defaultVerbose = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
		}
		
		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		
		String editReason = request.getParameter("editReason");
		if (editReason == null) {
			editReason = "";
		}
		
		map.put("editReason", editReason);
		
		return map;
	}
}
