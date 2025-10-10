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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.notification.AlertRecipient;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Unit tests for the {@link HibernateAlertDAO} class.
 * <p>
 * This class validates the Alert persistence logic including saving, fetching,
 * deleting, and filtering based on expiration and user-specific alerts.
 * <p>
 * Note: This test uses the test dataset located at
 * {@code HibernateAlertDAOTestDataSet.xml}.
 */

public class HibernateAlertDAOTest extends BaseContextSensitiveTest {

	private static final String DATA_XML = "org/openmrs/api/db/hibernate/include/HibernateAlertDAOTestDataSet.xml";

	@Autowired
	private HibernateAlertDAO hibernateAlertDAO;
	
	private volatile boolean didUpdateExpirationDate = false;
    
	/**
	 * Loads test dataset and sets up initial conditions before each test.
	 * It ensures that alert with ID 2 has an updated expiration date
	 * so that it's considered active during the test run.
	 */
	@BeforeEach
	public void setUp() {
		executeDataSet(DATA_XML);
		
		if (!didUpdateExpirationDate) {
			Alert activeAlert = hibernateAlertDAO.getAlert(2);
			activeAlert.setDateToExpire(
				Date.from(
					LocalDate.now().plus(5, ChronoUnit.DAYS).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

			hibernateAlertDAO.saveAlert(activeAlert);
			didUpdateExpirationDate = true;
		}
	}


	/**
	 * Verifies that a new alert can be saved and retrieved from the database.
	 */
	@Test
	public void saveAlert_shouldSaveAlertToDb() {
		Alert alert = new Alert();
		alert.setText("Coding time");
		alert.setId(5);
		hibernateAlertDAO.saveAlert(alert);

		Assertions.assertNotNull(hibernateAlertDAO.getAlert(5));
	}
    

	/**
	 * Verifies that an alert can be fetched by its ID.
	 */
	@Test
	public void getAlert_shouldGetAlertById() {
		Alert savedAlert = hibernateAlertDAO.getAlert(2);
		Assertions.assertEquals((int) savedAlert.getAlertId(), 2);
	}
    
	/**
	 * Ensures that deleting an alert actually removes it from the database.
	 */
	@Test
	public void deleteAlert_shouldReturnNullAfterDeleting() {
		Alert savedAlert = hibernateAlertDAO.getAlert(2);
		Assertions.assertNotNull(savedAlert);
		hibernateAlertDAO.deleteAlert(savedAlert);
		Assertions.assertNull(hibernateAlertDAO.getAlert(2));
	}
    
	/**
	 * Verifies that only non-expired alerts are returned when filtering by expiration.
	 */
	@Test
	public void getAllAlerts_shouldReturnOnlyNonExpiredAllerts() {
		Assertions.assertEquals(hibernateAlertDAO.getAllAlerts(false).size(), 1);
	}
    

	/**
	 * Verifies that alerts for a specific user are returned correctly
	 * when filtering by recipient.
	 */
	@Test
	public void getAlerts_shouldReturnAllAlertsWhenUserIsSpecified() {
		User user = Context.getUserService().getUserByUuid("c1d8f5c2-e131-11de-babe-001e378eb77e");
		Assertions.assertEquals(hibernateAlertDAO.getAlerts(user, true, false).size(), 1);
	}

}
