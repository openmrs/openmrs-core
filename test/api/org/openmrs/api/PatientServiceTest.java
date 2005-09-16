package org.openmrs.api;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
import org.openmrs.context.Context;
import org.openmrs.context.ContextFactory;

public class PatientServiceTest extends TestCase {
	
	protected PatientService ps;
	protected AdministrationService adminService;
	protected EncounterService encounterService;
	protected Patient createdPatient;
	
	public void setUp() throws Exception{
		Context context = ContextFactory.getContext();
		
		//TODO when do we force authentication ? each Service level call?
		context.authenticate("admin", "test");
		
		ps = context.getPatientService();
		adminService = context.getAdministrationService();
		encounterService = context.getEncounterService();
		assertNotNull(adminService);
		assertNotNull(encounterService);
		
		this.createPatient();
	}
	public void testGetPatient() throws APIException {
		
		Patient patient;
		
		patient = ps.getPatient(-1);
		assertNull(patient);

		patient = (Patient)ps.getPatient(createdPatient.getPatientId());
		assertNotNull(patient);
	}
	
	public void testGetPatientByIdentifier() throws APIException {

		List patientList;
		
		patientList = ps.getPatientByIdentifier("???");
		assertNotNull(patientList);
		assertTrue(patientList.size() == 0);
		
		// TODO should we be sending strings like %-% ? 
		// ...or have PatientService insert %'s (but that makes it db specific?
		patientList = ps.getPatientByIdentifier("%");
		assertNotNull(patientList);
		assertTrue(patientList.size() > 0);
	}
	
	public void createPatient() throws APIException {
		
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
		
		patient.setTribe(ps.getPatientTribes().get(0));
		patient.setCitizenship("citizen");
		//TODO make an optional pointer to the actual mother obj?
		patient.setMothersName("Mom's name");
		patient.setCivilStatus(1);
		patient.setDeathDate(new Date());
		patient.setCauseOfDeath("air");
		patient.setHealthDistrict("health dist");
		patient.setHealthCenter(0);
		patient.setBirthdate(new Date());
		patient.setBirthdateEstimated(true);
		patient.setBirthplace("Little town outside of nowhere");
		patient.setGender("male");
		
		List<PatientIdentifierType> patientIdTypes = ps.getPatientIdentifierTypes();
		assertNotNull(patientIdTypes);
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifier("ident");
		patientIdentifier.setIdentifierType(patientIdTypes.get(0));
		patientIdentifier.setLocation(encounterService.getLocations().get(0));
		
		List<PatientIdentifier> patientIdentifiers = new LinkedList<PatientIdentifier>();
		patientIdentifiers.add(patientIdentifier);
		
		patient.setIdentifiers(patientIdentifiers);
		
		createdPatient = ps.createPatient(patient);
		assertNotNull(createdPatient);
		
		assertNotNull(createdPatient.getPatientId());
		
		Patient createdPatientById = ps.getPatient(createdPatient.getPatientId());
		assertNotNull(createdPatientById);
		
	}
	
	public static Test suite() {
		return new TestSuite(PatientServiceTest.class, "Basic PatientService functionality");
	}

}
