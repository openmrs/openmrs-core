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
