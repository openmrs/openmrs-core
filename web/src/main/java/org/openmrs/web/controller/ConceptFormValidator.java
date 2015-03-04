/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.api.context.Context;
import org.openmrs.web.controller.ConceptFormController.ConceptFormBackingObject;
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
	@SuppressWarnings("rawtypes")
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
		Set<String> localesWithErrors = new HashSet<String>();
		if (backingObject.getConcept() == null) {
			errors.rejectValue("concept", "error.general");
		} else {
			// validate the concept term mappings
			for (int x = 0; x < backingObject.getConceptMappings().size(); x++) {
				ConceptMap map = backingObject.getConceptMappings().get(x);
				//this mapping has been removed or is new with no term selected, so ignore it
				if (map.getConceptReferenceTerm().getConceptReferenceTermId() == null) {
					continue;
				}
				if (map.getConceptMapType() == null) {
					errors.rejectValue("conceptMappings[" + x + "].conceptMapType", "Concept.map.typeRequired");
				}
			}
			
			boolean foundAtLeastOneFullySpecifiedName = false;
			
			for (Locale locale : backingObject.getLocales()) {
				
				for (int x = 0; x < backingObject.getSynonymsByLocale().get(locale).size(); x++) {
					ConceptName synonym = backingObject.getSynonymsByLocale().get(locale).get(x);
					// validate that synonym names are non-empty (null name means it was invalid and then removed)
					if (!synonym.isVoided() && synonym.getName() != null && synonym.getName().length() == 0) {
						errors.rejectValue("synonymsByLocale[" + locale + "][" + x + "].name",
						    "Concept.synonyms.textRequired");
						localesWithErrors.add(locale.getDisplayName());
					}
				}
				
				for (int x = 0; x < backingObject.getIndexTermsByLocale().get(locale).size(); x++) {
					ConceptName indexTerm = backingObject.getIndexTermsByLocale().get(locale).get(x);
					// validate that indexTerm names are non-empty (null name means it was invalid and then removed)
					if (!indexTerm.isVoided() && indexTerm.getName() != null && indexTerm.getName().length() == 0) {
						errors.rejectValue("indexTermsByLocale[" + locale + "][" + x + "].name",
						    "Concept.indexTerms.textRequired");
						localesWithErrors.add(locale.getDisplayName());
					}
				}
				
				// validate that at least one name in a locale is non-empty
				if (StringUtils.isNotEmpty(backingObject.getNamesByLocale().get(locale).getName())) {
					foundAtLeastOneFullySpecifiedName = true;
					
				}// if this is a new name and user has changed it into an empty string, reject it
				else if (backingObject.getNamesByLocale().get(locale).getConceptNameId() != null) {
					errors.rejectValue("namesByLocale[" + locale + "].name", "Concept.fullySpecified.textRequired");
					localesWithErrors.add(locale.getDisplayName());
				}
			}
			
			if (!foundAtLeastOneFullySpecifiedName) {
				errors.reject("Concept.name.atLeastOneRequired");
			}
			
		}
		
		if (errors.hasErrors() && localesWithErrors.size() > 0) {
			StringBuilder sb = new StringBuilder(Context.getMessageSourceService().getMessage("Concept.localesWithErrors"));
			sb.append(StringUtils.join(localesWithErrors, ", "));
			errors.rejectValue("concept", sb.toString());
		}
	}
	
}
