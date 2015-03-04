/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.notification.impl.AlertServiceImpl;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

public class AlertServiceTest extends BaseContextSensitiveTest {
	
	@Test
	@Verifies(value = "should add an alert with message of length equals Text Max Length", method = "notifySuperUsers(String,Exception,null)")
	public void notifySuperUsers_shouldAddAnAlertWithMessageOfLengthEqualsTextMaxLength() {
		Context.getAlertService().notifySuperUsers("Module.startupError.notification.message", new Exception(), "test");
		
		Alert lastAlert = Context.getAlertService().getAlertsByUser(null).iterator().next();
		
		Assert.assertEquals(Alert.TEXT_MAX_LENGTH, lastAlert.getText().length());
	}
	
	@Test
	@Verifies(value = "should add an alert with message text if cause is null", method = "notifySuperUsers(String,Exception,null)")
	public void notifySuperUsers_shouldAddAnAlertWithMessageTextIfCauseIsNull() {
		
		Context.getAlertService().notifySuperUsers("Module.startupError.notification.message", null, "test");
		
		Alert lastAlert = Context.getAlertService().getAlertsByUser(null).iterator().next();
		
		String expectedText = Context.getMessageSourceService().getMessage("Module.startupError.notification.message",
		    new Object[] { "test" }, Context.getLocale());
		
		Assert.assertEquals(expectedText, lastAlert.getText());
	}
	
	/**
	 * @see {@link AlertService#notifySuperUsers(String,Exception,null)}
	 * 
	 */
	@Test
	@Verifies(value = "should add an alert to the database", method = "notifySuperUsers(String,Exception,null)")
	public void notifySuperUsers_shouldAddAnAlertToTheDatabase() throws Exception {
		// Check there are no alerts before the method is called
		Assert.assertEquals(0, Context.getAlertService().getAlertsByUser(null).size());
		
		//Call the method to be tested
		AlertServiceImpl alert = new AlertServiceImpl();
		alert.notifySuperUsers("Module.startupError.notification.message", null, "test");
		
		// Check that there is exactly one alert after the message is called
		Assert.assertEquals(1, Context.getAlertService().getAlertsByUser(null).size());
		
		// Set alertOne to be that one alert
		Alert alertOne = Context.getAlertService().getAlertsByUser(null).iterator().next();
		
		//Test that alert contains the expected content
		Assert.assertTrue(alertOne.getText().equals("Module.startupError.notification.message"));
	}
}
