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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

public class AllergyTest extends BaseContextSensitiveTest {

	private static final String ALLERGY_TEST_DATASET =
		"org/openmrs/api/include/allergyTestDataset.xml";

	private Allergy allergy1;
	private Allergy allergy2;

	@BeforeEach
	void setup() {
		executeDataSet(ALLERGY_TEST_DATASET);

		Patient patient = Context.getPatientService().getPatient(2);
		Concept concept1 = Context.getConceptService().getConcept(3);

		Allergen allergen1 = new Allergen(AllergenType.DRUG, concept1, null);
		Allergen allergen2 = new Allergen(AllergenType.DRUG, concept1, null);

		Allergy baseAllergy = new Allergy();
		baseAllergy.setPatient(patient);
		baseAllergy.setAllergen(allergen1);

		AllergyReaction reaction1 =
			new AllergyReaction(baseAllergy, concept1, null);
		AllergyReaction reaction2 =
			new AllergyReaction(baseAllergy, concept1, null);

		allergy1 = new Allergy(patient, allergen1, concept1,
			"Comment 1", Arrays.asList(reaction1));
		allergy2 = new Allergy(patient, allergen2, concept1,
			"Comment 1", Arrays.asList(reaction2));

		// IMPORTANT: allergyId must be explicitly set
		allergy1.setAllergyId(1);
		allergy2.setAllergyId(1);
	}

	@Test
	void hasSameValues_shouldReturnTrueWhenAllValuesAreSame() {
		assertTrue(allergy1.hasSameValues(allergy2));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenAllergyIdIsDifferent() {
		allergy2.setAllergyId(2);
		assertFalse(allergy1.hasSameValues(allergy2));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenPatientIsDifferent() {
		Patient differentPatient = Context.getPatientService().getPatient(3);
		allergy2.setPatient(differentPatient);

		assertFalse(allergy1.hasSameValues(allergy2));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenCodedAllergenIsDifferent() {
		allergy2.getAllergen()
			.setCodedAllergen(Context.getConceptService().getConcept(5));

		assertFalse(allergy1.hasSameValues(allergy2));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenSeverityIsDifferent() {
		allergy2.setSeverity(Context.getConceptService().getConcept(5));

		assertFalse(allergy1.hasSameValues(allergy2));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenCommentsAreDifferent() {
		allergy2.setComments("Different comment");

		assertFalse(allergy1.hasSameValues(allergy2));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenNonCodedAllergenIsDifferent() {
		allergy2.getAllergen().setNonCodedAllergen("Non coded allergen");

		assertFalse(allergy1.hasSameValues(allergy2));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenReactionsAreDifferent() {
		AllergyReaction differentReaction =
			new AllergyReaction(allergy1,
				Context.getConceptService().getConcept(5), null);

		allergy2.setReactions(Arrays.asList(differentReaction));

		assertFalse(allergy1.hasSameValues(allergy2));
	}
}

