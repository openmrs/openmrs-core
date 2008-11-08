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
package org.openmrs.api;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.DrugOrder;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.GroupMethod;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 *
 */
public class PatientSetServiceTest extends BaseContextSensitiveTest {

	PatientSetService service;
	
	@Before
	public void getService() {
		service = Context.getPatientSetService();
	}
	
	@Test
	public void shouldGetDrugOrders() throws Exception {
		PatientSetService service = Context.getPatientSetService();
		Cohort nobody = new Cohort();
		Map<Integer, List<DrugOrder>> results = service.getDrugOrders(nobody, null);
		assertNotNull(results);
	}

    /**
     * @verifies {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)}
     * test = should get all patients when no parameters given
     */
    @Test
    public void getPatientsByCharacteristics_shouldGetAllPatientsWhenNoParametersGiven()
            throws Exception {
        Cohort cohort = service.getPatientsByCharacteristics(null, null, null, null, null, null, null);
        Assert.assertEquals(4, cohort.size());
    }

    /**
     * @verifies {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)}
     * test = should get patients of given gender
     */
    @Test
    public void getPatientsByCharacteristics_shouldGetPatientsOfGivenGender()
            throws Exception {
    	Cohort cohort = service.getPatientsByCharacteristics("m", null, null, null, null, null, null);
        Assert.assertEquals(2, cohort.size());
        Assert.assertTrue(cohort.contains(2));
        Assert.assertTrue(cohort.contains(6));
        
        cohort = service.getPatientsByCharacteristics("f", null, null, null, null, null, null);
        Assert.assertEquals(2, cohort.size());
        Assert.assertTrue(cohort.contains(7));
        Assert.assertTrue(cohort.contains(8));
    }

    /**
     * @verifies {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)}
     * test = should get patients who are alive
     */
    @Test
    public void getPatientsByCharacteristics_shouldGetPatientsWhoAreAlive()
            throws Exception {
    	Cohort cohort = service.getPatientsByCharacteristics(null, null, null, null, null, true, null);
        Assert.assertEquals(4, cohort.size());
        Assert.assertTrue(cohort.contains(2));
        Assert.assertTrue(cohort.contains(6));
        Assert.assertTrue(cohort.contains(7));
        Assert.assertTrue(cohort.contains(8));
    }

    /**
     * @verifies {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)}
     * test = should get patients who are dead
     */
    @Test
    public void getPatientsByCharacteristics_shouldGetPatientsWhoAreDead()
            throws Exception {
    	Cohort cohort = service.getPatientsByCharacteristics(null, null, null, null, null, null, true);
        Assert.assertEquals(0, cohort.size());
        //Assert.assertTrue(cohort.contains(2));
    }

    /**
     * @verifies {@link PatientSetService#getPatientsByProgramAndState(Program,List<QProgramWorkflowState;>,Date,Date)}
     * test = should get all patients in any program given null parameters
     */
    @Test
    public void getPatientsByProgramAndState_shouldGetAllPatientsInAnyProgramGivenNullParameters()
            throws Exception {
    	Cohort cohort = service.getPatientsByProgramAndState(null, null, null, null);
        Assert.assertEquals(2, cohort.size());
        Assert.assertTrue(cohort.contains(2));
        Assert.assertTrue(cohort.contains(7));
    }

    /**
     * @verifies {@link PatientSetService#getPatientsByProgramAndState(Program,List<QProgramWorkflowState;>,Date,Date)}
     * test = should get patients in program
     */
    @Test
    public void getPatientsByProgramAndState_shouldGetPatientsInProgram()
            throws Exception {
    	Cohort cohort = service.getPatientsByProgramAndState(Context.getProgramWorkflowService().getProgram(1), null, null, null);
        Assert.assertEquals(1, cohort.size());
        Assert.assertTrue(cohort.contains(2));
    }

    /**
     * @verifies {@link PatientSetService#getPatientsByProgramAndState(Program,List<QProgramWorkflowState;>,Date,Date)}
     * test = should get patients in state
     */
    @Test
    public void getPatientsByProgramAndState_shouldGetPatientsInState()
            throws Exception {
    	ProgramWorkflowService pws = Context.getProgramWorkflowService();
    	Cohort cohort = service.getPatientsByProgramAndState(pws.getProgram(1),
		                                                     Collections.singletonList(pws.getState(2)),
		                                                     null,
		                                                     null);
        Assert.assertEquals(1, cohort.size());
        Assert.assertTrue(cohort.contains(2));
        
        cohort = service.getPatientsByProgramAndState(pws.getProgram(1),
	                                                     Collections.singletonList(pws.getState(4)),
	                                                     null,
	                                                     null);
        Assert.assertEquals(0, cohort.size());
    }

    /**
     * @verifies {@link PatientSetService#getPatientsByProgramAndState(Program,List<QProgramWorkflowState;>,Date,Date)}
     * test = should get patients in states
     */
    @Test
    public void getPatientsByProgramAndState_shouldGetPatientsInStates()
            throws Exception {
    	ProgramWorkflowService pws = Context.getProgramWorkflowService();
    	List<ProgramWorkflowState> list = new ArrayList<ProgramWorkflowState>();
    	list.add(pws.getState(2));
    	list.add(pws.getState(4));
    	Cohort cohort = service.getPatientsByProgramAndState(pws.getProgram(1),
		                                                     list,
		                                                     null,
		                                                     null);
        Assert.assertEquals(1, cohort.size());
        Assert.assertTrue(cohort.contains(2));
        
        //TODO also test having two states get multiple poeple
    }

    /**
     * @verifies {@link PatientSetService#getPatientsHavingDrugOrder(Collection<QInteger;>,Collection<QInteger;>,GroupMethod,Date,Date)}
     * test = should get all patients with drug orders given null parameters
     */
    @Test
    public void getPatientsHavingDrugOrder_shouldGetAllPatientsWithDrugOrdersGivenNullParameters()
            throws Exception {
    	Cohort cohort = service.getPatientsHavingDrugOrder(null, null, null, null, null);
        Assert.assertEquals(2, cohort.size());
        Assert.assertTrue(cohort.contains(2));
        Assert.assertTrue(cohort.contains(7));
    }

    /**
     * @verifies {@link PatientSetService#getPatientsHavingDrugOrder(Collection<QInteger;>,Collection<QInteger;>,GroupMethod,Date,Date)}
     * test = should get patients with no drug orders
     */
    @Test
    public void getPatientsHavingDrugOrder_shouldGetPatientsWithNoDrugOrders()
            throws Exception {
    	Cohort cohort = service.getPatientsHavingDrugOrder(null, null, GroupMethod.NONE, null, null);
        Assert.assertEquals(2, cohort.size());
        Assert.assertTrue(cohort.contains(6));
        Assert.assertTrue(cohort.contains(8));
    }

    /**
     * @verifies {@link PatientSetService#getPatientsHavingDrugOrder(Collection<QInteger;>,Collection<QInteger;>,GroupMethod,Date,Date)}
     * test = should get patients with drug orders for drugs
     */
    @Test
    public void getPatientsHavingDrugOrder_shouldGetPatientsWithDrugOrdersForDrugs()
            throws Exception {
        List<Integer> drugIds = new ArrayList<Integer>();
        drugIds.add(2);
    	Cohort cohort = service.getPatientsHavingDrugOrder(drugIds, null, null, null, null);
        Assert.assertEquals(1, cohort.size());
        Assert.assertTrue(cohort.contains(2));
    }
	
}
