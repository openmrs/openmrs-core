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
package org.openmrs.web.controller;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.web.controller.ConceptFormController.ConceptFormBackingObject;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * The web validator for the concept editing form
 */
public class ConceptFormValidator implements Validator {
	
	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return c.equals(ConceptFormBackingObject.class);
	}
	
	/**
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		ConceptFormBackingObject backingObject = (ConceptFormBackingObject) obj;
		if (backingObject.getConcept() == null) {
			errors.rejectValue("concept", "error.general");
		} else {
			// TODO add more validation here
			
			// validate that each mapping's concept source text is not empty
			for (int x = 0; x < backingObject.getMappings().size(); x++) {
				ConceptMap map = backingObject.getMappings().get(x);
				// skip over null ones...those are deleted mappings
				if (map.getSourceCode() != null && map.getSourceCode().length() == 0) {
					errors.rejectValue("mappings[" + x + "].sourceCode", "Concept.mappings.sourceCodeRequired");
				}
			}
			
			boolean foundAtLeastOnePreferredName = false;
			
			for (Locale locale : backingObject.getLocales()) {
				// validate that a void reason was given for voided synonyms
				for (int x = 0; x < backingObject.getSynonymsByLocale().get(locale).size(); x++) {
					ConceptName synonym = backingObject.getSynonymsByLocale().get(locale).get(x);
					if (synonym.isVoided() && !StringUtils.hasLength(synonym.getVoidReason())) {
						errors.rejectValue("synonymsByLocale[" + locale + "][" + x + "].voidReason",
						    "Concept.synonyms.voidReasonRequired");
					}
					
					// validate that synonym names are non-empty (null name means it was invalid and then removed)
					if (synonym.getName() != null && synonym.getName().length() == 0) {
						errors.rejectValue("synonymsByLocale[" + locale + "][" + x + "].name",
						    "Concept.synonyms.textRequired");
					}
				}
				
				// validate that at least one name in a locale is non-empty
				if (StringUtils.hasLength(backingObject.getNamesByLocale().get(locale).getName())) {
					foundAtLeastOnePreferredName = true;
				}
				
			}
			
			if (foundAtLeastOnePreferredName == false) {
				errors.rejectValue("namesByLocale[" + backingObject.getLocales().get(0) + "].name",
				    "Concept.name.atLeastOneRequired");
			}
			
		}
	}
	
}
