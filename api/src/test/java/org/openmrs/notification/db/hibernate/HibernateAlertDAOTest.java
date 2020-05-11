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

import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertService;
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
	public void getAlert_shouldReturnSavedAlert() throws Exception {
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
	public void deleteAlert_shouldReturnNullAfterDeleting() {
    	Alert alert = createAlert();
		Context.getAlertService().saveAlert(alert);
		hibernateAlertDAO.deleteAlert(alert);
		Assert.assertNull(Context.getAlertService().getAlert(1234));

	}
	@Test
	public void getAlerts_shouldReturnAlertsWhenUserIsPassedIn() {
		User user = new User(1);
		Alert alert=new Alert();
		alert.setText("Testing get Alerts Method");
		alert.addRecipient(user);
		Context.getAlertService().saveAlert(alert);
		List<Alert> alerts = hibernateAlertDAO.getAlerts(user, true, true);
		Assert.assertEquals(alerts.size(),1);

	}
	@Test
	public void getAllAllerts_shouldreturnAllAlerts() {
		Alert alert=new Alert();
		alert.setText("Testing the Hibernate get All Alerts Method");
		Context.getAlertService().saveAlert(alert);
        List<Alert>alerts=hibernateAlertDAO.getAllAlerts(false);
	    Assert.assertEquals(alerts.size(), 1);
	    
	}
	
}
