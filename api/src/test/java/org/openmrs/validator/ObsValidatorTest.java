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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ObsValidator} class.
 */
public class ObsValidatorTest extends BaseContextSensitiveTest {
	
	@Autowired
	private ObsValidator obsValidator;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	@Verifies(value = "should fail validation if personId is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfPersonIdIsNull() throws Exception {
		Obs obs = new Obs();
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
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
	@Verifies(value = "should fail validation if obsDatetime is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfObsDatetimeIsNull() throws Exception {
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
	@Verifies(value = "should fail if the parent obs has values", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailIfParentObshasValues() throws Exception {
		
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
		
		Set<Obs> group = new HashSet<Obs>();
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
	@Verifies(value = "should fail if obs has no values and not parent", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailIfObsHasNoValuesAndNotParent() throws Exception {
		
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
	@Verifies(value = "should fail validation if concept is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfConceptIsNull() throws Exception {
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
	@Verifies(value = "should fail validation if concept datatype is boolean and valueBoolean is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfConceptDatatypeIsBooleanAndValueBooleanIsNull() throws Exception {
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
	@Verifies(value = "should fail validation if concept datatype is coded and valueCoded is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfConceptDatatypeIsCodedAndValueCodedIsNull() throws Exception {
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
	@Verifies(value = "should fail validation if concept datatype is date and valueDatetime is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfConceptDatatypeIsDateAndValueDatetimeIsNull() throws Exception {
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
	@Verifies(value = "should fail validation if concept datatype is numeric and valueNumeric is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfConceptDatatypeIsNumericAndValueNumericIsNull() throws Exception {
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
	@Verifies(value = "should fail validation if concept datatype is text and valueText is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfConceptDatatypeIsTextAndValueTextIsNull() throws Exception {
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
	@Verifies(value = "should fail validation if obs ancestors contains obs", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfObsAncestorsContainsObs() throws Exception {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(3)); // datatype = N/A
		obs.setObsDatetime(new Date());
		
		Set<Obs> group = new HashSet<Obs>();
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
	@Verifies(value = "should pass validation if all values present", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldPassValidationIfAllValuesPresent() throws Exception {
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
	@Verifies(value = "should fail validation if concept value for text datatype is greater than the maximum length", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfValueTextIsGreaterThanTheMaximumLength() throws Exception {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(19));
		obs.setObsDatetime(new Date());
		
		// Generate 1800+ characters length text.
		String valueText = "";
		for (int i = 0; i < 20; i++) {
			valueText = valueText
			        + "This text should not exceed 1000 characters. Below code will generate a text more than 1000";
		}
		
		obs.setValueText(valueText);
		
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
	@Verifies(value = "should reject an invalid concept and drug combination", method = "validate(Object,Errors)")
	public void validate_shouldRejectAnInvalidConceptAndDrugCombination() throws Exception {
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
	@Verifies(value = "should pass if answer concept and concept of value drug match", method = "validate(Object,Errors)")
	public void validate_shouldPassIfAnswerConceptAndConceptOfValueDrugMatch() throws Exception {
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
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
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
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		obs.setAccessionNumber("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		obs.setValueModifier("too long text");
		obs.setValueComplex("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
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
	 * @verifies support Obs class
	 * @see ObsValidator#supports(Class)
	 */
	@Test
	public void supports_shouldSupportObsClass() throws Exception {
		assertTrue(obsValidator.supports(Obs.class));
		assertFalse(obsValidator.supports(Concept.class));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	@Verifies(value = "should not validate if obs is voided", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldNotValidateIfObsIsVoided() throws Exception {
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
	@Verifies(value = "should not validate a voided child obs", method = "validate(Object, Errors)")
	public void validate_shouldNotValidateAVoidedChildObs() throws Exception {
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
	@Verifies(value = "should fail for a null object", method = "validate(Object, Errors)")
	public void validate_shouldFailForANullObject() throws Exception {
		expectedException.expect(APIException.class);
		expectedException.expectMessage(CoreMatchers.equalTo("Obs can't be null"));
		obsValidator.validate(null, null);
	}
}
