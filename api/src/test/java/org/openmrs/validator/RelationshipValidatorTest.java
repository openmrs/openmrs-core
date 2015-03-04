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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

/**
 * Tests methods on the {@link RelationshipValidator} class.
 */
public class RelationshipValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see {@link RelationshipValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "Should fail if start date is after end date", method = "validate(Relationship)")
	public void validate_shouldFailIfEndDateIsBeforeStartDate() throws Exception {
		Relationship relationship = new Relationship(1);
		relationship.setStartDate(Context.getDateFormat().parse("18/02/2012"));
		relationship.setEndDate(Context.getDateFormat().parse("18/02/2001"));
		Map<String, String> map = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		new RelationshipValidator().validate(relationship, errors);
		Assert.assertEquals(true, errors.hasErrors());
	}
	
	/**
	 * @see {@link RelationshipValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "Should pass if end date is after the start date", method = "validate(Relationship)")
	public void validate_shouldPassIfEndDateIsAfterStartDate() throws Exception {
		Relationship relationship = new Relationship(1);
		relationship.setStartDate(Context.getDateFormat().parse("18/02/2012"));
		relationship.setEndDate(Context.getDateFormat().parse("18/03/2012"));
		Map<String, String> map = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		new RelationshipValidator().validate(relationship, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link RelationshipValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should pass validation if field lengths are correct", method = "validate(Relationship)")
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		Relationship relationship = new Relationship(1);
		relationship.setVoidReason("voidReason");
		
		Map<String, String> map = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		new RelationshipValidator().validate(relationship, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link RelationshipValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "should fail validation if field lengths are not correct", method = "validate(Relationship)")
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		Relationship relationship = new Relationship(1);
		relationship
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(relationship, "relationship");
		new RelationshipValidator().validate(relationship, errors);
		Assert.assertEquals(true, errors.hasFieldErrors("voidReason"));
	}
	
	/**
	 * @see {@link RelationshipValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "Should fail if start date is in future", method = "validate(Object,Errors)")
	public void validate_shouldFailIfStartDateIsInFuture() throws Exception {
		Relationship relationship = new Relationship(1);
		Map<String, String> map = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		Date nextYear = cal.getTime();
		relationship.setStartDate(nextYear);
		new RelationshipValidator().validate(relationship, errors);
		Assert.assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see {@link RelationshipValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "Should pass if start date is not in future", method = "validate(Object,Errors)")
	public void validate_shouldPassIfStartDateIsNotInFuture() throws Exception {
		Relationship relationship = new Relationship(1);
		Map<String, String> map = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		Date lastYear = cal.getTime();
		relationship.setStartDate(lastYear);
		new RelationshipValidator().validate(relationship, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see {@link RelationshipValidator#validate(Object,Errors)}
	 */
	@Test
	@Verifies(value = "Should pass if start date is null since start date is optional while creating a relationship", method = "validate(Object,Errors)")
	public void validate_shouldPassIfStartDateIsEmpty() throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		Relationship relationship = new Relationship(1);
		relationship.setStartDate(null);
		new RelationshipValidator().validate(relationship, errors);
		Assert.assertFalse(errors.hasErrors());
	}
}
