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

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests hasSameValues method in {@link org.openmrs.Allergy}.
 */
class AllergyTest {

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnTrueWhenAllValuesMatch() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(5, 6, "rash")));
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(5, 6, "rash")));

		assertTrue(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnFalseWhenAllergyIdDiffers() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(99, patient(2), codedAllergen(3), severity(4), "comment", List.of());

		assertFalse(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnTrueWhenPatientsAreDifferentInstancesWithSamePatientId() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());

		assertNotSame(left.getPatient(), right.getPatient());
		assertTrue(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnFalseWhenPatientIdsDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(99), codedAllergen(3), severity(4), "comment", List.of());

		assertFalse(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnTrueWhenCodedAllergensAreDifferentInstancesWithSameConceptId() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());

		assertNotSame(left.getAllergen().getCodedAllergen(), right.getAllergen().getCodedAllergen());
		assertTrue(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnFalseWhenCodedAllergenConceptIdsDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(99), severity(4), "comment", List.of());

		assertFalse(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnFalseWhenNonCodedAllergenDiffers() {
		Allergy left = allergy(1, patient(2), nonCodedAllergen("Peanuts"), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), nonCodedAllergen("Shellfish"), severity(4), "comment", List.of());

		assertFalse(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnTrueWhenSeveritiesAreDifferentInstancesWithSameConceptId() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());

		assertNotSame(left.getSeverity(), right.getSeverity());
		assertTrue(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnFalseWhenSeverityConceptIdsDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(99), "comment", List.of());

		assertFalse(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnFalseWhenCommentDiffers() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "different", List.of());

		assertFalse(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnFalseWhenReactionCountDiffers() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(5, 6, "rash")));
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());

		assertFalse(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnFalseWhenReactionValuesDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(5, 6, "rash")));
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(5, 99, "rash")));

		assertFalse(left.hasSameValues(right));
	}

	@Test
	void hasSameValues_shouldThrowWhenOtherAllergyDoesNotContainReactionWithSameId() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(5, 6, "rash")));
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(99, 6, "rash")));

		assertThrows(NullPointerException.class, () -> left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldThrowWhenComparedAllergyIsNull() {
		Allergy allergy = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());

		assertThrows(NullPointerException.class, () -> allergy.hasSameValues(null));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldThrowWhenThisAllergenIsNull() {
		Allergy left = allergy(1, patient(2), null, severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());

		assertThrows(NullPointerException.class, () -> left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldThrowWhenComparedAllergenIsNull() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), null, severity(4), "comment", List.of());

		assertThrows(NullPointerException.class, () -> left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnTrueWhenBothPatientsAreNull() {
		Allergy left = allergy(1, null, codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, null, codedAllergen(3), severity(4), "comment", List.of());

		assertTrue(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnTrueWhenBothSeveritiesAreNull() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), null, "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), null, "comment", List.of());

		assertTrue(left.hasSameValues(right));
	}

	/**
	 * @see Allergy#hasSameValues(Allergy)
	 */
	@Test
	void hasSameValues_shouldReturnTrueWhenBothCommentsAreNull() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), null, List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), null, List.of());

		assertTrue(left.hasSameValues(right));
	}

	private Allergy allergy(Integer allergyId, Patient patient, Allergen allergen, Concept severity, String comments,
	        List<AllergyReaction> reactions) {
		Allergy allergy = new Allergy(patient, allergen, severity, comments, reactions);
		allergy.setAllergyId(allergyId);
		return allergy;
	}

	private Patient patient(Integer patientId) {
		Patient patient = new Patient();
		patient.setPatientId(patientId);
		return patient;
	}

	private Allergen codedAllergen(Integer conceptId) {
		Allergen allergen = new Allergen();
		allergen.setAllergenType(AllergenType.DRUG);
		allergen.setCodedAllergen(concept(conceptId));
		return allergen;
	}

	private Allergen nonCodedAllergen(String value) {
		Allergen allergen = new Allergen();
		allergen.setAllergenType(AllergenType.OTHER);
		allergen.setNonCodedAllergen(value);
		return allergen;
	}

	private Concept severity(Integer conceptId) {
		return concept(conceptId);
	}

	private Concept concept(Integer conceptId) {
		Concept concept = new Concept();
		concept.setConceptId(conceptId);
		return concept;
	}

	private AllergyReaction reaction(Integer reactionId, Integer conceptId, String reactionNonCoded) {

		AllergyReaction reaction = new AllergyReaction();
		reaction.setAllergyReactionId(reactionId);
		reaction.setReaction(concept(conceptId));
		reaction.setReactionNonCoded(reactionNonCoded);
		return reaction;
	}
}
