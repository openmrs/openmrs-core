/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.test.notification;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertService;
import org.openmrs.test.testutil.BaseContextSensitiveTest;

/**
 * TODO add more tests to cover the methods in <code>AlertService</code>
 */
public class AlertServiceTest extends BaseContextSensitiveTest {
	
	protected static final String XML_FILENAME = "org/openmrs/test/notification/include/AlertServiceTest.xml";
	
	/**
	 * Set up the database with the initial dataset before every test method
	 * in this class.
	 * 
	 * Require authorization before every test method in this class
	 */
	@Before
	public void runBeforeEachTest() throws Exception {
		// create the basic user and give it full rights
		initializeInMemoryDatabase();
		
		executeDataSet(XML_FILENAME);
		
		// authenticate to the temp database
		authenticate();
	}

	/**
	 * Test that we can create/update an alert
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldSaveBasicAlert() throws Exception {
		AlertService alertService = Context.getAlertService();
		
		Alert alert = new Alert("asdf", new User(1));
		
		Alert alertSaved = alertService.saveAlert(alert);
		
		assertEquals(alertSaved, alert);
		
	}
	
	/**
	 * Test add by role -- similar to how the alert form works
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldAlertRecipientsByRole() throws Exception {
		AlertService alertService = Context.getAlertService();
		UserService userService = Context.getUserService();
		
		Alert alert = new Alert("asdf", new User(1));
		
		List<User> users = userService.getUsersByRole(new Role("Some Role"));
		assertEquals(1, users.size());
		for (User user : users)
			alert.addRecipient(user);
		
		// there should only be one recip because user#1 has role "Some Role"
		assertEquals(1, alert.getRecipients().size());
		
		Alert alertSaved = alertService.saveAlert(alert);
		
		assertEquals(alertSaved, alert);
		
	}
	
}
