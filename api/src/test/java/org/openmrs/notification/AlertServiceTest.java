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

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.notification.impl.AlertServiceImpl;
import org.openmrs.scheduler.tasks.AlertReminderTask;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlertServiceTest extends BaseContextSensitiveTest {

	@Test
	public void notifySuperUsers_shouldAddAnAlertWithMessageOfLengthEqualsTextMaxLength() {
		Context.getAlertService().notifySuperUsers("Module.startupError.notification.message", new Exception(), "test");

		Alert lastAlert = Context.getAlertService().getAlertsByUser(null).iterator().next();

		assertEquals(Alert.TEXT_MAX_LENGTH, lastAlert.getText().length());
	}

	@Test
	public void notifySuperUsers_shouldAddAnAlertWithMessageTextIfCauseIsNull() {

		Context.getAlertService().notifySuperUsers("Module.startupError.notification.message", null, "test");

		Alert lastAlert = Context.getAlertService().getAlertsByUser(null).iterator().next();

		String expectedText = Context.getMessageSourceService().getMessage("Module.startupError.notification.message",
		    new Object[] { "test" }, Context.getLocale());

		assertEquals(expectedText, lastAlert.getText());
	}

	/**
	 * @see AlertService#notifySuperUsers(String,Exception,null)
	 */
	@Test
	public void notifySuperUsers_shouldAddAnAlertToTheDatabase() {
		// Check there are no alerts before the method is called
		assertEquals(0, Context.getAlertService().getAlertsByUser(null).size());

		//Call the method to be tested
		AlertServiceImpl alert = new AlertServiceImpl();
		alert.notifySuperUsers("Module.startupError.notification.message", null, "test");

		// Check that there is exactly one alert after the message is called
		assertEquals(1, Context.getAlertService().getAlertsByUser(null).size());

		// Set alertOne to be that one alert
		Alert alertOne = Context.getAlertService().getAlertsByUser(null).iterator().next();

		//Test that alert contains the expected content
		assertTrue(alertOne.getText().equals(Context.getMessageSourceService()
		        .getMessage("Module.startupError.notification.message", new Object[] { "test" }, null)));
	}

	/**
	 * @see AlertService#getAllAlerts(boolean, boolean)
	 */
	@Test
	public void getAllAlerts_shouldReturnOnlyUnreadAlertsWhenIncludeReadIsFalse() {

		Alert unread = new Alert();
		unread.setText("unread alert");
		unread.setAlertRead(false);
		unread.setDateToExpire(null);
		Context.getAlertService().saveAlert(unread);

		Alert read = new Alert();
		read.setText("read alert");
		read.setAlertRead(true);
		read.setDateToExpire(null);
		Context.getAlertService().saveAlert(read);

		List<Alert> alerts = Context.getAlertService().getAllAlerts(false, false);

		for (Alert a : alerts) {
			assertFalse(a.isAlertRead());
		}
	}

	/**
	 * @see AlertService#getAllAlerts(boolean, boolean)
	 */
	@Test
	public void getAllAlerts_shouldIncludeReadAlertsWhenIncludeReadIsTrue() {

		Alert read = new Alert();
		read.setText("read alert");
		read.setAlertRead(true);
		read.setDateToExpire(null);
		Context.getAlertService().saveAlert(read);

		List<Alert> alerts = Context.getAlertService().getAllAlerts(true, true);

		assertTrue(alerts.stream().anyMatch(Alert::isAlertRead));
	}

	/**
	 * @see AlertService#getAllAlerts(boolean, boolean)
	 */
	@Test
	public void getAllAlerts_shouldExcludeExpiredAlertsWhenIncludeExpiredIsFalse() {

		Alert expired = new Alert();
		expired.setText("expired alert");
		expired.setAlertRead(false);
		expired.setDateToExpire(new Date(System.currentTimeMillis() - 100000));
		Context.getAlertService().saveAlert(expired);

		List<Alert> alerts = Context.getAlertService().getAllAlerts(false, false);

		Date now = new Date();

		for (Alert a : alerts) {
			assertTrue(a.getDateToExpire() == null || a.getDateToExpire().after(now));
		}
	}

	@Test
	public void alertReminderTask_shouldExecuteWithoutErrors() {

		Alert alert = new Alert();
		alert.setText("test alert");
		alert.setAlertRead(false);
		alert.setDateToExpire(null);
		Context.getAlertService().saveAlert(alert);

		AlertReminderTask task = new AlertReminderTask();
		task.execute();

		assertTrue(true);
	}
}
