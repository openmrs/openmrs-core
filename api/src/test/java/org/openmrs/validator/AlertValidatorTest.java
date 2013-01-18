/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.validator;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.notification.Alert;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link AlertValidator} class.
 *
 */
public class AlertValidatorTest {
	
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
}
