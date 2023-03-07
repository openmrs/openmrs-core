/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs2_0;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertRecipient;
import org.openmrs.notification.AlertService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests CRUD operations for {@link AlertRecipient}s via web service calls
 */
public class AlertRecipientController2_0Test extends MainResourceControllerTest {

	private AlertService service;

	private static final String NEW_ALERT_UUID = "78c97b6b-ef39-47a1-ad77-73494e078ecb";

	private static final String EMPTY_ALERT_UUID = "4876faba-7b14-41e6-b2ad-3050a1030cae";

	@Before
	public void setUp() {
		this.service = Context.getAlertService();

		User alertRecipient = Context.getUserService().getUserByUuid(RestTestConstants1_8.USER_UUID);
		AlertRecipient recipient = new AlertRecipient(alertRecipient, false);
		recipient.setUuid(getUuid());

		Alert activeAlert = new Alert();
		activeAlert.setText("New Alert");
		activeAlert.setUuid(NEW_ALERT_UUID);
		activeAlert.setSatisfiedByAny(true);
		activeAlert.setAlertRead(true);
		activeAlert.addRecipient(recipient);
		this.service.saveAlert(activeAlert);

		Alert emptyAlert = new Alert();
		emptyAlert.setText("Empty Alert");
		emptyAlert.setUuid(EMPTY_ALERT_UUID);
		this.service.saveAlert(emptyAlert);
	}

	@Override
	public String getURI() {
		return "alert/" + NEW_ALERT_UUID + "/recipient";
	}

	@Override
	public String getUuid() {
		return "735c2b72-1cc6-422f-b137-af8413427ed5";
	}

	@Override
	public long getAllCount() {
		return 1;
	}

	@Test
	public void shouldAddRecipientToAlert() throws Exception {
		Alert existingAlert = getAlertByUuid(EMPTY_ALERT_UUID);
		assertNotNull(existingAlert);
		assertNull(existingAlert.getRecipients());

		String json = "{\"recipient\": \"" + RestTestConstants1_8.USER_UUID + "\"}";
		handle(newPostRequest("alert/" + EMPTY_ALERT_UUID + "/recipient", json));

		existingAlert = getAlertByUuid(EMPTY_ALERT_UUID);
		assertNotNull(existingAlert);

		assertEquals(1, existingAlert.getRecipients().size());
	}

	@Test
	public void shouldPurgeRecipientFromAlert() throws Exception {
        Alert existingAlert = getAlertByUuid(NEW_ALERT_UUID);
        assertNotNull(existingAlert);

        assertEquals(1, existingAlert.getRecipients().size());

		handle(newDeleteRequest(getURI() + "/" + getUuid()));

		existingAlert = getAlertByUuid(NEW_ALERT_UUID);
		assertNotNull(existingAlert);

		assertEquals(0, existingAlert.getRecipients().size());
	}

	private Alert getAlertByUuid(String uuid) {
		for (Alert alert : service.getAllAlerts(true)) {
			if (alert.getUuid().equals(uuid))
				return alert;
		}
		return null;
	}
}
