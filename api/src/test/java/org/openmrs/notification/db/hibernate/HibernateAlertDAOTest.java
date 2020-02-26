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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertService;
import org.openmrs.notification.db.AlertDAO;
import org.openmrs.test.BaseContextSensitiveTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class tests the hibernate alert data access. TODO Consider changing this
 * and all subsequent tests to use dbunit
 */
public class HibernateAlertDAOTest extends BaseContextSensitiveTest {

	private static final Logger log = LoggerFactory.getLogger(HibernateAlertDAOTest.class);
	private HibernateAlertDAO hibernateAlertDAO;
	private Object getAllAlerts;
	private Object alertId;

	@Before
	public void runBeforeEachTest() {

		if (hibernateAlertDAO == null)
			hibernateAlertDAO = (HibernateAlertDAO) applicationContext.getBean("HibernateAlertDao");
		authenticate();
	}

	/**
	 * Test that you can get alerts
	 * 
	 * @throws Exception
	 */
	// @Test
	// public void shouldGetAlerts() {

	// }
    @Test
	public void getAllAlerts_shouldReturnTrueIfRequested() {
		AlertService alertService = Context.getAlertService();
		if (getAllAlerts != null) {
			Assert.assertEquals(" ", alertService.getAllAlerts(), alertService.getClass());
		}
	}
    @Test
	public void getAlert_shouldGetAlertById(){
		assertEquals(1, AlertService.getAlertId();
	}
	@Test
	public void saveAlert_shouldReturnSavedAlertObjectInDatabase() {
		Session session = null;
		Transaction transaction =null;
		  try {
		      session = HibernateUtil.getSessionFactory().openSession();
		      transaction = session.beginTransaction();
		      transaction.begin();
		      Alert alert = new Alert();
		      alert.setAlertsByRole("Alert role meesage",alertRole.getMessage());
		      alert.setAlertByUuid(12223, alertService.getAlertByUuid);
		      alert.setAlertByText("",alertService.getText());
		      session.save(alert);
		      Assert.assertEquals(alert.setAlertByRole("alert role message"),alert.getRole());
		      Assert.assertEquals(alert.setAlertByUuid(12344),alertService.getAlertUuid());
		      Assert.assertEquals(alert.setAlertByText(""),alertService.getText());
		      Assert.assertEquals(alertService.getSavedAlerts().list(),alertService.getClass());
		      session.commit();
		      
	} catch (Exception e) {
	      e.printStackTrace();
	    } finally {
	      if (session != null) {
	        session.close();
	      }
	    }

	    HibernateUtil.shutdown();
	    
	 }
	 @Test
	public void deleteAlert_shouldDeleteSavedAlertsFromDatabaseIfRequested() {
	 	Object alertDAO;
		AlertService.setAlertDAO(alertDAO);
	    Alert alert = new Alert((int) 1L);
	    Alert alert = new Alert((int) 2L);
	    when(((Object) alertDAO).returnAlert(1L)).thenReturnAlert(2L);// expect a fetch, return return a fetched alert
		 ((Object) alertDAO).deleteFromAlert(Alert.class).deleteAllData();
		 Assert.assertEquals(alert.getAlertId(), alert.getDeletedAlert());
		 AlertService.deleteAlert(2L);// deleting second alert
		 Assert.assertEquals(AlertService.getDeletedAlert(), alert.deleteAllData());

	}
	
}
