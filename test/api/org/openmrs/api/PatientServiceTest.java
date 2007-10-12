package org.openmrs.api;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.BaseTest;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;

/**
 * This class tests methods in the PatientService class
 */
public class PatientServiceTest extends BaseTest {
	
	protected PatientService ps = null; 
	protected AdministrationService adminService = null;
	protected EncounterService encounterService = null;
	
	protected Patient createdPatient;
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		authenticate();
		
		if (ps == null) {
			ps = Context.getPatientService();
			adminService = Context.getAdministrationService();
			encounterService = Context.getEncounterService();
		}
		
	}

	public void testGetPatient() throws Exception {

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
		
		PersonAddress pAddress = patient.getAddresses().iterator().next();
		patient.removeAddress(pAddress);
		PersonName pName = patient.getNames().iterator().next();
		patient.removeName(pName);
		
		
	}
	
	public void createPatient() throws Exception {
		
		Patient patient = new Patient();
		
		PersonName pName = new PersonName();
		pName.setGivenName("Tom");
		pName.setMiddleName("E.");
		pName.setFamilyName("Patient");
		patient.addName(pName);
		
		PersonAddress pAddress = new PersonAddress();
		pAddress.setAddress1("123 My street");
		pAddress.setAddress2("Apt 402");
		pAddress.setCityVillage("Anywhere city");
		pAddress.setCountry("Some Country");
		Set<PersonAddress> pAddressList = patient.getAddresses();
		pAddressList.add(pAddress);
		patient.setAddresses(pAddressList);
		patient.addAddress(pAddress);
		//patient.removeAddress(pAddress);
		
		patient.setTribe(ps.getTribes().get(0));
		//patient.setCitizenship("citizen");
		//TODO make an optional pointer to the actual mother obj?
		//patient.setMothersName("Mom's name");
		//patient.setCivilStatus(1);
		patient.setDeathDate(new Date());
		//patient.setCauseOfDeath("air");
		//patient.setHealthDistrict("health dist");
		//patient.setHealthCenter(null);
		patient.setBirthdate(new Date());
		patient.setBirthdateEstimated(true);
		//patient.setBirthplace("Little town outside of nowhere");
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
	
	/**
	 * Gets the first patient, then sees if it can get that patient by its identifier as well
	 * 
	 * @throws Exception
	 */
	public void testGetPatientsByIdentifier() throws Exception {
		
		// get the first patient
		Set<Patient> johnPatients = ps.getPatientsByName("John");
		assertNotNull("There should be a patient named 'John'", johnPatients);
		assertFalse("There should be a patient named 'John'", johnPatients.isEmpty());
		
		Patient firstJohnPatient = johnPatients.iterator().next();
		
		// get a list of patients with this identifier, make sure the john patient is actually there
		String identifier = firstJohnPatient.getPatientIdentifier().getIdentifier();
		assertNotNull("Uh oh, the patient doesn't have an identifier", identifier);
		Set<Patient> patients = ps.getPatientsByIdentifier(identifier, true);
		assertTrue("Odd. The firstJohnPatient isn't in the list of patients for this identifier", patients.contains(firstJohnPatient));
		
	}
	
}
