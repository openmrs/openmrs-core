package org.openmrs.api.db.hibernate;

import org.openmrs.BaseTest;
import org.openmrs.api.context.Context;
import org.openmrs.notification.AlertService;

public class HibernateAlertDAOTest extends BaseTest {

	public void testClass() throws Exception {
		startup();
		
		
		Context.authenticate("ben", "");

		try {
			AlertService as = Context.getAlertService();

			System.out.println(as.getAlerts());
		}  finally {
			shutdown();
		}
		
	}

}
