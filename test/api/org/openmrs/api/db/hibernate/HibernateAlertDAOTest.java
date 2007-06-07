package org.openmrs.api.db.hibernate;

import org.openmrs.BaseTest;
import org.openmrs.api.context.Context;
import org.openmrs.notification.AlertService;

public class HibernateAlertDAOTest extends BaseTest {
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		authenticate();
	}

	public void testClass() throws Exception {
		
		Context.openSession();
		
		AlertService as = Context.getAlertService();
		System.out.println(as.getAlerts());
		
		Context.closeSession();
	}

}
