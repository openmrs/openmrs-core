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
	
	/**
	 * @see AlertValidator#validate(Object,Errors)
	 * @verifies fail validation if Alert Text is null or empty or whitespace
	 */
	@Test
	public void validate_shouldFailValidationIfAlertTextIsNullOrEmptyOrWhitespace() throws Exception {
		Alert alert = new Alert();
		Assert.assertNull(alert.getText());
		
		Errors errors = new BindException(alert, "alert");
		new AlertValidator().validate(alert, errors);
		Assert.assertTrue(errors.hasFieldErrors("text"));
		
		alert.setText("");
		errors = new BindException(alert, "alert");
		new AlertValidator().validate(alert, errors);
		Assert.assertTrue(errors.hasFieldErrors("text"));
		
		alert.setText(" ");
		errors = new BindException(alert, "alert");
		new AlertValidator().validate(alert, errors);
		Assert.assertTrue(errors.hasFieldErrors("text"));
	}
	
	/**
	 * Test for all the values being set
	 * @see AlertValidator#validate(Object,Errors)
	 * @verifies pass validation if all required values are set
	 */
	@Test
	public void validate_shouldPassValidationIfAllRequiredValuesAreSet() throws Exception {
		Alert alert = new Alert();
		alert.setText("Alert Text");
		
		Errors errors = new BindException(alert, "alert");
		new AlertValidator().validate(alert, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * Test for all the values being set
	 * @see AlertValidator#validate(Object,Errors)
	 * @verifies pass validation if field lengths are correct
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() throws Exception {
		Alert alert = new Alert();
		alert.setText("text");
		
		Errors errors = new BindException(alert, "alert");
		new AlertValidator().validate(alert, errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	/**
	 * Test for all the values being set
	 * @see AlertValidator#validate(Object,Errors)
	 * @verifies fail validation if field lengths are not correct
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() throws Exception {
		Alert alert = new Alert();
		alert
		        .setText("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(alert, "alert");
		new AlertValidator().validate(alert, errors);
		Assert.assertTrue(errors.hasFieldErrors("text"));
	}
}
