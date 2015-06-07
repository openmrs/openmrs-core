/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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

public class GenericReasonHandler extends AbstractFieldGenHandler implements FieldGenHandler {
	
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
			if (optionHeader == null) {
				optionHeader = "";
			}
			
			String onChange = "";
			if (this.fieldGenTag.getParameterMap() != null) {
				onChange = (String) this.fieldGenTag.getParameterMap().get("onChange");
			}
			if (onChange == null) {
				onChange = "";
			}
			
			// get global reason for stopping drug property and concept
			String reasonProperty = "";
			if (this.fieldGenTag.getParameterMap() != null) {
				reasonProperty = (String) this.fieldGenTag.getParameterMap().get("globalProp");
			}
			if (reasonProperty == null) {
				reasonProperty = "";
			}
			String reasonConceptId = Context.getAdministrationService().getGlobalProperty(reasonProperty);
			Concept reasonConcept = Context.getConceptService().getConcept(reasonConceptId);
			if (reasonConcept == null) {
				log.debug("Could not get even the default reason concept from global properties");
			}
			
			// get override if it's there - not sure if there's a good reason for this anymore
			if (this.fieldGenTag.getParameterMap() != null) {
				String reasonSetOverride = (String) this.fieldGenTag.getParameterMap().get("reasonSet");
				if (reasonSetOverride != null) {
					reasonConcept = Context.getConceptService().getConcept(reasonSetOverride);
				}
			}
			
			List<Concept> possibleReasons = null;
			
			if (reasonConcept != null) {
				Collection<ConceptAnswer> answers = reasonConcept.getAnswers(false);
				for (ConceptAnswer answer : answers) {
					if (possibleReasons == null) {
						possibleReasons = new ArrayList<Concept>();
					}
					possibleReasons.add(answer.getAnswerConcept());
				}
			} else {
				log
				        .debug("No reason concept found, either as global property or override.  Cannot generate list of possible reasons.");
			}
			
			Map<String, String> reasons = new HashMap<String, String>();
			
			if (possibleReasons != null) {
				for (Concept reason : possibleReasons) {
					reasons.put(reason.getConceptId().toString(), reason.getName(Context.getLocale()).getName());
				}
			} else {
				log.debug("No possible reasons found.");
			}
			
			setParameter("initialValue", initialValue);
			setParameter("optionHeader", optionHeader);
			setParameter("onChange", onChange);
			setParameter("reasons", reasons);
		}
	}
}
