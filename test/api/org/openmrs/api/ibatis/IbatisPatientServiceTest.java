package org.openmrs.api.ibatis;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.*;
import org.openmrs.PatientName;
import org.openmrs.context.IbatisContext;
import org.openmrs.context.*;
import org.openmrs.api.*;
import java.util.*;
import java.util.Date;

public class IbatisPatientServiceTest extends TestCase {
	
	protected PatientService ps;
	
	public void setUp(){
		Context context = new IbatisContext();
		ps = new IbatisPatientService(context);
	}
	public void testGetPatient() throws APIException {
		
		Patient patient;
		
		patient = ps.getPatient(-1);
		assertNull(patient);

		patient = (Patient)ps.getPatient(1);
		assertNotNull(patient);
	}
	
	public void testGetPatientByIdentifier() throws APIException {

		List patientList;
		
		patientList = ps.getPatientByIdentifier("???");
		assertNotNull(patientList);
		assertTrue(patientList.size() == 0);
		
		// should we be sending strings like %-% ? 
		// ...or have PatientService insert %'s ?
		patientList = ps.getPatientByIdentifier("%-%");
		assertNotNull(patientList);
		assertTrue(patientList.size() > 0);
	}
	
	public void testCreateDeletePatient() throws APIException {
		
		Patient patient = new Patient();
		
		PatientName pName = new PatientName();
		pName.setGivenName("Tom");
		pName.setMiddleName("E.");
		pName.setFamilyName("Patient");
		patient.addName(pName);
		
		PatientAddress pAddress = new PatientAddress();
		pAddress.setAddress1("123 My street");
		pAddress.setAddress2("Apt 402");
		pAddress.setCityVillage("Anywhere city");
		pAddress.setCountry("Some Country");
		List<PatientAddress> pAddressList = patient.getAddresses();
		pAddressList.add(pAddress);
		patient.setAddresses(pAddressList);
		
		patient.setBirthdate(new Date());
		
		patient.setBirthdateEstimated(true);
		
		patient.setBirthplace("Little town outside of nowhere");
		
		Patient createdPatient = ps.createPatient(patient);
		assertNotNull(createdPatient);
		
		Integer pId = createdPatient.getPatientId();
		assertNotNull(pId);
		
		Patient createdPatientById = ps.getPatient(pId);
		assertNotNull(createdPatientById);
		
		ps.deletePatient(pId);
		Patient deletedPatientById = ps.getPatient(pId);
		assertNull(deletedPatientById);
	}
	
	public static Test suite() {
		return new TestSuite(IbatisPatientServiceTest.class, "Basic IbatisPatientService functionality");
	}

}
