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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

/**
 * Tests methods on the {@link RelationshipValidator} class.
 */
public class RelationshipValidatorTest extends BaseContextSensitiveTest {
	
	/**
	 * @throws ParseException
	 * @see RelationshipValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfEndDateIsBeforeStartDate() throws ParseException {
		Relationship relationship = new Relationship(1);
		relationship.setStartDate(Context.getDateFormat().parse("18/02/2012"));
		relationship.setEndDate(Context.getDateFormat().parse("18/02/2001"));
		Map<String, String> map = new HashMap<>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		new RelationshipValidator().validate(relationship, errors);
		assertTrue(errors.hasErrors());
	}
	
	/**
	 * @throws ParseException
	 * @see RelationshipValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfEndDateIsAfterStartDate() throws ParseException {
		Relationship relationship = new Relationship(1);
		relationship.setStartDate(Context.getDateFormat().parse("18/02/2012"));
		relationship.setEndDate(Context.getDateFormat().parse("18/03/2012"));
		Map<String, String> map = new HashMap<>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		new RelationshipValidator().validate(relationship, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see RelationshipValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Relationship relationship = new Relationship(1);
		relationship.setVoidReason("voidReason");
		
		Map<String, String> map = new HashMap<>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		new RelationshipValidator().validate(relationship, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see RelationshipValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		Relationship relationship = new Relationship(1);
		relationship
		        .setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(relationship, "relationship");
		new RelationshipValidator().validate(relationship, errors);
		assertTrue(errors.hasFieldErrors("voidReason"));
	}
	
	/**
	 * @see RelationshipValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldFailIfStartDateIsInFuture() {
		Relationship relationship = new Relationship(1);
		Map<String, String> map = new HashMap<>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		Date nextYear = cal.getTime();
		relationship.setStartDate(nextYear);
		new RelationshipValidator().validate(relationship, errors);
		assertTrue(errors.hasErrors());
	}
	
	/**
	 * @see RelationshipValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfStartDateIsNotInFuture() {
		Relationship relationship = new Relationship(1);
		Map<String, String> map = new HashMap<>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		Date lastYear = cal.getTime();
		relationship.setStartDate(lastYear);
		new RelationshipValidator().validate(relationship, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see RelationshipValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfStartDateIsEmpty() {
		Map<String, String> map = new HashMap<>();
		MapBindingResult errors = new MapBindingResult(map, Relationship.class.getName());
		Relationship relationship = new Relationship(1);
		relationship.setStartDate(null);
		new RelationshipValidator().validate(relationship, errors);
		assertFalse(errors.hasErrors());
	}
}
