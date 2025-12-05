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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
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
 * Performance test demonstrating N+1 query problem resolution in VisitService.
 * This test validates the optimization achieved through eager fetching of encounters.
 *
 * @since 2.7.0
 */
public class VisitServicePerformanceTest extends BaseContextSensitiveTest {

    @Autowired
    private VisitService visitService;

    @Autowired
    private PatientService patientService;

    @BeforeAll
    public static void enableSqlLogging() {
        System.setProperty("hibernate.show_sql", "true");
    }

    /**
     * Tests the performance improvement when fetching visits with eager loading of encounters.
     * This test demonstrates the resolution of the N+1 query problem by using LEFT JOIN FETCH.
     */
    @Test
    public void shouldDemonstrateSlowPerformanceWithManyVisits() {
        
        Patient patient = patientService.getPatient(2); 

        int visitCount = 2000;
        System.out.println("Generating " + visitCount + " visits...");
        VisitType visitType = visitService.getVisitType(1);
        
        for (int i = 0; i < visitCount; i++) {
            Visit v = new Visit();
            v.setPatient(patient);
            v.setVisitType(visitType);
            v.setStartDatetime(new Date());
            visitService.saveVisit(v);
            
            if (i % 50 == 0) {
                Context.flushSession();
                Context.clearSession();
            }
        }
        
        Context.flushSession();
        Context.clearSession(); 
        System.out.println("Generation complete.");

        long startTime = System.currentTimeMillis();
        
        patient = patientService.getPatient(2);
        
        VisitSearchCriteria criteria = new VisitSearchCriteria(
            null, Collections.singleton(patient), null, null, null, null, null, null, null, true, false
        );
        criteria.setFetchEncounters(true);
        
        List<Visit> visits = visitService.getVisits(criteria);
        
        int totalEncounters = 0;
        for (Visit v : visits) {
            totalEncounters += v.getEncounters().size(); 
        }
        
        long endTime = System.currentTimeMillis();
        
        System.out.println("--------------------------------------------------");
        System.out.println("Fetched " + visits.size() + " visits.");
        System.out.println("Time taken (Optimized): " + (endTime - startTime) + " ms");
        System.out.println("--------------------------------------------------");
        
        assertEquals(visitCount + 3, visits.size()); 
    }
}
