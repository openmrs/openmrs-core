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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ObsValidator} class.
 */
public class ObsValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if personId is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfPersonIdIsNull() throws Exception {
		Obs obs = new Obs();
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		Errors errors = new BindException(obs, "obs");
		new ObsValidator().validate(obs, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("person"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("obsDatetime"));
		Assert.assertFalse(errors.hasFieldErrors("valueNumeric"));
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if obsDatetime is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfObsDatetimeIsNull() throws Exception {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setValueNumeric(1.0);
		
		Errors errors = new BindException(obs, "obs");
		new ObsValidator().validate(obs, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("person"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertTrue(errors.hasFieldErrors("obsDatetime"));
		Assert.assertFalse(errors.hasFieldErrors("valueNumeric"));
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
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
		new ObsValidator().validate(obs, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("person"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertTrue(errors.hasFieldErrors("valueCoded"));
		Assert.assertTrue(errors.hasFieldErrors("valueDrug"));
		Assert.assertTrue(errors.hasFieldErrors("valueDatetime"));
		Assert.assertTrue(errors.hasFieldErrors("valueNumeric"));
		Assert.assertTrue(errors.hasFieldErrors("valueModifier"));
		Assert.assertTrue(errors.hasFieldErrors("valueText"));
		Assert.assertTrue(errors.hasFieldErrors("valueBoolean"));
		Assert.assertTrue(errors.hasFieldErrors("valueComplex"));
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail if obs has no values and not parent", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailIfObsHasNoValuesAndNotParent() throws Exception {
		
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(18));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		new ObsValidator().validate(obs, errors);
		
		Assert.assertTrue(errors.getGlobalErrorCount() > 0);
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if concept is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfConceptIsNull() throws Exception {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		Errors errors = new BindException(obs, "obs");
		new ObsValidator().validate(obs, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("person"));
		Assert.assertTrue(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("obsDatetime"));
		Assert.assertFalse(errors.hasFieldErrors("valueNumeric"));
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if concept datatype is boolean and valueBoolean is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfConceptDatatypeIsBooleanAndValueBooleanIsNull() throws Exception {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(18));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		new ObsValidator().validate(obs, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("person"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("obsDatetime"));
		Assert.assertTrue(errors.hasFieldErrors("valueBoolean"));
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if concept datatype is coded and valueCoded is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfConceptDatatypeIsCodedAndValueCodedIsNull() throws Exception {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(4));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		new ObsValidator().validate(obs, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("person"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("obsDatetime"));
		Assert.assertTrue(errors.hasFieldErrors("valueCoded"));
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if concept datatype is date and valueDatetime is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfConceptDatatypeIsDateAndValueDatetimeIsNull() throws Exception {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(20));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		new ObsValidator().validate(obs, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("person"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("obsDatetime"));
		Assert.assertTrue(errors.hasFieldErrors("valueDatetime"));
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if concept datatype is numeric and valueNumeric is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfConceptDatatypeIsNumericAndValueNumericIsNull() throws Exception {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		new ObsValidator().validate(obs, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("person"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("obsDatetime"));
		Assert.assertTrue(errors.hasFieldErrors("valueNumeric"));
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if concept datatype is text and valueText is null", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfConceptDatatypeIsTextAndValueTextIsNull() throws Exception {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(19));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		new ObsValidator().validate(obs, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("valueText"));
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
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
		new ObsValidator().validate(obs, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("person"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("obsDatetime"));
		Assert.assertTrue(errors.hasFieldErrors("groupMembers"));
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
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
		new ObsValidator().validate(obs, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
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
		new ObsValidator().validate(obs, errors);
		
		Assert.assertFalse(errors.hasFieldErrors("person"));
		Assert.assertFalse(errors.hasFieldErrors("concept"));
		Assert.assertFalse(errors.hasFieldErrors("obsDatetime"));
		Assert.assertTrue(errors.hasFieldErrors("valueText"));
	}
	
	/**
	 * @see {@link ObsValidator#validate(Object,Errors)}
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
		new ObsValidator().validate(obs, errors);
		Assert.assertTrue(errors.hasFieldErrors("valueDrug"));
	}
	
	/**
	 * @see {@link ObsValidator#validate(Object,Errors)}
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
		new ObsValidator().validate(obs, errors);
		Assert.assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
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
		new ObsValidator().validate(obs, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(java.lang.Object, org.springframework.validation.Errors)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		obs
		        .setAccessionNumber("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		obs.setValueModifier("too long text");
		obs
		        .setValueComplex("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		obs
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		obs
		        .setComment("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(obs, "obs");
		new ObsValidator().validate(obs, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("accessionNumber"));
		Assert.assertTrue(errors.hasFieldErrors("valueModifier"));
		Assert.assertTrue(errors.hasFieldErrors("valueComplex"));
		Assert.assertTrue(errors.hasFieldErrors("comment"));
		Assert.assertTrue(errors.hasFieldErrors("voidReason"));
	}
}
