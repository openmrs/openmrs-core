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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Allergen;
import org.openmrs.AllergenType;
import org.openmrs.Allergies;
import org.openmrs.Allergy;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class AllergyValidatorTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	private Allergy allergy;
	
	private Errors errors;
	
	@InjectMocks
	private AllergyValidator validator;
	
	@Mock
	private PatientService ps;
	
	@Before
	public void setUp() {
		allergy = new Allergy();
		errors = new BindException(allergy, "allergy");
	}
	
	private Concept createMockConcept() {
		return createMockConcept(null);
	}
	
	private Concept createMockConcept(String uuid) {
		Concept concept = mock(Concept.class);
		when(concept.getUuid()).thenReturn(uuid != null ? uuid : "some uuid");
		when(concept.getName()).thenReturn(new ConceptName());
		return concept;
	}
	
	private String getOtherNonCodedConceptUuid() {
		return "5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	}
	
	/**
	 * @see AllergyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailForANullValue() {
		
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Allergy should not be null");
		validator.validate(null, errors);
	}
	
	/**
	 * @see AllergyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfPatientIsNull() {
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("patient"));
		assertThat(errors.getFieldError("patient").getCode(), is("allergyapi.patient.required"));
	}
	
	/**
	 * @see AllergyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfAllergenIsNull() {
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("allergen"));
		assertThat(errors.getFieldError("allergen").getCode(), is("allergyapi.allergen.required"));
	}
	
	/**
	 * @see AllergyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIdAllergenTypeIsNull() {
		
		allergy.setAllergen(new Allergen());
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("allergen"));
		assertThat(errors.getFieldError("allergen").getCode(), is("allergyapi.allergenType.required"));
	}
	
	/**
	 * @see AllergyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfCodedAndNonCodedAllergenAreNull() {
		
		Allergen allergen = new Allergen(AllergenType.DRUG, null, null);
		allergy.setAllergen(allergen);
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("allergen"));
		assertThat(errors.getFieldError("allergen").getCode(), is("allergyapi.allergen.codedOrNonCodedAllergen.required"));
	}
	
	/**
	 * @see AllergyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfNonCodedAllergenIsNullAndAllergenIsSetToOtherNonCoded() {
		
		Allergen allergen = mock(Allergen.class);
		when(allergen.getAllergenType()).thenReturn(AllergenType.DRUG);
		Concept concept = createMockConcept(getOtherNonCodedConceptUuid());
		when(allergen.getCodedAllergen()).thenReturn(concept);
		when(allergen.getNonCodedAllergen()).thenReturn("");
		when(allergen.isCoded()).thenReturn(false);
		allergy.setAllergen(allergen);
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("allergen"));
		assertThat(errors.getFieldError("allergen").getCode(), is("allergyapi.allergen.nonCodedAllergen.required"));
	}
	
	/**
	 * @see AllergyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldRejectADuplicateAllergen() {
		
		PowerMockito.mockStatic(Context.class);
		MessageSourceService ms = mock(MessageSourceService.class);
		when(Context.getMessageSourceService()).thenReturn(ms);
		
		Allergies allergies = new Allergies();
		Concept aspirin = createMockConcept();
		Allergen allergen1 = new Allergen(AllergenType.DRUG, aspirin, null);
		allergies.add(new Allergy(null, allergen1, null, null, null));
		when(ps.getAllergies(any(Patient.class))).thenReturn(allergies);
		
		Allergen duplicateAllergen = new Allergen(AllergenType.FOOD, aspirin, null);
		Allergy allergy = new Allergy(mock(Patient.class), duplicateAllergen, null, null, null);
		Errors errors = new BindException(allergy, "allergy");
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("allergen"));
		assertThat(errors.getFieldError("allergen").getCode(), is("allergyapi.message.duplicateAllergen"));
	}
	
	/**
	 * @see AllergyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldRejectADuplicateNonCodedAllergen() {
		
		PowerMockito.mockStatic(Context.class);
		MessageSourceService ms = mock(MessageSourceService.class);
		when(Context.getMessageSourceService()).thenReturn(ms);
		
		Allergies allergies = new Allergies();
		Concept nonCodedConcept = createMockConcept(getOtherNonCodedConceptUuid());
		final String freeText = "some text";
		Allergen allergen1 = new Allergen(AllergenType.DRUG, nonCodedConcept, freeText);
		allergies.add(new Allergy(null, allergen1, null, null, null));
		when(ps.getAllergies(any(Patient.class))).thenReturn(allergies);
		
		Allergen duplicateAllergen = new Allergen(AllergenType.FOOD, nonCodedConcept, freeText);
		Allergy allergy = new Allergy(mock(Patient.class), duplicateAllergen, null, null, null);
		Errors errors = new BindException(allergy, "allergy");
		
		validator.validate(allergy, errors);
		
		assertTrue(errors.hasFieldErrors("allergen"));
		assertThat(errors.getFieldError("allergen").getCode(), is("allergyapi.message.duplicateAllergen"));
	}
	
	/**
	 * @see AllergyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassForAValidAllergy() {
		
		Allergies allergies = new Allergies();
		Concept aspirin = new Concept();
		Allergen allergen1 = new Allergen(AllergenType.DRUG, aspirin, null);
		allergies.add(new Allergy(null, allergen1, null, null, null));
		when(ps.getAllergies(any(Patient.class))).thenReturn(allergies);
		
		Allergen anotherAllergen = new Allergen(AllergenType.DRUG, new Concept(), null);
		Allergy allergy = new Allergy(mock(Patient.class), anotherAllergen, null, null, null);
		Errors errors = new BindException(allergy, "allergy");
		
		validator.validate(allergy, errors);
		
		assertFalse(errors.hasErrors());
	}
}
