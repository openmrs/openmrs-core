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

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptProposal;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ConceptProposalListController extends SimpleFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * This is called prior to displaying a form for the first time. It tells Spring the
	 * form/command object to load into the request
	 *
	 * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
	 */
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		//default empty Object
		List<ConceptProposal> cpList = new Vector<ConceptProposal>();
		Map<String, List<ConceptProposal>> origText = new HashMap<String, List<ConceptProposal>>();
		
		//only fill the Object is the user has authenticated properly
		if (Context.isAuthenticated()) {
			ConceptService cs = Context.getConceptService();
			log.debug("tmp value: " + request.getParameter("includeCompleted"));
			boolean b = new Boolean(request.getParameter("includeCompleted"));
			log.debug("b value: " + b);
			cpList = cs.getAllConceptProposals(b);
		}
		
		// create map of distinct OriginalText->#occurences
		for (ConceptProposal cp : cpList) {
			List<ConceptProposal> matchingProposals = origText.get(cp.getOriginalText());
			if (matchingProposals == null) {
				matchingProposals = new Vector<ConceptProposal>();
			}
			matchingProposals.add(cp);
			origText.put(cp.getOriginalText(), matchingProposals);
		}
		
		boolean asc = new Boolean("asc".equals(request.getParameter("sortOrder")));
		String sortOn = request.getParameter("sortOn");
		if (sortOn == null) {
			sortOn = "occurences";
		}
		
		TreeMap<List<ConceptProposal>, Integer> cpMap = new TreeMap<List<ConceptProposal>, Integer>();
		
		if (sortOn.equals("occurences")) {
			cpMap = new TreeMap<List<ConceptProposal>, Integer>(new CompareListSize(asc));
		} else {
			//if (sortOn.equals("text"))
			cpMap = new TreeMap<List<ConceptProposal>, Integer>(new CompareListText(asc));
		}
		
		// loop over that map to sort on size or text
		for (List<ConceptProposal> matchingProposals : origText.values()) {
			cpMap.put(matchingProposals, matchingProposals.size());
		}
		
		return cpMap;
	}
	
	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest,
	 *      java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request, Object obj, Errors errors) throws Exception {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("unmapped", OpenmrsConstants.CONCEPT_PROPOSAL_UNMAPPED);
		map.put("states", OpenmrsConstants.CONCEPT_PROPOSAL_STATES());
		
		return map;
	}
	
	private class CompareListSize implements Comparator<List<?>> {
		
		private boolean asc = true;
		
		public CompareListSize(boolean asc) {
			this.asc = asc;
		}
		
		public int compare(List<?> list1, List<?> list2) throws ClassCastException {
			
			int value = list2.size() - list1.size();
			
			// no items are equal
			if (value == 0) {
				value = -1;
			}
			
			if (asc) {
				value = value * -1;
			}
			
			return value;
		}
	}
	
	private class CompareListText implements Comparator<List<ConceptProposal>> {
		
		private boolean asc = true;
		
		public CompareListText(boolean asc) {
			this.asc = asc;
		}
		
		public int compare(List<ConceptProposal> list1, List<ConceptProposal> list2) throws ClassCastException {
			
			ConceptProposal cp1 = list1.get(0);
			ConceptProposal cp2 = list2.get(0);
			
			int value = cp2.getOriginalText().compareToIgnoreCase(cp1.getOriginalText());
			
			// no items are equal
			if (value == 0) {
				value = -1;
			}
			
			if (asc) {
				value = value * -1;
			}
			
			return value;
		}
	}
	
}
