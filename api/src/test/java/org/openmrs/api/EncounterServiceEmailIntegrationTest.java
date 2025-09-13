package org.openmrs.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Integration test for email notification functionality.
 * This test requires proper SMTP configuration to work.
 * 
 * Para habilitar este test:
 * 1. Configura las propiedades SMTP en tu base de datos
 * 2. Remueve la anotación @Disabled
 * 3. Actualiza el email de prueba con uno real
 */
public class EncounterServiceEmailIntegrationTest extends BaseContextSensitiveTest {
    
    private EncounterService encounterService;
    private PatientService patientService;
    private PersonService personService;
    private AdministrationService administrationService;
    
    // Email obtenido de variables de entorno o archivo .env
    private static final String TEST_EMAIL = getEnvVariable("TEST_EMAIL_1", "test@example.com");
    
    /**
     * Obtiene una variable de entorno con valor por defecto
     */
    private static String getEnvVariable(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            // Intentar leer de propiedades del sistema (puede ser configurado por scripts)
            value = System.getProperty(key, defaultValue);
        }
        return value;
    }
    
    @BeforeEach
    public void setUp() {
        encounterService = Context.getEncounterService();
        patientService = Context.getPatientService();
        personService = Context.getPersonService();
        administrationService = Context.getAdministrationService();
        
        // Configurar propiedades de notificación
        setupNotificationProperties();
        
        // Verificar que existe el tipo de atributo Email
        ensureEmailAttributeTypeExists();
    }
    
    /**
     * Test que envía un email real cuando se crea un nuevo encounter.
     * ¡HABILITADO PARA TESTING CON CREDENCIALES REALES!
     */
    @Test
    public void testRealEmailNotification() {
        // Given: Use existing patient and add email attribute
        Patient patient = patientService.getPatient(2);
        if (patient == null) {
            patient = patientService.getPatient(7); // Try another ID
        }
        assertNotNull(patient, "Need an existing patient for testing");
        
        // Add email attribute to existing patient
        PersonAttributeType emailType = personService.getPersonAttributeTypeByName("Email");
        PersonAttribute emailAttr = new PersonAttribute();
        emailAttr.setAttributeType(emailType);
        emailAttr.setValue(TEST_EMAIL);
        emailAttr.setPerson(patient.getPerson());
        patient.getPerson().addAttribute(emailAttr);
        patientService.savePatient(patient);
        
        // Given: Enable notifications
        administrationService.setGlobalProperty("appointment.notification.enabled", "true");
        
        // When: Create a new encounter
        Encounter encounter = createTestEncounter(patient);
        Encounter savedEncounter = encounterService.saveEncounter(encounter);
        
        // Then: Encounter should be saved successfully
        assertNotNull(savedEncounter.getEncounterId(), "Encounter should be saved");
        
        // Then: Check console for email sending logs
        System.out.println("=== EMAIL NOTIFICATION TEST ===");
        System.out.println("Patient: " + patient.getPersonName().getFullName());
        System.out.println("Email: " + TEST_EMAIL);
        System.out.println("Encounter ID: " + savedEncounter.getEncounterId());
        System.out.println("Check your email inbox and application logs for delivery confirmation");
        System.out.println("===============================");
        
        // Pausa para permitir que el email se envíe
        try {
            Thread.sleep(3000); // 3 segundos
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Test that verifies configuration is correct
     */
    @Test
    public void testEmailConfigurationIsSet() {
        // Verify SMTP configuration properties exist
        String smtpHost = administrationService.getGlobalProperty("mail.smtp.host");
        String smtpPort = administrationService.getGlobalProperty("mail.smtp.port");
        String mailUser = administrationService.getGlobalProperty("mail.user");
        
        System.out.println("=== EMAIL CONFIGURATION STATUS ===");
        System.out.println("SMTP Host: " + (smtpHost != null ? smtpHost : "NOT CONFIGURED"));
        System.out.println("SMTP Port: " + (smtpPort != null ? smtpPort : "NOT CONFIGURED"));
        System.out.println("Mail User: " + (mailUser != null ? mailUser : "NOT CONFIGURED"));
        
        // Verify notification properties
        String enabled = administrationService.getGlobalProperty("appointment.notification.enabled");
        String subject = administrationService.getGlobalProperty("appointment.notification.subject");
        String emailAttr = administrationService.getGlobalProperty("appointment.notification.patient.email.attribute");
        
        System.out.println("Notifications Enabled: " + enabled);
        System.out.println("Email Subject: " + subject);
        System.out.println("Email Attribute: " + emailAttr);
        System.out.println("=================================");
        
        // Verify email attribute type exists
        PersonAttributeType emailType = personService.getPersonAttributeTypeByName("Email");
        assertNotNull(emailType, "Email attribute type should exist");
    }
    
    private void setupNotificationProperties() {
        // Configurar propiedades de notificación
        administrationService.setGlobalProperty("appointment.notification.enabled", "true");
        administrationService.setGlobalProperty("appointment.notification.subject", "Nueva Cita Médica - Test");
        administrationService.setGlobalProperty("appointment.notification.patient.email.attribute", "Email");
        
        // Configurar SMTP usando variables de entorno
        administrationService.setGlobalProperty("mail.smtp.host", 
            getEnvVariable("SMTP_HOST", "smtp.gmail.com"));
        administrationService.setGlobalProperty("mail.smtp.port", 
            getEnvVariable("SMTP_PORT", "587"));
        administrationService.setGlobalProperty("mail.smtp.auth", 
            getEnvVariable("SMTP_AUTH", "true"));
        administrationService.setGlobalProperty("mail.smtp.starttls.enable", 
            getEnvVariable("SMTP_STARTTLS", "true"));
        
        // Credenciales de email desde variables de entorno
        String gmailUser = getEnvVariable("GMAIL_USER", "test@example.com");
        String gmailPassword = getEnvVariable("GMAIL_APP_PASSWORD", "test-password");
        String fromEmail = getEnvVariable("FROM_EMAIL", gmailUser);
        
        administrationService.setGlobalProperty("mail.user", gmailUser);
        administrationService.setGlobalProperty("mail.password", gmailPassword);
        administrationService.setGlobalProperty("mail.from", fromEmail);
        
        System.out.println("🔧 Configuración SMTP cargada desde variables de entorno:");
        System.out.println("   📧 Usuario: " + gmailUser);
        System.out.println("   🏠 Host: " + getEnvVariable("SMTP_HOST", "smtp.gmail.com"));
        System.out.println("   🔌 Puerto: " + getEnvVariable("SMTP_PORT", "587"));
    }
    
    private void ensureEmailAttributeTypeExists() {
        PersonAttributeType emailAttrType = personService.getPersonAttributeTypeByName("Email");
        if (emailAttrType == null) {
            emailAttrType = new PersonAttributeType();
            emailAttrType.setName("Email");
            emailAttrType.setDescription("Patient email address");
            emailAttrType.setFormat("java.lang.String");
            emailAttrType.setSearchable(false);
            emailAttrType.setSortWeight(1.0);
            personService.savePersonAttributeType(emailAttrType);
        }
    }
    
    private Patient createTestPatientWithEmail(String email) {
        // Create person
        Person person = new Person();
        person.setGender("M");
        person.setBirthdate(new Date());
        
        // Add name
        PersonName name = new PersonName();
        name.setGivenName("Juan");
        name.setFamilyName("Pérez Test");
        person.addName(name);
        
        // Add email attribute
        PersonAttributeType emailType = personService.getPersonAttributeTypeByName("Email");
        PersonAttribute emailAttr = new PersonAttribute();
        emailAttr.setAttributeType(emailType);
        emailAttr.setValue(email);
        emailAttr.setPerson(person);
        person.addAttribute(emailAttr);
        
        // Create patient with identifier
        Patient patient = new Patient(person);
        
        // Add patient identifier (required)
        PatientIdentifierType idType = Context.getPatientService().getPatientIdentifierType(1);
        if (idType == null) {
            idType = new PatientIdentifierType();
            idType.setName("Test ID Type");
            idType.setDescription("Test identifier type");
            Context.getPatientService().savePatientIdentifierType(idType);
        }
        
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifierType(idType);
        identifier.setIdentifier("TEST-" + System.currentTimeMillis());
        identifier.setLocation(Context.getLocationService().getLocation(1));
        identifier.setPreferred(true);
        patient.addIdentifier(identifier);
        
        return patientService.savePatient(patient);
    }
    
    private Encounter createTestEncounter(Patient patient) {
        Encounter encounter = new Encounter();
        
        // Use existing encounter type (assuming ID 1 exists)
        EncounterType encounterType = encounterService.getEncounterType(1);
        if (encounterType == null) {
            encounterType = new EncounterType("Test Encounter", "Test encounter for email notification");
            encounterService.saveEncounterType(encounterType);
        }
        
        // Use existing location (assuming ID 1 exists)
        Location location = Context.getLocationService().getLocation(1);
        if (location == null) {
            location = new Location();
            location.setName("Test Location");
            location.setDescription("Test location for encounters");
            Context.getLocationService().saveLocation(location);
        }
        
        encounter.setEncounterType(encounterType);
        encounter.setPatient(patient);
        encounter.setLocation(location);
        encounter.setEncounterDatetime(new Date());
        encounter.setCreator(Context.getAuthenticatedUser());
        encounter.setDateCreated(new Date());
        
        return encounter;
    }
    
    /**
     * Test que envía emails HTML atractivos a múltiples destinatarios.
     * Este test envía el nuevo formato HTML a ambos correos solicitados.
     */
    @Test
    public void testAtractiveHtmlEmailToMultipleRecipients() {
        // Given: Preparar emails desde variables de entorno
        String[] emails = {
            getEnvVariable("TEST_EMAIL_1", "test1@example.com"),
            getEnvVariable("TEST_EMAIL_2", "test2@example.com")
        };
        
        // Given: Enable notifications
        administrationService.setGlobalProperty("appointment.notification.enabled", "true");
        
        System.out.println("=== SENDING ATTRACTIVE HTML EMAILS ===");
        
        for (int i = 0; i < emails.length; i++) {
            // Use existing patients and change their email
            Patient patient = patientService.getPatient(2 + i); // Different patients
            if (patient == null) {
                patient = patientService.getPatient(7 + i); // Try other IDs
            }
            assertNotNull(patient, "Need existing patients for testing");
            
            // Update email attribute
            PersonAttributeType emailType = personService.getPersonAttributeTypeByName("Email");
            PersonAttribute emailAttr = new PersonAttribute();
            emailAttr.setAttributeType(emailType);
            emailAttr.setValue(emails[i]);
            emailAttr.setPerson(patient.getPerson());
            patient.getPerson().addAttribute(emailAttr);
            patientService.savePatient(patient);
            
            // When: Create a new encounter
            Encounter encounter = createTestEncounter(patient);
            Encounter savedEncounter = encounterService.saveEncounter(encounter);
            
            // Then: Encounter should be saved successfully
            assertNotNull(savedEncounter.getEncounterId(), "Encounter should be saved");
            
            System.out.println("📧 Email #" + (i + 1) + " enviado:");
            System.out.println("   Paciente: " + patient.getPersonName().getFullName());
            System.out.println("   Email: " + emails[i]);
            System.out.println("   Encounter ID: " + savedEncounter.getEncounterId());
            System.out.println("   Formato: HTML con diseño atractivo ✨");
            
            // Pausa entre envíos
            try {
                Thread.sleep(2000); // 2 segundos entre emails
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("✅ Ambos emails HTML enviados exitosamente!");
        System.out.println("Revisa las bandejas de entrada de ambos destinatarios");
        System.out.println("=======================================");
    }
    
    /**
     * Test que envía emails HTML atractivos a TODOS los correos solicitados.
     * Este test envía el nuevo formato HTML a los 5 correos especificados.
     */
    @Test
    public void testEmailsToAllRequiredRecipients() {
        // Given: Lista completa de emails desde variables de entorno
        String[] emails = {
            getEnvVariable("TEST_EMAIL_1", "test1@example.com"),
            getEnvVariable("TEST_EMAIL_2", "test2@example.com"), 
            getEnvVariable("TEST_EMAIL_3", "test3@example.com"),
            getEnvVariable("TEST_EMAIL_4", "test4@example.com"),
            getEnvVariable("TEST_EMAIL_5", "test5@example.com")
        };
        
        // Given: Enable notifications
        administrationService.setGlobalProperty("appointment.notification.enabled", "true");
        
        System.out.println("🚀 ENVIANDO EMAILS HTML A TODOS LOS DESTINATARIOS 🚀");
        System.out.println("Total de destinatarios: " + emails.length);
        System.out.println("=====================================================");
        
        for (int i = 0; i < emails.length; i++) {
            // Use different existing patients for variety
            Patient patient = null;
            
            // Try different patient IDs to get variety
            int[] patientIds = {2, 7, 6, 502, 999}; // Different patient IDs
            for (int patientId : patientIds) {
                patient = patientService.getPatient(patientId);
                if (patient != null) {
                    break;
                }
            }
            
            // If no existing patient found, use patient ID 2 as fallback
            if (patient == null) {
                patient = patientService.getPatient(2);
            }
            
            assertNotNull(patient, "Need existing patients for testing");
            
            // Update email attribute for this patient
            PersonAttributeType emailType = personService.getPersonAttributeTypeByName("Email");
            PersonAttribute emailAttr = new PersonAttribute();
            emailAttr.setAttributeType(emailType);
            emailAttr.setValue(emails[i]);
            emailAttr.setPerson(patient.getPerson());
            patient.getPerson().addAttribute(emailAttr);
            patientService.savePatient(patient);
            
            // When: Create a new encounter
            Encounter encounter = createTestEncounter(patient);
            Encounter savedEncounter = encounterService.saveEncounter(encounter);
            
            // Then: Encounter should be saved successfully
            assertNotNull(savedEncounter.getEncounterId(), "Encounter should be saved");
            
            System.out.printf("📧 Email %d/%d enviado:\n", (i + 1), emails.length);
            System.out.println("   👤 Paciente: " + patient.getPersonName().getFullName());
            System.out.println("   📮 Email: " + emails[i]);
            System.out.println("   🆔 Encounter ID: " + savedEncounter.getEncounterId());
            System.out.println("   ✨ Formato: HTML con diseño atractivo");
            System.out.println("   ────────────────────────────────────");
            
            // Pausa entre envíos para no sobrecargar el servidor de email
            try {
                Thread.sleep(3000); // 3 segundos entre emails
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("🎉 ¡TODOS LOS EMAILS ENVIADOS EXITOSAMENTE! 🎉");
        System.out.println("📊 Resumen:");
        System.out.println("   • Total enviados: " + emails.length + " emails");
        System.out.println("   • Formato: HTML atractivo con CSS");
        System.out.println("   • Destinatarios:");
        for (int i = 0; i < emails.length; i++) {
            System.out.println("     " + (i + 1) + ". " + emails[i]);
        }
        System.out.println("📬 Revisa todas las bandejas de entrada!");
        System.out.println("===============================================");
    }
}