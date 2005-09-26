package org.openmrs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.api.AdministrationServiceTest;
import org.openmrs.api.EncounterServiceTest;
import org.openmrs.api.FormServiceTest;
import org.openmrs.api.ObsServiceTest;
import org.openmrs.api.OrderServiceTest;
import org.openmrs.api.PatientServiceTest;
import org.openmrs.api.UserServiceTest;
import org.openmrs.api.hibernate.HibernateTest;
//import org.openmrs.context.ContextTest;

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
