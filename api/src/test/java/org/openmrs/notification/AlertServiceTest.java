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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.notification.impl.AlertServiceImpl;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;

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
	 * 
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
		assertTrue(alertOne.getText().equals(Context.getMessageSourceService().getMessage("Module.startupError.notification.message", new Object[] { "test" }, null)));
	}

	@Test
	public void getAllAlerts_shouldOnlyBeAllowedForUsersWithTheGetAlertsPrivilege() {
		// the default authenticated user is a super user and may read every user's alerts
		assertNotNull(Context.getAlertService().getAllAlerts());
		assertNotNull(Context.getAlertService().getAllAlerts(true));

		// "butch" is a Provider with no privileges, so he must not be able to read all alerts
		Context.becomeUser("3-4");
		assertThrows(APIAuthenticationException.class, () -> Context.getAlertService().getAllAlerts());
		assertThrows(APIAuthenticationException.class, () -> Context.getAlertService().getAllAlerts(true));
	}

	@Test
	public void getAllAlerts_shouldBeAllowedForAnUnprivilegedCallerHoldingAProxyGetAlertsPrivilege() {
		// mirrors how the AlertReminderTask scheduled job reads every user's alerts: it runs as its
		// configured (possibly unprivileged) user and grants itself a proxy Get Alerts privilege
		Context.becomeUser("3-4");
		Context.addProxyPrivilege(PrivilegeConstants.GET_ALERTS);
		try {
			assertNotNull(Context.getAlertService().getAllAlerts(false));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_ALERTS);
		}
	}

	@Test
	public void getAlert_shouldReadOwnAlertButReturnNullForAnotherUsersAlertWithoutTheGetAlertsPrivilege() {
		AlertService as = Context.getAlertService();
		User superUser = Context.getAuthenticatedUser();
		User butch = Context.getUserService().getUserByUsername("butch");

		// create one alert addressed to butch and one addressed only to the super user
		Integer butchAlertId = as.saveAlert(new Alert("butch's alert", butch)).getAlertId();
		Integer superUsersAlertId = as.saveAlert(new Alert("super user's alert", superUser)).getAlertId();

		Context.becomeUser("3-4");

		// butch is a recipient of his own alert, so he may still read it
		assertNotNull(as.getAlert(butchAlertId));
		// an alert addressed to someone else reads back as null - identical to a non-existent id - so the
		// method cannot be used to probe which alert ids exist (no existence oracle over sequential ids)
		assertNull(as.getAlert(superUsersAlertId));
		assertNull(as.getAlert(112233445));
	}

	@Test
	public void getAlert_shouldReadAnotherUsersAlertForACallerHoldingAProxyGetAlertsPrivilege() {
		AlertService as = Context.getAlertService();
		Integer superUsersAlertId = as.saveAlert(new Alert("super user's alert", Context.getAuthenticatedUser()))
		        .getAlertId();

		Context.becomeUser("3-4");
		Context.addProxyPrivilege(PrivilegeConstants.GET_ALERTS);
		try {
			assertNotNull(as.getAlert(superUsersAlertId));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_ALERTS);
		}
	}

	@Test
	public void getAlerts_shouldReadOwnAlertsButNotAnotherUsersAlertsWithoutTheGetAlertsPrivilege() {
		AlertService as = Context.getAlertService();
		User superUser = Context.getAuthenticatedUser();

		Context.becomeUser("3-4");
		User butch = Context.getAuthenticatedUser();

		// butch may list his own alerts through any of the user-scoped overloads...
		assertNotNull(as.getAlerts(butch, true, true));
		assertNotNull(as.getAlertsByUser(butch));
		assertNotNull(as.getAllActiveAlerts(butch));
		// ...but not another user's
		assertThrows(APIAuthenticationException.class, () -> as.getAlerts(superUser, true, true));
		assertThrows(APIAuthenticationException.class, () -> as.getAlertsByUser(superUser));
		assertThrows(APIAuthenticationException.class, () -> as.getAllActiveAlerts(superUser));
	}

	@Test
	public void getAlerts_shouldReadAnotherUsersAlertsForACallerHoldingAProxyGetAlertsPrivilege() {
		AlertService as = Context.getAlertService();
		User superUser = Context.getAuthenticatedUser();

		Context.becomeUser("3-4");
		Context.addProxyPrivilege(PrivilegeConstants.GET_ALERTS);
		try {
			assertNotNull(as.getAlerts(superUser, true, true));
		} finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_ALERTS);
		}
	}
}
