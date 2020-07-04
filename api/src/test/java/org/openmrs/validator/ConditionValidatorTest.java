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
import static org.junit.Assert.assertThrows;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Class to implement tests for {@link ConditionValidator}
 */
public class ConditionValidatorTest {

	private static final String NULL_ERROR_MESSAGE = "The object parameter should not be null";
	private static final String INCOMPATIBLE_ERROR_MESSAGE = "The object parameter should be of type " + Condition.class;
	
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
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validate(null, errors));
		assertThat(exception.getMessage(), is(NULL_ERROR_MESSAGE));
	}

	@Test
	public void shouldFailIfGivenInstanceOfClassOtherThanCondition(){
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> validator.validate(new Cohort(), errors));
		assertThat(exception.getMessage(), is(INCOMPATIBLE_ERROR_MESSAGE));
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
}
