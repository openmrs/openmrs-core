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
	
	@Test
	void shouldReturnTrueWhenAllValuesMatch() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(5, 6, "rash")));
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(5, 6, "rash")));
		
		assertTrue(left.hasSameValues(right));
	}
	
	@Test
	void shouldReturnFalseWhenAllergyIdDiffers() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(99, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		
		assertFalse(left.hasSameValues(right));
	}
	
	@Test
	void shouldReturnTrueWhenPatientsAreDifferentInstancesWithSamePatientId() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		
		assertNotSame(left.getPatient(), right.getPatient());
		assertTrue(left.hasSameValues(right));
	}
	
	@Test
	void shouldReturnFalseWhenPatientIdsDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(99), codedAllergen(3), severity(4), "comment", List.of());
		
		assertFalse(left.hasSameValues(right));
	}
	
	@Test
	void shouldReturnTrueWhenCodedAllergensAreDifferentInstancesWithSameConceptId() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		
		assertNotSame(left.getAllergen().getCodedAllergen(), right.getAllergen().getCodedAllergen());
		assertTrue(left.hasSameValues(right));
	}
	
	@Test
	void shouldReturnFalseWhenCodedAllergenConceptIdsDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(99), severity(4), "comment", List.of());
		
		assertFalse(left.hasSameValues(right));
	}
	
	@Test
	void shouldReturnFalseWhenNonCodedAllergenDiffers() {
		Allergy left = allergy(1, patient(2), nonCodedAllergen("Peanuts"), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), nonCodedAllergen("Shellfish"), severity(4), "comment", List.of());
		
		assertFalse(left.hasSameValues(right));
	}
	
	@Test
	void shouldReturnTrueWhenSeveritiesAreDifferentInstancesWithSameConceptId() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		
		assertNotSame(left.getSeverity(), right.getSeverity());
		assertTrue(left.hasSameValues(right));
	}
	
	@Test
	void shouldReturnFalseWhenSeverityConceptIdsDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(99), "comment", List.of());
		
		assertFalse(left.hasSameValues(right));
	}
	
	@Test
	void shouldReturnFalseWhenCommentDiffers() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "different", List.of());
		
		assertFalse(left.hasSameValues(right));
	}
	
	@Test
	void shouldReturnFalseWhenReactionCountDiffers() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(5, 6, "rash")));
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		
		assertFalse(left.hasSameValues(right));
	}
	
	@Test
	void shouldReturnFalseWhenReactionValuesDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(5, 6, "rash")));
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(5, 99, "rash")));
		
		assertFalse(left.hasSameValues(right));
	}
	
	@Test
	void shouldThrowWhenMatchingReactionIdsDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(5, 6, "rash")));
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of(reaction(99, 6, "rash")));
		
		assertThrows(NullPointerException.class, () -> left.hasSameValues(right));
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
