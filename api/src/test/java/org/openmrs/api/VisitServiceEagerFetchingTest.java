/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.parameter.VisitSearchCriteria;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Functional test that verifies that VisitService can fetch encounters eagerly
 * when fetchEncounters=true is specified in VisitSearchCriteria.
 *
 * This avoids LazyInitializationException and ensures the N+1 problem
 * resolution logic behaves as expected.
 *
 * @since 2.7.0
 */
public class VisitServiceEagerFetchingTest extends BaseContextSensitiveTest {

    @Autowired
    private VisitService visitService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private LocationService locationService;

    /**
     * Ensures that encounters are eagerly loaded when fetchEncounters = true.
     */
    @Test
    public void shouldFetchVisitsWithEncountersEagerly() {
        VisitType visitType = visitService.getAllVisitTypes().get(0);
        EncounterType encounterType = encounterService.getAllEncounterTypes().get(0);
        Location location = locationService.getAllLocations().get(0);
        PatientIdentifierType idType = patientService.getAllPatientIdentifierTypes(false).get(0);

        Patient patient = new Patient();
        patient.setGender("M");
        patient.addName(new PersonName("Test", "Eager", "Patient"));
        patient.addIdentifier(new PatientIdentifier("TEST-" + System.currentTimeMillis(), idType, location));
        patient = patientService.savePatient(patient);

        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setVisitType(visitType);
        visit.setStartDatetime(new Date());
        visit.setLocation(location);
        visit = visitService.saveVisit(visit);

        Encounter e1 = new Encounter();
        e1.setPatient(patient);
        e1.setEncounterType(encounterType);
        e1.setEncounterDatetime(new Date());
        e1.setLocation(location);
        e1.setVisit(visit);
        encounterService.saveEncounter(e1);

        Encounter e2 = new Encounter();
        e2.setPatient(patient);
        e2.setEncounterType(encounterType);
        e2.setEncounterDatetime(new Date());
        e2.setLocation(location);
        e2.setVisit(visit);
        encounterService.saveEncounter(e2);

        Context.flushSession();
        Context.clearSession();

        VisitSearchCriteria criteria = new VisitSearchCriteria(
            null,
            Collections.singleton(patient),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            true,
            false
        );
        criteria.setFetchEncounters(true);

        List<Visit> visits = visitService.getVisits(criteria);

        assertEquals(1, visits.size(), "Expected exactly 1 visit to be returned");
        Visit loadedVisit = visits.get(0);

        assertEquals(2, loadedVisit.getEncounters().size(),
            "Expected 2 encounters to be eagerly fetched when fetchEncounters=true");
    }
}
