package org.openmrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class PatientIdentifierTest {

    @Test
    public void shouldCopyBusinessFields() {
        PatientIdentifier template = new PatientIdentifier();
        template.setIdentifier("123");
        template.setIdentifierType(new PatientIdentifierType(1));
        template.setLocation(new Location(1));
        template.setPatient(new Patient(1));
        template.setPatientProgram(new PatientProgram());
        template.setPreferred(true);
        template.setVoided(true);
        template.setVoidReason("Reason");

        // Identity fields (should NOT be copied)
        template.setPatientIdentifierId(500);
        template.setUuid("some-uuid");

        PatientIdentifier copy = new PatientIdentifier(template);

        assertEquals(template.getIdentifier(), copy.getIdentifier());
        assertEquals(template.getIdentifierType(), copy.getIdentifierType());
        assertEquals(template.getLocation(), copy.getLocation());
        assertEquals(template.getPatient(), copy.getPatient());
        assertEquals(template.getPatientProgram(), copy.getPatientProgram());
        assertEquals(template.getPreferred(), copy.getPreferred());
        assertEquals(template.getVoided(), copy.getVoided());
        assertEquals(template.getVoidReason(), copy.getVoidReason());

        // Verify identity fields are NOT copied
        assertNull(copy.getPatientIdentifierId());
        assertNotEquals(template.getUuid(), copy.getUuid(), "The copy should have its own unique UUID");
    }
}
