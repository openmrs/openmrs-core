/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
}
