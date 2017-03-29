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

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.OrderFrequency;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link OrderFrequencyValidator} class.
 */
public class OrderFrequencyValidatorTest extends BaseContextSensitiveTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * @see OrderFrequencyValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfOrderFrequencyIsNull() {
		Errors errors = new BindException(new OrderFrequency(), "orderFrequency");
		new OrderFrequencyValidator().validate(null, errors);
		
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see OrderFrequencyValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfConceptIsNull() {
		OrderFrequency orderFrequency = new OrderFrequency();
		
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("concept"));
	}
	
	/**
	 * @see OrderFrequencyValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheConceptIsNotOfClassFrequency() {
		OrderFrequency orderFrequency = new OrderFrequency();
		orderFrequency.setConcept(Context.getConceptService().getConcept(88));
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("concept"));
	}
	
	/**
	 * @see OrderFrequencyValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfConceptIsUsedByAnotherFrequency() {
		OrderFrequency orderFrequency = new OrderFrequency();
		orderFrequency.setConcept(Context.getConceptService().getConcept(113));
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("concept"));
	}
	
	/**
	 * @see OrderFrequencyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassForAValidNewOrderFrequency() {
		ConceptService cs = Context.getConceptService();
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("new name", Context.getLocale());
		concept.setDatatype(cs.getConceptDatatype(1));
		concept.setConceptClass(cs.getConceptClass(19));
		concept.addName(cn);
		concept.addDescription(new ConceptDescription("some description",null));
		cs.saveConcept(concept);
		
		OrderFrequency orderFrequency = new OrderFrequency();
		orderFrequency.setConcept(concept);
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see OrderFrequencyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassForAValidExistingOrderFrequency() {
		OrderFrequency orderFrequency = Context.getOrderService().getOrderFrequency(1);
		assertNotNull(orderFrequency);
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see OrderFrequencyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldBeInvokedWhenAnOrderFrequencyIsSaved() {
		OrderFrequency orderFrequency = Context.getOrderService().getOrderFrequency(2);
		orderFrequency.setConcept(null);
		expectedException.expect(APIException.class);
		String expectedMsg = "'" + orderFrequency + "' failed to validate with reason: concept: " + Context.getMessageSourceService().getMessage("Concept.noConceptSelected");
		expectedException.expectMessage(expectedMsg);
		Context.getOrderService().saveOrderFrequency(orderFrequency);
	}
	
	/**
	 * @see OrderFrequencyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		ConceptService cs = Context.getConceptService();
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("new name", Context.getLocale());
		concept.setDatatype(cs.getConceptDatatype(1));
		concept.setConceptClass(cs.getConceptClass(19));
		concept.addName(cn);
		concept.addDescription(new ConceptDescription("some description",null));
		cs.saveConcept(concept);
		
		OrderFrequency orderFrequency = new OrderFrequency();
		orderFrequency.setConcept(concept);
		
		orderFrequency.setRetireReason("retireReason");
		
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see OrderFrequencyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		ConceptService cs = Context.getConceptService();
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("new name", Context.getLocale());
		concept.setDatatype(cs.getConceptDatatype(1));
		concept.setConceptClass(cs.getConceptClass(19));
		concept.addName(cn);
		concept.addDescription(new ConceptDescription("some description",null));
		cs.saveConcept(concept);
		
		OrderFrequency orderFrequency = new OrderFrequency();
		orderFrequency.setConcept(concept);
		
		orderFrequency
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
