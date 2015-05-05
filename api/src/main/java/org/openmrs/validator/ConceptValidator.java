/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.context.Context;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates {@link Concept} objects. <br>
 * These validations are also documented at <a
 * href="https://wiki.openmrs.org/x/-gkdAg">https://wiki.openmrs.org/x/-gkdAg</a>. Any changes made
 * to this source also need to be reflected on that page.
 */
@Handler(supports = { Concept.class }, order = 50)
public class ConceptValidator implements Validator {
	
	// Log for this class
	private static final Log log = LogFactory.getLog(ConceptValidator.class);
	
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public boolean supports(Class c) {
		return Concept.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks that a given concept object is valid.
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should pass if the concept has atleast one fully specified name added to it
	 * @should fail if there is a duplicate unretired concept name in the locale
	 * @should fail if there is a duplicate unretired preferred name in the same locale
	 * @should fail if there is a duplicate unretired fully specified name in the same locale
	 * @should fail if any names in the same locale for this concept are similar
	 * @should pass if the concept with a duplicate name is retired
	 * @should pass if the concept being validated is retired and has a duplicate name
	 * @should fail if any name is an empty string
	 * @should fail if the object parameter is null
	 * @should pass if the concept is being updated with no name change
	 * @should fail if any name is a null value
	 * @should not allow multiple preferred names in a given locale
	 * @should not allow multiple fully specified conceptNames in a given locale
	 * @should not allow multiple short names in a given locale
	 * @should not allow an index term to be a locale preferred name
	 * @should fail if there is no name explicitly marked as fully specified
	 * @should pass if the duplicate ConceptName is neither preferred nor fully Specified
	 * @should pass if the concept has a synonym that is also a short name
	 * @should fail if a term is mapped multiple times to the same concept
	 * @should not fail if a term has two new mappings on it
	 * @should fail if there is a duplicate unretired concept name in the same locale different than
	 *         the system locale
	 * @should pass for a new concept with a map created with deprecated concept map methods
	 * @should pass for an edited concept with a map created with deprecated concept map methods
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 * @should pass if fully specified name is the same as short name
	 * @should pass if different concepts have the same short name
	 */
	public void validate(Object obj, Errors errors) throws APIException, DuplicateConceptNameException {
		
		if (obj == null || !(obj instanceof Concept)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type" + Concept.class);
		}
		
		Concept conceptToValidate = (Concept) obj;
		//no name to validate, but why is this the case?
		if (conceptToValidate.getNames().size() == 0) {
			errors.reject("Concept.name.atLeastOneRequired");
			return;
		}
		
		boolean hasFullySpecifiedName = false;
		for (Locale conceptNameLocale : conceptToValidate.getAllConceptNameLocales()) {
			boolean fullySpecifiedNameForLocaleFound = false;
			boolean preferredNameForLocaleFound = false;
			boolean shortNameForLocaleFound = false;
			Set<String> validNamesFoundInLocale = new HashSet<String>();
			Collection<ConceptName> namesInLocale = conceptToValidate.getNames(conceptNameLocale);
			for (ConceptName nameInLocale : namesInLocale) {
				if (StringUtils.isBlank(nameInLocale.getName())) {
					log.debug("Name in locale '" + conceptNameLocale.toString()
					        + "' cannot be an empty string or white space");
					errors.reject("Concept.name.empty");
				}
				if (nameInLocale.isLocalePreferred() != null) {
					if (nameInLocale.isLocalePreferred() && !preferredNameForLocaleFound) {
						if (nameInLocale.isIndexTerm()) {
							log.warn("Preferred name in locale '" + conceptNameLocale.toString()
							        + "' shouldn't be an index term");
							errors.reject("Concept.error.preferredName.is.indexTerm");
						} else if (nameInLocale.isShort()) {
							log.warn("Preferred name in locale '" + conceptNameLocale.toString()
							        + "' shouldn't be a short name");
							errors.reject("Concept.error.preferredName.is.shortName");
						} else if (nameInLocale.isVoided()) {
							log.warn("Preferred name in locale '" + conceptNameLocale.toString()
							        + "' shouldn't be a voided name");
							errors.reject("Concept.error.preferredName.is.voided");
						}
						
						preferredNameForLocaleFound = true;
					}
					//should have one preferred name per locale
					else if (nameInLocale.isLocalePreferred() && preferredNameForLocaleFound) {
						log.warn("Found multiple preferred names in locale '" + conceptNameLocale.toString() + "'");
						errors.reject("Concept.error.multipleLocalePreferredNames");
					}
				}
				
				if (nameInLocale.isFullySpecifiedName()) {
					if (!hasFullySpecifiedName) {
						hasFullySpecifiedName = true;
					}
					if (!fullySpecifiedNameForLocaleFound) {
						fullySpecifiedNameForLocaleFound = true;
					} else {
						log.warn("Found multiple fully specified names in locale '" + conceptNameLocale.toString() + "'");
						errors.reject("Concept.error.multipleFullySpecifiedNames");
					}
					if (nameInLocale.isVoided()) {
						log.warn("Fully Specified name in locale '" + conceptNameLocale.toString()
						        + "' shouldn't be a voided name");
						errors.reject("Concept.error.fullySpecifiedName.is.voided");
					}
				}
				
				if (nameInLocale.isShort()) {
					if (!shortNameForLocaleFound) {
						shortNameForLocaleFound = true;
					}
					//should have one short name per locale
					else {
						log.warn("Found multiple short names in locale '" + conceptNameLocale.toString() + "'");
						errors.reject("Concept.error.multipleShortNames");
					}
				}
				
				//find duplicate names for a non-retired concept
				if (Context.getConceptService().isConceptNameDuplicate(nameInLocale)) {
					throw new DuplicateConceptNameException("'" + nameInLocale.getName()
					        + "' is a duplicate name in locale '" + conceptNameLocale.toString() + "'");
				}
				
				//
				if (errors.hasErrors()) {
					log.debug("Concept name '" + nameInLocale.getName() + "' for locale '" + conceptNameLocale
					        + "' is invalid");
					//if validation fails for any conceptName in current locale, don't proceed
					//This helps not to have multiple messages shown that are identical though they might be
					//for different conceptNames
					return;
				}
				
				//No duplicate names allowed for the same locale and concept, keep the case the same
				//except for short names
				if (!nameInLocale.isShort() && !validNamesFoundInLocale.add(nameInLocale.getName().toLowerCase())) {
					throw new DuplicateConceptNameException("'" + nameInLocale.getName()
					        + "' is a duplicate name in locale '" + conceptNameLocale.toString() + "' for the same concept");
				}
				
				if (log.isDebugEnabled()) {
					log.debug("Valid name found: " + nameInLocale.getName());
				}
			}
		}
		
		//Ensure that each concept has atleast a fully specified name
		if (!hasFullySpecifiedName) {
			log.debug("Concept has no fully specified name");
			errors.reject("Concept.error.no.FullySpecifiedName");
		}
		
		if (CollectionUtils.isNotEmpty(conceptToValidate.getConceptMappings())) {
			//validate all the concept maps
			int index = 0;
			Set<Integer> mappedTermIds = null;
			for (ConceptMap map : conceptToValidate.getConceptMappings()) {
				if (map.getConceptReferenceTerm().getConceptReferenceTermId() == null) {
					//if this term is getting created on the fly e.g. from old legacy code, validate it
					try {
						errors.pushNestedPath("conceptMappings[" + index + "].conceptReferenceTerm");
						ValidationUtils.invokeValidator(new ConceptReferenceTermValidator(), map.getConceptReferenceTerm(),
						    errors);
					}
					finally {
						errors.popNestedPath();
					}
					
				}
				/*if (map.getConceptMapType() == null) {
					errors.rejectValue("conceptMappings[" + index + "].conceptMapType", "Concept.map.typeRequired",
					    "The concept map type is required for a concept map");
					return;
				}*/

				//don't proceed to the next maps since the current one already has errors
				if (errors.hasErrors()) {
					return;
				}
				
				if (mappedTermIds == null) {
					mappedTermIds = new HashSet<Integer>();
				}
				
				//if we already have a mapping to this term, reject it this map
				if (map.getConceptReferenceTerm().getId() != null
				        && !mappedTermIds.add(map.getConceptReferenceTerm().getId())) {
					errors.rejectValue("conceptMappings[" + index + "]", "ConceptReferenceTerm.term.alreadyMapped",
					    "Cannot map a reference term multiple times to the same concept");
				}
				
				index++;
			}
		}
		ValidateUtil.validateFieldLengths(errors, obj.getClass(), "version", "retireReason");
	}
}
