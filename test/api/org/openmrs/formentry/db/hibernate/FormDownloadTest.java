package org.openmrs.formentry.db.hibernate;

import org.openmrs.BaseTest;
import org.openmrs.api.context.Context;
import org.openmrs.notification.AlertService;

public class FormDownloadTest extends BaseTest {

	public void testClass() throws Exception {
		
		startup();
		
		Context.authenticate("ben", "");
		
		AlertService as = Context.getAlertService();

		shutdown();
		
	}

}
