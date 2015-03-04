/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.concept;

import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptReferenceTermMap;
import org.openmrs.ConceptSource;
import org.openmrs.validator.ConceptReferenceTermValidator;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Provides additional validation for {@link ConceptReferenceTerm}. It should be called aside from
 * {@link ConceptReferenceTermValidator} in the web layer to make sure conceptSource, conceptMapType
 * and termB are persisted in the DB.
 */
public class ConceptReferenceTermWebValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return ConceptReferenceTerm.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		ConceptReferenceTerm conceptReferenceTerm = (ConceptReferenceTerm) target;
		ConceptSource conceptSource = conceptReferenceTerm.getConceptSource();
		if (conceptSource != null && conceptSource.getId() == null) {
			errors.rejectValue("conceptSource", "ConceptReferenceTerm.source.notInDatabase",
			    "Only existing concept reference sources can be used");
		}
		if (conceptReferenceTerm.getConceptReferenceTermMaps() != null) {
			int mapsIndex = 0;
			for (ConceptReferenceTermMap map : conceptReferenceTerm.getConceptReferenceTermMaps()) {
				if (map.getConceptMapType().getId() == null) {
					errors.rejectValue("conceptReferenceTermMaps[" + mapsIndex + "].conceptMapType",
					    "ConceptReferenceTerm.mapType.notInDatabase", "Only existing concept map types can be used");
				} else if (map.getTermB().getId() == null) {
					errors.rejectValue("conceptReferenceTermMaps[" + mapsIndex + "].termB",
					    "ConceptReferenceTerm.term.notInDatabase", "Only existing concept reference terms can be mapped");
				}
			}
		}
	}
	
}
