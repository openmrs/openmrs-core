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
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.notification.Alert;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class tests the hibernate alert data access. TODO Consider changing this and all subsequent
 * tests to use dbunit
 */
public class HibernateAlertDAOTest extends BaseContextSensitiveTest {
	
	private static final String DATA_XML = "org/openmrs/api/db/hibernate/include/HibernateAlertDAOTestDataSet.xml";
	
	@Autowired
	private HibernateAlertDAO hibernateAlertDAO;
	
	@Before
	public void setUp() {
		executeDataSet(DATA_XML);
	}

	@Test
	public void saveAlert_shouldSaveAlertToDb() {
		Alert saveAlert = hibernateAlertDAO.getAlert(2);
	    hibernateAlertDAO.saveAlert(saveAlert);
	    Assert.assertNotNull(saveAlert.getText());
	}
	
	@Test
	public void getAlert_shouldGetAlertById() {
		Alert savedAlert = hibernateAlertDAO.getAlert(2);
		int id=savedAlert.getAlertId();
		Assert.assertEquals(id,2);
		
	}
	
	@Test
	public void deleteAlert_shouldReturnNullAfterDeleting() {
		Alert savedAlert = hibernateAlertDAO.getAlert(2);
		Assert.assertNotNull(savedAlert);
		hibernateAlertDAO.deleteAlert(savedAlert);
		Assert.assertNull(hibernateAlertDAO.getAlert(2));
	}
	
	@Test
	public void getAllAlerts_shouldReturnOnlyNonExpiredAllerts() {
		Assert.assertEquals(hibernateAlertDAO.getAllAlerts(false).size(), 1);
	}
	
	@Test
	public void getAlerts_shouldReturnAllAlertsWhenUserIsSpecified() {
		User user = Context.getUserService().getUserByUuid("c1d8f5c2-e131-11de-babe-001e378eb77e");
		Assert.assertEquals(hibernateAlertDAO.getAlerts(user, true, false).size(),1);
	}

}
