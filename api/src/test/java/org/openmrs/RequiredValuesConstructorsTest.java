/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;



import org.junit.jupiter.api.Test;




public class RequiredValuesConstructorsTest {

    @Test
    public void shouldCreatePatientWithRequiredValues() {
        Patient patient = new Patient();
        assertNotNull(patient);
    }

    @Test
    public void shouldCreateUserWithRequiredValues() {
        User user = new User();
        assertNotNull(user);
    }

    @Test
    public void shouldCreateConceptWithRequiredValues() {
        Concept concept = new Concept();
        assertNotNull(concept);
    }

    @Test
    public void shouldCreateProgramWithRequiredValues() {
        Concept concept = new Concept();
        Program program = new Program("HIV Program", concept);
        assertEquals("HIV Program", program.getName());
        assertEquals(concept, program.getConcept());
    }

    @Test
    public void shouldCreateLocationWithRequiredValues() {
        Location location = new Location("Kampala");
        assertEquals("Kampala", location.getName());
    }

        @Test
    public void shouldCreatePersonWithRequiredValues() {
        Person person = new Person();
        assertNotNull(person);
    }

    @Test
    public void shouldCreatePersonNameWithRequiredValues() {
        PersonName name = new PersonName("John", "Middle", "Doe");
        assertEquals("John", name.getGivenName());
        assertEquals("Middle", name.getMiddleName());
        assertEquals("Doe", name.getFamilyName());
    }


    @Test
    public void shouldCreateEncounterWithRequiredValues() {
        Patient patient = new Patient();
        EncounterType type = new EncounterType();
        Location location = new Location("Mengo");
		Encounter encounter = new Encounter(patient, type, new java.util.Date(), location);
        assertEquals(patient, encounter.getPatient());
        assertEquals(type, encounter.getEncounterType());
        assertEquals("Mengo", encounter.getLocation().getName());
    }

    @Test
    public void shouldCreateObsWithRequiredValues() {
        Person person = new Person();
        Concept concept = new Concept();
        java.util.Date date = new java.util.Date();
        Obs obs = new Obs(person, concept, date, null);
        assertEquals(person, obs.getPerson());
        assertEquals(concept, obs.getConcept());
        assertEquals(date, obs.getObsDatetime());
    }

    @Test
    public void shouldCreateVisitWithRequiredValues() {
        Patient patient = new Patient();
        VisitType visitType = new VisitType();
        Location location = new Location("Rubaga");
        java.util.Date date = new java.util.Date();
        Visit visit = new Visit(patient, visitType, date, location);
        assertEquals(patient, visit.getPatient());
        assertEquals(visitType, visit.getVisitType());
        assertEquals(location, visit.getLocation());
    }
}
