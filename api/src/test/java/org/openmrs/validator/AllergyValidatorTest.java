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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Allergen;
import org.openmrs.AllergenType;
import org.openmrs.Allergy;
import org.openmrs.Allergies;
import org.openmrs.Patient;
import org.openmrs.AllergyReaction;
import org.openmrs.Concept;
import org.openmrs.api.PatientService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.test.jupiter.BaseContextMockTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class AllergyValidatorTest extends BaseContextMockTest {
	
	private Allergy allergy;
	
	private Errors errors;

	@Mock
	private PatientService patientService;

	@Mock
	private MessageSourceService messageSourceService;
	
	@InjectMocks
	private AllergyValidator validator;
	
	@BeforeEach
	public void setUp() {
		allergy = new Allergy();
		errors = new BindException(allergy, "allergy");
	}
	
	private String otherNonCodedConceptUuid() {
		return "5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	}
	
	@Test
	public void validate_shouldFailForANullValue() { 
		
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validate(null, errors));
		assertThat(exception.getMessage(), is("Allergy should not be null"));
	}
	
	@Test
	public void validate_shouldFailIfPatientIsNull() {
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("patient"));
		assertThat(errors.getFieldError("patient").getCode(), is("allergyapi.patient.required"));
	}
	
	@Test
	public void validate_shouldFailIfAllergenIsNull() {
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("allergen"));
		assertThat(errors.getFieldError("allergen").getCode(), is("allergyapi.allergen.required"));
	}
	
	@Test
	public void validate_shouldFailIdAllergenTypeIsNull() {
		
		allergy.setAllergen(new Allergen());
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("allergen"));
		assertThat(errors.getFieldError("allergen").getCode(), is("allergyapi.allergenType.required"));
	}
	
	@Test
	public void validate_shouldFailIfCodedAndNonCodedAllergenAreNull() {
		
		Allergen allergen = new Allergen(AllergenType.DRUG, null, null);
		allergy.setAllergen(allergen);
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("allergen"));
		assertThat(errors.getFieldError("allergen").getCode(), is("allergyapi.allergen.codedOrNonCodedAllergen.required"));
	}
	
	@Test
	public void validate_shouldFailIfNonCodedAllergenIsNullAndAllergenIsSetToOtherNonCoded(@Mock Concept concept, @Mock Allergen allergen) {
		
		when(allergen.getAllergenType()).thenReturn(AllergenType.DRUG);
		when(allergen.getCodedAllergen()).thenReturn(concept);
		when(allergen.getNonCodedAllergen()).thenReturn("");
		when(allergen.isCoded()).thenReturn(false);
		allergy.setAllergen(allergen);
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("allergen"));
		assertThat(errors.getFieldError("allergen").getCode(), is("allergyapi.allergen.nonCodedAllergen.required"));
	}
	
	@Test
	public void validate_shouldRejectADuplicateAllergen(@Mock Concept aspirin) {

		when(aspirin.getUuid()).thenReturn("some uuid");
		Allergen allergen1 = new Allergen(AllergenType.DRUG, aspirin, null);
		Allergies allergies = new Allergies();
		allergies.add(new Allergy(null, allergen1, null, null, null));
		when(patientService.getAllergies(any(Patient.class))).thenReturn(allergies);
		
		Allergen duplicateAllergen = new Allergen(AllergenType.FOOD, aspirin, null);
		Allergy allergy = new Allergy(mock(Patient.class), duplicateAllergen, null, null, null);
		Errors errors = new BindException(allergy, "allergy");
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("allergen"));
		assertThat(errors.getFieldError("allergen").getCode(), is("allergyapi.message.duplicateAllergen"));
	}
	
	@Test
	public void validate_shouldRejectADuplicateNonCodedAllergen(@Mock Concept nonCodedConcept) {

		when(nonCodedConcept.getUuid()).thenReturn(otherNonCodedConceptUuid());
		Allergies allergies = new Allergies();
		final String freeText = "some text";
		Allergen allergen1 = new Allergen(AllergenType.DRUG, nonCodedConcept, freeText);
		allergies.add(new Allergy(null, allergen1, null, null, null));
		when(patientService.getAllergies(any(Patient.class))).thenReturn(allergies);
		
		Allergen duplicateAllergen = new Allergen(AllergenType.FOOD, nonCodedConcept, freeText);
		Allergy allergy = new Allergy(mock(Patient.class), duplicateAllergen, null, null, null);
		Errors errors = new BindException(allergy, "allergy");
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("allergen"));
		assertThat(errors.getFieldError("allergen").getCode(), is("allergyapi.message.duplicateAllergen"));
	}
	
	@Test
	public void validate_shouldPassForAValidAllergy() {
		
		Allergies allergies = new Allergies();
		Concept aspirin = new Concept();
		Allergen allergen1 = new Allergen(AllergenType.DRUG, aspirin, null);
		allergies.add(new Allergy(null, allergen1, null, null, null));
		when(patientService.getAllergies(any(Patient.class))).thenReturn(allergies);
		
		Allergen anotherAllergen = new Allergen(AllergenType.DRUG, new Concept(), null);
		Allergy allergy = new Allergy(mock(Patient.class), anotherAllergen, null, null, null);
		Errors errors = new BindException(allergy, "allergy");
		
		validator.validate(allergy, errors);
		
		assertFalse(errors.hasErrors());
	}

	@Test
	public void validate_shouldRejectNumericReactionValue() {
		Allergy allergy = new Allergy();
		AllergyReaction reaction = new AllergyReaction();
		String nonCoded = "45";
		reaction.setReactionNonCoded(nonCoded);
		allergy.addReaction(reaction);
		Errors errors = new BindException(allergy, "allergy");
		validator.validate(allergy, errors);
		assertTrue(errors.hasErrors());
	}
}
