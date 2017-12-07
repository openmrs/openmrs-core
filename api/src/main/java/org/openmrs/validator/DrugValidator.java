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

import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Drug;
import org.openmrs.DrugReferenceMap;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Validates {@link Drug} objects.
 * 
 * @since 1.10
 */
@Handler(supports = { Drug.class })
public class DrugValidator implements Validator {
	
	/**
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return Drug.class.isAssignableFrom(c);
	}
	
	/**
	 * Validates an Drug object
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail if the drug object is null
	 * @should fail if drug on drugReferenceMap is null
	 * @should fail if conceptReferenceTerm on drugReferenceMap is null
	 * @should invoke ConceptReferenceTermValidator if term on drugReferenceMap is new
	 * @should invoke ConceptMapTypeValidator if conceptMapType on drugReferenceMap is new
	 * @should pass if all fields are correct
	 * @should reject drug multiple mappings to the same term
	 * @should pass validation if field lengths are correct
	 * @should fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object obj, Errors errors) {
		if (obj == null || !(obj instanceof Drug)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type" + Drug.class);
		} else {
			Drug drug = (Drug) obj;
			Set<DrugReferenceMap> drugReferenceMaps = drug.getDrugReferenceMaps();
			Set<String> mappedTermUuids = new HashSet<>();
			int index = 0;
			for (DrugReferenceMap referenceMap : drugReferenceMaps) {
				Drug mappedDrug = referenceMap.getDrug();
				ConceptReferenceTerm referenceTerm = referenceMap.getConceptReferenceTerm();
				ConceptMapType mapType = referenceMap.getConceptMapType();
				
				if (mappedDrug == null) {
					errors.rejectValue("drugReferenceMaps[" + index + "].drug", "Drug.drugReferenceMap.mappedDrug");
				}
				if (referenceTerm == null) {
					errors.rejectValue("drugReferenceMaps[" + index + "].conceptReferenceTerm",
					    "Drug.drugReferenceMap.conceptReferenceTerm");
				} else if (referenceTerm.getConceptReferenceTermId() == null) {
					try {
						errors.pushNestedPath("drugReferenceMaps[" + index + "].conceptReferenceTerm");
						ValidationUtils.invokeValidator(new ConceptReferenceTermValidator(), referenceTerm, errors);
					}
					finally {
						errors.popNestedPath();
					}
				}
				
				if (mapType == null) {
					errors.rejectValue("drugReferenceMaps[" + index + "].conceptMapType",
					    "Drug.drugReferenceMap.conceptMapType");
				} else if (mapType.getConceptMapTypeId() == null) {
					try {
						errors.pushNestedPath("drugReferenceMaps[" + index + "].conceptMapType");
						ValidationUtils.invokeValidator(new ConceptMapTypeValidator(), mapType, errors);
					}
					finally {
						errors.popNestedPath();
					}
				}
				
				//don't proceed to the next map
				if (errors.hasErrors()) {
					return;
				}
				
				//if we already have a mapping to this term, reject it this map
				if (!mappedTermUuids.add(referenceMap.getConceptReferenceTerm().getUuid())) {
					errors.rejectValue("drugReferenceMaps[" + index + "].conceptReferenceTerm",
					    "Drug.drugReferenceMap.termAlreadyMapped",
					    "Cannot map a drug multiple times to the same reference term");
				}
				index++;
			}
			ValidateUtil.validateFieldLengths(errors, obj.getClass(), "name", "retireReason", "strength");
		}
	}
}
