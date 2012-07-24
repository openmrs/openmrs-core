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
package org.openmrs.web.taglib.fieldgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.context.Context;

public class DrugOrderDiscontinuedReasonHandler extends AbstractFieldGenHandler implements FieldGenHandler {
	
	private String defaultUrl = "reason.field";
	
	public void run() {
		setUrl(defaultUrl);
		
		if (fieldGenTag != null) {
			String initialValue = "";
			checkEmptyVal("");
			String optionHeader = "";
			if (this.fieldGenTag.getParameterMap() != null) {
				optionHeader = (String) this.fieldGenTag.getParameterMap().get("optionHeader");
			}
			if (optionHeader == null)
				optionHeader = "";
			
			// get global reason for stopping drug property and concept
			String reasonOrderStopped = Context.getAdministrationService().getGlobalProperty("concept.reasonOrderStopped");
			Concept reasonStopped = Context.getConceptService().getConcept(reasonOrderStopped);
			if (reasonStopped == null)
				log.debug("Could not get even the default reason concept from global properties");
			
			// get override if it's there
			if (this.fieldGenTag.getParameterMap() != null) {
				String reasonSetOverride = (String) this.fieldGenTag.getParameterMap().get("reasonSet");
				if (reasonSetOverride != null)
					reasonStopped = Context.getConceptService().getConcept(reasonSetOverride);
			}
			
			List<Concept> possibleReasons = null;
			
			if (reasonStopped != null) {
				Collection<ConceptAnswer> answers = reasonStopped.getAnswers(false);
				for (ConceptAnswer answer : answers) {
					if (possibleReasons == null)
						possibleReasons = new ArrayList<Concept>();
					possibleReasons.add(answer.getAnswerConcept());
				}
			} else {
				log
				        .debug("No reasonStopped concept found, either as global property or override.  Cannot generate list of possible reasons.");
			}
			
			Map<String, String> reasons = new HashMap<String, String>();
			
			if (possibleReasons != null) {
				for (Concept reason : possibleReasons) {
					reasons.put(reason.getConceptId().toString(), reason.getName(Context.getLocale()).getName());
				}
			} else {
				log.debug("No possible reasons found.");
			}
			
			/*
			reasons.add("DrugOrder.discontinue.reason.toxicity");
			reasons.add("DrugOrder.discontinue.reason.failure");
			reasons.add("DrugOrder.discontinue.reason.pregnancy");
			reasons.add("DrugOrder.discontinue.reason.interactionTB");
			reasons.add("DrugOrder.discontinue.reason.outOfStock");
			reasons.add("DrugOrder.discontinue.reason.patientRefusal");
			reasons.add("DrugOrder.discontinue.reason.other");
			*/

			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
			setParameter("reasons", reasons);
		}
	}
}
