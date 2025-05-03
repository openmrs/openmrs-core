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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ObsValidator} class.
 */
public class ObsValidatorTest extends BaseContextSensitiveTest {
	
	@Autowired
	private ObsValidator obsValidator;

	Calendar calendar = Calendar.getInstance();
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonIdIsNull() {
		Person person = new Person();
		
		Obs obs = new Obs();
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		obs.setPerson(person);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertTrue(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertFalse(errors.hasFieldErrors("valueNumeric"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfObsDatetimeIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setValueNumeric(1.0);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertTrue(errors.hasFieldErrors("obsDatetime"));
		assertFalse(errors.hasFieldErrors("valueNumeric"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfParentObshasValues() {
		
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(18));
		
		obs.setValueBoolean(false);
		obs.setValueCoded(Context.getConceptService().getConcept(18));
		obs.setValueComplex("test");
		obs.setValueDatetime(new Date());
		obs.setValueDrug(Context.getConceptService().getDrug(3));
		obs.setValueGroupId(getLoadCount());
		obs.setValueModifier("test");
		obs.setValueNumeric(1212.0);
		obs.setValueText("test");
		
		Set<Obs> group = new HashSet<>();
		group.add(Context.getObsService().getObs(7));
		group.add(Context.getObsService().getObs(9));
		obs.setGroupMembers(group);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertTrue(errors.hasFieldErrors("valueCoded"));
		assertTrue(errors.hasFieldErrors("valueDrug"));
		assertTrue(errors.hasFieldErrors("valueDatetime"));
		assertTrue(errors.hasFieldErrors("valueNumeric"));
		assertTrue(errors.hasFieldErrors("valueModifier"));
		assertTrue(errors.hasFieldErrors("valueText"));
		assertTrue(errors.hasFieldErrors("valueBoolean"));
		assertTrue(errors.hasFieldErrors("valueComplex"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfObsHasNoValuesAndNotParent() {
		
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(18));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertTrue(errors.getGlobalErrorCount() > 0);
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertTrue(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertFalse(errors.hasFieldErrors("valueNumeric"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptDatatypeIsBooleanAndValueBooleanIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(18));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertTrue(errors.hasFieldErrors("valueBoolean"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptDatatypeIsCodedAndValueCodedIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(4));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertTrue(errors.hasFieldErrors("valueCoded"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptDatatypeIsDateAndValueDatetimeIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(20));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertTrue(errors.hasFieldErrors("valueDatetime"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptDatatypeIsNumericAndValueNumericIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertTrue(errors.hasFieldErrors("valueNumeric"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptDatatypeIsTextAndValueTextIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(19));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertTrue(errors.hasFieldErrors("valueText"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfObsAncestorsContainsObs() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(3)); // datatype = N/A
		obs.setObsDatetime(new Date());
		
		Set<Obs> group = new HashSet<>();
		group.add(obs);
		obs.setGroupMembers(group);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertTrue(errors.hasFieldErrors("groupMembers"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllValuesPresent() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfValueTextIsGreaterThanTheMaximumLength() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(19));
		obs.setObsDatetime(new Date());
		
		// Generate 65535+ characters length text.
		StringBuilder valueText = new StringBuilder();
		for (int i = 0; i < 730; i++) {
			valueText
					.append("This text should not exceed 65535 characters. Below code will generate a text more than 65535");
		}
		
		obs.setValueText(valueText.toString());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertTrue(errors.hasFieldErrors("valueText"));
	}
	
	/**
	 * @see ObsValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldRejectAnInvalidConceptAndDrugCombination() {
		Obs obs = new Obs();
		obs.setPerson(new Person(7));
		obs.setObsDatetime(new Date());
		Concept questionConcept = new Concept(100);
		ConceptDatatype dt = new ConceptDatatype(1);
		dt.setUuid(ConceptDatatype.CODED_UUID);
		questionConcept.setDatatype(dt);
		obs.setConcept(questionConcept);
		obs.setValueCoded(new Concept(101));
		
		Drug drug = new Drug();
		drug.setConcept(new Concept(102));
		obs.setValueDrug(drug);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		assertTrue(errors.hasFieldErrors("valueDrug"));
	}
	
	/**
	 * @see ObsValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfAnswerConceptAndConceptOfValueDrugMatch() {
		Obs obs = new Obs();
		obs.setPerson(new Person(7));
		obs.setObsDatetime(new Date());
		Concept questionConcept = new Concept(100);
		ConceptDatatype dt = new ConceptDatatype(1);
		dt.setUuid(ConceptDatatype.CODED_UUID);
		questionConcept.setDatatype(dt);
		obs.setConcept(questionConcept);
		Concept answerConcept = new Concept(101);
		obs.setValueCoded(answerConcept);
		
		Drug drug = new Drug();
		drug.setConcept(answerConcept);
		obs.setValueDrug(drug);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		obs.setAccessionNumber("AccessionNumber");
		obs.setValueModifier("m");
		obs.setValueComplex("ValueComplex");
		obs.setVoidReason("VoidReason");
		obs.setComment("comment");
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		obs.setAccessionNumber("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		obs.setValueModifier("too long text");
		obs.setValueComplex(StringUtils.repeat("a", 1001));
		obs.setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		obs.setComment("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertTrue(errors.hasFieldErrors("accessionNumber"));
		assertTrue(errors.hasFieldErrors("valueModifier"));
		assertTrue(errors.hasFieldErrors("valueComplex"));
		assertTrue(errors.hasFieldErrors("comment"));
		assertTrue(errors.hasFieldErrors("voidReason"));
	}
	
	/**
	 * @see ObsValidator#supports(Class)
	 */
	@Test
	public void supports_shouldSupportObsClass() {
		assertTrue(obsValidator.supports(Obs.class));
		assertFalse(obsValidator.supports(Concept.class));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldNotValidateIfObsIsVoided() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(null);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		assertTrue(errors.hasFieldErrors("valueNumeric"));
		
		obs.setVoided(true);
		errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		assertFalse(errors.hasErrors());
		
	}
	
	/**
	 * @see ObsValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldNotValidateAVoidedChildObs() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		Obs validChild = new Obs();
		validChild.setPerson(Context.getPersonService().getPerson(2));
		validChild.setConcept(Context.getConceptService().getConcept(5089));
		validChild.setObsDatetime(new Date());
		validChild.setValueNumeric(80.0);
		obs.addGroupMember(validChild);
		Obs inValidChild = new Obs();
		obs.addGroupMember(inValidChild);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		assertTrue(errors.hasErrors());
		
		inValidChild.setVoided(true);
		errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ObsValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailForANullObject() {
		APIException exception = assertThrows(APIException.class, () -> obsValidator.validate(null, null));
		assertThat(exception.getMessage(), is(CoreMatchers.equalTo("Obs can't be null"))); 
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfValueTextIsLessThanTheMaximumLength() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(19));
		obs.setObsDatetime(new Date());

		// Generate 2700+ characters length text.
		StringBuilder valueText = new StringBuilder();
		for (int i = 0; i < 30; i++) {
			valueText
					.append("This text should not exceed 65535 characters. Below code will generate a text Less than 65535");
		}

		obs.setValueText(valueText.toString());

		Errors errors = new BindException(obs, "obs");
		new ObsValidator().validate(obs, errors);

		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertFalse(errors.hasFieldErrors("valueText"));
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfObsValueExceedsHiAbsolute() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -10);
		Person person = new Person(10);
		person.setBirthdate(calendar.getTime());
		
		Obs obs = new Obs();
		obs.setId(1);
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(4090));
		obs.setValueNumeric(201.0);
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertTrue(errors.hasFieldErrors("valueNumeric"));
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfObsValueBelowLowAbsolute() {
		Obs obs = getObs(10, 4089, 50.0);

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertTrue(errors.hasFieldErrors("valueNumeric"));
	}
	
	@Test
	public void getReferenceRange_shouldReturnCorrectRangeWhenHiAbsoluteAndLowAbsoluteAreNull() {
		Obs obs = getObs(10, 4091, 50.0);

		ConceptReferenceRange referenceRange = obsValidator.getReferenceRange(obs);
		
		assertNotNull(referenceRange.getHiCritical());
		assertNotNull(referenceRange.getHiNormal());
		assertNotNull(referenceRange.getLowCritical());
		assertNotNull(referenceRange.getLowNormal());
		assertNull(referenceRange.getHiAbsolute());
		assertNull(referenceRange.getLowAbsolute());
		assertNotNull(referenceRange.getConceptNumeric());
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfObsValueWithinRange() {
		Obs obs = new Obs();
		Person person = new Person(10);
		calendar.add(Calendar.YEAR, -10);
		
		person.setBirthdate(calendar.getTime());
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(4090));
		obs.setValueNumeric(80.0);
		obs.setObsDatetime(new Date());

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertFalse(errors.hasErrors());
		assertNotNull(obs.getReferenceRange());
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void shouldSetObsReferenceRangeIfCriteriaMatches() {
		Person person = new Person(1);
		calendar.add(Calendar.YEAR, -6);
		person.setBirthdate(calendar.getTime());
		
		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(4090));
		obs.setValueNumeric(88.0); 
		obs.setObsDatetime(new Date());

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertFalse(errors.hasErrors());
		assertNotNull(obs.getReferenceRange());
		assertEquals(140.0, obs.getReferenceRange().getHiAbsolute());
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void shouldSetObsReferenceRangeValuesIfConceptReferenceRangeIsNullAndConceptNumericIsNotNull() {
		Person person = new Person(1);
		calendar.add(Calendar.YEAR, -600);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(4090));
		obs.setValueNumeric(88.0);
		obs.setObsDatetime(new Date());

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertFalse(errors.hasErrors());
		assertNotNull(obs.getReferenceRange());
		assertEquals(145.0, obs.getReferenceRange().getHiAbsolute());
		assertEquals(70.0, obs.getReferenceRange().getLowAbsolute());
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void shouldNotSetObsReferenceRangeValueIfConceptIsNotFound() {
		Person person = new Person(1);
		calendar.add(Calendar.YEAR, -6);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(409000));
		obs.setValueNumeric(8.0);
		obs.setObsDatetime(new Date());

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertTrue(errors.hasErrors());
		assertNull(obs.getReferenceRange());
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 * 
	 * Fetching conceptReferenceRanges by concept id (4090) returns 2 items with absolute value bounds;- 80-150 and 60-140
	 */
	@Test
	public void shouldSetErrorIfObsValueIsHigherThanStrictHigherBound() {
		Obs obs = getObs(10, 4090, 145.0);

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("valueNumeric"));
		assertEquals("error.value.outOfRange.high", errors.getAllErrors().get(0).getCode());
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 *
	 * Existing Obs associated with concept id 5089 are more than 1. In this test, we want to make sure we are getting right obs by person.
	 * With the right obs, the criteria should evaluate to true, then validate ranges. 
	 * The valid range in accordance to conceptReferenceRange = 60-140
	 */
	@Test
	public void shouldPickTheRightObsGivenTwoObsWithTheSameConcept() {
		Person person = new Person(1);
		person.setGender("M");

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setValueNumeric(145.0);
		obs.setObsDatetime(new Date());

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertTrue(errors.hasErrors());
		assertTrue(errors.hasFieldErrors("valueNumeric"));
		assertEquals("error.value.outOfRange.high", errors.getAllErrors().get(0).getCode());
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void shouldSetInterpretationToHighIfObsValueIsAboveHiNormalAndLessThanHighCritical() {
		Obs obs = getObs(60, 4090, 121.0);

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertFalse(errors.hasErrors());
		assertEquals(Obs.Interpretation.HIGH, obs.getInterpretation());
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void shouldSetInterpretationToCriticallyHighIfObsValueIsAboveHighCritical() {
		Obs obs = getObs(60, 4090, 131.0);

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertFalse(errors.hasErrors());
		assertEquals(Obs.Interpretation.CRITICALLY_HIGH, obs.getInterpretation());
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void shouldSetInterpretationToCriticallyHighIfObsValueIsEqualToHighCritical() {
		Obs obs = getObs(60, 4090, 130.0);

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertFalse(errors.hasErrors());
		assertEquals(Obs.Interpretation.CRITICALLY_HIGH, obs.getInterpretation());
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void shouldSetInterpretationToCriticallyLowIfObsValueIsEqualToLowCritical() {
		Obs obs = getObs(60, 4090, 75.0);

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertFalse(errors.hasErrors());
		assertEquals(Obs.Interpretation.CRITICALLY_LOW, obs.getInterpretation());
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void shouldSetInterpretationToNormalIfObsValueIsWithinNormalRange() {
		Obs obs = getObs(60, 4090, 100.0);

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertFalse(errors.hasErrors());
		assertEquals(Obs.Interpretation.NORMAL, obs.getInterpretation());
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void shouldSetInterpretationToCriticalLowIfObsValueIsBelowLowCritical() {
		Obs obs = getObs(60, 4090, 74.0);

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertFalse(errors.hasErrors());
		assertEquals(Obs.Interpretation.CRITICALLY_LOW, obs.getInterpretation());
	}

	private static Obs getObs(int numberOfYears, int conceptId, double valueNumeric) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -numberOfYears);

		Person person = new Person(10);
		person.setBirthdate(calendar.getTime());

		Obs obs = new Obs();
		obs.setPerson(person);
		obs.setConcept(Context.getConceptService().getConcept(conceptId));
		obs.setValueNumeric(valueNumeric);
		obs.setObsDatetime(new Date());
		return obs;
	}
}
