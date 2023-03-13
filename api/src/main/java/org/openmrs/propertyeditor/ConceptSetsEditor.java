/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Turns a list of concept ids "123 1234 1235" into a List of ConceptSets
 */
public class ConceptSetsEditor extends PropertyEditorSupport {
	
	private static final Logger log = LoggerFactory.getLogger(ConceptSetsEditor.class);
	
	private Collection<ConceptSet> originalConceptSets;
	
	/**
	 * Default constructor taking in the current sets on a concept
	 * 
	 * @param conceptSets the current object on the concept
	 */
	public ConceptSetsEditor(Collection<ConceptSet> conceptSets) {
		if (conceptSets == null) {
			originalConceptSets = new ArrayList<>();
		}
		
		this.originalConceptSets = conceptSets;
	}
	
	/**
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		log.debug("setting conceptSets with text: " + text);
		
		if (StringUtils.hasText(text)) {
			ConceptService cs = Context.getConceptService();
			String[] conceptIds = text.split(" ");
			List<Integer> requestConceptIds = new ArrayList<>();
			//set up parameter Set for easier add/delete functions
			// and removal of duplicates
			for (String id : conceptIds) {
				id = id.trim();
				if (!("".equals(id)) && !requestConceptIds.contains(Integer.valueOf(id))) { //remove whitespace, blank lines, and duplicate entries
					requestConceptIds.add(Integer.valueOf(id));
				}
			}
			
			// used when adding in concept sets
			List<Integer> originalConceptSetIds = new ArrayList<>(originalConceptSets.size());
			
			// remove all sets that aren't in the request (aka, that have been deleted by the user)
			Collection<ConceptSet> copyOfOriginalConceptSets = new ArrayList<>(originalConceptSets);
			for (ConceptSet origConceptSet : copyOfOriginalConceptSets) {
				if (!requestConceptIds.contains(origConceptSet.getConcept().getConceptId())) {
					originalConceptSets.remove(origConceptSet);
				}
				
				// add to quick list used when adding later
				originalConceptSetIds.add(origConceptSet.getConcept().getConceptId());
			}
			
			// insert all sets that are new (aka, that have been added by the user).
			// Also normalize all weight attributes
			for (int x = 0; x < requestConceptIds.size(); x++) {
				Integer requestConceptId = requestConceptIds.get(x);
				
				// if this isn't in the originalList, add it
				
				if (!originalConceptSetIds.contains(requestConceptId)) {
					// the null weight will be reset in the next step of normalization
					originalConceptSets.add(new ConceptSet(cs.getConcept(requestConceptId), (double) x));
				} else {
					// find this conceptId in the original set and set its weight
					for (ConceptSet conceptSet : originalConceptSets) {
						if (conceptSet.getConcept().getConceptId().equals(requestConceptId)) {
							conceptSet.setSortWeight((double) x);
						}
					}
				}
			}
			
		} else {
			originalConceptSets.clear();
		}
		
		setValue(originalConceptSets);
	}
	
}
