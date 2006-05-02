package org.openmrs.api.db;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.openmrs.Patient;
import org.openmrs.PatientAddress;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientName;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextFactory;

public class PatientServiceTest extends TestCase {
	
	protected PatientService ps;
	protected AdministrationService adminService;
	protected EncounterService encounterService;
	protected Patient createdPatient;
	
	public void setUp() throws Exception{
		Context context = ContextFactory.getContext();
		
		//TODO when do we force authentication ? each Service level call?
		context.authenticate("USER-1", "test");
		
		ps = context.getPatientService();
		adminService = context.getAdministrationService();
		encounterService = context.getEncounterService();
		assertNotNull(adminService);
		assertNotNull(encounterService);
		
		
	}
	public void testGetPatient() throws APIException {

		this.createPatient();
		
		Set patientList;
		
		patientList = ps.getPatientsByIdentifier("???", true);
		assertNotNull(patientList);
		assertTrue(patientList.size() == 0);
		
		patientList = ps.getPatientsByIdentifier("", true);
		assertNotNull(patientList);
		assertTrue(patientList.size() > 0);
		
		Patient patient;
		
		patient = ps.getPatient(-1);
		assertNull(patient);

		patient = (Patient)ps.getPatient(createdPatient.getPatientId());
		assertNotNull(patient);

		patient.setGender("female");
		
		ps.updatePatient(patient);
		
		Patient patient2 = ps.getPatient(patient.getPatientId());
		
		assertTrue(patient.equals(patient2));
		
		PatientAddress pAddress = patient.getAddresses().iterator().next();
		patient.removeAddress(pAddress);
		PatientName pName = patient.getNames().iterator().next();
		patient.removeName(pName);
		
		
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
		Set<PatientAddress> pAddressList = patient.getAddresses();
		pAddressList.add(pAddress);
		patient.setAddresses(pAddressList);
		patient.addAddress(pAddress);
		//patient.removeAddress(pAddress);
		
		patient.setTribe(ps.getTribes().get(0));
		patient.setCitizenship("citizen");
		//TODO make an optional pointer to the actual mother obj?
		patient.setMothersName("Mom's name");
		//patient.setCivilStatus(1);
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
		
		Set<PatientIdentifier> patientIdentifiers = new HashSet<PatientIdentifier>();
		patientIdentifiers.add(patientIdentifier);
		
		patient.setIdentifiers(patientIdentifiers);
		
		ps.createPatient(patient);
		createdPatient = ps.getPatient(patient.getPatientId());
		assertNotNull(createdPatient);
		
		assertNotNull(createdPatient.getPatientId());
		
		Patient createdPatientById = ps.getPatient(createdPatient.getPatientId());
		assertNotNull(createdPatientById);
		
	}
	
	public static Test suite() {
		return new TestSuite(PatientServiceTest.class, "Basic Patient Service functionality");
	}

}
