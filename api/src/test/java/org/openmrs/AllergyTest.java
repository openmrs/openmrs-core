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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AllergyTest {

    @Test
	public void hasSameValues_otherAllergyIsNull_shouldThrowNullPointerException() {
		Allergy allergy = new Allergy();
		assertThrows(NullPointerException.class, () -> allergy.hasSameValues(null));
	}

	@Test
	public void hasSameValues_checkingAgainstSameInstanceOfAllergy_shouldInterpretAsSameValues() {
		Allergy allergy = allergy();
		assertThat(allergy.hasSameValues(allergy()),is(true));
	}

	@Test
	public void hasSameValues_allergiesWithNullIds_shouldBeInterpretedAsSameValues() {
		assertThat(allergyWithId(null).hasSameValues(allergyWithId(null)), is(true));
	}

	@Test
	public void hasSameValues_allergiesWithDifferentIds_shouldBeInterpretAsNotSameValues() {
		assertThat(allergyWithId(1).hasSameValues(allergyWithId(2)), is(false));
	}

	@Test
	public void hasSameValues_allergyWithNullIdAndAllergyWithId_shouldBeInterpretAsNotSameValues() {
		assertThat(allergyWithId(null).hasSameValues(allergyWithId(1)), is(false));
	}

	@Test
	public void hasSameValues_allergyWithIdAndAllergyWithNullId_shouldBeInterpretAsNotSameValues() {
		assertThat(allergyWithId(1).hasSameValues(allergyWithId(null)), is(false));
	}

	@Test
	public void hasSameValues_allergiesWithDifferentPatientIds_shouldNotBeInterpretAsNotSameValues() {
		Allergy allergy = allergyWithPatientId(1);
		Allergy otherAllergy = allergyWithPatientId(2);

		assertThat(allergy.hasSameValues(otherAllergy), is(false));
	}

	private Allergy allergyWithPatientId(int patientId) {
		Patient patient = new Patient(patientId);
		Allergy allergy = allergy();
		allergy.setPatient(patient);
		return allergy;
	}

	@Test
	public void hasSameValues_allergyWithPatientAndAllergyWithoutPatient_shouldBeInterpretAsNotSameValues() {
		assertThat(allergyWithPatient().hasSameValues(allergyWithoutPatient()), is(false));
	}

	@Test
	public void hasSameValues_allergyWithoutPatientAndAllergyWithPatient_shouldBeInterpretAsNotSameValues() {
		assertThat(allergyWithoutPatient().hasSameValues(allergyWithPatient()), is(false));
	}

	@Test
	public void hasSameValues_allergiesWithSamePatientIdsButDifferentInstances_shouldBeInterpretedAsSameValues() {
		assertThat(allergyWithPatient().hasSameValues(allergyWithPatient()), is(true));
	}

	private Allergy allergyWithPatient() {
		return allergyWithPatient(new Patient(1));
	}

	@Test
	public void hasSameValues_allergiesWithSameIdButWithoutPatients_shouldBeInterpretedAsSameValues() {
		assertThat(allergyWithoutPatient().hasSameValues(allergyWithoutPatient()), is(true));
	}

	@Test
	public void hasSameValues_allergiesWithDifferentComments_shouldNotBeInterpretedAsSameValues() {
		assertThat(allergyWithComment("commentA").hasSameValues(allergyWithComment("B")), is(false));
	}

	@Test
	public void hasSameValues_allergiesWithSameComments_shouldBeInterpretedAsSameValues() {
		assertThat(allergyWithComment("comment").hasSameValues(allergyWithComment("comment")), is(true));
	}

	@Test
	public void hasSameValues_allergiesWithDifferentNonCodedAllergen_shouldNotBeInterpretedAsSameValues() {
		assertThat(
			allergyWithNonCodedAllergen("nonCoded").hasSameValues(allergyWithNonCodedAllergen("other")),
			is(false));
	}

	@Test
	public void hasSameValues_allergiesWithNullNonCodedAllergen_shouldBeInterpretedAsSameValues() {
		assertThat(allergyWithNonCodedAllergen(null).hasSameValues(allergyWithNonCodedAllergen(null)), is(true));
	}

	@Test
	public void hasSameValues_allergiesWithSameNonCodedAllergen_shouldBeInterpretedAsSameValues() {
		assertThat(allergyWithNonCodedAllergen("same").hasSameValues(allergyWithNonCodedAllergen("same")), is(true));
	}

	@Test
	public void hasSameValues_allergyWithNullNonCodedAllergenAndOtherAllergyWithNonCodedAllergen_shouldNotBeInterpretedAsSameValues() {
		assertThat(allergyWithNonCodedAllergen(null).hasSameValues(allergyWithNonCodedAllergen("allergen")), is(false));
	}

	@Test
	public void hasSameValues_allergyWithNonCodedAllergenAndOtherAllergyWithoutNonCodedAllergen_shouldNotBeInterpretedAsSameValues() {
		assertThat(allergyWithNonCodedAllergen("allergen").hasSameValues(allergyWithNonCodedAllergen(null)), is(false));
	}

	private Allergy allergyWithNonCodedAllergen(String nonCodedAllergen) {
		Allergy allergy = allergy();
		allergy.getAllergen().setNonCodedAllergen(nonCodedAllergen);
		return allergy;
	}

	@Test
	public void hasSameValues_allergiesWithSameInstanceOfCodedAllergen_shouldBeInterpretedAsSameValues() {
		Concept allergen = new Concept();
		assertThat(allergyWithCodedAllergen(allergen).hasSameValues(allergyWithCodedAllergen(allergen)), is(true));
	}

	@Test
	public void hasSameValues_allergiesWithSameIdsOfCodedAllergen_shouldBeInterpretedAsSameValues() {
		Concept allergen = new Concept(1);
		Concept otherAllergen = new Concept(1);
		assertThat(allergyWithCodedAllergen(allergen).hasSameValues(allergyWithCodedAllergen(otherAllergen)), is(true));
	}

	@Test
	public void hasSameValues_allergiesWithDifferentCodedAllergen_shouldNotBeInterpretedAsSameValues() {
		Concept allergen = new Concept(1);
		Concept otherAllergen = new Concept(2);
		assertThat(allergyWithCodedAllergen(allergen).hasSameValues(allergyWithCodedAllergen(otherAllergen)), is(false));
	}

	@Test
	public void hasSameValues_allergyWithoutCodedAllergenComparedToAllergyWithCodedAllergen_shouldNotBeInterpretedAsSameValues() {
		Concept allergen = new Concept(1);
		assertThat(allergyWithCodedAllergen(null).hasSameValues(allergyWithCodedAllergen(allergen)), is(false));
	}

	@Test
	public void hasSameValues_allergyWithCodedAllergenComparedToAllergyWithoutCodedAllergen_shouldNotBeInterpretedAsSameValues() {
		Concept allergen = new Concept(1);
		assertThat(allergyWithCodedAllergen(allergen).hasSameValues(allergyWithCodedAllergen(null)), is(false));
	}

	private Allergy allergyWithCodedAllergen(Concept allergen) {
		Allergy allergy = allergy();
		allergy.getAllergen().setCodedAllergen(allergen);
		return allergy;

	}

	@Test
	public void hasSameValues_allergiesWithSameInstanceOfSeverity_shouldBeInterpretedAsSameValues() {
		Concept severity = new Concept(1);

		assertThat(allergyWithSeverity(severity).hasSameValues(allergyWithSeverity(severity)), is(true));
	}

	@Test
	public void hasSameValues_allergiesWithDifferentInstancesOfSeverityHavingIdenticalId_shouldBeInterpretedAsSameValues() {
		Concept severity = new Concept(1);
		Concept otherSeverity = new Concept(1);

		assertThat(allergyWithSeverity(severity).hasSameValues(allergyWithSeverity(otherSeverity)), is(true));
	}

	@Test
	public void hasSameValues_allergiesWithNullSeverity_shouldBeInterpretedAsSameValues() {
		assertThat(allergyWithSeverity(null).hasSameValues(allergyWithSeverity(null)), is(true));
	}

	@Test
	public void hasSameValues_allergyWithNullSeverityComparedAllergyWithToNonNullSeverity_shouldNotBeInterpretedAsSameValues() {
		Concept severity = new Concept(1);

		assertThat(allergyWithSeverity(null).hasSameValues(allergyWithSeverity(severity)), is(false));
	}

	@Test
	public void hasSameValues_allergyWithSeverityComparedToAllergyWithNullSeverity_shouldNotBeInterpretedAsSameValues() {
		Concept severity = new Concept(1);

		assertThat(allergyWithSeverity(severity).hasSameValues(allergyWithSeverity(null)), is(false));
	}

	@Test
	public void hasSameValues_allergiesWithDifferentSeverities_shouldNotBeInterpretedAsSameValues() {
		Concept severity = new Concept(1);
		Concept otherSeverity = new Concept(2);

		assertThat(allergyWithSeverity(severity).hasSameValues(allergyWithSeverity(otherSeverity)), is(false));
	}

	private Allergy allergyWithSeverity(Concept severity) {
		Allergy allergy = allergy();
		allergy.setSeverity(severity);
		return allergy;
	}

	@Test
	public void hasSameValues_allergiesWithEmptyListsOfReactions_shouldBeInterpretedAsSameValues() {
		Allergy allergy = allergy();
		allergy.setReactions(new ArrayList<>());

		Allergy otherAllergy = allergy();
		otherAllergy.setReactions(new ArrayList<>());

		assertThat(allergy.hasSameValues(otherAllergy), is(true));
	}

	@Test
	public void hasSameValues_allergiesWithEmptyAndNotEmptyReactions_shouldNotBeInterpretedAsSameValues() {
		Allergy allergy = allergy();
		allergy.getReactions().add(new AllergyReaction());

		Allergy otherAllergy = allergy();

		assertThat(allergy.hasSameValues(otherAllergy), is(false));
	}

	private Allergy allergyWithComment(String comment) {
		Allergy allergy = allergy();
		allergy.setComments(comment);
		return allergy;
	}

	private Allergy allergyWithoutPatient() {
		return allergyWithPatient(null);
	}

	private Allergy allergyWithPatient(Patient patient) {
		Allergy allergy = allergy();
		allergy.setPatient(patient);
		return allergy;
	}

	private Allergy allergy() {
		return new Allergy(null, new Allergen(), null, "comment", new ArrayList<>());
	}

	private Allergy allergyWithId(Integer id) {
		Allergy allergy = allergy();
		allergy.setId(id);
		return allergy;
	}
}