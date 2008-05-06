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
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

public class ConceptAnswersEditor extends PropertyEditorSupport {

	private Log log = LogFactory.getLog(this.getClass());
	
	public ConceptAnswersEditor() {	}
	
	/**
	 * loops over the textbox assigned to this property.  The textbox is assumed to be a
	 * string of conceptIds^drugIds separated by spaces
	 */
	@SuppressWarnings("unchecked")
	public void setAsText(String text) throws IllegalArgumentException {
		
		if (StringUtils.hasText(text)) {
			ConceptService cs = Context.getConceptService();
			String[] conceptIds = text.split(" ");
			List<String> requestConceptIds = new Vector<String>();
			//Set<ConceptAnswer> newAnswers = new HashSet<ConceptAnswer>();
			//set up parameter answer Set for easier add/delete functions
			// and removal of duplicates
			for (String id : conceptIds) {
				id = id.trim();
				if (!id.equals("") && !requestConceptIds.contains(id)) //remove whitespace, blank lines, and duplicates
					requestConceptIds.add(id);
			}
			
			Collection<ConceptAnswer> originalConceptAnswers = (Collection<ConceptAnswer>)getValue();
			Collection<ConceptAnswer> deletedConceptAnswers = new HashSet<ConceptAnswer>();
			
			// loop over original concept answers to find any deleted answers
			for (ConceptAnswer origConceptAnswer : originalConceptAnswers) {
				boolean answerDeleted = true;
				for (String conceptId : requestConceptIds) {
					Integer id = getConceptId(conceptId);
					Integer drugId = getDrugId(conceptId);
					Drug answerDrug = origConceptAnswer.getAnswerDrug();
					if (id.equals(origConceptAnswer.getAnswerConcept().getConceptId())) {
						if (drugId == null && answerDrug == null)
							answerDeleted = false;
						else if ((drugId != null && answerDrug != null) &&
							drugId.equals(origConceptAnswer.getAnswerDrug().getDrugId()))
							answerDeleted = false;
					}
				}
				if (answerDeleted)
					deletedConceptAnswers.add(origConceptAnswer);
			}
			
			// loop over those deleted answers to delete them
			for(ConceptAnswer conceptAnswer : deletedConceptAnswers) {
				originalConceptAnswers.remove(conceptAnswer);
			}
			
			// loop over concept ids in the request to add any that are new
			for (String conceptId : requestConceptIds) {
				Integer id = getConceptId(conceptId);
				Integer drugId = getDrugId(conceptId);
				boolean newAnswerConcept = true;
				for (ConceptAnswer origConceptAnswer : originalConceptAnswers) {
					Drug answerDrug = origConceptAnswer.getAnswerDrug();
					if (id.equals(origConceptAnswer.getAnswerConcept().getConceptId())) {
						if (drugId == null && answerDrug == null)
							newAnswerConcept = false;
						else if ((drugId != null && answerDrug != null) &&
							drugId.equals(origConceptAnswer.getAnswerDrug().getDrugId()))
							newAnswerConcept = false;
					}
				}
				// if the current request answer is new, add it to the originals
				if (newAnswerConcept) {
					Concept answer = cs.getConcept(id);
					Drug drug = null;
					if (drugId != null)
						drug = cs.getDrug(drugId);
					ConceptAnswer ac = new ConceptAnswer(answer, drug);
					originalConceptAnswers.add(ac);
				}
			}
			
			log.debug("originalConceptAnswers.getConceptId(): ");
			for (ConceptAnswer a : originalConceptAnswers)
				log.debug("id: " + a.getAnswerConcept().getConceptId());
			
			log.debug("requestConceptIds: ");
			for (String i : requestConceptIds)
				log.debug("id: " + i);
			
			setValue(originalConceptAnswers);
		}
		else {
			setValue(null);
		}
	}
	
	/**
	 * Parses the string and returns the Integer concept id
	 * Expected string: "123" or "123^34"  ("conceptId^drugId")
	 * @param conceptId
	 * @return
	 */
	private Integer getConceptId(String conceptId) {
		if (conceptId.contains("^")) 
			return Integer.valueOf(conceptId.substring(0, conceptId.indexOf("^")));
		else
			return Integer.valueOf(conceptId);
	}
	
	/**
	 * Parses the string and returns the Integer drug id or null if none
	 * Expected string: "123" or "123^34"  ("conceptId^drugId")
	 * @param conceptId
	 * @return
	 */
	private Integer getDrugId(String conceptId) {
		if (conceptId.contains("^"))
			return Integer.valueOf(conceptId.substring(conceptId.indexOf("^")+1, conceptId.length()));
		
		return null;	
	}

}
