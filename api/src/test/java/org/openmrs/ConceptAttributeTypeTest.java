package org.openmrs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

public class ConceptAttributeTypeTest extends BaseContextSensitiveTest {

    @Test
    public void shouldSaveAndFetchConceptAttributeType() {
        ConceptAttributeType type = new ConceptAttributeType();
        type.setName("JPA Test Type");
        type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
        type.setMinOccurs(0);
        type.setMaxOccurs(1);

        Context.getConceptService().saveConceptAttributeType(type);

        ConceptAttributeType fetched = Context.getConceptService().getConceptAttributeType(type.getId());
        assertNotNull(fetched);
        assertEquals("JPA Test Type", fetched.getName());
    }
}