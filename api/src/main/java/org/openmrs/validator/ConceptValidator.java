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
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.openmrs.api.DuplicateConceptNameException;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ConceptValidator extends BaseCustomizableValidator implements Validator {
	
	// Logger for this class
	private static final Logger log = LoggerFactory.getLogger(ConceptValidator.class);
	
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return Concept.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks that a given concept object is valid.
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> pass if the concept has at least one fully specified name added to it
	 * <strong>Should</strong> fail if there is a duplicate unretired concept name in the locale
	 * <strong>Should</strong> fail if there is a duplicate unretired preferred name in the same locale
	 * <strong>Should</strong> fail if there is a duplicate unretired fully specified name in the same locale
	 * <strong>Should</strong> fail if any names in the same locale for this concept are similar
	 * <strong>Should</strong> pass if the concept with a duplicate name is retired
	 * <strong>Should</strong> pass if the concept being validated is retired and has a duplicate name
	 * <strong>Should</strong> fail if any name is an empty string
	 * <strong>Should</strong> fail if the object parameter is null
	 * <strong>Should</strong> pass if the concept is being updated with no name change
	 * <strong>Should</strong> fail if any name is a null value
	 * <strong>Should</strong> not allow multiple preferred names in a given locale
	 * <strong>Should</strong> not allow multiple fully specified conceptNames in a given locale
	 * <strong>Should</strong> not allow multiple short names in a given locale
	 * <strong>Should</strong> not allow an index term to be a locale preferred name
	 * <strong>Should</strong> fail if there is no name explicitly marked as fully specified
	 * <strong>Should</strong> pass if the duplicate ConceptName is neither preferred nor fully Specified
	 * <strong>Should</strong> pass if the concept has a synonym that is also a short name
	 * <strong>Should</strong> fail if a term is mapped multiple times to the same concept
	 * <strong>Should</strong> not fail if a term has two new mappings on it
	 * <strong>Should</strong> fail if there is a duplicate unretired concept name in the same locale different than
	 *         the system locale
	 * <strong>Should</strong> pass for a new concept with a map created with deprecated concept map methods
	 * <strong>Should</strong> pass for an edited concept with a map created with deprecated concept map methods
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 * <strong>Should</strong> pass if fully specified name is the same as short name
	 * <strong>Should</strong> pass if different concepts have the same short name
	 * <strong>Should</strong> fail if the concept datatype is null
	 * <strong>Should</strong> fail if the concept class is null
	 */
	@Override
	public void validate(Object obj, Errors errors) throws APIException, DuplicateConceptNameException {
		
		if (obj == null || !(obj instanceof Concept)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type" + Concept.class);
		}
		
		Concept conceptToValidate = (Concept) obj;
		//no name to validate, but why is this the case?
		if (conceptToValidate.getNames().isEmpty()) {
			errors.reject("Concept.name.atLeastOneRequired");
			return;
		}

		ValidationUtils.rejectIfEmpty(errors, "datatype", "Concept.datatype.empty");
		ValidationUtils.rejectIfEmpty(errors, "conceptClass", "Concept.conceptClass.empty");

		boolean hasFullySpecifiedName = false;
		for (Locale conceptNameLocale : conceptToValidate.getAllConceptNameLocales()) {
			boolean fullySpecifiedNameForLocaleFound = false;
			boolean preferredNameForLocaleFound = false;
			boolean shortNameForLocaleFound = false;
			Set<String> validNamesFoundInLocale = new HashSet<>();
			Collection<ConceptName> namesInLocale = conceptToValidate.getNames(conceptNameLocale);
			for (ConceptName nameInLocale : namesInLocale) {
				if (StringUtils.isBlank(nameInLocale.getName())) {
					log.debug("Name in locale '" + conceptNameLocale.toString()
					        + "' cannot be an empty string or white space");
					errors.reject("Concept.name.empty");
				}
				if (nameInLocale.getLocalePreferred() != null) {
					if (nameInLocale.getLocalePreferred() && !preferredNameForLocaleFound) {
						if (nameInLocale.isIndexTerm()) {
							log.warn("Preferred name in locale '" + conceptNameLocale.toString()
							        + "' shouldn't be an index term");
							errors.reject("Concept.error.preferredName.is.indexTerm");
						} else if (nameInLocale.isShort()) {
							log.warn("Preferred name in locale '" + conceptNameLocale.toString()
							        + "' shouldn't be a short name");
							errors.reject("Concept.error.preferredName.is.shortName");
						} else if (nameInLocale.getVoided()) {
							log.warn("Preferred name in locale '" + conceptNameLocale.toString()
							        + "' shouldn't be a voided name");
							errors.reject("Concept.error.preferredName.is.voided");
						}
						
						preferredNameForLocaleFound = true;
					}
					//should have one preferred name per locale
					else if (nameInLocale.getLocalePreferred() && preferredNameForLocaleFound) {
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
					if (nameInLocale.getVoided()) {
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
				
				log.debug("Valid name found: {}", nameInLocale.getName());
			}
		}
		
		//Ensure that each concept has at least a fully specified name
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

				//don't proceed to the next maps since the current one already has errors
				if (errors.hasErrors()) {
					return;
				}
				
				if (mappedTermIds == null) {
					mappedTermIds = new HashSet<>();
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
		if (CollectionUtils.isNotEmpty(conceptToValidate.getAnswers())) {
			for (ConceptAnswer conceptAnswer : conceptToValidate.getAnswers()) {
				if (conceptAnswer.getAnswerConcept().equals(conceptToValidate)) {
					errors.reject("Concept.contains.itself.as.answer");
				}
			}
		}
		ValidateUtil.validateFieldLengths(errors, obj.getClass(), "version", "retireReason");
		super.validateAttributes(conceptToValidate, errors, Context.getConceptService().getAllConceptAttributeTypes());
	}
}
