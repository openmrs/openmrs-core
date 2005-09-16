package org.openmrs.api;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.context.Context;
import org.openmrs.context.ContextFactory;

public class ObsServiceTest extends TestCase {
	
	protected ObsService es;
	protected PatientService ps;
	protected UserService us;
	protected ObsService os;
	
	public void setUp() throws Exception{
		Context context = ContextFactory.getContext();
		
		context.authenticate("3-4", "test");
		
		es = context.getObsService();
		assertNotNull(es);
		ps = context.getPatientService();
		assertNotNull(ps);
		us = context.getUserService();
		assertNotNull(us);
		os = context.getObsService();
		assertNotNull(os);
		
	}

	public void testObsCreateUpdateDelete() throws Exception {
		
		Obs o = new Obs();
		
		//testing creation
		
		

		
	}	
	
	public static Test suite() {
		return new TestSuite(ObsServiceTest.class, "Basic ObsService functionality");
	}

}
