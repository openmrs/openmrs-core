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

import org.junit.jupiter.api.Test;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AllergyTest extends BaseContextSensitiveTest {

	@Autowired
	PatientService patientService;

	@Autowired
	ConceptService conceptService;

	@Test
	public void requiredPropertiesConstructor_shouldConstructAValidObject() {
		// setup required properties
		Patient patient = patientService.getPatient(2);

		Allergen allergen = new Allergen();
		allergen.setCodedAllergen(conceptService.getConcept(792));
		allergen.setAllergenType(AllergenType.DRUG);

		Concept severity = conceptService.getConcept(23);
		String comments = "test allergy";

		List<AllergyReaction> reactions = new ArrayList<>();
		AllergyReaction reaction = new AllergyReaction();
		reaction.setReaction(conceptService.getConcept(3));

		// construct the object
		Allergy allergy = new Allergy(patient, allergen, severity, comments, reactions);

		// save to the database
		patientService.saveAllergy(allergy);
	}
}
