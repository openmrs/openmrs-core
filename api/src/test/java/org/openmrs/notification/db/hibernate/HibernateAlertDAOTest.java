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
 * This class tests the hibernate alert data access. TODO Consider changing this and all subsequent
 * tests to use dbunit
 */
public class HibernateAlertDAOTest extends BaseContextSensitiveTest {

	private static final String DATA_XML = "org/openmrs/api/db/hibernate/include/HibernateAlertDAOTestDataSet.xml";

	@Autowired
	private HibernateAlertDAO hibernateAlertDAO;
	
	private volatile boolean didUpdateExpirationDate = false;

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

	@Test
	public void saveAlert_shouldSaveAlertToDb() {
		Alert alert = new Alert();
		alert.setText("Coding time");
		alert.setId(5);
		hibernateAlertDAO.saveAlert(alert);

		Assertions.assertNotNull(hibernateAlertDAO.getAlert(5));
	}

	@Test
	public void getAlert_shouldGetAlertById() {
		Alert savedAlert = hibernateAlertDAO.getAlert(2);
		Assertions.assertEquals((int) savedAlert.getAlertId(), 2);
	}

	@Test
	public void deleteAlert_shouldReturnNullAfterDeleting() {
		Alert savedAlert = hibernateAlertDAO.getAlert(2);
		Assertions.assertNotNull(savedAlert);
		hibernateAlertDAO.deleteAlert(savedAlert);
		Assertions.assertNull(hibernateAlertDAO.getAlert(2));
	}

	@Test
	public void getAllAlerts_shouldReturnOnlyNonExpiredAllerts() {
		Assertions.assertEquals(hibernateAlertDAO.getAllAlerts(false).size(), 1);
	}

	@Test
	public void getAlerts_shouldReturnAllAlertsWhenUserIsSpecified() {
		User user = Context.getUserService().getUserByUuid("c1d8f5c2-e131-11de-babe-001e378eb77e");
		Assertions.assertEquals(hibernateAlertDAO.getAlerts(user, true, false).size(), 1);
	}

}
