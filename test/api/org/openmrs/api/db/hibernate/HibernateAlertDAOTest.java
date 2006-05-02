package org.openmrs.api.db.hibernate;

import java.io.FileInputStream;
import java.util.Properties;

import junit.framework.TestCase;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;
import org.openmrs.notification.AlertService;

public class HibernateAlertDAOTest extends TestCase {

	public void testClass() throws Exception {
		String filepath = System.getenv("OPENMRS_RUNTIME_PROPERTIES_FILE");
		
		FileInputStream propertyStream = new FileInputStream(filepath);
		
		Properties props = new Properties();
		props.load(propertyStream);

		propertyStream.close();
		
		// TODO Generify
		HibernateUtil.startup(props);

		Context context = ContextFactory.getContext();
		
		context.authenticate("ben", "");

		try {
			AlertService as = context.getAlertService();

			System.out.println(as.getAlerts());
		}  finally {
			HibernateUtil.shutdown();
		}
		
	}

}
