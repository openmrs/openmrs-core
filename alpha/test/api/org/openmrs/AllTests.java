package org.openmrs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.api.db.AdministrationServiceTest;
import org.openmrs.api.db.EncounterServiceTest;
import org.openmrs.api.db.FormServiceTest;
import org.openmrs.api.db.ObsServiceTest;
import org.openmrs.api.db.OrderServiceTest;
import org.openmrs.api.db.PatientServiceTest;
import org.openmrs.api.db.UserServiceTest;
import org.openmrs.api.db.hibernate.HibernateTest;
//import org.openmrs.api.context.ContextTest;

public class AllTests extends TestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		
		//suite.addTest(IbatisTest.suite());
		suite.addTest(HibernateTest.suite());
		
		suite.addTest(AdministrationServiceTest.suite());
		suite.addTest(EncounterServiceTest.suite());
		suite.addTest(FormServiceTest.suite());
		suite.addTest(ObsServiceTest.suite());
		suite.addTest(OrderServiceTest.suite());
		suite.addTest(PatientServiceTest.suite());
		suite.addTest(UserServiceTest.suite());
		
		
		return suite;
	}
}
