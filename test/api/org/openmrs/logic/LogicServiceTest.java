/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.logic;

import java.util.Calendar;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.result.Result;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * TODO: add more tests to this test case
 */
public class LogicServiceTest extends BaseContextSensitiveTest {

    private Log log = LogFactory.getLog(this.getClass());

    /**
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
     */
    @Before
    public void runBeforeEachTest() throws Exception {
    	initializeInMemoryDatabase();
    	executeDataSet("org/openmrs/logic/include/LogicTests-patients.xml");
    	executeDataSet("org/openmrs/logic/include/LogicBasicTest.concepts.xml");
        authenticate();
    }

    /**
     * TODO make this test use assert statements instead of printing to stdout
     * 
     */
    @Test
	public void shouldObservationRule() {
        LogicService logicService = Context.getLogicService();
        Cohort patients = new Cohort();
        Map<Integer, Result> result = null;

        patients.addMember(2);

        try {
            log.error("Evaluating CD4 COUNT"); // for a single patient
            Result r = logicService.eval(new Patient(2), "CD4 COUNT");
            log.error("PatientID: 2 " + r);

            log.error("Evaluating CD4 COUNT");
            result = logicService.eval(patients, "CD4 COUNT");
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            log.error("Evaluating CD4 COUNT < 170");
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .lt(170));
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            log.error("Evaluating CD4 COUNT > 185");
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .gt(185));
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            log.error("Evaluating CD4 COUNT EQUALS 190");
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .equalTo(190));
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            log.error("Evaluating CD4 COUNT BEFORE 2006-04-11");
            Calendar cal = Calendar.getInstance();
            cal.set(2006, 3, 11);
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .before(cal.getTime()));
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            log.error("Evaluating CD4 COUNT < 190 BEFORE 2006-04-11");
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .lt(190).before(cal.getTime()));
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            log.error("Evaluating CD4 COUNT AFTER 2006-04-11");
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .after(cal.getTime()));
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            log.error("Evaluating LAST CD4 COUNT");
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .last());
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            log.error("Evaluating LAST CD4 COUNT BEFORE 2006-4-11");
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .last().before(cal.getTime()));
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            log.error("Evaluating NOT CD4 COUNT < 150");
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .not().lt(150));
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            log.error("Evaluating NOT NOT CD4 COUNT < 150");
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .not().not().lt(150));
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            log.error("Evaluating CD4 COUNT < 200");
            patients.addMember(2);
            patients.addMember(3);
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .lt(200));
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            patients.addMember(39);
            log
                    .error("Evaluating PROBLEM ADDED ASNWERED BY HUMAN IMMUNODEFICIENCY VIRUS");
            result = logicService.eval(patients, new LogicCriteria(
                    "PROBLEM ADDED").contains("HUMAN IMMUNODEFICIENCY VIRUS"));
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            // test the index date functionality
//            Calendar index = Calendar.getInstance();
//            index.set(2006, 3, 1);
//            logicService.setIndexDate(index.getTime());

            log.error("Evaluating CD4 COUNT AS OF 2006-04-01");
            result = logicService.eval(patients, "CD4 COUNT");
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            log.error("Evaluating CD4 COUNT AS OF 2006-04-01, WITHIN 1 MONTH");
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .within(Duration.months(1)));
            for (Integer id : result.keySet()) {
                log.error("PatientID: " + id + " " + result.get(id));
            }

            // set it back to today
