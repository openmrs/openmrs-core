package org.openmrs;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllergiesSameValuesTest {

	@Test
	void hasSameValues_shouldReturnTrueForSameSimpleValues() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment",
			List.of(reaction(5, reactionConcept(6), "rash")));

		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment",
			List.of(reaction(5, reactionConcept(6), "rash")));

		assertTrue(left.hasSameValues(right));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenAllergyIdDiffers() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(99, patient(2), codedAllergen(3), severity(4), "comment", List.of());

		assertFalse(left.hasSameValues(right));
	}

	@Test
	void hasSameValues_shouldTreatDifferentPatientInstancesWithSamePatientIdAsSame() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());

		assertNotSame(left.getPatient(), right.getPatient());
		assertTrue(left.hasSameValues(right));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenPatientIdsDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(99), codedAllergen(3), severity(4), "comment", List.of());

		assertFalse(left.hasSameValues(right));
	}

	@Test
	void hasSameValues_shouldTreatDifferentCodedAllergenInstancesWithSameConceptIdAsSame() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());

		assertNotSame(left.getAllergen().getCodedAllergen(), right.getAllergen().getCodedAllergen());
		assertTrue(left.hasSameValues(right));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenCodedAllergenConceptIdsDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(99), severity(4), "comment", List.of());

		assertFalse(left.hasSameValues(right));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenNonCodedAllergenDiffers() {
		Allergy left = allergy(1, patient(2), nonCodedAllergen("Peanuts"), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), nonCodedAllergen("Shellfish"), severity(4), "comment", List.of());

		assertFalse(left.hasSameValues(right));
	}

	@Test
	void hasSameValues_shouldTreatDifferentSeverityInstancesWithSameConceptIdAsSame() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());

		assertNotSame(left.getSeverity(), right.getSeverity());
		assertTrue(left.hasSameValues(right));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenSeverityConceptIdsDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(99), "comment", List.of());

		assertFalse(left.hasSameValues(right));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenCommentDiffers() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());
		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "different", List.of());

		assertFalse(left.hasSameValues(right));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenReactionCountDiffers() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment",
			List.of(reaction(5, reactionConcept(6), "rash")));

		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment", List.of());

		assertFalse(left.hasSameValues(right));
	}

	@Test
	void hasSameValues_shouldReturnFalseWhenReactionValuesDiffer() {
		Allergy left = allergy(1, patient(2), codedAllergen(3), severity(4), "comment",
			List.of(reaction(5, reactionConcept(6), "rash")));

		Allergy right = allergy(1, patient(2), codedAllergen(3), severity(4), "comment",
			List.of(reaction(5, reactionConcept(99), "rash")));

		assertFalse(left.hasSameValues(right));
	}

	private Allergy allergy(Integer allergyId, Patient patient, Allergen allergen, Concept severity, String comment,
	                        List<AllergyReaction> reactions) {
		Allergy allergy = new Allergy(patient, allergen, severity, comment, reactions);
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

	private Concept reactionConcept(Integer conceptId) {
		return concept(conceptId);
	}

	private Concept concept(Integer conceptId) {
		Concept concept = new Concept();
		concept.setConceptId(conceptId);
		return concept;
	}

	private AllergyReaction reaction(Integer allergyReactionId, Concept reactionConcept, String nonCodedReaction) {
		AllergyReaction reaction = new AllergyReaction();
		reaction.setAllergyReactionId(allergyReactionId);
		reaction.setReaction(reactionConcept);
		reaction.setReactionNonCoded(nonCodedReaction);
		return reaction;
	}
}
