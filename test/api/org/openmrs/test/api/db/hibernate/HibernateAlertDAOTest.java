/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.test.api.db.hibernate;

import org.openmrs.api.context.Context;
import org.openmrs.notification.AlertService;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * This class tests the hibernate alert data access.  
 * 
 * TODO Consider changing this and all subsequent tests to use dbunit
 */
public class HibernateAlertDAOTest extends BaseContextSensitiveTest {
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		authenticate();
	}

	/**
	 * Test that you can get alerts
	 * 
	 * @throws Exception
	 */
	public void testGetAlerts() throws Exception {
		
		AlertService as = Context.getAlertService();
		System.out.println(as.getAlerts());
		
	}

}
