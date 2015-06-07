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

import static junit.framework.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.OrderFrequency;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link OrderFrequencyValidator} class.
 */
public class OrderFrequencyValidatorTest extends BaseContextSensitiveTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * @see {@link OrderFrequencyValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if orderFrequency is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfOrderFrequencyIsNull() throws Exception {
		Errors errors = new BindException(new OrderFrequency(), "orderFrequency");
		new OrderFrequencyValidator().validate(null, errors);
		
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see {@link OrderFrequencyValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if concept is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfConceptIsNull() throws Exception {
		OrderFrequency orderFrequency = new OrderFrequency();
		
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("concept"));
	}
	
	/**
	 * @see {@link OrderFrequencyValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if the concept is not of class frequency", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheConceptIsNotOfClassFrequency() throws Exception {
		OrderFrequency orderFrequency = new OrderFrequency();
		orderFrequency.setConcept(Context.getConceptService().getConcept(88));
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("concept"));
	}
	
	/**
	 * @see {@link OrderFrequencyValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if concept is used by another frequency", method = "validate(Object,Errors)")
	public void validate_shouldFailIfConceptIsUsedByAnotherFrequency() throws Exception {
		OrderFrequency orderFrequency = new OrderFrequency();
		orderFrequency.setConcept(Context.getConceptService().getConcept(113));
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertTrue(errors.hasFieldErrors("concept"));
	}
	
	/**
	 * @verifies pass for a valid new order frequency
	 * @see OrderFrequencyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassForAValidNewOrderFrequency() throws Exception {
		ConceptService cs = Context.getConceptService();
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("new name", Context.getLocale());
		concept.setConceptClass(cs.getConceptClass(19));
		concept.addName(cn);
		cs.saveConcept(concept);
		
		OrderFrequency orderFrequency = new OrderFrequency();
		orderFrequency.setConcept(concept);
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @verifies pass for a valid existing order frequency
	 * @see OrderFrequencyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassForAValidExistingOrderFrequency() throws Exception {
		OrderFrequency orderFrequency = Context.getOrderService().getOrderFrequency(1);
		assertNotNull(orderFrequency);
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @verifies be invoked when an order frequency is saved
	 * @see OrderFrequencyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldBeInvokedWhenAnOrderFrequencyIsSaved() throws Exception {
		OrderFrequency orderFrequency = Context.getOrderService().getOrderFrequency(2);
		orderFrequency.setConcept(null);
		expectedException.expect(APIException.class);
		String expectedMsg = "'" + orderFrequency + "' failed to validate with reason: concept: Concept.noConceptSelected";
		expectedException.expectMessage(expectedMsg);
		Context.getOrderService().saveOrderFrequency(orderFrequency);
	}
	
	/**
	 * @verifies pass validation if field lengths are correct
	 * @see OrderFrequencyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		ConceptService cs = Context.getConceptService();
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("new name", Context.getLocale());
		concept.setConceptClass(cs.getConceptClass(19));
		concept.addName(cn);
		cs.saveConcept(concept);
		
		OrderFrequency orderFrequency = new OrderFrequency();
		orderFrequency.setConcept(concept);
		
		orderFrequency.setRetireReason("retireReason");
		
		Errors errors = new BindException(orderFrequency, "orderFrequency");
		new OrderFrequencyValidator().validate(orderFrequency, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @verifies fail validation if field lengths are not correct
	 * @see OrderFrequencyValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		ConceptService cs = Context.getConceptService();
		Concept concept = new Concept();
		ConceptName cn = new ConceptName("new name", Context.getLocale());
		concept.setConceptClass(cs.getConceptClass(19));
		concept.addName(cn);
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
