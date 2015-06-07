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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.ConceptClass;
import org.openmrs.OrderType;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Contains tests methods for the {@link OrderTypeValidator}
 */
public class OrderTypeValidatorTest extends BaseContextSensitiveTest {
	
	@Autowired
	private OrderService orderService;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test(expected = IllegalArgumentException.class)
	@Verifies(value = "should fail if the orderType object is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfTheOrderTypeObjectIsNull() throws Exception {
		Errors errors = new BindException(new OrderType(), "orderType");
		new OrderTypeValidator().validate(null, errors);
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if name is null", method = "validate(Object,Errors)")
	public void validate_shouldFailIfNameIsNull() throws Exception {
		OrderType orderType = new OrderType();
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if name is empty", method = "validate(Object,Errors)")
	public void validate_shouldFailIfNameIsEmpty() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName("");
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @verifies fail if name is whitespace
	 * @see OrderTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfNameIsWhitespace() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName("");
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if name is white space", method = "validate(Object,Errors)")
	public void validate_shouldFailIfNameIsWhiteSpace() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName(" ");
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if name is a duplicate", method = "validate(Object,Errors)")
	public void validate_shouldFailIfNameIsADuplicate() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName(orderService.getOrderType(1).getName());
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if conceptClass is a duplicate", method = "validate(Object,Errors)")
	public void validate_shouldFailIfConceptClassIsADuplicate() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName("concept class test");
		OrderType existing = orderService.getOrderType(2);
		assertEquals(1, existing.getConceptClasses().size());
		orderType.addConceptClass(existing.getConceptClasses().iterator().next());
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("conceptClasses[0]"));
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if parent is among its descendants", method = "validate(Object,Errors)")
	public void validate_shouldFailIfParentIsAmongItsDescendants() throws Exception {
		OrderType orderType = orderService.getOrderType(2);
		OrderType descendant = orderService.getOrderType(9);
		Assert.assertTrue(descendant.getParent().getParent().equals(orderType));
		orderType.setParent(descendant);
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("parent"));
	}
	
	/**
	 * @see {@link OrderTypeValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail if parent is also a direct child", method = "validate(Object,Errors)")
	public void validate_shouldFailIfParentIsAlsoADirectChild() throws Exception {
		OrderType orderType = orderService.getOrderType(8);
		OrderType descendant = orderService.getOrderType(12);
		Assert.assertTrue(descendant.getParent().equals(orderType));
		orderType.setParent(descendant);
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("parent"));
	}
	
	/**
	 * @verifies pass if all fields are correct for a new order type
	 * @see OrderTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassIfAllFieldsAreCorrectForANewOrderType() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName("unique name");
		orderType.setJavaClassName("org.openmrs.TestDrugOrder");
		Collection<ConceptClass> col = new HashSet<ConceptClass>();
		col.add(Context.getConceptService().getConceptClass(2));
		orderType.setConceptClasses(col);
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @verifies pass if all fields are correct for an existing order type
	 * @see OrderTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassIfAllFieldsAreCorrectForAnExistingOrderType() throws Exception {
		OrderType orderType = orderService.getOrderType(1);
		assertNotNull(orderType);
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @verifies be invoked when an order type is saved
	 * @see OrderTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldBeInvokedWhenAnOrderTypeIsSaved() throws Exception {
		OrderType orderType = orderService.getOrderType(1);
		orderType.setName(null);
		expectedException.expect(APIException.class);
		String expectedMsg = "'" + orderType + "' failed to validate with reason: name: error.name";
		expectedException.expectMessage(expectedMsg);
		orderService.saveOrderType(orderType);
	}
	
	/**
	 * @verifies pass validation if field lengths are correct
	 * @see OrderTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		OrderType orderType = new OrderType();
		orderType.setName("unique name");
		orderType.setJavaClassName("org.openmrs.TestDrugOrder");
		Collection<ConceptClass> col = new HashSet<ConceptClass>();
		col.add(Context.getConceptService().getConceptClass(2));
		orderType.setConceptClasses(col);
		
		orderType.setDescription("description");
		orderType.setRetireReason("retireReason");
		
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @verifies fail validation if field lengths are not correct
	 * @see OrderTypeValidator#validate(Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		OrderType orderType = new OrderType();
		orderType
		        .setName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		orderType
		        .setJavaClassName("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		Collection<ConceptClass> col = new HashSet<ConceptClass>();
		col.add(Context.getConceptService().getConceptClass(2));
		orderType.setConceptClasses(col);
		
		orderType
		        .setDescription("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		orderType
		        .setRetireReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(orderType, "orderType");
		new OrderTypeValidator().validate(orderType, errors);
		
		Assert.assertEquals(true, errors.hasFieldErrors("name"));
		Assert.assertEquals(true, errors.hasFieldErrors("javaClassName"));
		Assert.assertEquals(true, errors.hasFieldErrors("description"));
		Assert.assertEquals(true, errors.hasFieldErrors("retireReason"));
	}
}
