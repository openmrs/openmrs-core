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
import org.junit.Test;
import org.openmrs.notification.Alert;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link AlertValidator} class.
 *
 */
public class AlertValidatorTest extends BaseContextSensitiveTest {
	
	private AlertValidator validator;
	
	private Alert alert;
	
	private Errors errors;
	
	@Before
	public void setUp() {
		validator = new AlertValidator();
		
		alert = new Alert();
		
		errors = new BindException(alert, "alert");
	}
	
	@Test
	public void shouldFailValidationIfAlertTextIsNull() {
		
		validator.validate(alert, errors);
		
		assertThatFieldTextHasError();
	}

	@Test
	public void shouldFailValidationIfAlertTextIsEmpty() {
		
		alert.setText("");
		
		validator.validate(alert, errors);
		
		assertThatFieldTextHasError();
	}
	
	@Test
	public void shouldFailValidationIfAlertTextIsOnlyWhitespaces() {
		
		alert.setText(" ");
		
		validator.validate(alert, errors);
		
		assertThatFieldTextHasError();
	}
	
	@Test
	public void validate_shouldPassValidationIfAllRequiredValuesAreSet() {
		
		alert.setText("Alert Text");
		
		validator.validate(alert, errors);
		
		Assert.assertFalse(errors.hasErrors());
	}
	
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		
		alert
		        .setText("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		validator.validate(alert, errors);
		
		assertThatFieldTextHasError();
	}
	
	private void assertThatFieldTextHasError() {
		Assert.assertTrue(errors.hasFieldErrors("text"));
	}
}