//            logicService.resetIndexDate();

        } catch (LogicException e) {
            log.error("testObservationRule: Error generated", e);
        }
    }

	/**
     * TODO make this test use assert statements instead of printing to stdout
     * 
     */
    @Test
	public void shouldDemographicsRule() {
        LogicService logicService = Context.getLogicService();
        Cohort patients = new Cohort();
        Map<Integer, Result> result;

        patients.addMember(2);
        //patients.addMember(9342);
        //patients.addMember(9170);
        //patients.addMember(39);

        try {

            log.error("Evaluating BIRTHDATE");
            result = logicService.eval(patients, "BIRTHDATE");
            for (Integer id : result.keySet())
                log.error("Patient ID: " + id + " " + result.get(id));

            log.error("Evaluating AGE");
            result = logicService.eval(patients, "AGE");
            for (Integer id : result.keySet())
                log.error("Patient ID: " + id + " " + result.get(id));

            log.error("Evaluating AGE > 10");
            result = logicService.eval(patients, new LogicCriteria("AGE")
                    .gt(10));
            for (Integer id : result.keySet())
                log.error("Patient ID: " + id + " " + result.get(id));

            log.error("Evaluating BIRTHDATE AFTER 2000");
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2000);
            result = logicService.eval(patients, new LogicCriteria("BIRTHDATE")
                    .after(cal.getTime()));
            for (Integer id : result.keySet())
                log.error("Patient ID: " + id + " " + result.get(id));
            
            log.error("Evaluating BIRTHDATE BEFORE 2000");
            result = logicService.eval(patients, new LogicCriteria("BIRTHDATE")
                    .before(cal.getTime()));
            for (Integer id : result.keySet())
                log.error("Patient ID: " + id + " " + result.get(id));

            // test the index date functionality
            Calendar index = Calendar.getInstance();
            index.set(1970, 0, 1);
//            logicService.setIndexDate(index.getTime());

            log.error("Evaluating BIRTHDATE AS OF 1970-01-01");
            result = logicService.eval(patients, "BIRTHDATE");
            for (Integer id : result.keySet())
                log.error("Patient ID: " + id + " " + result.get(id));

            log.error("Evaluating BIRTHDATE AS OF 1970-01-01, WITHIN 5 YEARS");
            result = logicService.eval(patients, new LogicCriteria("BIRTHDATE")
                    .within(Duration.years(5)));
            for (Integer id : result.keySet())
                log.error("Patient ID: " + id + " " + result.get(id));

            // set it back to today
//            logicService.resetIndexDate();

        } catch (LogicException e) {
            log.error("testDemographicsRule: Error generated", e);
        }

    }

    /**
     * TODO make this test use assert statements instead of printing to stdout
     * 
     */
    @Test
	public void shouldHIVPositiveRule() {
        LogicService logicService = Context.getLogicService();
        Cohort patients = new Cohort();
        Map<Integer, Result> result = null;

        patients.addMember(2);
        //patients.addMember(9342);
        //patients.addMember(9170);
        //patients.addMember(39);

        try {

            log.error("Evaluating FIRST HUMAN IMMUNODEFICIENCY VIRUS");
            result = logicService.eval(patients, new LogicCriteria(
                    "PROBLEM ADDED").contains("HUMAN IMMUNODEFICIENCY VIRUS")
                    .first());
            for (Integer id : result.keySet()) {
                log.error("Patient ID: " + id + " " + result.get(id));
            }

            log.error("Evaluating FIRST HIV INFECTED");
            result = logicService.eval(patients, new LogicCriteria(
                    "PROBLEM ADDED").contains("HIV INFECTED").first());
            for (Integer id : result.keySet()) {
                log.error("Patient ID: " + id + " " + result.get(id));
            }

            log.error("Evaluating FIRST ASYMPTOMATIC HIV INFECTION");
            result = logicService.eval(patients, new LogicCriteria(
                    "PROBLEM ADDED").contains("ASYMPTOMATIC HIV INFECTION")
                    .first());
            for (Integer id : result.keySet()) {
                log.error("Patient ID: " + id + " " + result.get(id));
            }

            log.error("Evaluating FIRST HIV VIRAL LOAD");
            result = logicService.eval(patients, new LogicCriteria(
                    "HIV VIRAL LOAD").first());
            for (Integer id : result.keySet()) {
                log.error("Patient ID: " + id + " " + result.get(id));
            }

            log.error("Evaluating FIRST HIV VIRAL LOAD, QUALITATIVE");
            result = logicService.eval(patients, new LogicCriteria(
                    "HIV VIRAL LOAD, QUALITATIVE").first());
            for (Integer id : result.keySet()) {
                log.error("Patient ID: " + id + " " + result.get(id));
            }

            log.error("Evaluating FIRST CD4 COUNT < 200");
            result = logicService.eval(patients, new LogicCriteria("CD4 COUNT")
                    .lt(200).first());
            for (Integer id : result.keySet()) {
                log.error("Patient ID: " + id + " " + result.get(id));
            }

            log.error("Evaluating HIV POSITIVE");
            result = logicService.eval(patients, "HIV POSITIVE");
            for (Integer id : result.keySet()) {
                log.error("Patient ID: " + id + " " + result.get(id));
            }

        } catch (LogicException e) {
            log.error("Error generated", e);
        }
    }

    /**
     * TODO make this test use assert statements instead of printing to stdout
     * 
     */
    @Test
	public void shouldReferenceRule() {
        LogicService logicService = Context.getLogicService();
        Cohort patients = new Cohort();
        Map<Integer, Result> result = null;

        patients.addMember(2);
        //patients.addMember(9342);
        //patients.addMember(9170);
        //patients.addMember(39);

        try {

            log.error("Evaluating ${OBS::CD4 COUNT}");
            result = logicService.eval(patients, "${OBS::CD4 COUNT}");
            for (Integer id : result.keySet()) {
                log.error("Patient ID: " + id + " " + result.get(id));
            }

            log.error("Evaluating FIRST ${OBS::CD4 COUNT}");
            result = logicService.eval(patients, new LogicCriteria(
                    "${OBS::CD4 COUNT}").first());
            for (Integer id : result.keySet()) {
                log.error("Patient ID: " + id + " " + result.get(id));
            }
            
            log.error("Evaluating ${PERSON::BIRTHDATE}");
            result = logicService.eval(patients, "${PERSON::BIRTHDATE}");
            for (Integer id : result.keySet()) {
                log.error("Patient ID: " + id + " " + result.get(id));
            }
            
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 1980);
            cal.set(Calendar.MONTH, 0);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            log.error("Evaluating ${PERSON::BIRTHDATE} BEFORE 1980-01-01");
            result = logicService.eval(patients, new LogicCriteria(
                    "${PERSON::BIRTHDATE}").before(cal.getTime()));
            for (Integer id : result.keySet()) {
                log.error("Patient ID: " + id + " " + result.get(id));
            }

        } catch (LogicException e) {
            log.error("Error generated", e);
        }
    }
}
