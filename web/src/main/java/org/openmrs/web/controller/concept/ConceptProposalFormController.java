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

import java.util.Collection;
import java.util.Date;
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
import org.openmrs.ConceptName;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSearchResult;
import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertService;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.PrivilegeConstants;
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
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#processFormSubmission(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 */
	@Override
	protected ModelAndView processFormSubmission(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		ConceptProposal cp = (ConceptProposal) obj;
		String action = request.getParameter("action");
		
		Concept c = null;
		if (StringUtils.hasText(request.getParameter("conceptId"))) {
			c = Context.getConceptService().getConcept(Integer.valueOf(request.getParameter("conceptId")));
		}
		cp.setMappedConcept(c);
		
		MessageSourceAccessor msa = getMessageSourceAccessor();
		if (action.equals(msa.getMessage("general.cancel"))) {
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "general.canceled");
			return new ModelAndView(new RedirectView(getSuccessView()));
		} else if (action.equals(msa.getMessage("ConceptProposal.ignore"))) {
			cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT);
		} else {
			// Set the state of the concept according to the button pushed
			if (cp.getMappedConcept() == null) {
				errors.rejectValue("mappedConcept", "ConceptProposal.mappedConcept.error");
			} else {
				String proposalAction = request.getParameter("actionToTake");
				if (proposalAction.equals("saveAsMapped")) {
					cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_CONCEPT);
				} else if (proposalAction.equals("saveAsSynonym")) {
					if (cp.getMappedConcept() == null) {
						errors.rejectValue("mappedConcept", "ConceptProposal.mappedConcept.error");
					}
					if (!StringUtils.hasText(cp.getFinalText())) {
						errors.rejectValue("finalText", "error.null");
					}
					cp.setState(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM);
				}
			}
		}
		
		return super.processFormSubmission(request, response, cp, errors);
	}
	
	/**
	 * The onSubmit function receives the form/command object that was modified by the input form
	 * and saves it to the db
	 * 
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.Object,
	 *      org.springframework.validation.BindException)
	 * @should create a single unique synonym and obs for all similar proposals
	 * @should work properly for country locales
	 */
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
	        BindException errors) throws Exception {
		
		HttpSession httpSession = request.getSession();
		
		String view = getFormView();
		
		Locale locale = Context.getLocale();
		MessageSourceAccessor msa = getMessageSourceAccessor();
		
		if (Context.isAuthenticated()) {
			// this concept proposal
			ConceptProposal cp = (ConceptProposal) obj;
			
			// this proposal's final text
			String finalText = cp.getFinalText();
			
			ConceptService cs = Context.getConceptService();
			AlertService alertService = Context.getAlertService();
			
			// find the mapped concept
			Concept c = null;
			if (StringUtils.hasText(request.getParameter("conceptId"))) {
				c = cs.getConcept(Integer.valueOf(request.getParameter("conceptId")));
			}
			Collection<ConceptName> oldNames = null;
			if (c != null) {
				oldNames = c.getNames();
			}
			// The users to be alerted of this change
			Set<User> uniqueProposers = new HashSet<User>();
			Locale conceptNameLocale = LocaleUtility.fromSpecification(request.getParameter("conceptNamelocale"));
			// map the proposal to the concept (creating obs along the way)
			uniqueProposers.add(cp.getCreator());
			cp.setFinalText(finalText);
			try {
				cs.mapConceptProposalToConcept(cp, c, conceptNameLocale);
			}
			catch (DuplicateConceptNameException e) {
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "ConceptProposal.save.fail");
				return new ModelAndView(new RedirectView(getSuccessView()));
			}
			
			ConceptName newConceptName = null;
			if (c != null) {
				Collection<ConceptName> newNames = c.getNames();
				newNames.removeAll(oldNames);
				if (newNames.size() == 1) {
					newConceptName = newNames.iterator().next();
				}
			}
			
			// all of the proposals to map with similar text
			List<ConceptProposal> allProposals = cs.getConceptProposals(cp.getOriginalText());
			//exclude the proposal submitted with the form since it is already handled above
			if (allProposals.contains(cp)) {
				allProposals.remove(cp);
			}
			
			//Just mark the rest of the proposals as mapped to avoid duplicate synonyms and obs
			for (ConceptProposal conceptProposal : allProposals) {
				if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT)) {
					conceptProposal.rejectConceptProposal();
					conceptProposal.setComments(cp.getComments());
				} else {
					//the question concept differs, this needs to me handled separately from the form
					if (conceptProposal.getObsConcept() != null
					        && !conceptProposal.getObsConcept().equals(cp.getObsConcept())) {
						continue;
					}
					
					uniqueProposers.add(conceptProposal.getCreator());
					conceptProposal.setFinalText(cp.getFinalText());
					conceptProposal.setState(cp.getState());
					conceptProposal.setMappedConcept(c);
					conceptProposal.setComments(cp.getComments());
					if (conceptProposal.getObsConcept() != null) {
						Obs ob = new Obs();
						ob.setEncounter(conceptProposal.getEncounter());
						ob.setConcept(conceptProposal.getObsConcept());
						ob.setValueCoded(conceptProposal.getMappedConcept());
						if (conceptProposal.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM)
						        && newConceptName != null) {
							ob.setValueCodedName(newConceptName);
						}
						ob.setCreator(Context.getAuthenticatedUser());
						ob.setDateCreated(new Date());
						ob.setObsDatetime(conceptProposal.getEncounter().getEncounterDatetime());
						ob.setLocation(conceptProposal.getEncounter().getLocation());
						ob.setPerson(conceptProposal.getEncounter().getPatient());
						cp.setObs(ob);
					}
				}
				
				cs.saveConceptProposal(conceptProposal);
			}
			
			String msg = "";
			if (c != null) {
				String mappedName = c.getName(locale).getName();
				String[] args = null;
				if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_SYNONYM)) {
					args = new String[] { cp.getFinalText(), mappedName, cp.getComments() };
					msg = msa.getMessage("ConceptProposal.alert.synonymAdded", args, locale);
				} else {
					args = new String[] { cp.getOriginalText(), mappedName, cp.getComments() };
					msg = msa.getMessage("ConceptProposal.alert.mappedTo", args, locale);
				}
			} else if (cp.getState().equals(OpenmrsConstants.CONCEPT_PROPOSAL_REJECT)) {
				msg = msa.getMessage("ConceptProposal.alert.ignored",
				    new String[] { cp.getOriginalText(), cp.getComments() }, locale);
			}
			
			try {
				// allow this user to save changes to alerts temporarily
				Context.addProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
				alertService.saveAlert(new Alert(msg, uniqueProposers));
			}
			finally {
				Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_ALERTS);
			}
			
			view = getSuccessView();
			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "ConceptProposal.saved");
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
		
		ConceptProposal cp = null;
		
		if (Context.isAuthenticated()) {
			ConceptService cs = Context.getConceptService();
			String id = request.getParameter("conceptProposalId");
			if (id != null) {
				cp = cs.getConceptProposal(Integer.valueOf(id));
			}
		}
		
		if (cp == null) {
			cp = new ConceptProposal();
		}
		
		return cp;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	protected Map<String, Object> referenceData(HttpServletRequest request, Object object, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		ConceptProposal cp = (ConceptProposal) object;
		Locale locale = Context.getLocale();
		List<ConceptProposal> matchingProposals = new Vector<ConceptProposal>();
		List<ConceptListItem> possibleConceptsListItems = new Vector<ConceptListItem>();
		ConceptListItem listItem = null;
		
		Concept obsConcept = cp.getObsConcept();
		if (obsConcept != null) {
			listItem = new ConceptListItem(obsConcept, obsConcept.getName(locale), locale);
		}
		map.put("obsConcept", listItem);
		
		String defaultVerbose = "false";
		if (Context.isAuthenticated() && cp.getConceptProposalId() != null) {
			ConceptService cs = Context.getConceptService();
			// optional user property for default verbose display in concept search
			defaultVerbose = Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_SHOW_VERBOSE);
			
			// find all concept proposals with the same originalText
			matchingProposals = cs.getConceptProposals(cp.getOriginalText());
			
			// search on part of the originalText to find possible matching concepts
			String phrase = cp.getOriginalText();
			if (phrase.length() > 3) {
				phrase = phrase.substring(0, 3);
			}
			List<ConceptSearchResult> possibleConcepts = cs.getConcepts(phrase, locale, false);
			if (possibleConcepts != null) {
				for (ConceptSearchResult searchResult : possibleConcepts) {
					possibleConceptsListItems.add(new ConceptListItem(searchResult));
				}
			}
			
			// premtively get the mapped concept name
			if (cp.getMappedConcept() != null) {
				map.put("mappedConceptName", cp.getMappedConcept().getName(locale));
			}
		}
		map.put("possibleConcepts", possibleConceptsListItems);
		map.put("defaultVerbose", defaultVerbose.equals("true") ? true : false);
		map.put("states", OpenmrsConstants.CONCEPT_PROPOSAL_STATES());
		map.put("matchingProposals", matchingProposals);
		map.put("locales", Context.getAdministrationService().getAllowedLocales());
		
		return map;
	}
	
}
