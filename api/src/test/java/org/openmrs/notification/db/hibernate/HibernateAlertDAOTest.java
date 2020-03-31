/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.db.hibernate;

import org.junit.Assert;
import org.junit.Before;

import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * This class tests the hibernate alert data access. TODO Consider changing this and all subsequent
 * tests to use dbunit
 */
public class HibernateAlertDAOTest extends BaseContextSensitiveTest {
	private HibernateAlertDAO hibernateAlertDAO;
	
	@Before
	public void runBeforeEachTest() {
		Context.openSession();
		authenticate();
		hibernateAlertDAO = new HibernateAlertDAO();
		hibernateAlertDAO.setSessionFactory((SessionFactory) applicationContext.getBean("sessionFactory"));
	}
	
	/**
	 * Creates and Returns alert with Id 1234
	 * 
	 * @return Alert object
	 */

	private Alert createAlert() {
		Alert alert = new Alert(1234);
		alert.setText("Testing the hibernate DAO");
		return alert;

	}

	/**
	 * Test that you can get alerts
	 * 
	 * @throws Exception
	 */
	@Test
	public void getAlert_shouldReturnAlert() throws Exception {

		Alert alert = createAlert();
		Context.getAlertService().saveAlert(alert);
		Alert savedAlert = Context.getAlertService().getAlert(1234);
		Assert.assertEquals(savedAlert.getText(), "Testing the hibernate DAO");

	}

	@Test
	public void saveAlert_shouldReturnNotNull() {

		Alert alert = createAlert();
		Context.getAlertService().saveAlert(alert);
		Alert savedAlert = Context.getAlertService().getAlert(1234);
		Assert.assertNotNull(savedAlert.getAlertId());

	}

	@Test

	public void deleteAlert_shouldReturnNull() {

		Alert alert = createAlert();
		Context.getAlertService().saveAlert(alert);
		hibernateAlertDAO.deleteAlert(alert);
		Assert.assertNull(Context.getAlertService().getAlert(1234));

	}

	@Test

	public void getAllAllerts_shouldReturnNotNull() {

		List<Alert> allAllerts = hibernateAlertDAO.getAllAlerts(true);
		Assert.assertNotNull(allAllerts);

	}

	@Test

	public void getAlerts_shouldReturnNotNull() {
		User user = new User(1);
		List<Alert> alerts = hibernateAlertDAO.getAlerts(user, true, true);
		Assert.assertNotNull(alerts);

	}
	
}
