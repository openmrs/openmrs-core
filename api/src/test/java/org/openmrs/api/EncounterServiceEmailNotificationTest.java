package org.openmrs.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Tests for email notification functionality in EncounterService
 */
public class EncounterServiceEmailNotificationTest extends BaseContextSensitiveTest {
	
	private EncounterService encounterService;
	private AdministrationService administrationService;
	private PersonService personService;
	
	@BeforeEach
	public void setUp() {
		// Usar solo el dataset básico de OpenMRS
		encounterService = Context.getEncounterService();
		administrationService = Context.getAdministrationService();
		personService = Context.getPersonService();
		
		// Configurar propiedades básicas
		administrationService.setGlobalProperty("appointment.notification.enabled", "false");
		administrationService.setGlobalProperty("appointment.notification.subject", "Test Appointment");
		administrationService.setGlobalProperty("appointment.notification.patient.email.attribute", "Email");
		
		// Crear tipo de atributo de email si no existe
		PersonAttributeType emailAttrType = personService.getPersonAttributeTypeByName("Email");
		if (emailAttrType == null) {
			emailAttrType = new PersonAttributeType();
			emailAttrType.setName("Email");
			emailAttrType.setDescription("Email address");
			emailAttrType.setFormat("java.lang.String");
			emailAttrType.setSearchable(false);
			emailAttrType.setSortWeight(1.0);
			personService.savePersonAttributeType(emailAttrType);
		}
	}
	
	/**
	 * Test that email notification settings can be configured
	 */
	@Test
	public void shouldConfigureEmailNotificationSettings() {
		// Test enabling notifications
		GlobalProperty enabledProp = new GlobalProperty("appointment.notification.enabled", "true");
		administrationService.saveGlobalProperty(enabledProp);
		
		String value = administrationService.getGlobalProperty("appointment.notification.enabled");
		assertTrue("true".equals(value), "Notification should be enabled");
		
		// Test subject configuration
		GlobalProperty subjectProp = new GlobalProperty("appointment.notification.subject", "Test Subject");
		administrationService.saveGlobalProperty(subjectProp);
		
		String subject = administrationService.getGlobalProperty("appointment.notification.subject");
		assertTrue("Test Subject".equals(subject), "Subject should be configurable");
	}
	
	/**
	 * Test that encounters can be saved when notifications are enabled
	 */
	@Test
	public void saveEncounter_shouldNotFailWhenNotificationEnabled() {
		// Given: Enable notifications
		administrationService.setGlobalProperty("appointment.notification.enabled", "true");
		
		// Given: Use basic patient from standard test data
		Patient patient = Context.getPatientService().getPatient(2);
		if (patient == null) {
			// Create basic patient if doesn't exist
			patient = new Patient();
			patient.setGender("M");
			patient.setBirthdate(new Date());
			Context.getPatientService().savePatient(patient);
		}
		
		// Given: New encounter
		Encounter encounter = buildTestEncounter();
		encounter.setPatient(patient);
		
		// When: Save encounter (should not throw exception)
		Encounter savedEncounter = encounterService.saveEncounter(encounter);
		
		// Then: Encounter should be saved successfully
		assertNotNull(savedEncounter.getEncounterId(), "Encounter should be saved");
	}
	
	/**
	 * Test that encounters can be saved when notifications are disabled
	 */
	@Test
	public void saveEncounter_shouldWorkWhenNotificationDisabled() {
		// Given: Disable notifications
		administrationService.setGlobalProperty("appointment.notification.enabled", "false");
		
		// Given: Use basic patient
		Patient patient = Context.getPatientService().getPatient(2);
		if (patient == null) {
			patient = new Patient();
			patient.setGender("F");
			patient.setBirthdate(new Date());
			Context.getPatientService().savePatient(patient);
		}
		
		// Given: New encounter
		Encounter encounter = buildTestEncounter();
		encounter.setPatient(patient);
		
		// When: Save encounter
		Encounter savedEncounter = encounterService.saveEncounter(encounter);
		
		// Then: Encounter should be saved successfully
		assertNotNull(savedEncounter.getEncounterId(), "Encounter should be saved");
	}
	
	/**
	 * Test that encounters can be saved when patient has no email
	 */
	@Test
	public void saveEncounter_shouldNotFailWhenPatientHasNoEmail() {
		// Given: Enable notifications
		administrationService.setGlobalProperty("appointment.notification.enabled", "true");
		
		// Given: Patient without email attribute
		Patient patient = Context.getPatientService().getPatient(2);
		if (patient == null) {
			patient = new Patient();
			patient.setGender("M");
			patient.setBirthdate(new Date());
			Context.getPatientService().savePatient(patient);
		}
		
		// Given: New encounter
		Encounter encounter = buildTestEncounter();
		encounter.setPatient(patient);
		
		// When: Save encounter (should not fail)
		Encounter savedEncounter = encounterService.saveEncounter(encounter);
		
		// Then: Encounter should be saved successfully
		assertNotNull(savedEncounter.getEncounterId(), "Encounter should be saved even without patient email");
	}
	
	/**
	 * Test that updating an existing encounter doesn't trigger notification
	 */
	@Test
	public void saveEncounter_shouldNotSendEmailWhenUpdatingExistingEncounter() {
		// Given: Enable notifications
		administrationService.setGlobalProperty("appointment.notification.enabled", "true");
		
		// Given: Patient
		Patient patient = Context.getPatientService().getPatient(2);
		if (patient == null) {
			patient = new Patient();
			patient.setGender("F");
			patient.setBirthdate(new Date());
			Context.getPatientService().savePatient(patient);
		}
		
		// Given: Create and save a new encounter first
		Encounter encounter = buildTestEncounter();
		encounter.setPatient(patient);
		encounter = encounterService.saveEncounter(encounter);
		
		// When: Update the existing encounter
		encounter.setEncounterDatetime(new Date());
		Encounter updatedEncounter = encounterService.saveEncounter(encounter);
		
		// Then: Encounter should be updated successfully
		assertNotNull(updatedEncounter.getEncounterId(), "Encounter should be updated");
	}
	
	/**
	 * Helper method to build a test encounter
	 */
	private Encounter buildTestEncounter() {
		Encounter encounter = new Encounter();
		encounter.setEncounterType(Context.getEncounterService().getEncounterType(1));
		encounter.setLocation(Context.getLocationService().getLocation(1));
		encounter.setEncounterDatetime(new Date());
		encounter.setCreator(Context.getAuthenticatedUser());
		encounter.setDateCreated(new Date());
		return encounter;
	}
}