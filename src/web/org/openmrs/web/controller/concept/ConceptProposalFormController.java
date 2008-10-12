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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptWord;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertService;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.dwr.ConceptListItem;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class ConceptProposalFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#processFormSubmission(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj, BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		ConceptProposal cp = (ConceptProposal)obj;
		String action = request.getParameter("action");
		
		Concept c = null;
		if (StringUtils.hasText(request.getParameter("conceptId")))
			c = Context.getConceptService().getConcept(Integer.valueOf(request.getParameter("conceptId")));
		cp.setMappedConcept(c);
	
		MessageSourceAccessor msa = getMessageSourceAccessor();
		if (action.equals(msa.getMessage("general.cancel"))) {
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "general.canceled");
			return new ModelAndView(new RedirectView(getSuccessView()));
		}
		else if (action.equals(msa.getMessage("ConceptProposal.ignore"))) {
			cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT);
		}
		else {
			// Set the state of the concept according to the button pushed
			if (cp.getMappedConcept() == null)
				errors.rejectValue("mappedConcept", "ConceptProposal.mappedConcept.error");
			else {
				if (action.equals(msa.getMessage("ConceptProposal.saveAsConcept")) ) {
					cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT);
				}
				else if (action.equals(msa.getMessage("ConceptProposal.saveAsSynonym"))) {
					if (cp.getMappedConcept() == null)
						errors.rejectValue("mappedConcept", "ConceptProposal.mappedConcept.error");
					if (!StringUtils.hasText(cp.getFinalText()))
						errors.rejectValue("finalText", "error.null");
					cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM);
				}
			}
		}
		
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
		
		Locale locale = Context.getLocale();
		MessageSourceAccessor msa = getMessageSourceAccessor();
		
		if (Context.isAuthenticated()) {
			// this concept proposal
			ConceptProposal cp = (ConceptProposal)obj;
			
			// this proposal's final text
			String finalText = cp.getFinalText();
			
			ConceptService cs = Context.getConceptService();
			AdministrationService as = Context.getAdministrationService(); 
			AlertService alertService = Context.getAlertService();
			
			// find the mapped concept
			Concept c = null;
			if (StringUtils.hasText(request.getParameter("conceptId")))
				c = cs.getConcept(Integer.valueOf(request.getParameter("conceptId")));
			
			// all of the proposals to map
			List<ConceptProposal> allProposals = cs.findMatchingConceptProposals(cp.getOriginalText());
			
			// The users to be alerted of this change
			Set<User> uniqueProposers = new HashSet<User>();
			
			// map the proposals to the concept (creating obs along the way)
			for (ConceptProposal conceptProposal : allProposals) {
				uniqueProposers.add(conceptProposal.getCreator());
				conceptProposal.setFinalText(finalText);
				conceptProposal.setState(cp.getState());
				as.mapConceptProposalToConcept(conceptProposal, c);
			}
			
			String msg = "";
			if (c != null) {
				String mappedName = c.getName(locale).getName();
				String[] args = new String[] {cp.getOriginalText(), mappedName, cp.getComments()};
				msg = msa.getMessage("ConceptProposal.alert.mappedTo", args, locale);
			}
			else {
				String[] args = new String[] {cp.getOriginalText(), cp.getComments()};
				msg = msa.getMessage("ConceptProposal.alert.ignored", args, locale);
			}
			
			try {
				// allow this user to save changes to alerts temporarily
				Context.addProxyPrivilege(OpenmrsConstants.PRIV_MANAGE_ALERTS);
				alertService.saveAlert(new Alert(msg, uniqueProposers));
			}
			finally {
				Context.removeProxyPrivilege(OpenmrsConstants.PRIV_MANAGE_ALERTS);
			}
			
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptProposal.saved");
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

		ConceptProposal cp = null;
		
		if (Context.isAuthenticated()) {
			ConceptService cs = Context.getConceptService();
			String id = request.getParameter("conceptProposalId");
	    	if (id != null)
	    		cp = cs.getConceptProposal(Integer.valueOf(id));	
		}
		
		if (cp == null)
			cp = new ConceptProposal();
    	
        return cp;
    }
    
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object object, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		ConceptProposal cp = (ConceptProposal)object;
		Locale locale = Context.getLocale();
		List<ConceptProposal> matchingProposals = new Vector<ConceptProposal>();
		List<ConceptListItem> possibleConceptsListItems = new Vector<ConceptListItem>();
		ConceptListItem listItem = null;
		
		Concept obsConcept = cp.getObsConcept();
		if (obsConcept != null)
			listItem = new ConceptListItem(obsConcept, obsConcept.getName(locale), locale);
		map.put("obsConcept", listItem);
		
		String defaultVerbose = "false";
		if (Context.isAuthenticated() && cp.getConceptProposalId() != null){
			ConceptService cs = Context.getConceptService();
			// optional user property for default verbose display in concept search
			defaultVerbose = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
			
			// find all concept proposals with the same originalText
			matchingProposals = cs.findMatchingConceptProposals(cp.getOriginalText());
			
			// search on part of the originalText to find possible matching concepts
			String phrase = cp.getOriginalText();
			if (phrase.length() > 3)
				phrase = phrase.substring(0, 3);
			List<ConceptWord> possibleConcepts = cs.findConcepts(phrase, locale, false);
			if (possibleConcepts != null)
				for (ConceptWord word : possibleConcepts)
					possibleConceptsListItems.add(new ConceptListItem(word));
			
			// premtively get the mapped concept name
			if (cp.getMappedConcept() != null)
				map.put("mappedConceptName", cp.getMappedConcept().getName(locale));
		}
		map.put("possibleConcepts", possibleConceptsListItems);
		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		map.put("states", OpenmrsConstants.CONCEPT_PROPOSAL_STATES());
		map.put("matchingProposals", matchingProposals);
		
		return map;
	}
    
}