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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates {@link ConceptReferenceTerm} objects.
 *
 * @since 1.9
 */
@Handler(supports = { ConceptReferenceTerm.class }, order = 50)
public class ConceptReferenceTermValidator implements Validator {
	
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return ConceptReferenceTerm.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks that a given concept reference term object is valid.
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail if the concept reference term object is null
	 * <strong>Should</strong> fail if the name is a white space character
	 * <strong>Should</strong> fail if the code is null
	 * <strong>Should</strong> fail if the code is an empty string
	 * <strong>Should</strong> fail if the code is a white space character
	 * <strong>Should</strong> fail if the concept reference term code is a duplicate in its concept source
	 * <strong>Should</strong> fail if the concept source is null
	 * <strong>Should</strong> pass if all the required fields are set and valid
	 * <strong>Should</strong> pass if the duplicate name is for a term from another concept source
	 * <strong>Should</strong> pass if the duplicate code is for a term from another concept source
	 * <strong>Should</strong> fail if a concept reference term map has no concept map type
	 * <strong>Should</strong> fail if termB of a concept reference term map is not set
	 * <strong>Should</strong> fail if a term is mapped to itself
	 * <strong>Should</strong> fail if a term is mapped multiple times to the same term
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) throws APIException {
		
		if (obj == null || !(obj instanceof ConceptReferenceTerm)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type"
			        + ConceptReferenceTerm.class);
		}
		
		ConceptReferenceTerm conceptReferenceTerm = (ConceptReferenceTerm) obj;
		
		String code = conceptReferenceTerm.getCode();
		boolean hasBlankFields = false;
		if (!StringUtils.hasText(code)) {
			errors.rejectValue("code", "ConceptReferenceTerm.error.codeRequired",
			    "The code property is required for a concept reference term");
			hasBlankFields = true;
		}
		if (conceptReferenceTerm.getConceptSource() == null) {
			errors.rejectValue("conceptSource", "ConceptReferenceTerm.error.sourceRequired",
			    "The conceptSource property is required for a concept reference term");
			hasBlankFields = true;
		}
		if (hasBlankFields) {
			return;
		}
		
		code = code.trim();
		//Ensure that there are no terms with the same code in the same source
		ConceptReferenceTerm termWithDuplicateCode = Context.getConceptService().getConceptReferenceTermByCode(code,
		    conceptReferenceTerm.getConceptSource());
		if (termWithDuplicateCode != null
		        && !OpenmrsUtil.nullSafeEquals(termWithDuplicateCode.getUuid(), conceptReferenceTerm.getUuid())) {
			errors.rejectValue("code", "ConceptReferenceTerm.duplicate.code",
			    "Duplicate concept reference term code in its concept source: " + code);
		}
		
		//validate the concept reference term maps
		if (CollectionUtils.isNotEmpty(conceptReferenceTerm.getConceptReferenceTermMaps())) {
			int index = 0;
			Set<String> mappedTermUuids = null;
			for (ConceptReferenceTermMap map : conceptReferenceTerm.getConceptReferenceTermMaps()) {
				if (map == null) {
					throw new APIException("ConceptReferenceTerm.add.null", (Object[]) null);
				}
				
				if (map.getConceptMapType() == null) {
					errors.rejectValue("conceptReferenceTermMaps[" + index + "].conceptMapType",
					    "ConceptReferenceTerm.error.mapTypeRequired", "Concept Map Type is required");
				} else if (map.getTermB() == null) {
					errors.rejectValue("conceptReferenceTermMaps[" + index + "].termB",
					    "ConceptReferenceTerm.error.termBRequired", "Mapped Term is required");
				} else if (map.getTermB().equals(conceptReferenceTerm)) {
					errors.rejectValue("conceptReferenceTermMaps[" + index + "].termB", "ConceptReferenceTerm.map.sameTerm",
					    "Cannot map a concept reference term to itself");
				}
				
				//don't proceed to the next map
				if (errors.hasErrors()) {
					return;
				}
				
				if (mappedTermUuids == null) {
					mappedTermUuids = new HashSet<>();
				}
				
				//if we already have a mapping to this term, reject it this map
				if (!mappedTermUuids.add(map.getTermB().getUuid())) {
					errors.rejectValue("conceptReferenceTermMaps[" + index + "].termB",
					    "ConceptReferenceTerm.termToTerm.alreadyMapped",
					    "Cannot map a reference term multiple times to the same concept reference term");
				}
				
				index++;
			}
		}
		ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "code", "version", "description", "retireReason");
	}
}
