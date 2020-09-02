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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.openmrs.ConceptClass;
import org.openmrs.OrderType;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains tests methods for the {@link OrderTypeValidator}
 */
public class OrderTypeValidatorTest extends BaseContextSensitiveTest {
	
	@Autowired
	private OrderService orderService;
	
	
	/**
	 * @see OrderTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfTheOrderTypeObjectIsNull() {
		Errors errors = new BindException(new OrderType(), "orderType");
		assertThrows(IllegalArgumentException.class, () -> new OrderTypeValidator().validate(null, errors));
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfNameIsNull() {
		OrderType orderType = new OrderType();
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfNameIsEmpty() {
		OrderType orderType = new OrderType();
		orderType.setName("");
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfNameIsWhitespace() {
		OrderType orderType = new OrderType();
		orderType.setName("");
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfNameIsWhiteSpace() {
		OrderType orderType = new OrderType();
		orderType.setName(" ");
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfNameIsADuplicate() {
		OrderType orderType = new OrderType();
		orderType.setName(orderService.getOrderType(1).getName());
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		assertTrue(errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfConceptClassIsADuplicate() {
		OrderType orderType = new OrderType();
		orderType.setName("concept class test");
		OrderType existing = orderService.getOrderType(2);
		assertEquals(1, existing.getConceptClasses().size());
		orderType.addConceptClass(existing.getConceptClasses().iterator().next());
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		assertTrue(errors.hasFieldErrors("conceptClasses[0]"));
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfParentIsAmongItsDescendants() {
		OrderType orderType = orderService.getOrderType(2);
		OrderType descendant = orderService.getOrderType(9);
		assertTrue(descendant.getParent().getParent().equals(orderType));
		orderType.setParent(descendant);
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		assertTrue(errors.hasFieldErrors("parent"));
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfParentIsAlsoADirectChild() {
		OrderType orderType = orderService.getOrderType(8);
		OrderType descendant = orderService.getOrderType(12);
		assertTrue(descendant.getParent().equals(orderType));
		orderType.setParent(descendant);
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		assertTrue(errors.hasFieldErrors("parent"));
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassIfAllFieldsAreCorrectForANewOrderType() {
		OrderType orderType = new OrderType();
		orderType.setName("unique name");
		orderType.setJavaClassName("org.openmrs.TestDrugOrder");
		Collection<ConceptClass> col = new HashSet<>();
		col.add(Context.getConceptService().getConceptClass(2));
		orderType.setConceptClasses(col);
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassIfAllFieldsAreCorrectForAnExistingOrderType() {
		OrderType orderType = orderService.getOrderType(1);
		assertNotNull(orderType);
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldBeInvokedWhenAnOrderTypeIsSaved() {
		OrderType orderType = orderService.getOrderType(1);
		orderType.setName(null);
		String expectedMsg = "'" + orderType + "' failed to validate with reason: name: " + Context.getMessageSourceService().getMessage("error.name");
		APIException exception = assertThrows(APIException.class, () -> orderService.saveOrderType(orderType));
		assertThat(exception.getMessage(), is(expectedMsg));
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		OrderType orderType = new OrderType();
		orderType.setName("unique name");
		orderType.setJavaClassName("org.openmrs.TestDrugOrder");
		Collection<ConceptClass> col = new HashSet<>();
		col.add(Context.getConceptService().getConceptClass(2));
		orderType.setConceptClasses(col);
		
		orderType.setDescription("description");
		orderType.setRetireReason("retireReason");
		
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see OrderTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		OrderType orderType = new OrderType();
		orderType
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		orderType
		        .setJavaClassName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		Collection<ConceptClass> col = new HashSet<>();
		col.add(Context.getConceptService().getConceptClass(2));
		orderType.setConceptClasses(col);
		
		orderType
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		orderType
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		
		assertTrue(errors.hasFieldErrors("name"));
		assertTrue(errors.hasFieldErrors("javaClassName"));
		assertTrue(errors.hasFieldErrors("description"));
		assertTrue(errors.hasFieldErrors("retireReason"));
	}
}
