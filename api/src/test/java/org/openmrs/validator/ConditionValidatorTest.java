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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Cohort;
import org.openmrs.ConceptName;
import org.openmrs.Condition;
import org.openmrs.Concept;
import org.openmrs.ConditionClinicalStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.util.Locale;
import java.util.Date;

/**
 * Class to implement tests for {@link ConditionValidator}
 */
public class ConditionValidatorTest {

	private static final String NULL_ERROR_MESSAGE = "The object parameter should not be null";
	private static final String INCOMPATIBLE_ERROR_MESSAGE = "The object parameter should be of type " + Condition.class;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private ConditionValidator validator;

	private Condition condition;

	private Errors errors;

	@Before
	public void setUp(){
		validator = new ConditionValidator();
		condition = new Condition();
		errors = new BindException(condition, "condition");
	}

	@Test
	public void shouldFailIfGivenNull(){
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(NULL_ERROR_MESSAGE);
		validator.validate(null, errors);
	}

	@Test
	public void shouldFailIfGivenInstanceOfClassOtherThanCondition(){
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(INCOMPATIBLE_ERROR_MESSAGE);
		validator.validate(new Cohort(), errors);
	}
	
	@Test
	public void shouldFailIfGivenConditionWithNullConditionProperties(){
		Condition condition = new Condition();
		validator.validate(condition, errors);
		Assert.assertTrue(errors.hasFieldErrors("condition"));
		Assert.assertTrue(errors.hasFieldErrors("clinicalStatus"));
	}

	@Test
	public void shouldPassIfConditionClassIsPassedWithRequiredConditionProperties(){
		Condition condition = new Condition();
		condition.setCondition(new CodedOrFreeText(new Concept(), new ConceptName("name", new Locale("en")), "nonCoded"));
		condition.setClinicalStatus(ConditionClinicalStatus.ACTIVE);
		validator.validate(condition, errors);
		Assert.assertFalse(errors.hasFieldErrors("condition"));
		Assert.assertFalse(errors.hasFieldErrors("clinicalStatus"));
	}

	@Test
	public void shouldPassWhenOnsetDateIsBeforeEndDate(){
		Condition condition = new Condition();
		condition.setCondition(new CodedOrFreeText(new Concept(), new ConceptName("name", new Locale("en")), "nonCoded"));
		condition.setOnsetDate(new Date(1562591017000L));
		condition.setEndDate(new Date(1562936617000L));
		condition.setClinicalStatus(ConditionClinicalStatus.INACTIVE);
		validator.validate(condition,errors);
		Assert.assertFalse(errors.hasFieldErrors("onsetDate"));
	}

	@Test
	public void shouldFailForInvalidOnsetDate(){
		Condition condition1 = new Condition();
		condition1.setCondition(new CodedOrFreeText(new Concept(), new ConceptName("name", new Locale("en")), "nonCoded"));
		condition1.setEndDate(new Date(1562591017000L));
		condition1.setOnsetDate(new Date(1562936617000L));
		condition1.setClinicalStatus(ConditionClinicalStatus.INACTIVE);
		validator.validate(condition1,errors);
		Assert.assertTrue(errors.hasFieldErrors("onsetDate"));
	
	}
}
