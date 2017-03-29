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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.notification.AlertService;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * This class tests the hibernate alert data access. TODO Consider changing this and all subsequent
 * tests to use dbunit
 */
public class HibernateAlertDAOTest extends BaseContextSensitiveTest {
	
	@Before
	public void runBeforeEachTest() {
		authenticate();
	}
	
	/**
	 * Test that you can get alerts
	 * 
	 * @throws Exception
	 */
	@Test
	public void shouldGetAlerts() {
		
		AlertService as = Context.getAlertService();
		//System.out.println(as.getAllAlerts());
		
	}
	
}
