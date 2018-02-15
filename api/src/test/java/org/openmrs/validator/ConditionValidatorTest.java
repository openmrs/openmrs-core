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
import org.openmrs.Cohort;
import org.openmrs.Condition;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

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
	public void shouldPassIfConditionClassIsPassed(){
		validator.validate(new Condition(), errors);

		Assert.assertFalse(errors.hasErrors());
		Assert.assertFalse(errors.hasFieldErrors("condition"));
	}
}
