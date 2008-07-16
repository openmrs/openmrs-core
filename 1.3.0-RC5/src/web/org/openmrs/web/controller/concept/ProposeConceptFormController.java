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

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptProposal;
import org.openmrs.Encounter;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ProposeConceptFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		ConceptProposal cp = (ConceptProposal)obj;
		
		Concept c = cp.getObsConcept();
		String id = ServletRequestUtils.getStringParameter(request, "conceptId", null);
		if (c == null && id != null) {
			c = Context.getConceptService().getConcept(Integer.valueOf(id));
			cp.setObsConcept(c);
		}
		
		Encounter e = cp.getEncounter();
		id = ServletRequestUtils.getStringParameter(request, "encounterId", null);
		if (e == null && id != null) {
			e = Context.getEncounterService().getEncounter(Integer.valueOf(id));
			cp.setEncounter(e);
		}
		
		if (cp.getOriginalText().equals(""))
			errors.rejectValue("originalText", "error.null");
		
		return super.processFormSubmission(request, response, cp, errors);
	}

	/**
	 * 
	 * The onSubmit function receives the form/command object that was modified
	 *   by the input form and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		
		if (Context.isAuthenticated()) {
			// this concept proposal
			ConceptProposal cp = (ConceptProposal)obj;
			
			// this proposal's final text
			ConceptService cs = Context.getConceptService();
			
			cp.setCreator(Context.getAuthenticatedUser());
			cp.setDateCreated(new Date());
			
			cs.proposeConcept(cp);
			
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptProposal.proposed");
		}
		
		return new ModelAndView(new RedirectView(view));
	}

	/**
	 * 
	 * This is called prior to displaying a form for the first time.  It tells Spring
	 *   the form/command object to load into the request
	 * 
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {

		ConceptProposal cp = new ConceptProposal();
		
		if (Context.isAuthenticated()) {
			ConceptService cs = Context.getConceptService();
			EncounterService es = Context.getEncounterService();
			String id = ServletRequestUtils.getStringParameter(request, "encounterId");
	    	if (id != null)
	    		cp.setEncounter(es.getEncounter(Integer.valueOf(id)));
	    	
	    	id = ServletRequestUtils.getStringParameter(request, "obsConceptId");
	    	if (id != null)
	    		cp.setObsConcept(cs.getConcept(Integer.valueOf(id)));
	    	
		}
		
        return cp;
    }
    
	protected Map referenceData(HttpServletRequest request, Object object, Errors errors) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		
		ConceptProposal cp = (ConceptProposal)object;
		Locale locale = Context.getLocale();
		
		String defaultVerbose = "false";
		if (Context.isAuthenticated()){
			// optional user property for default verbose display in concept search
			defaultVerbose = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
			
			// preemptively get the obs concept name
			if (cp.getObsConcept() != null)
				map.put("conceptName", cp.getObsConcept().getName(locale));
		}
		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		
		return map;
	}
    
}