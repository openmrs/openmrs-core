/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("allergyProperties")
public class AllergyProperties {
	
	@Autowired
	@Qualifier("conceptService")
	protected ConceptService conceptService;
	
	@Autowired
	@Qualifier("adminService")
	protected AdministrationService administrationService;
	
	protected Concept getConceptByGlobalProperty(String globalPropertyName) {
		String globalProperty = administrationService.getGlobalProperty(globalPropertyName);
		Concept concept = conceptService.getConceptByUuid(globalProperty);
		if (concept == null) {
			throw new IllegalStateException("Configuration required: " + globalPropertyName);
		}
		return concept;
	}
	
	public Concept getMildSeverityConcept() {
		return getConceptByGlobalProperty("allergy.concept.severity.mild");
	}
	
	public Concept getModerateSeverityConcept() {
		return getConceptByGlobalProperty("allergy.concept.severity.moderate");
	}
	
	public Concept getSevereSeverityConcept() {
		return getConceptByGlobalProperty("allergy.concept.severity.severe");
	}
	
	public Concept getFoodAllergensConcept() {
		return getConceptByGlobalProperty("allergy.concept.allergen.food");
	}
	
	public Concept getDrugAllergensConcept() {
		return getConceptByGlobalProperty("allergy.concept.allergen.drug");
	}
	
	public Concept getEnvironmentAllergensConcept() {
		return getConceptByGlobalProperty("allergy.concept.allergen.environment");
	}
	
	public Concept getAllergyReactionsConcept() {
		return getConceptByGlobalProperty("allergy.concept.reactions");
	}
	
	public Concept getOtherNonCodedConcept() {
		return getConceptByGlobalProperty("allergy.concept.otherNonCoded");
	}
	
	public Concept getUnknownConcept() {
		return getConceptByGlobalProperty("allergy.concept.unknown");
	}
}
