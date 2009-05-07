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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.GroupMethod;
import org.openmrs.api.PatientSetService.Modifier;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 *
 */
public class PatientSetServiceTest extends BaseContextSensitiveTest {
	
	PatientSetService service;
	
	@Before
	public void getService() {
		service = Context.getPatientSetService();
	}
	
	/**
	 * @see {@link PatientSetService#getDrugOrders(Cohort,Concept)}
	 */
	@Test
	@Verifies(value = "should return an empty list if cohort is empty", method = "getDrugOrders(Cohort,Concept)")
	public void getDrugOrders_shouldReturnAnEmptyListIfCohortIsEmpty() throws Exception {
		Cohort nobody = new Cohort();
		Map<Integer, List<DrugOrder>> results = Context.getPatientSetService().getDrugOrders(nobody, null);
		assertNotNull(results);
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should get all patients when no parameters given", method = "getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)")
	public void getPatientsByCharacteristics_shouldGetAllPatientsWhenNoParametersGiven() throws Exception {
		Cohort cohort = service.getPatientsByCharacteristics(null, null, null, null, null, null, null);
		Assert.assertEquals(4, cohort.size());
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should get patients of given gender", method = "getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)")
	public void getPatientsByCharacteristics_shouldGetPatientsOfGivenGender() throws Exception {
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
	 * @see {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should get patients who are alive", method = "getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)")
	public void getPatientsByCharacteristics_shouldGetPatientsWhoAreAlive() throws Exception {
		Cohort cohort = service.getPatientsByCharacteristics(null, null, null, null, null, true, null);
		Assert.assertEquals(4, cohort.size());
		Assert.assertTrue(cohort.contains(2));
		Assert.assertTrue(cohort.contains(6));
		Assert.assertTrue(cohort.contains(7));
		Assert.assertTrue(cohort.contains(8));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)}
	 */
	@Test
	@Verifies(value = "should get patients who are dead", method = "getPatientsByCharacteristics(String,Date,Date,Integer,Integer,Boolean,Boolean)")
	public void getPatientsByCharacteristics_shouldGetPatientsWhoAreDead() throws Exception {
		Cohort cohort = service.getPatientsByCharacteristics(null, null, null, null, null, null, true);
		Assert.assertEquals(0, cohort.size());
		//Assert.assertTrue(cohort.contains(2));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByProgramAndState(org.openmrs.Program,List,Date, Date)}
	 */
	@Test
	@Verifies(value = "should get all patients in any program given null parameters", method = "getPatientsByProgramAndState(Program,List<QProgramWorkflowState;>,Date,Date)")
	public void getPatientsByProgramAndState_shouldGetAllPatientsInAnyProgramGivenNullParameters() throws Exception {
		Cohort cohort = service.getPatientsByProgramAndState(null, null, null, null);
		Assert.assertEquals(2, cohort.size());
		Assert.assertTrue(cohort.contains(2));
		Assert.assertTrue(cohort.contains(7));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByProgramAndState(org.openmrs.Program,List,Date,Date)}
	 */
	@Test
	@Verifies(value = "should get patients in program", method = "getPatientsByProgramAndState(Program,List<QProgramWorkflowState;>,Date,Date)")
	public void getPatientsByProgramAndState_shouldGetPatientsInProgram() throws Exception {
		Cohort cohort = service.getPatientsByProgramAndState(Context.getProgramWorkflowService().getProgram(1), null, null,
		    null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(2));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByProgramAndState(org.openmrs.Program, List, Date, Date)
	 */
	@Test
	@Verifies(value = "should get patients in state", method = "getPatientsByProgramAndState(Program,List<QProgramWorkflowState;>,Date,Date)")
	public void getPatientsByProgramAndState_shouldGetPatientsInState() throws Exception {
		ProgramWorkflowService pws = Context.getProgramWorkflowService();
		Cohort cohort = service.getPatientsByProgramAndState(pws.getProgram(1), Collections.singletonList(pws.getState(2)),
		    null, null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(2));
		
		cohort = service.getPatientsByProgramAndState(pws.getProgram(1), Collections.singletonList(pws.getState(4)), null,
		    null);
		Assert.assertEquals(0, cohort.size());
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsByProgramAndState(org.openmrs.Program,List,Date,Date)}
	 */
	@Test
	@Verifies(value = "should get patients in states", method = "getPatientsByProgramAndState(Program,List<QProgramWorkflowState;>,Date,Date)")
	public void getPatientsByProgramAndState_shouldGetPatientsInStates() throws Exception {
		ProgramWorkflowService pws = Context.getProgramWorkflowService();
		List<ProgramWorkflowState> list = new ArrayList<ProgramWorkflowState>();
		list.add(pws.getState(2));
		list.add(pws.getState(4));
		Cohort cohort = service.getPatientsByProgramAndState(pws.getProgram(1), list, null, null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(2));
		
		//TODO also test having two states get multiple poeple
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingDrugOrder(java.util.Collection, java.util.Collection, GroupMethod, Date, Date)}
	 */
	@Test
	@Verifies(value = "should get all patients with drug orders given null parameters", method = "getPatientsHavingDrugOrder(Collection<QInteger;>,Collection<QInteger;>,GroupMethod,Date,Date)")
	public void getPatientsHavingDrugOrder_shouldGetAllPatientsWithDrugOrdersGivenNullParameters() throws Exception {
		Cohort cohort = service.getPatientsHavingDrugOrder(null, null, null, null, null);
		Assert.assertEquals(2, cohort.size());
		Assert.assertTrue(cohort.contains(2));
		Assert.assertTrue(cohort.contains(7));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingDrugOrder(java.util.Collection, java.util.Collection, GroupMethod, Date, Date)}
	 */
	@Test
	@Verifies(value = "should get patients with no drug orders", method = "getPatientsHavingDrugOrder(Collection<QInteger;>,Collection<QInteger;>,GroupMethod,Date,Date)")
	public void getPatientsHavingDrugOrder_shouldGetPatientsWithNoDrugOrders() throws Exception {
		Cohort cohort = service.getPatientsHavingDrugOrder(null, null, GroupMethod.NONE, null, null);
		Assert.assertEquals(2, cohort.size());
		Assert.assertTrue(cohort.contains(6));
		Assert.assertTrue(cohort.contains(8));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingDrugOrder(java.util.Collection, java.util.Collection, GroupMethod, Date, Date)}
	 */
	@Test
	@Verifies(value = "should get patients with no drug orders for drugs", method = "getPatientsHavingDrugOrder(Collection<QInteger;>,Collection<QInteger;>,GroupMethod,Date,Date)")
	public void getPatientsHavingDrugOrder_shouldGetPatientsWithNoDrugOrdersForDrugs() throws Exception {
		List<Integer> drugIds = new ArrayList<Integer>();
		drugIds.add(2);
		Cohort cohort = service.getPatientsHavingDrugOrder(drugIds, null, null, null, null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(2));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)}
	 *      test = should get patients by concept and false boolean value
	 */
	@Test
	@Verifies(value = "should get patients by concept and false boolean value", method = "getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)")
	public void getPatientsHavingObs_shouldGetPatientsByConceptAndFalseBooleanValue() throws Exception {
		// there aren't any of these obs in the standard test dataset. Verify that:
		Cohort cohort = service.getPatientsHavingObs(18, TimeModifier.ANY, Modifier.EQUAL, Boolean.FALSE, null, null);
		Assert.assertEquals(0, cohort.size());
		// now create a couple of these so we can try searching.
		DateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
		{ // start with a true one
			Obs obs = new Obs();
			obs.setPerson(new Person(8));
			obs.setConcept(Context.getConceptService().getConcept(18));
			obs.setObsDatetime(ymd.parse("2007-01-01"));
			obs.setValueNumeric(1.0);
			obs.setLocation(new Location(1));
			Context.getObsService().saveObs(obs, null);
		}
		{ // then do a false one
			Obs obs = new Obs();
			obs.setPerson(new Person(8));
			obs.setConcept(Context.getConceptService().getConcept(18));
			obs.setObsDatetime(ymd.parse("2008-01-01"));
			obs.setValueNumeric(0.0);
			obs.setLocation(new Location(1));
			Context.getObsService().saveObs(obs, null);
		}
		// search again
		cohort = service.getPatientsHavingObs(18, TimeModifier.FIRST, Modifier.EQUAL, Boolean.FALSE, null, null);
		Assert.assertEquals(0, cohort.size());
		cohort = service.getPatientsHavingObs(18, TimeModifier.LAST, Modifier.EQUAL, Boolean.FALSE, null, null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(8));
	}
	
	/**
	 * @see {@link PatientSetService#getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)}
	 *      test = should get patients by concept and true boolean value
	 */
	@Test
	@Verifies(value = "should get patients by concept and true boolean value", method = "getPatientsHavingObs(Integer,TimeModifier,Modifier,Object,Date,Date)")
	public void getPatientsHavingObs_shouldGetPatientsByConceptAndTrueBooleanValue() throws Exception {
		Cohort cohort = service.getPatientsHavingObs(18, TimeModifier.ANY, Modifier.EQUAL, Boolean.TRUE, null, null);
		Assert.assertEquals(1, cohort.size());
		Assert.assertTrue(cohort.contains(7));
	}
	
}
