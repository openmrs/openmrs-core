package org.openmrs.api.ibatis;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.Patient;
import org.openmrs.context.IbatisContext;
import org.openmrs.context.*;
import org.openmrs.api.*;
import java.util.*;

public class IbatisPatientServiceTest extends TestCase {
	
	protected PatientService ps;
	
	public void setUp(){
		Context context = new IbatisContext();
		ps = new IbatisPatientService(context);
	}
	public void testGetPatient() throws APIException {
		
		Patient patient = (Patient)ps.getPatient(1);
		assertNotNull(patient);
		patient = ps.getPatient(-1);
		assertNull(patient);
	}
	
	public void testGetPatientByIdentifier() throws APIException {
		// should we be sending strings like %-% ? 
		// ...or have PatientService insert %'s ?
		List patientList = ps.getPatientByIdentifier("%-%");
		assertNotNull(patientList);
		assertTrue(patientList.size() > 0);
		
		patientList = ps.getPatientByIdentifier("???");
		assertNotNull(patientList);
		assertTrue(patientList.size() == 0);
		
	}
	
	public static Test suite() {
		return new TestSuite(IbatisPatientServiceTest.class, "Basic IbatisPatientService functionality");
	}

}
